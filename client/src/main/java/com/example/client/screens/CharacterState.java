package com.example.client.screens;

import com.jme3.font.BitmapText;
import com.jme3.math.ColorRGBA;

public class CharacterState extends AbstractScreenState {
    @Override
    protected void onScreenInit() {
        float W = 520, H = 320;
        float px = (screenW - W)/2f, py = (screenH - H)/2f;

        var panel = makeRect(W, H, new ColorRGBA(0,0,0,0.7f));
        panel.setLocalTranslation(px, py, 0);
        uiRoot.attachChild(panel);

        var title = new BitmapText(font, false);
        title.setSize(font.getCharSet().getRenderedSize()*1.2f);
        title.setText("Характеристики (WIP)");
        title.setLocalTranslation(px + 16, py + H - 20, 0);
        uiRoot.attachChild(title);
    }
}
