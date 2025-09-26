package com.example.shared.net;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Повідомлення з серверу до клієнта про список гравців у локації.
 * Клієнту достатньо знати лише імена гравців, без повного об'єкта Player.
 */
public class PlayerListMessage implements Serializable {
    private int x;
    private int y;
    private List<String> players = new ArrayList<>();

    public PlayerListMessage() {}

    public PlayerListMessage(int x, int y, Iterable<String> playerNames) {
        this.x = x;
        this.y = y;
        for (String name : playerNames) {
            this.players.add(name);
        }
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public List<String> getPlayers() { return players; }

    @Override
    public String toString() {
        return "PlayerListMessage{" +
                       "x=" + x +
                       ", y=" + y +
                       ", players=" + players +
                       '}';
    }
}
