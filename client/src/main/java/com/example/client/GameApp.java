package com.example.client;

import com.example.client.screens.FactoryState;
import com.example.client.screens.MineState;
import com.example.client.screens.PlayerHudState;
import com.example.client.screens.ShopState;
import com.example.client.screens.WarehouseState;
import com.example.shared.messages.LocationMessage;
import com.example.shared.messages.OpenBuildingResponse;
import com.example.shared.messages.PlayerListMessage;
import com.example.shared.messages.PlayerPositionMessage;
import com.example.shared.model.WorldLocation;
import com.jme3.app.SimpleApplication;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.RawInputListener;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.event.TouchEvent;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Quad;
import com.jme3.app.state.AppState;
import io.netty.channel.Channel;


import java.util.List;

public class GameApp extends SimpleApplication {

    private Channel channel;

    // 2D-сцена
    private Node uiWorld;          // тут малюємо локацію
    private Node uiHUD;            // тут — HUD (список гравців, підписи)
    private BitmapText playersText;
    private BitmapText locationText;

    private AppState activeSubScene = null;
    private boolean mainSceneHidden = false;

    // геометрія фону локації
    private Geometry bgGeom;

    // поточні координати
    private WorldLocation currentLoc = new WorldLocation(0, 0);

    // розміри “поля локації” у пікселях
    private final int LOC_W = 800;
    private final int LOC_H = 480;

    // геометрії-стрілки
    private Geometry arrowLeft, arrowRight, arrowUp, arrowDown;
    // екрані координати областей (для хіттесту)
    private float ax, ay, aw, ah; // спільні розміри
    private float leftX, leftY, rightX, rightY, upX, upY, downX, downY;
    // іконки будівальЄЄЄЄ

    private final java.util.ArrayList<IconRegion> buildingIcons = new java.util.ArrayList<>();


    public void setChannel(Channel ch) {
        this.channel = ch;
    }

    @Override
    public void simpleInitApp() {

        // 1) Вимкнути дефолтний ESC = вихід
        inputManager.deleteMapping(SimpleApplication.INPUT_MAPPING_EXIT);

        // 2D
        cam.setParallelProjection(true);

        // Вимикаємо flyCam повністю, щоб не ловити скрол/zoom:
        flyCam.setEnabled(false);
        flyCam.unregisterInput();

        // Курсор видимий; емулюємо мишу з тачу (Android):
        inputManager.setCursorVisible(true);
        inputManager.setSimulateMouse(true);

        // (На всяк) не ставимо гру на паузу, якщо втрачає фокус
        setPauseOnLostFocus(false);
        if (!inputManager.hasMapping("UI_CLICK")) {
            inputManager.addMapping("UI_CLICK", new com.jme3.input.controls.MouseButtonTrigger(
                    com.jme3.input.MouseInput.BUTTON_LEFT));
        }

        // Фон
        viewPort.setBackgroundColor(new ColorRGBA(0.12f, 0.14f, 0.18f, 1f));

        // Вузли
        uiWorld = new Node("uiWorld");
        uiHUD = new Node("uiHUD");
        guiNode.attachChild(uiWorld);
        guiNode.attachChild(uiHUD);

        // HUD
        BitmapFont font = assetManager.loadFont("Interface/Fonts/Default.fnt");

        locationText = new BitmapText(font, false);
        locationText.setSize(font.getCharSet().getRenderedSize());
        locationText.setColor(ColorRGBA.White);
        locationText.setLocalTranslation(20, settings.getHeight() - 20, 0);
        uiHUD.attachChild(locationText);

        playersText = new BitmapText(font, false);
        playersText.setSize(font.getCharSet().getRenderedSize());
        playersText.setColor(ColorRGBA.White);
        playersText.setLocalTranslation(settings.getWidth() - 280f, settings.getHeight() - 20f, 0);
        uiHUD.attachChild(playersText);
        playersText.setText("Players in location:\n(loading)");

        // Фон локації
        bgGeom = makeRect(LOC_W, LOC_H, new ColorRGBA(0.18f, 0.22f, 0.18f, 1f));
        float x = (settings.getWidth() - LOC_W) / 2f;
        float y = (settings.getHeight() - LOC_H) / 2f;
        bgGeom.setLocalTranslation(x, y, 0);
        uiWorld.attachChild(bgGeom);

        // Стрілки і мапінги
        initArrowsUI();

        // усередині simpleInitApp(), після побудови UI
        PlayerHudState hud = new PlayerHudState(this::enterSubScene);
        stateManager.attach(hud);


        initKeys();         // WASD
        initMouseClick();   // миша
        initTouch();        // тач (Android)

        inputManager.setCursorVisible(true);

        stateManager.attach(new com.example.client.states.OutOfCombatRegenState());
    }


