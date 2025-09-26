package com.example.shared.model;

public class ShopItem {
    private Item item;
    private int buyPrice;   // ціна покупки у магазині
    private int sellPrice;  // ціна продажу магазину

    public ShopItem(Item item, int buyPrice, int sellPrice) {
        this.item = item;
        this.buyPrice = buyPrice;
        this.sellPrice = sellPrice;
    }

    public Item getItem() {
        return item;
    }

    public int getBuyPrice() {
        return buyPrice;
    }

    public int getSellPrice() {
        return sellPrice;
    }
}
