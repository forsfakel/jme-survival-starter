package com.example.client.screens;

import com.jme3.math.ColorRGBA;
import com.jme3.font.BitmapText;

public class WarehouseState extends AbstractScreenState {
    @Override protected void onScreenInit() {
        var panel = makeRect(480, 280, new ColorRGBA(0,0,0,0.7f));
        float W = 480, H = 280;
        float px = (screenW - W) / 2f;
        float py = (screenH - H) / 2f;
        panel.setLocalTranslation(px, py, 0);
        uiRoot.attachChild(panel);

        BitmapText title = new BitmapText(font,false);
        title.setSize(font.getCharSet().getRenderedSize()*1.2f);
        title.setText("WAREHOUSE — Сховище");
        title.setLocalTranslation(px+16, py+260, 0);
        uiRoot.attachChild(title);
    }

    @Override
    protected void onPointerClick(float mx, float my) {
        // спочатку даємо шанс базовому стану зловити «Назад»
        super.onPointerClick(mx, my);
    }
}
