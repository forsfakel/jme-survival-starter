package com.example.server.service;

import com.example.server.dao.BuildingDao;
import com.example.shared.model.Building;

import java.util.List;

public class BuildingService {

    private final BuildingDao dao;

    public BuildingService(BuildingDao dao) {
        this.dao = dao;
    }

    public void saveBuilding(Building building) {
        dao.save(building);
    }

    public void updateBuilding(Building building) {
        dao.update(building);
    }

    public Building getBuilding(Long id) {
        return dao.findById(id);
    }

    public List<Building> getAllBuildings() {
        return dao.findAll();
    }

    public void deleteBuilding(Building building) {
        dao.delete(building);
    }
}
