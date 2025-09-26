package com.example.shared;

import com.example.shared.buildings.*;
import com.example.shared.model.GameLocation;

import java.util.HashMap;
import java.util.Map;

public class LocationManager {

//    // Ключ: "x:y", значення: GameLocation
//    private final Map<String, GameLocation> locations = new HashMap<>();
//
//    public LocationManager() {
//        // Ініціалізація стартових локацій з будівлями
//        initDefaultLocations();
//    }
//
//    private void initDefaultLocations() {
//        // Локація (0,0) — містить магазин і сховище
//        GameLocation center = new GameLocation(0, 0);
//        center.addBuilding(new Shop("shop-001"));
//        center.addBuilding(new Storage("storage-001"));
//        locations.put(key(0, 0), center);
//
//        // Локація (1,0) — шахта
//        GameLocation mine = new GameLocation(1, 0);
//        mine.addBuilding(new Mine("mine-001"));
//        locations.put(key(1, 0), mine);
//
//        // Локація (0,1) — завод (для гравців з ключами)
//        GameLocation factory = new GameLocation(0, 1);
//        factory.addBuilding(new Factory("factory-001"));
//        locations.put(key(0, 1), factory);
//
//        // Локація (-1,0) — порожня (без будівель)
//        GameLocation empty = new GameLocation(-1, 0);
//        locations.put(key(-1, 0), empty);
//    }
//
//    // Повертає локацію за координатами
//    public GameLocation getLocation(int x, int y) {
//        return locations.get(key(x, y));
//    }
//
//    // Додає/оновлює локацію
//    public void setLocation(GameLocation location) {
//        locations.put(key(location.getX(), location.getY()), location);
//    }
//
//    // Допоміжний метод для ключа
//    private String key(int x, int y) {
//        return x + ":" + y;
//    }
}
