package com.example.client.screens;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Quad;

public abstract class AbstractScreenState extends BaseAppState {

    private com.jme3.input.controls.ActionListener uiClickListener;
    protected SimpleApplication app;
    protected Node uiRoot;  // корінь для цієї сцени (guiNode-підвузол)
    protected Node uiHUD;   // HUD шар
    protected BitmapFont font;
    private Runnable onExit;

    private Geometry backBtnGeom;
    private float backX, backY, backW = 120f, backH = 36f;
    private ActionListener backListener;
    protected int screenW, screenH;

    @Override
    protected void initialize(Application app) {
        this.app = (SimpleApplication) app;
        app.getInputManager().setCursorVisible(true);
        var settings = app.getContext().getSettings();
        screenW = settings.getWidth();
        screenH = settings.getHeight();

        uiRoot = new Node(getClass().getSimpleName() + "_UI");
        uiHUD = new Node(getClass().getSimpleName() + "_HUD");
        this.app.getGuiNode().attachChild(uiRoot);
        this.app.getGuiNode().attachChild(uiHUD);

        font = this.app.getAssetManager().loadFont("Interface/Fonts/Default.fnt");

        uiClickListener = (name, pressed, tpf) -> {
            if (!"UI_CLICK".equals(name) || !pressed) return;
            var cp = app.getInputManager().getCursorPosition();
            onPointerClick(cp.x, cp.y);
        };
        app.getInputManager().addListener(uiClickListener, "UI_CLICK");
        // Кнопка Назад (правий верхній кут)
        backW = 120f;
        backH = 36f;
        backX = screenW - backW - 16f;
        backY = screenH - backH - 16f;

        backBtnGeom = makeRect(backW, backH, new ColorRGBA(0.25f,0.3f,0.35f,1f));
        backBtnGeom.setLocalTranslation(backX, backY, 0);
        uiHUD.attachChild(backBtnGeom);

        BitmapText backLbl = new BitmapText(font, false);
        backLbl.setSize(font.getCharSet().getRenderedSize());
        backLbl.setColor(ColorRGBA.White);
        backLbl.setText("Назад");
        backLbl.setLocalTranslation(backX + 14f, backY + backH - 10f, 0);
        uiHUD.attachChild(backLbl);

        // Esc також повертає назад
        if (!this.app.getInputManager().hasMapping("SCREEN_BACK_ESC")) {
            this.app.getInputManager().addMapping("SCREEN_BACK_ESC",
                    new KeyTrigger(KeyInput.KEY_ESCAPE));
        }
        backListener = (name, pressed, tpf) -> {
            if (pressed && "SCREEN_BACK_ESC".equals(name)) {
                exitToMain();
            }
        };
        this.app.getInputManager().addListener(backListener, "SCREEN_BACK_ESC");

        onScreenInit();
    }

    /**
     * Викликається всередині initialize — для конкретних сцен.
     */
    protected abstract void onScreenInit();

    /**
     * Повернення у головну сцену
     */
    public void setOnExit(Runnable onExit) { this.onExit = onExit; }

    protected void exitToMain() {
        if (onExit != null) onExit.run();  // повідомляємо GameApp, що треба відновити головну сцену
        getStateManager().detach(this);
    }

    @Override
    protected void cleanup(Application app) {
        // прибрати UI та лістенери
        this.app.getInputManager().removeListener(backListener);
        this.app.getGuiNode().detachChild(uiRoot);
        this.app.getGuiNode().detachChild(uiHUD);

        if (uiClickListener != null) app.getInputManager().removeListener(uiClickListener);
    }

    protected void onPointerClick(float x, float y) {  // якщо клік у прямокутнику кнопки «Назад» — виходимо
        if (hit(x, y, backX, backY, backW, backH)) {
            exitToMain();
        }
        // інші стани (Mine/Shop/…) можуть перевизначити цей метод, але ПОВИННІ викликати super.onPointerClick(x,y) }
    }
    @Override
    protected void onEnable() {
    }

    @Override
    protected void onDisable() {
    }

    // ==== помічники UI ====

    protected Geometry makeRect(float w, float h, ColorRGBA color) {
        Quad q = new Quad(w, h);
        Geometry g = new Geometry("rect", q);
        Material m = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        m.setColor("Color", color);
        g.setMaterial(m);
        return g;
    }

    /**
     * Хелпер для хіттесту прямокутників у GUI-координатах
     */
    protected boolean hit(float mx, float my, float rx, float ry, float rw, float rh) {
        return mx >= rx && mx <= rx + rw && my >= ry && my <= ry + rh;
    }

    protected int sw() {
        return app.getContext().getSettings().getWidth();
    }

    protected int sh() {
        return app.getContext().getSettings().getHeight();
    }


}
