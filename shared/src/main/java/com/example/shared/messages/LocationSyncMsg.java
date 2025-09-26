package com.example.shared.messages;

import com.example.shared.model.WorldLocation;

import java.io.Serializable;
import java.util.List;

public class LocationSyncMsg implements Serializable {
    private WorldLocation coords;
    private String name;
    private List<String> buildings;

    public LocationSyncMsg() {
    }

    public LocationSyncMsg(WorldLocation coords, String name, List<String> buildings) {
        this.coords = coords;
        this.name = name;
        this.buildings = buildings;
    }

    public WorldLocation getCoords() {
        return coords;
    }

    public String getName() {
        return name;
    }

    public List<String> getBuildings() {
        return buildings;
    }
}
