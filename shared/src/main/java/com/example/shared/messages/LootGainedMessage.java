package com.example.shared.messages;

import java.io.Serializable;

public class LootGainedMessage implements Serializable {
    private String itemId;  // "ore_copper", "coal" тощо
    private int amount;
    private int exp;        // скільки досвіду за бій

    public LootGainedMessage(){}
    public LootGainedMessage(String itemId, int amount, int exp){
        this.itemId=itemId; this.amount=amount; this.exp=exp;
    }
    public String getItemId(){ return itemId; }
    public int getAmount(){ return amount; }
    public int getExp(){ return exp; }
}
