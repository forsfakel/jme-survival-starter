package com.example.server.model;

import com.example.server.model.id.LocationId;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "locations")
public class LocationEntity {

    @EmbeddedId
    private LocationId id;

    @OneToMany(mappedBy = "location",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    private List<BuildingEntity> buildings = new ArrayList<>();

    public LocationEntity() {
    }

    public LocationEntity(LocationId id) {
        this.id = id;
    }

    public LocationId getId() {
        return id;
    }

    public void setId(LocationId id) {
        this.id = id;
    }

    public List<BuildingEntity> getBuildings() {
        return buildings;
    }

    public void setBuildings(List<BuildingEntity> buildings) {
        this.buildings = buildings;
    }
}
