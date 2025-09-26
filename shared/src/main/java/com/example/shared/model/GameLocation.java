package com.example.shared.model;

import java.io.Serializable;

/**
 * Спрощене представлення локації (наприклад, для відображення на клієнті).
 */
public class GameLocation implements Serializable {
    private final int x;
    private final int y;

    public GameLocation(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() { return x; }
    public int getY() { return y; }

    @Override
    public String toString() {
        return "GameLocation{" + "x=" + x + ", y=" + y + '}';
    }
}
