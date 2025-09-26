package com.example.server.game;

import com.example.shared.model.GameLocation;
import com.example.shared.model.Player;

import java.util.*;

public class LocationManager {
    private final Map<String, GameLocation> locations = new HashMap<>();
    private final Map<String, Set<Player>> playersInLocations = new HashMap<>();

    private String key(int x, int y) {
        return x + ":" + y;
    }

    public void addLocation(GameLocation location) {
        locations.put(key(location.getX(), location.getY()), location);
        playersInLocations.putIfAbsent(key(location.getX(), location.getY()), new HashSet<>());
    }

    public void playerEnter(Player player, GameLocation location) {
        String locKey = key(location.getX(), location.getY());
        playersInLocations.putIfAbsent(locKey, new HashSet<>());
        playersInLocations.get(locKey).add(player);
    }

    public void playerLeave(Player player, GameLocation location) {
        String locKey = key(location.getX(), location.getY());
        if (playersInLocations.containsKey(locKey)) {
            playersInLocations.get(locKey).remove(player);
        }
    }

    public Set<Player> getPlayersInLocation(GameLocation location) {
        return playersInLocations.getOrDefault(key(location.getX(), location.getY()), Collections.emptySet());
    }
}
