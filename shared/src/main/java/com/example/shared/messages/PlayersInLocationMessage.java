package com.example.shared.messages;

import java.util.List;

public class PlayersInLocationMessage {
    private List<String> players;

    public PlayersInLocationMessage() {}

    public PlayersInLocationMessage(List<String> players) {
        this.players = players;
    }

    public List<String> getPlayers() { return players; }
}
