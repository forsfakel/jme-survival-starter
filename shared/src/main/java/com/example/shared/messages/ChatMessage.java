package com.example.shared.messages;

import java.io.Serializable;

/**
 * Повідомлення чату
 */
public class ChatMessage implements Serializable {
    private String from;
    private String text;

    public ChatMessage() {}

    public ChatMessage(String from, String text) {
        this.from = from;
        this.text = text;
    }

    public String getFrom() { return from; }
    public String getText() { return text; }

    public void setFrom(String from) { this.from = from; }
    public void setText(String text) { this.text = text; }
}
