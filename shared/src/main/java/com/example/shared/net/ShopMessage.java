package com.example.shared.net;

public class ShopMessage {
    public enum Action {
        BUY, SELL
    }

    private Action action;
    private String itemId;
    private int quantity;
    private String playerId;

    public ShopMessage(Action action, String itemId, int quantity, String playerId) {
        this.action = action;
        this.itemId = itemId;
        this.quantity = quantity;
        this.playerId = playerId;
    }

    public Action getAction() {
        return action;
    }

    public String getItemId() {
        return itemId;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getPlayerId() {
        return playerId;
    }
}
