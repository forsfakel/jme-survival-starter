package com.example.client.screens;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.input.controls.ActionListener;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Quad;

import com.jme3.app.state.BaseAppState;
import com.jme3.app.state.AppState;
import java.util.function.Consumer;

public class PlayerHudState extends BaseAppState {

    private SimpleApplication app;
    private Node layer;
    private BitmapFont font;

    // розмір екрану
    private int sw, sh;

    // панель
    private Geometry panel;
    private float px, py, W = 240, H = 110;

    // HP/EXP бари
    private Geometry hpBack, hpFill, expBack, expFill;
    private float barW = 160, barH = 16;

    // текст
    private BitmapText lvlText, hpText, expText;

    // кнопки
    private Geometry btnInv, btnChar;
    private float btnW = 90, btnH = 28;
    private float invX, invY, charX, charY;

    // стати
    private int hp = 50, maxHp = 100, level = 1, exp = 0, expToNext = 100;

    // клік-слухач
    private ActionListener clickListener;

    // як відкрити підсцену (передає GameApp)
    private final Consumer<AppState> openSubScene;

    public PlayerHudState(Consumer<AppState> openSubScene) {
        this.openSubScene = openSubScene;
    }

    @Override
    protected void initialize(Application application) {
        this.app = (SimpleApplication) application;
        font = app.getAssetManager().loadFont("Interface/Fonts/Default.fnt");
        app.getInputManager().setCursorVisible(true);

        var settings = app.getContext().getSettings();
        sw = settings.getWidth();
        sh = settings.getHeight();

        layer = new Node("PlayerHUD");
        app.getGuiNode().attachChild(layer);

        // позиція панелі (лівий верхній кут з відступом 12)
        px = 12;
        py = sh - H - 12;

        panel = rect(W, H, new ColorRGBA(0,0,0,0.6f));
        panel.setLocalTranslation(px, py, 0);
        layer.attachChild(panel);

        // Level
        lvlText = new BitmapText(font, false);
        lvlText.setSize(font.getCharSet().getRenderedSize());
        lvlText.setColor(ColorRGBA.White);
        lvlText.setLocalTranslation(px + 10, py + H - 10, 0);
        layer.attachChild(lvlText);

        // HP bar
        hpBack = rect(barW, barH, new ColorRGBA(0.15f,0.15f,0.18f,1f));
        hpBack.setLocalTranslation(px + 10, py + H - 34, 0);
        layer.attachChild(hpBack);

        hpFill = rect(1, barH, new ColorRGBA(0.9f,0.25f,0.25f,1f));
        hpFill.setLocalTranslation(px + 10, py + H - 34, 0);
        layer.attachChild(hpFill);

        hpText = new BitmapText(font, false);
        hpText.setSize(font.getCharSet().getRenderedSize());
        hpText.setColor(ColorRGBA.White);
        hpText.setLocalTranslation(px + 12, py + H - 36, 1);
        layer.attachChild(hpText);

        // EXP bar
        expBack = rect(barW, barH, new ColorRGBA(0.15f,0.15f,0.18f,1f));
        expBack.setLocalTranslation(px + 10, py + H - 56, 0);
        layer.attachChild(expBack);

        expFill = rect(1, barH, new ColorRGBA(0.25f,0.7f,0.95f,1f));
        expFill.setLocalTranslation(px + 10, py + H - 56, 0);
        layer.attachChild(expFill);

        expText = new BitmapText(font, false);
        expText.setSize(font.getCharSet().getRenderedSize());
        expText.setColor(ColorRGBA.White);
        expText.setLocalTranslation(px + 12, py + H - 58, 1);
        layer.attachChild(expText);

        // Кнопки
        invX = px + 10;                  invY = py + 10;
        charX = px + 10 + btnW + 10;     charY = py + 10;

        btnInv = rect(btnW, btnH, new ColorRGBA(0.25f,0.3f,0.35f,1f));
        btnInv.setLocalTranslation(invX, invY, 0);
        layer.attachChild(btnInv);
        var invLbl = label("Інвентар", invX + 12, invY + btnH - 10);
        layer.attachChild(invLbl);

        btnChar = rect(btnW, btnH, new ColorRGBA(0.25f,0.3f,0.35f,1f));
        btnChar.setLocalTranslation(charX, charY, 0);
        layer.attachChild(btnChar);
        var charLbl = label("Характеристики", charX + 12, charY + btnH - 10);
        layer.attachChild(charLbl);

        // слухаємо глобальний UI_CLICK
        if (!app.getInputManager().hasMapping("UI_CLICK")) {
            app.getInputManager().addMapping("UI_CLICK",
                    new com.jme3.input.controls.MouseButtonTrigger(
                            com.jme3.input.MouseInput.BUTTON_LEFT));
        }
        clickListener = (name, pressed, tpf) -> {
            if (!"UI_CLICK".equals(name) || !pressed) return;
            var cp = app.getInputManager().getCursorPosition();
            float mx = cp.x, my = cp.y;

            // кліки по кнопках
            if (hit(mx,my, invX, invY, btnW, btnH)) {
                pulse(btnInv);
                if (openSubScene != null) {
                    openSubScene.accept(new com.example.client.screens.InventoryState());
                }
                return;
            }
            if (hit(mx,my, charX, charY, btnW, btnH)) {
                pulse(btnChar);
                if (openSubScene != null) {
                    openSubScene.accept(new com.example.client.screens.CharacterState());
                }
                return;
            }
        };
        app.getInputManager().addListener(clickListener, "UI_CLICK");

        // первинний рендер
        refresh();
    }

