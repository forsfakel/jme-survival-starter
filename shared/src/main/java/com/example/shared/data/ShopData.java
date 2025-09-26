package com.example.shared.data;

import com.example.shared.model.Item;
import com.example.shared.model.ItemType;
import com.example.shared.model.Shop;
import com.example.shared.model.ShopItem;

public class ShopData {
    public static Shop createDefaultShop() {

        Shop shop = new Shop("shop-1", "Загальний магазин");

        // 🌲 Ресурси
        shop.addItem(new ShopItem(new Item("wood", "Дерево", ItemType.WOOD, 1), 5,  // купівля
                2   // продаж
        ));

        shop.addItem(new ShopItem(new Item("stone", "Камінь", ItemType.STONE, 1), 8, 3));

        shop.addItem(
                new ShopItem(new Item("iron_ore", "Залізна руда", ItemType.IRON_ORE, 1), 20, 10));

        // 🍎 Їжа
        shop.addItem(new ShopItem(new Item("apple", "Яблуко", ItemType.FOOD, 1), 3, 1));

        shop.addItem(new ShopItem(new Item("bread", "Хліб", ItemType.FOOD, 1), 6, 2));

        // ⚔️ Спорядження
        shop.addItem(new ShopItem(new Item("sword", "Залізний меч", ItemType.WEAPON, 1), 100, 50));

        shop.addItem(new ShopItem(new Item("armor", "Шкіряна броня", ItemType.ARMOR, 1), 80, 40));

        return shop;
    }
}
