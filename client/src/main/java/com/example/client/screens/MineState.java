package com.example.client.screens;

import com.example.shared.model.WorldLocation;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;

import java.util.Random;

public class MineState extends AbstractScreenState {

    private final WorldLocation outerLoc; // локація на поверхні
    private final String buildingName;

    // сітка
    private int cols = 15, rows = 9;
    private boolean[][] passable;

    // розміри тайла
    private float tile = 32f;
    private float gridW, gridH;
    private float originX, originY;

    // позиція гравця всередині шахти (клітинка)
    private int px = 1, py = 1;

    // стрілки
    private Geometry aLeft, aRight, aUp, aDown;
    private float aw = 48, ah = 48;
    private float leftX, leftY, rightX, rightY, upX, upY, downX, downY;

    private ActionListener moveListener, clickListener;

    private final java.util.Random rnd = new java.util.Random();

    public MineState(WorldLocation outerLoc, String buildingName) {
        this.outerLoc = outerLoc;
        this.buildingName = buildingName;
    }

    @Override
    protected void onScreenInit() {
        // фон
        app.getViewPort().setBackgroundColor(new ColorRGBA(0.05f, 0.06f, 0.08f, 1f));

        // 1) генеруємо лабіринт (дуже просто: стіни з імовірністю, але залишаємо прохідність)
        passable = new boolean[cols][rows];
        Random R = new Random((outerLoc.getX() * 73856093) ^ (outerLoc.getY() * 19349663) ^ buildingName.hashCode());
        for (int x=0;x<cols;x++) {
            for (int y=0;y<rows;y++) {
                boolean wall = R.nextFloat() < 0.22f;
                passable[x][y] = !wall;
            }
        }
        // гарантовано прохідний периметр і старт
        for (int x=0;x<cols;x++){ passable[x][0]=false; passable[x][rows-1]=false; }
        for (int y=0;y<rows;y++){ passable[0][y]=false; passable[cols-1][y]=false; }
        passable[px][py]=true;

        // 2) позиціонування сітки по центру
        gridW = cols * tile;
        gridH = rows * tile;

        originX = (sw() - gridW) / 2f;
        originY = (sh() - gridH) / 2f;

        // випадкова зустріч ~25%
        if (rnd.nextFloat() < 0.25f) {
            var app = (com.example.client.GameApp) getApplication();
            app.sendToServer(new com.example.shared.messages.BattleStartRequest(
                    outerLoc.getX(), outerLoc.getY(),
                    com.example.shared.messages.BattleStartRequest.Arena.MINE_TUNNEL
            ));
        }
        // Після успішного переміщення в тунелі:
        var app = (com.example.client.GameApp) getApplication();
        app.sendToServer(new com.example.shared.messages.BattleStartRequest(
                outerLoc.getX(), outerLoc.getY(),
                com.example.shared.messages.BattleStartRequest.Arena.MINE_TUNNEL
        ));
        System.out.println("[MINE] BattleStartRequest sent");

        // 3) відмалювати
        drawGrid();

        // 4) стрілки керування
        initArrows();

        // 5) клавіатура
        initKeys();
        // 6) кліки мишею для стрілок
      //  initMouseClick();
    }

    private void drawGrid() {
        uiRoot.detachAllChildren();
        // фон
        var bg = makeRect(gridW, gridH, new ColorRGBA(0.10f,0.12f,0.10f,1f));
        bg.setLocalTranslation(originX, originY, 0);
        uiRoot.attachChild(bg);

        // клітинки
        for (int x=0;x<cols;x++) {
            for (int y=0;y<rows;y++) {
                ColorRGBA c = passable[x][y] ? new ColorRGBA(0.25f,0.28f,0.25f,1f)
                                      : new ColorRGBA(0.08f,0.08f,0.10f,1f);
                var cell = makeRect(tile-2, tile-2, c);
                float cx = originX + x*tile + 1;
                float cy = originY + y*tile + 1;
                cell.setLocalTranslation(cx, cy, 0);
                uiRoot.attachChild(cell);
            }
        }

        // маркер гравця
        var p = makeRect(tile-4, tile-4, new ColorRGBA(0.35f,0.75f,0.35f,1f));
        float pxs = originX + px*tile + 2;
        float pys = originY + py*tile + 2;
        p.setLocalTranslation(pxs, pys, 0);
        uiRoot.attachChild(p);
    }

