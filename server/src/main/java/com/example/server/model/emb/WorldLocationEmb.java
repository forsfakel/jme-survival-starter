package com.example.server.model.emb;

import jakarta.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public class WorldLocationEmb implements Serializable {
    private int x;
    private int y;

    public WorldLocationEmb() {}
    public WorldLocationEmb(int x, int y) { this.x = x; this.y = y; }

    public int getX() { return x; }
    public int getY() { return y; }
    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }
}
