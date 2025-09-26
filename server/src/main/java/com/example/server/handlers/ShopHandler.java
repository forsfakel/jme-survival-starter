package com.example.server.handlers;

import com.example.shared.model.*;
import com.example.shared.data.ShopData;
import com.example.shared.net.ShopMessage;

public class ShopHandler {

    private final Shop shop = ShopData.createDefaultShop();

    public String handleShopAction(ShopMessage msg, int playerGold) {
        switch (msg.getAction()) {
            case BUY: {
                Item bought = shop.buy(msg.getItemId(), playerGold);
                if (bought != null) {
                    return "OK: Куплено " + bought.getName();
                } else {
                    return "ERR: Недостатньо золота або товар відсутній";
                }
            }
            case SELL: {
                ShopItem si = shop.getItem(msg.getItemId());
                if (si == null) {
                    return "ERR: Магазин не приймає цей товар";
                }
                int gold = si.getSellPrice() * msg.getQuantity();
                return "OK: Продано " + msg.getQuantity() + " × " + si.getItem().getName() + " за "
                               + gold + " золота";
            }
            default:
                return "ERR: Невідома дія";
        }
    }
}
