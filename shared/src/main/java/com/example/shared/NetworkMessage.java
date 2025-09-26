package com.example.shared;

import java.io.Serializable;
import java.util.List;

public class NetworkMessage implements Serializable {
    private List<PlayerState> players;
    public NetworkMessage() {}

    public NetworkMessage(List<PlayerState> players) {
        this.players = players;
    }

    public List<PlayerState> getPlayers() {
        return players;
    }

    public void setPlayers(List<PlayerState> players) {
        this.players = players;
    }
}
