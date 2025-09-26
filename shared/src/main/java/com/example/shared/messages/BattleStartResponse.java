package com.example.shared.messages;

import java.io.Serializable;
import java.util.List;

public class BattleStartResponse implements Serializable {
    private boolean ok;
    private String battleId;
    private String message;
    private List<String> enemyTypes; // напр., ["rat","bat"]

    public BattleStartResponse() {}
    public BattleStartResponse(boolean ok, String battleId, String message, List<String> enemyTypes){
        this.ok=ok; this.battleId=battleId; this.message=message; this.enemyTypes=enemyTypes;
    }
    public boolean isOk(){ return ok; }
    public String getBattleId(){ return battleId; }
    public String getMessage(){ return message; }
    public List<String> getEnemyTypes(){ return enemyTypes; }
}
