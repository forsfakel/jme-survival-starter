package com.example.shared.model;

import java.io.Serializable;

public class WorldLocation implements Serializable {
    private int x;
    private int y;

    public WorldLocation() {
    }

    public WorldLocation(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    @Override
    public String toString() {
        return x + ":" + y;
    }
}
