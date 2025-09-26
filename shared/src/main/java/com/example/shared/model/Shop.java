package com.example.shared.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Shop {
    private String id;
    private String name;
    private Map<String, ShopItem> items = new HashMap<>();

    public Shop(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public void addItem(ShopItem shopItem) {
        items.put(shopItem.getItem().getId(), shopItem);
    }

    public ShopItem getItem(String itemId) {
        return items.get(itemId);
    }

    public Collection<ShopItem> getAllItems() {
        return items.values();
    }

    /**
     * Купівля предмета у магазині.
     * @param itemId ідентифікатор предмета
     * @param gold у гравця
     * @return предмет або null, якщо не вистачає грошей
     */
    public Item buy(String itemId, int gold) {
        ShopItem shopItem = items.get(itemId);
        if (shopItem != null && gold >= shopItem.getBuyPrice()) {
            return new Item(
                    shopItem.getItem().getId(),
                    shopItem.getItem().getName(),
                    shopItem.getItem().getType(),
                    1
            );
        }
        return null;
    }

    /**
     * Продаж предмета у магазин.
     * @param item предмет
     * @return золото, отримане від продажу
     */
    public int sell(Item item) {
        ShopItem shopItem = items.get(item.getId());
        if (shopItem != null) {
            return shopItem.getSellPrice() * item.getQuantity();
        }
        return 0;
    }
}
