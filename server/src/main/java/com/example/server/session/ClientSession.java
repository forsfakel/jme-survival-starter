package com.example.server.session;

import com.example.shared.model.Player;

public class ClientSession {
    private Player player; // DTO для зручної відправки в клієнт

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }
}