    private void initKeys() {
        inputManager.addMapping("Left", new KeyTrigger(KeyInput.KEY_A));
        inputManager.addMapping("Right", new KeyTrigger(KeyInput.KEY_D));
        inputManager.addMapping("Up", new KeyTrigger(KeyInput.KEY_W));
        inputManager.addMapping("Down", new KeyTrigger(KeyInput.KEY_S));
        inputManager.addListener(moveListener, "Left", "Right", "Up", "Down");
    }

    // ===== ВИКЛИКИ З NETTY-ХЕНДЛЕРА =====

    public void onLocation(LocationMessage msg) {
        this.currentLoc = msg.getLocation();
        enqueue(() -> {
            clearLocationLayer();
            drawLocationBackground(currentLoc);          // фон + напис координат
            drawBuildings(msg.getBuildings());           // “іконки” будівель
        });
    }

    public void onPlayerList(PlayerListMessage msg) {
        enqueue(() -> updatePlayerList(msg.getPlayers()));
    }

    // ===== ЛОГІКА РУХУ ПО ЛОКАЦІЯХ (2D) =====

    private final ActionListener moveListener = (name, isPressed, tpf) -> {
        if (!isPressed) {
            return;                // тепер реагуємо на натискання
        }
        if (channel == null || !channel.isActive()) {
            return;
        }

        int nx = currentLoc.getX();
        int ny = currentLoc.getY();
        switch (name) {
            case "Left" -> nx -= 1;
            case "Right" -> nx += 1;
            case "Up" -> ny -= 1;
            case "Down" -> ny += 1;
        }
        System.out.println("[KEY] move to " + nx + "," + ny);
        channel.writeAndFlush(new PlayerPositionMessage("self", nx, ny));
    };

    // ===== РЕНДЕР 2D ЛОКАЦІЇ =====

    private void clearLocationLayer() {
        // Залишаємо bgGeom, щоб не перевираховувати трансляцію/матеріал
        uiWorld.detachAllChildren();
        uiWorld.attachChild(bgGeom);
    }

    private void drawLocationBackground(WorldLocation loc) {
        // змінюємо колір фону трохи за парністю координат — щоб було видно перехід
        boolean alt = ((loc.getX() + loc.getY()) & 1) == 0;
        ((Material) bgGeom.getMaterial()).setColor("Color",
                alt ? new ColorRGBA(0.18f, 0.22f, 0.18f, 1f)
                        : new ColorRGBA(0.22f, 0.18f, 0.18f, 1f));

        locationText.setText("Location: " + loc.getX() + " : " + loc.getY());
    }

    private void drawBuildings(List<String> buildings) {
        buildingIcons.clear();
        if (buildings == null || buildings.isEmpty()) {
            return;
        }

        // розкладемо іконки у ряд, всередині прямокутника локації
        float pad = 24f;
        float slot = 72f;   // крок іконок
        float icon = 64f;   // розмір іконки
        // положення фону
        float bx = bgGeom.getLocalTranslation().x;
        float by = bgGeom.getLocalTranslation().y;

        for (int i = 0; i < buildings.size(); i++) {
            String name = buildings.get(i);
            Geometry g = makeRect(icon, icon, ColorRGBA.Gray);
            float x = bx + pad + (i + 1) * slot;
            float y = by + LOC_H / 2f - icon / 2f - 120f; // нижче центру на 120
            g.setLocalTranslation(x, y, 0);
            uiWorld.attachChild(g);

            // підпис під іконкою
            BitmapFont font = assetManager.loadFont("Interface/Fonts/Default.fnt");
            BitmapText label = new BitmapText(font, false);
            label.setSize(font.getCharSet().getRenderedSize());
            label.setColor(ColorRGBA.White);
            label.setText(name);
            label.setLocalTranslation(x, y - 6f, 0); // трохи нижче
            uiWorld.attachChild(label);

            buildingIcons.add(new IconRegion(x, y, icon, icon, name));
        }
    }

