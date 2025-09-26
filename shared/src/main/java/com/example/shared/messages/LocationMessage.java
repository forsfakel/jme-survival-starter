package com.example.shared.messages;

import com.example.shared.model.WorldLocation;
import java.io.Serializable;
import java.util.List;

public class LocationMessage implements Serializable {
    private WorldLocation location;
    private List<String> buildings;

    public LocationMessage() {
    }

    public LocationMessage(WorldLocation location, List<String> buildings) {
        this.location = location;
        this.buildings = buildings;
    }

    public WorldLocation getLocation() {
        return location;
    }

    public List<String> getBuildings() {
        return buildings;
    }

    public void setLocation(WorldLocation location) {
        this.location = location;
    }

    public void setBuildings(List<String> buildings) {
        this.buildings = buildings;
    }
}
