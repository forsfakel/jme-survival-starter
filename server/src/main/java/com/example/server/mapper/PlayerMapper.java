package com.example.server.mapper;

import com.example.server.model.LocationEntity;
import com.example.server.model.PlayerEntity;
import com.example.server.model.id.LocationId;
import com.example.shared.model.Player;
import com.example.shared.model.WorldLocation;

/**
 * Маппінг між JPA-сутністю PlayerEntity (має ManyToOne на LocationEntity)
 * та DTO Player (має WorldLocation (x,y)).
 */
public final class PlayerMapper {
    private PlayerMapper() {}

    /** Entity -> DTO */
    public static Player toDto(PlayerEntity e) {
        if (e == null) return null;

        int x = 0, y = 0;
        LocationEntity loc = e.getLocation();
        if (loc != null && loc.getId() != null) {
            LocationId id = loc.getId();
            x = id.getX();
            y = id.getY();
        }
        return new Player(
                e.getUuid(),
                e.getNickname(),
                new WorldLocation(x, y)
        );
    }

    /**
     * DTO -> NEW Entity
     * Використовуй, коли створюєш нового гравця: локацію передай явно (отриману з repo.getOrCreate)
     */
    public static PlayerEntity toNewEntity(Player dto, LocationEntity locationEntity) {
        if (dto == null) return null;
        PlayerEntity e = new PlayerEntity();
        e.setUuid(dto.getId());
        e.setNickname(dto.getName());
        e.setLocation(locationEntity);
        return e;
    }

    /**
     * Оновити існуючу Entity з DTO (ім’я/uuid/локація).
     * Локацію передай явно (отриману з репозиторію), щоб не тримати всередині маппера доступ до БД.
     */
    public static void updateEntity(Player dto, PlayerEntity e, LocationEntity locationEntity) {
        if (dto == null || e == null) return;
        e.setUuid(dto.getId());
        e.setNickname(dto.getName());
        e.setLocation(locationEntity);
    }
}
