package com.example.shared.messages;

import java.io.Serializable;

public class PlayerPositionMessage implements Serializable {
    private String playerId;
    private int x;
    private int y;

    public PlayerPositionMessage() {
    }

    public PlayerPositionMessage(String playerId, int x, int y) {
        this.playerId = playerId;
        this.x = x;
        this.y = y;
    }

    public String getPlayerId() {
        return playerId;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    @Override
    public String toString() {
        return "PlayerPositionMessage{" + playerId + " @ " + x + "," + y + "}";
    }
}
