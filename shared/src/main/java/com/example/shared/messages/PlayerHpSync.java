package com.example.shared.messages;
import java.io.Serializable;

public class PlayerHpSync implements Serializable {
    public final int hp, hpMax;
    public PlayerHpSync(int hp, int hpMax) { this.hp = hp; this.hpMax = hpMax; }
}