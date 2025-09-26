package com.example.client.states;

import com.example.client.PlayerContext;
import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;

public class OutOfCombatRegenState extends BaseAppState {
    private float timer = 0f;        // сек
    private float interval = 60f;    // 1 хвилина

    @Override protected void initialize(Application app) {}
    @Override protected void cleanup(Application app) {}

    @Override protected void onEnable() {}
    @Override protected void onDisable() {}

    @Override public void update(float tpf) {
        PlayerContext pc = PlayerContext.get();
        if (pc.isInBattle()) { timer = 0f; return; } // у бою — не регенимось

        int hp = pc.getHp();
        int max = pc.getHpMax();
        if (hp >= max) { timer = 0f; return; }

        timer += tpf;
        if (timer >= interval) {
            timer -= interval;
            int heal = Math.max(1, Math.round(max * 0.10f)); // 10% від максимуму
            pc.setHp(hp + heal);
            // TODO (опційно): відправити на сервер повідомлення синхронізації HP
        }
    }
}