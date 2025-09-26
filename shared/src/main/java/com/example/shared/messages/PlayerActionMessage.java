package com.example.shared.messages;

import java.io.Serializable;

/**
 * Дія гравця (рух, атака, взаємодія)
 */
public class PlayerActionMessage implements Serializable {
    private String playerId;
    private String action; // "move", "attack", "interact"
    private String target; // може бути ID іншого гравця чи об'єкта

    public PlayerActionMessage() {}

    public PlayerActionMessage(String playerId, String action, String target) {
        this.playerId = playerId;
        this.action = action;
        this.target = target;
    }

    public String getPlayerId() { return playerId; }
    public String getAction() { return action; }
    public String getTarget() { return target; }

    public void setPlayerId(String playerId) { this.playerId = playerId; }
    public void setAction(String action) { this.action = action; }
    public void setTarget(String target) { this.target = target; }
}
