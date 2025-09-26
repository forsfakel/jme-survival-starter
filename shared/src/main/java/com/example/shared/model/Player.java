package com.example.shared.model;

import java.io.Serializable;

public class Player implements Serializable {
    private String id;     // uuid / унікальне ім'я
    private String name;
    private WorldLocation location;

    public Player() {}
    public Player(String id, String name, WorldLocation loc) {
        this.id = id; this.name = name; this.location = loc;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public WorldLocation getLocation() { return location; }

    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setLocation(WorldLocation location) { this.location = location; }
}
