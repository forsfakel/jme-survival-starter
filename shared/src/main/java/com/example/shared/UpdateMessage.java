package com.example.shared;

import java.io.Serializable;
import java.util.List;

public class UpdateMessage implements Serializable {
    public List<PlayerState> players;

    public UpdateMessage() {}
    public UpdateMessage(List<PlayerState> players) {
        this.players = players;
    }
}