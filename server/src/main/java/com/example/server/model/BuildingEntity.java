package com.example.server.model;

import jakarta.persistence.*;

@Entity
@Table(name = "buildings",
        indexes = {@Index(name = "ix_buildings_loc",
                columnList = "loc_x,loc_y")},
        uniqueConstraints = {@UniqueConstraint(name = "ux_buildings_loc_name",
                columnNames = {"loc_x", "loc_y", "name"})})
public class BuildingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column
    private String type; // "Mine", "Shop", "Warehouse", "Factory" тощо

    @ManyToOne(fetch = FetchType.LAZY,
            optional = false)
    @JoinColumns({@JoinColumn(name = "loc_x",
            referencedColumnName = "x",
            nullable = false), @JoinColumn(name = "loc_y",
            referencedColumnName = "y",
            nullable = false)})
    private LocationEntity location;

    @Column(name = "owner_uuid")
    private String ownerUuid; // опційно

    @Column(name = "access_key")
    private String accessKey; // опційно

    public BuildingEntity() {
    }

    public BuildingEntity(String name, String type, LocationEntity location) {
        this.name = name;
        this.type = type;
        this.location = location;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public LocationEntity getLocation() {
        return location;
    }

    public String getOwnerUuid() {
        return ownerUuid;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setLocation(LocationEntity location) {
        this.location = location;
    }

    public void setOwnerUuid(String ownerUuid) {
        this.ownerUuid = ownerUuid;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

}
