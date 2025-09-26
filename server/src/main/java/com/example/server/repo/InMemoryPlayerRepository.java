package com.example.server.repo;

import com.example.shared.model.Player;
import com.example.shared.model.WorldLocation;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryPlayerRepository implements PlayerRepository {
    private final Map<String, Player> byName = new ConcurrentHashMap<>();

    @Override
    public Player findByName(String name) {
        return byName.get(name);
    }

    @Override
    public void saveOrUpdate(Player p) {
        if (p.getLocation() == null) p.setLocation(new WorldLocation(0,0));
        byName.put(p.getName(), p);
    }
}
