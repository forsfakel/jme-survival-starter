package com.example.shared.messages;

import java.io.Serializable;

public class BattleEndReport implements Serializable {
    private String battleId;
    private boolean victory;

    public BattleEndReport(){}
    public BattleEndReport(String battleId, boolean victory){
        this.battleId=battleId; this.victory=victory;
    }
    public String getBattleId(){ return battleId; }
    public boolean isVictory(){ return victory; }
}
