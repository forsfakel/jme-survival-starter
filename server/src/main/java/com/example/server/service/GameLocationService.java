package com.example.server.service;

import com.example.server.dao.GameLocationDao;
import com.example.shared.model.GameLocation;

import java.util.List;

public class GameLocationService {

    private final GameLocationDao dao;

    public GameLocationService(GameLocationDao dao) {
        this.dao = dao;
    }

    public void saveLocation(GameLocation location) {
        dao.save(location);
    }

    public void updateLocation(GameLocation location) {
        dao.update(location);
    }

    public GameLocation getLocation(Long id) {
        return dao.findById(id);
    }

    public List<GameLocation> getAllLocations() {
        return dao.findAll();
    }
}
