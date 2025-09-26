package com.example.server.model;

import jakarta.persistence.*;

@Entity
@Table(name = "building_access")
public class BuildingAccessEntity {

    @EmbeddedId
    private BuildingAccessId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("buildingId")
    @JoinColumn(name = "building_id", nullable = false)
    private BuildingEntity building;

    @Column(name = "role", nullable = false)
    private String role = "KEYHOLDER";

    public BuildingAccessEntity() {}
    public BuildingAccessEntity(BuildingEntity building, String playerUuid, String role) {
        this.building = building;
        this.id = new BuildingAccessId(building.getId(), playerUuid);
        this.role = role;
    }

    public BuildingAccessId getId() { return id; }
    public BuildingEntity getBuilding() { return building; }
    public String getRole() { return role; }

    public void setRole(String role) { this.role = role; }

    @Embeddable
    public static class BuildingAccessId implements java.io.Serializable {
        @Column(name = "building_id")
        private Long buildingId;

        @Column(name = "player_uuid")
        private String playerUuid;

        public BuildingAccessId() {}
        public BuildingAccessId(Long buildingId, String playerUuid) {
            this.buildingId = buildingId; this.playerUuid = playerUuid;
        }

        public Long getBuildingId() { return buildingId; }
        public String getPlayerUuid() { return playerUuid; }
        public void setBuildingId(Long buildingId) { this.buildingId = buildingId; }
        public void setPlayerUuid(String playerUuid) { this.playerUuid = playerUuid; }

        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof BuildingAccessId that)) return false;
            return java.util.Objects.equals(buildingId, that.buildingId)
                           && java.util.Objects.equals(playerUuid, that.playerUuid);
        }
        @Override public int hashCode() { return java.util.Objects.hash(buildingId, playerUuid); }
    }
}
