package com.example.shared.messages;

import java.io.Serializable;

public class LoginMessage implements Serializable {
    private String playerName;

    public LoginMessage() {
    }

    public LoginMessage(String playerName) {
        this.playerName = playerName;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }
}
