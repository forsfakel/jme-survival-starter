package com.example.shared.messages;

import java.io.Serializable;

/** Клієнт повідомляє про бажаний рух */
public class MoveMessage implements Serializable {
    private int x, y;

    public MoveMessage() {}
    public MoveMessage(int x, int y) {
        this.x = x; this.y = y;
    }

    public int getX() { return x; }
    public int getY() { return y; }
}
