package com.example.shared.net;

import java.io.Serializable;

/**
 * Клієнт повідомляє серверу про свій рух.
 * playerId не передається — сервер визначає гравця за каналом.
 */
public class PlayerMoveMessage implements Serializable {
    private int dx;
    private int dy;

    public PlayerMoveMessage() {}

    public PlayerMoveMessage(int dx, int dy) {
        this.dx = dx;
        this.dy = dy;
    }

    public int getDx() { return dx; }
    public int getDy() { return dy; }
}