    private void updatePlayerList(List<String> players) {
        StringBuilder sb = new StringBuilder("Players in location:\n");
        if (players == null || players.isEmpty()) {
            sb.append("(none)");
        } else {
            for (String p : players)
                sb.append(" - ").append(p).append("\n");
        }
        playersText.setText(sb.toString());
    }

    // ===== УТИЛІТИ =====

    private Geometry makeRect(float w, float h, ColorRGBA color) {
        Quad q = new Quad(w, h);
        Geometry g = new Geometry("rect", q);
        Material m = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        m.setColor("Color", color);
        g.setMaterial(m);
        return g;
    }

    private void initArrowsUI() {
        // розміри та відступ
        aw = 64f;
        ah = 64f;
        float pad = 16f;

        // позиції (у пікселях, бо guiNode)
        float cx = settings.getWidth() / 2f;
        float cy = settings.getHeight() / 2f;

        // фон локації вже центрований, тож стрілки робимо по краях екрану:
        leftX = pad;
        leftY = cy - ah / 2f;
        rightX = settings.getWidth() - aw - pad;
        rightY = cy - ah / 2f;
        upX = cx - aw / 2f;
        upY = settings.getHeight() - ah - pad;
        downX = cx - aw / 2f;
        downY = pad;

        arrowLeft = makeRect(aw, ah, new ColorRGBA(0.25f, 0.25f, 0.30f, 1f));
        arrowRight = makeRect(aw, ah, new ColorRGBA(0.25f, 0.25f, 0.30f, 1f));
        arrowUp = makeRect(aw, ah, new ColorRGBA(0.25f, 0.25f, 0.30f, 1f));
        arrowDown = makeRect(aw, ah, new ColorRGBA(0.25f, 0.25f, 0.30f, 1f));

        arrowLeft.setLocalTranslation(leftX, leftY, 0);
        arrowRight.setLocalTranslation(rightX, rightY, 0);
        arrowUp.setLocalTranslation(upX, upY, 0);
        arrowDown.setLocalTranslation(downX, downY, 0);

        // опційно: намалювати прості «напрямки» кольором (ліву зробимо трохи синішою, праву — зеленішою)
        ((Material) arrowLeft.getMaterial()).setColor("Color",
                new ColorRGBA(0.35f, 0.45f, 0.9f, 1f));
        ((Material) arrowRight.getMaterial()).setColor("Color",
                new ColorRGBA(0.35f, 0.9f, 0.45f, 1f));
        ((Material) arrowUp.getMaterial()).setColor("Color", new ColorRGBA(0.9f, 0.9f, 0.35f, 1f));
        ((Material) arrowDown.getMaterial()).setColor("Color",
                new ColorRGBA(0.9f, 0.45f, 0.35f, 1f));

        // кладемо на HUD-шар, щоб завжди були зверху
        uiHUD.attachChild(arrowLeft);
        uiHUD.attachChild(arrowRight);
        uiHUD.attachChild(arrowUp);
        uiHUD.attachChild(arrowDown);
    }

    private void initMouseClick() {
        inputManager.addListener(uiClickListener, "UI_CLICK");
    }

    private boolean hit(float mx, float my, float rx, float ry, float rw, float rh) {
        return mx >= rx && mx <= rx + rw && my >= ry && my <= ry + rh;
    }

    private void sendMove(int nx, int ny) {
        System.out.println("[CLICK] move to " + nx + "," + ny);
        channel.writeAndFlush(new PlayerPositionMessage("self", nx, ny));
    }

