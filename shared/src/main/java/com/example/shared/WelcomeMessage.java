package com.example.shared;

import java.io.Serializable;

public class WelcomeMessage implements Serializable {
    public String assignedId;

    public WelcomeMessage() {}
    public WelcomeMessage(String assignedId) {
        this.assignedId = assignedId;
    }
}