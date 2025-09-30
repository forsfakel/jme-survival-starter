package com.example.server.session;

import com.example.shared.model.Player;

public class ClientSession {
    private Player player; // DTO для зручної відправки в клієнт
    private volatile boolean inBattle = false;

    private volatile boolean inBuilding = false;
    private volatile String  buildingName;

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public boolean isInBattle() { return inBattle; }
    public void setInBattle(boolean inBattle) { this.inBattle = inBattle; }

    public boolean isInBuilding(){ return inBuilding; }
    public void setInBuilding(boolean v){ inBuilding = v; }

    public String getBuildingName(){ return buildingName; }
    public void setBuildingName(String n){ buildingName = n; }
}
