package com.example.server.state;

/** Поточний стан гравця на сервері. */
public class PlayerState {
    private final String playerId;
    private int x;
    private int y;

    public PlayerState(String playerId, int startX, int startY) {
        this.playerId = playerId; this.x = startX; this.y = startY;
    }

    public String getPlayerId() { return playerId; }
    public int getX() { return x; }
    public int getY() { return y; }

    public void move(int dx, int dy) { this.x += dx; this.y += dy; }
    public void set(int x, int y) { this.x = x; this.y = y; }
}
