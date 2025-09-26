package com.example.server.service;

import com.example.shared.model.WorldLocation;

import java.util.*;

public class LocationService {
    // простий реєстр будівель по ключу "x:y"
    private final Map<String, List<String>> buildings = new HashMap<>();

    public LocationService() {
        // демо-дані
        putBuildings(0, 0, List.of("Mine", "Shop"));
        putBuildings(1, 0, List.of("Warehouse"));
        putBuildings(-1, 0, List.of());
    }

    public List<String> getBuildings(WorldLocation loc) {
        return buildings.getOrDefault(key(loc.getX(), loc.getY()), Collections.emptyList());
    }

    public void putBuildings(int x, int y, List<String> b) {
        buildings.put(key(x, y), new ArrayList<>(b));
    }

    public static String key(int x, int y) {
        return x + ":" + y;
    }
}
