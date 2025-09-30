package com.example.server.model;

import jakarta.persistence.*;

@Entity
@Table(name = "players",
        indexes = {@Index(name = "ix_players_loc",
                columnList = "loc_x,loc_y")})
public class PlayerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // ЄДИНИЙ первинний ключ

    @Column(nullable = false,
            unique = true)
    private String uuid;

    // У БД колонка називається "name", тому мапимося на неї.
    @Column(name = "name",
            length = 64,
            nullable = false,
            updatable = false,
            unique = true)
    private String nickname;

    // Ці колонки в БД уже є з дефолтами. columnDefinition допоможе, якщо Hibernate колись
    // спробує створити/оновити схему під SQLite.
    @Column(name = "hp",
            nullable = false,
            columnDefinition = "INTEGER NOT NULL DEFAULT 100")
    private int hp;

    @Column(name = "hp_max",
            nullable = false,
            columnDefinition = "INTEGER NOT NULL DEFAULT 100")
    private int hpMax;

    @Version
    @Column(name = "version",
            nullable = false,
            columnDefinition = "INTEGER NOT NULL DEFAULT 0")
    private long version;

    @ManyToOne(fetch = FetchType.LAZY,
            optional = false)
    @JoinColumns({@JoinColumn(name = "loc_x",
            referencedColumnName = "x",
            nullable = false), @JoinColumn(name = "loc_y",
            referencedColumnName = "y",
            nullable = false)})
    private LocationEntity location;

    @Column(name = "in_building",
            nullable = false)
    private boolean inBuilding;

    @Column(name = "building_name")
    private String buildingName;

    public PlayerEntity() {
    }

    public PlayerEntity(String uuid, String name, LocationEntity location) {
        this.uuid = uuid;
        this.nickname = name;
        this.location = location;
    }

    public Long getId() {
        return id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public int getHp() {
        return hp;
    }

    public void setHp(int hp) {
        this.hp = hp;
    }

    public int getHpMax() {
        return hpMax;
    }

    public void setHpMax(int hpMax) {
        this.hpMax = hpMax;
    }

    public long getVersion() {
        return version;
    }

    public LocationEntity getLocation() {
        return location;
    }

    public void setLocation(LocationEntity location) {
        this.location = location;
    }

    public boolean isInBuilding() {
        return inBuilding;
    }

    public void setInBuilding(boolean v) {
        this.inBuilding = v;
    }

    public String getBuildingName() {
        return buildingName;
    }

    public void setBuildingName(String n) {
        this.buildingName = n;
    }

    @PrePersist
    public void prePersist() {
        if (hpMax <= 0) {
            hpMax = 100;
        }
        if (hp <= 0) {
            hp = hpMax;
        }
    }
}