    // невеликий візуальний «пульс» (зміна альфи), щоб було видно натиск
    private void pulse(Geometry g) {
        Material m = g.getMaterial();
        ColorRGBA c = (ColorRGBA) m.getParam("Color").getValue();
        ColorRGBA hi = c.clone();
        hi.a = 0.6f;
        m.setColor("Color", hi);
        // повернути назад через ~0.1с
        new Thread(() -> {
            try {
                Thread.sleep(120);
            } catch (InterruptedException ignored) {
            }
            enqueue(() -> m.setColor("Color", c));
        }).start();
    }

    private void initTouch() {
        inputManager.addRawInputListener(new RawInputListener() {
            @Override
            public void onTouchEvent(TouchEvent evt) {
                if (evt.getType() == TouchEvent.Type.UP) {
                    // тільки при відпусканні
                    handlePointerClick(evt.getX(), evt.getY());
                }
            }

            // решта методів RawInputListener можна залишити пустими
            @Override
            public void beginInput() {
            }

            @Override
            public void endInput() {
            }

            @Override
            public void onJoyAxisEvent(com.jme3.input.event.JoyAxisEvent evt) {
            }

            @Override
            public void onJoyButtonEvent(com.jme3.input.event.JoyButtonEvent evt) {
            }

            @Override
            public void onMouseMotionEvent(com.jme3.input.event.MouseMotionEvent evt) {
            }

            @Override
            public void onMouseButtonEvent(com.jme3.input.event.MouseButtonEvent evt) {
            }

            @Override
            public void onKeyEvent(com.jme3.input.event.KeyInputEvent evt) {
            }
        });
    }

    private void handlePointerClick(float mx, float my) {
        System.out.printf("[POINTER] at (%.1f, %.1f)%n", mx, my);
        if (channel == null || !channel.isActive()) {
            System.out.println("[POINTER] channel not ready");
            return;
        }

        // 1) клік по будівлі?
        for (var r : buildingIcons) {
            if (r.hit(mx, my)) {
                System.out.println("[CLICK] open building: " + r.name);
                channel.writeAndFlush(
                        new com.example.shared.messages.OpenBuildingRequest(currentLoc.getX(),
                                currentLoc.getY(), r.name));
                return;
            }
        }

        // 2) інакше — стрілки
        if (hit(mx, my, leftX, leftY, aw, ah)) {
            sendMove(currentLoc.getX() - 1, currentLoc.getY());
            pulse(arrowLeft);
            return;
        }
        if (hit(mx, my, rightX, rightY, aw, ah)) {
            sendMove(currentLoc.getX() + 1, currentLoc.getY());
            pulse(arrowRight);
            return;
        }
        if (hit(mx, my, upX, upY, aw, ah)) {
            sendMove(currentLoc.getX(), currentLoc.getY() - 1);
            pulse(arrowUp);
            return;
        }
        if (hit(mx, my, downX, downY, aw, ah)) {
            sendMove(currentLoc.getX(), currentLoc.getY() + 1);
            pulse(arrowDown);
            return;
        }
    }

    private static class IconRegion {
        float x, y, w, h;
        String name;

        IconRegion(float x, float y, float w, float h, String name) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            this.name = name;
        }

