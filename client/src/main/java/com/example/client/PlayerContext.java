package com.example.client;

import java.util.prefs.Preferences;


public final class PlayerContext {
    private static final PlayerContext I = new PlayerContext();

    private int hp;
    private int hpMax;
    private boolean inBattle = false;
    private boolean inBuilding = false;
    private String buildingName;

    // ── persistence ─────────────────────────────────────────────────────────────
    private static final String KEY_HP = "hp";
    private static final String KEY_HPMAX = "hpMax";
    private final Preferences prefs = Preferences.userNodeForPackage(PlayerContext.class);

    private PlayerContext() {
        // load persisted values (fall back to 100/100)
        this.hpMax = Math.max(1, prefs.getInt(KEY_HPMAX, 100));
        this.hp = Math.max(0, Math.min(prefs.getInt(KEY_HP, 100), this.hpMax));
    }

    public static PlayerContext get() {
        return I;
    }

    public synchronized int getHp() {
        return hp;
    }

    public synchronized int getHpMax() {
        return hpMax;
    }

    public synchronized boolean isInBattle() {
        return inBattle;
    }

    public synchronized void setHp(int hp) {
        int old = this.hp;
        this.hp = Math.max(0, Math.min(hp, hpMax));
        prefs.putInt(KEY_HP, this.hp);            // persist
        System.out.println("[PlayerContext] setHp: " + old + " -> " + this.hp + " (max=" + hpMax
                                   + ", inBattle=" + inBattle + ") [PERSISTED]");
    }

    public synchronized void setHpMax(int hpMax) {
        int oldMax = this.hpMax;
        this.hpMax = Math.max(1, hpMax);
        if (hp > this.hpMax) {
            hp = this.hpMax;
        }
        prefs.putInt(KEY_HPMAX, this.hpMax);      // persist
        prefs.putInt(KEY_HP, this.hp);            // keep hp clamped & persisted
        System.out.println(
                "[PlayerContext] setHpMax: " + oldMax + " -> " + this.hpMax + " (hp now " + hp
                        + ") [PERSISTED]");
    }

    public synchronized void setInBattle(boolean inBattle) {
        boolean old = this.inBattle;
        this.inBattle = inBattle;
        System.out.println("[PlayerContext] setInBattle: " + old + " -> " + this.inBattle);
    }

    public boolean isInBuilding() {
        return inBuilding;
    }

    public String getBuildingName() {
        return buildingName;
    }

    public void setBuilding(String name, boolean in) {
        this.buildingName = name;
        this.inBuilding = in;
        System.out.println("[PlayerContext] setBuilding: " + name + " in=" + in);
    }

    /**
     * (опційно) скинути весь прогрес HP локально
     */
    public synchronized void resetHpLocal(int hp, int hpMax) {
        this.hpMax = Math.max(1, hpMax);
        this.hp = Math.max(0, Math.min(hp, this.hpMax));
        prefs.putInt(KEY_HPMAX, this.hpMax);
        prefs.putInt(KEY_HP, this.hp);
    }


}
