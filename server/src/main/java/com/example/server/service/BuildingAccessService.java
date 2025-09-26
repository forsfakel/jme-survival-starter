package com.example.server.service;

import com.example.server.model.BuildingEntity;

public class BuildingAccessService {
    // Спрощено: шахта/магазин/сховище — завжди; завод — лише з accessKey/owner
    public boolean hasAccess(BuildingEntity b, String playerUuid) {
        if (b == null) return false;
        String t = b.getType() != null ? b.getType().toUpperCase() : "";
        switch (t) {
            case "FACTORY":
                // тут може бути перевірка таблиці building_access, поки — власник або наявність accessKey == null
                if (b.getOwnerUuid() != null && b.getOwnerUuid().equals(playerUuid)) return true;
                return b.getAccessKey() == null || b.getAccessKey().isBlank(); // тимчасово
            default:
                return true;
        }
    }
}
