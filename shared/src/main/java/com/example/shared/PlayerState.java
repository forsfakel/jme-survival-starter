package com.example.shared;

import java.io.Serializable;

public class PlayerState implements Serializable {
    private String playerId;
    private float x, y, z;

    public PlayerState() {}

    public PlayerState(String playerId, float x, float y, float z) {
        this.playerId = playerId;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public String getPlayerId() { return playerId; }
    public float getX() { return x; }
    public float getY() { return y; }
    public float getZ() { return z; }
}
