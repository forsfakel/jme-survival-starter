package com.example.shared.messages;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Синхронізація стану гри (позиції гравців, об'єкти на локації)
 */
public class SyncStateMessage implements Serializable {
    private Map<String, String> playerStates = new HashMap<>();

    public SyncStateMessage() {}

    public SyncStateMessage(Map<String, String> playerStates) {
        this.playerStates = playerStates;
    }

    public Map<String, String> getPlayerStates() { return playerStates; }
    public void setPlayerStates(Map<String, String> playerStates) { this.playerStates = playerStates; }
}
