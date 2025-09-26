package com.example.server.repo;

import com.example.shared.model.Player;

public interface PlayerRepository {
    Player findByName(String name);
    void saveOrUpdate(Player p);
}
