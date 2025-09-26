package com.example.shared.messages;

import java.io.Serializable;

public class PlayerStatsMessage implements Serializable {
    private int hp;
    private int maxHp;
    private int level;
    private int exp;        // поточний досвід у рівні
    private int expToNext;  // скільки потрібно до наступного рівня

    public PlayerStatsMessage() {}
    public PlayerStatsMessage(int hp, int maxHp, int level, int exp, int expToNext) {
        this.hp = hp; this.maxHp = maxHp; this.level = level; this.exp = exp; this.expToNext = expToNext;
    }

    public int getHp() { return hp; }
    public int getMaxHp() { return maxHp; }
    public int getLevel() { return level; }
    public int getExp() { return exp; }
    public int getExpToNext() { return expToNext; }
}
