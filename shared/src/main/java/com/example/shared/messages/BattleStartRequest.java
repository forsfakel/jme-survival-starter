package com.example.shared.messages;

import java.io.Serializable;

public class BattleStartRequest implements Serializable {
    public enum Arena { SURFACE, MINE_TUNNEL }
    private int x, y;
    private Arena arena;

    public BattleStartRequest() {}
    public BattleStartRequest(int x, int y, Arena arena) {
        this.x = x; this.y = y; this.arena = arena;
    }
    public int getX(){ return x; }
    public int getY(){ return y; }
    public Arena getArena(){ return arena; }
}