    // оновити стати ззовні
    public void updateStats(int hp, int maxHp, int level, int exp, int expToNext) {
        this.hp = hp; this.maxHp = maxHp; this.level = level; this.exp = exp; this.expToNext = expToNext;
        if (isInitialized()) refresh();
    }

    private void refresh() {
        lvlText.setText("Lv." + level);
        // HP bar
        float hpPerc = Math.max(0f, Math.min(1f, maxHp>0 ? (float)hp / (float)maxHp : 0f));
        setRectWidth(hpFill, barW * hpPerc);
        hpText.setText(hp + " / " + maxHp);

        // EXP bar
        float expPerc = Math.max(0f, Math.min(1f, expToNext>0 ? (float)exp / (float)expToNext : 0f));
        setRectWidth(expFill, barW * expPerc);
        expText.setText("EXP: " + exp + "/" + expToNext);
    }

    private Geometry rect(float w, float h, ColorRGBA color) {
        var g = new Geometry("rect", new Quad(w, h));
        var m = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        m.setColor("Color", color);
        g.setMaterial(m);
        return g;
    }

    private BitmapText label(String s, float x, float y) {
        var t = new BitmapText(font, false);
        t.setSize(font.getCharSet().getRenderedSize());
        t.setColor(ColorRGBA.White);
        t.setText(s);
        t.setLocalTranslation(x, y, 0);
        return t;
    }

    private boolean hit(float mx, float my, float rx, float ry, float rw, float rh) {
        return mx >= rx && mx <= rx + rw && my >= ry && my <= ry + rh;
    }

    private void setRectWidth(Geometry g, float newW) {
        // створимо новий Quad з потрібною шириною, щоб не морочитися з UV
        float h = ((Quad)g.getMesh()).getHeight();
        g.setMesh(new Quad(Math.max(1f, newW), h));
    }

    private void pulse(Geometry g) {
        var m = g.getMaterial();
        var c = (ColorRGBA)m.getParam("Color").getValue();
        var hi = c.clone(); hi.a = 0.6f;
        m.setColor("Color", hi);
        new Thread(() -> {
            try { Thread.sleep(100);} catch (InterruptedException ignored) {}
            app.enqueue(() -> m.setColor("Color", c));
        }).start();
    }

    @Override
    protected void cleanup(Application application) {
        if (clickListener != null) app.getInputManager().removeListener(clickListener);
        app.getGuiNode().detachChild(layer);
    }

    @Override protected void onEnable() {}
    @Override protected void onDisable() {}
}