    private void initArrows() {
        originX = (screenW - gridW) / 2f;
        originY = (screenH - gridH) / 2f;

        float pad = 18f;
        float cx = screenW / 2f;
        float cy = screenH / 2f;

        leftX  = pad;                 leftY  = cy - ah/2f;
        rightX = screenW - aw - pad;  rightY = cy - ah/2f;
        upX    = cx - aw/2f;          upY    = screenH - ah - pad;
        downX  = cx - aw/2f;          downY  = pad;

        aLeft  = makeRect(aw, ah, new ColorRGBA(0.35f,0.45f,0.9f,1f));
        aRight = makeRect(aw, ah, new ColorRGBA(0.35f,0.9f,0.45f,1f));
        aUp    = makeRect(aw, ah, new ColorRGBA(0.9f,0.9f,0.35f,1f));
        aDown  = makeRect(aw, ah, new ColorRGBA(0.9f,0.45f,0.35f,1f));

        aLeft.setLocalTranslation(leftX,leftY,0);
        aRight.setLocalTranslation(rightX,rightY,0);
        aUp.setLocalTranslation(upX,upY,0);
        aDown.setLocalTranslation(downX,downY,0);

        uiHUD.attachChild(aLeft);
        uiHUD.attachChild(aRight);
        uiHUD.attachChild(aUp);
        uiHUD.attachChild(aDown);
    }

    private void initKeys() {
        var im = app.getInputManager();
        if (!im.hasMapping("M_LEFT"))  im.addMapping("M_LEFT",  new KeyTrigger(KeyInput.KEY_A), new KeyTrigger(KeyInput.KEY_LEFT));
        if (!im.hasMapping("M_RIGHT")) im.addMapping("M_RIGHT", new KeyTrigger(KeyInput.KEY_D), new KeyTrigger(KeyInput.KEY_RIGHT));
        if (!im.hasMapping("M_UP"))    im.addMapping("M_UP",    new KeyTrigger(KeyInput.KEY_W), new KeyTrigger(KeyInput.KEY_UP));
        if (!im.hasMapping("M_DOWN"))  im.addMapping("M_DOWN",  new KeyTrigger(KeyInput.KEY_S), new KeyTrigger(KeyInput.KEY_DOWN));

        moveListener = (name, pressed, tpf) -> {
            if (!pressed) return;
            int nx = px, ny = py;
            switch (name) {
                case "M_LEFT"  -> nx -= 1;
                case "M_RIGHT" -> nx += 1;
                case "M_UP"    -> ny += 1; // Y вгору, бо GUI-координати ростуть вгору
                case "M_DOWN"  -> ny -= 1;
            }
            if (inBounds(nx,ny) && passable[nx][ny]) {
                px = nx; py = ny;
                drawGrid();
            }
        };
        im.addListener(moveListener, "M_LEFT","M_RIGHT","M_UP","M_DOWN");
    }


    @Override
    protected void onPointerClick(float mx, float my) {
        // спочатку даємо шанс базовому стану зловити «Назад»
        super.onPointerClick(mx, my);
        // далі — своя логіка стрілок:
        if (hit(mx,my,leftX,leftY,aw,ah))  { tryMove(-1,0); pulse(aLeft);  return; }
        if (hit(mx,my,rightX,rightY,aw,ah)){ tryMove(1,0);  pulse(aRight); return; }
        if (hit(mx,my,upX,upY,aw,ah))      { tryMove(0,1);  pulse(aUp);    return; }
        if (hit(mx,my,downX,downY,aw,ah))  { tryMove(0,-1); pulse(aDown);  return; }
    }

    private void tryMove(int dx, int dy) {
        int nx = px + dx, ny = py + dy;
        if (inBounds(nx,ny) && passable[nx][ny]) {
            px = nx; py = ny;
            drawGrid();
        }
    }

    private boolean inBounds(int x, int y) {
        return x>=0 && x<cols && y>=0 && y<rows;
    }

    private void pulse(Geometry g) {
        var m = g.getMaterial();
        var c = (ColorRGBA)m.getParam("Color").getValue();
        var hi = c.clone(); hi.a = 0.6f;
        m.setColor("Color", hi);
        new Thread(() -> { try { Thread.sleep(120);} catch (InterruptedException ignored) {}
            app.enqueue(() -> m.setColor("Color", c));
        }).start();
    }

    @Override
    protected void cleanup(com.jme3.app.Application app) {
        // при виході — прибрати UI і мапінги
        var im = this.app.getInputManager();
        if (moveListener != null) im.removeListener(moveListener);
        if (clickListener != null) im.removeListener(clickListener);
        if (im.hasMapping("M_Click")) im.deleteMapping("M_Click");
        if (im.hasMapping("M_LEFT"))  im.deleteMapping("M_LEFT");
        if (im.hasMapping("M_RIGHT")) im.deleteMapping("M_RIGHT");
        if (im.hasMapping("M_UP"))    im.deleteMapping("M_UP");
        if (im.hasMapping("M_DOWN"))  im.deleteMapping("M_DOWN");
        super.cleanup(app);
    }
}
