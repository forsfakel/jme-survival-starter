package com.example.shared.net;

import com.example.shared.model.WorldLocation;
import java.io.Serializable;

/**
 * Використовується для повідомлень про зміну локації.
 */
public class LocationMessage implements Serializable {
    private String playerId;
    private WorldLocation newLocation;

    public LocationMessage(String playerId, WorldLocation newLocation) {
        this.playerId = playerId;
        this.newLocation = newLocation;
    }

    public String getPlayerId() { return playerId; }
    public WorldLocation getNewLocation() { return newLocation; }
}
