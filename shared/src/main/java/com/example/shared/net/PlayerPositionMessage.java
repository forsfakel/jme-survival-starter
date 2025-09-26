package com.example.shared.net;

import java.io.Serializable;

/**
 * Оновлення позиції гравця, яке сервер розсилає всім клієнтам.
 */
public class PlayerPositionMessage implements Serializable {
    private String playerId;
    private int x;
    private int y;

    public PlayerPositionMessage() {}

    public PlayerPositionMessage(String playerId, int x, int y) {
        this.playerId = playerId;
        this.x = x;
        this.y = y;
    }

    public String getPlayerId() { return playerId; }
    public int getX() { return x; }
    public int getY() { return y; }

    @Override
    public String toString() {
        return "PlayerPositionMessage{" + playerId + " @ " + x + "," + y + "}";
    }
}
