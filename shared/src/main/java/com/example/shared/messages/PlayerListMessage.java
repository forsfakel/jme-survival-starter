package com.example.shared.messages;

import java.io.Serializable;
import java.util.List;

public class PlayerListMessage implements Serializable {
    private List<String> players;

    public PlayerListMessage() {
    }

    public PlayerListMessage(List<String> players) {
        this.players = players;
    }

    public List<String> getPlayers() {
        return players;
    }

    public void setPlayers(List<String> players) {
        this.players = players;
    }
}