        boolean hit(float px, float py) {
            return px >= x && px <= x + w && py >= y && py <= y + h;
        }
    }

    public void onOpenBuildingResponse(com.example.shared.messages.OpenBuildingResponse ob) {
        enqueue(() -> {
            // Якщо не ок — показати коротке повідомлення
            if (!ob.isOk()) {
                BitmapFont font = assetManager.loadFont("Interface/Fonts/Default.fnt");
                BitmapText toast = new BitmapText(font, false);
                toast.setSize(font.getCharSet().getRenderedSize());
                toast.setColor(ColorRGBA.Red);
                toast.setText("Access denied: " + ob.getReason());
                toast.setLocalTranslation(40, 80, 0);
                uiHUD.attachChild(toast);

                new Thread(() -> {
                    try {
                        Thread.sleep(1500);
                    } catch (InterruptedException ignored) {
                    }
                    enqueue(() -> uiHUD.detachChild(toast));
                }).start();
                return;
            }

            switch (ob.getType()) {
                case MINE -> {
                    var st = new com.example.client.screens.MineState(currentLoc,
                            ob.getBuildingName());
                    st.setOnExit(this::leaveSubScene);   // ← повідом, як повертатись
                    enterSubScene(st);                   // ← повна заміна сцени
                    return;
                }
                case SHOP -> {
                    var st = new com.example.client.screens.ShopState();
                    st.setOnExit(this::leaveSubScene);
                    enterSubScene(st);
                    return;
                }
                case WAREHOUSE -> {
                    var st = new com.example.client.screens.WarehouseState();
                    st.setOnExit(this::leaveSubScene);
                    enterSubScene(st);
                    return;
                }
                case FACTORY -> {
                    var st = new com.example.client.screens.FactoryState();
                    st.setOnExit(this::leaveSubScene);
                    enterSubScene(st);
                    return;
                }
            }
        });
    }

    private com.jme3.input.controls.ActionListener uiClickListener = (name, pressed, tpf) -> {
        if (!"UI_CLICK".equals(name) || !pressed) {
            return;
        }
        var cp = inputManager.getCursorPosition();
        handlePointerClick(cp.x, cp.y);   // Твій існуючий метод
    };

    private void hideMainScene() {
        if (mainSceneHidden) {
            return;
        }
        // прибираємо UI та інпут головної сцени
        guiNode.detachChild(uiWorld);
        guiNode.detachChild(uiHUD);
        if (inputManager.hasMapping("UI_CLICK")) {
            inputManager.removeListener(uiClickListener); // не видаляємо mapping, тільки listener
        }
        inputManager.removeListener(moveListener); // WASD більше не потрібні в підсцені
        mainSceneHidden = true;
    }

    private void showMainScene() {
        if (!mainSceneHidden) {
            return;
        }
        // повертаємо UI та інпут
        guiNode.attachChild(uiWorld);
        guiNode.attachChild(uiHUD);
        if (inputManager.hasMapping("UI_CLICK")) {
            inputManager.addListener(uiClickListener, "UI_CLICK");
        }
        inputManager.addListener(moveListener, "Left", "Right", "Up", "Down");
        mainSceneHidden = false;
    }

    // методи менеджменту підсцен:
    public void enterSubScene(AppState st) {
        if (activeSubScene != null) {
            stateManager.detach(activeSubScene);
            activeSubScene = null;
        }
        hideMainScene();
        stateManager.attach(st);
        activeSubScene = st;
    }

    public void leaveSubScene() {
        if (activeSubScene != null) {
            stateManager.detach(activeSubScene);
            activeSubScene = null;
        }
        showMainScene();
    }

    public void onPlayerStats(com.example.shared.messages.PlayerStatsMessage m) {
        enqueue(() -> {
            System.out.println("[CLIENT] onPlayerStats " + m.getHp() + "/" + m.getMaxHp());
            com.example.client.PlayerContext.get().resetHpLocal(m.getHp(), m.getMaxHp());

            var hud = stateManager.getState(com.example.client.screens.PlayerHudState.class);
            if (hud != null) {
                hud.updateStats(m.getHp(), m.getMaxHp(), m.getLevel(), m.getExp(),
                        m.getExpToNext());
            }
        });
    }

    public void toast(String text) {
        BitmapFont font = assetManager.loadFont("Interface/Fonts/Default.fnt");
        BitmapText t = new BitmapText(font, false);
        t.setSize(font.getCharSet().getRenderedSize());
        t.setColor(ColorRGBA.White);
        t.setText(text);
        t.setLocalTranslation(40, 100, 0);
        uiHUD.attachChild(t);
        new Thread(() -> {
            try {
                Thread.sleep(1400);
            } catch (InterruptedException ignored) {
            }
            enqueue(() -> uiHUD.detachChild(t));
        }).start();
    }

    public void sendToServer(Object msg) {
        if (channel != null && channel.isActive()) {
            channel.writeAndFlush(msg);
        }
    }

    public void onOpenBuilding(OpenBuildingResponse r) {
        // TODO: тут твій справжній UI. Поки — тост/лог.
        toast("Відкрито будівлю: " + r.getBuildingName() + " (" + r.getType() + ")");
    }

    public void onOpenBuildingError(String error) {
        System.out.println("[CLIENT] OpenBuilding failed: " + error);
        toast("Помилка відкриття будівлі: " + error);
    }

}
