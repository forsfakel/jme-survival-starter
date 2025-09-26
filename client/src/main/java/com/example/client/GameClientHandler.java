package com.example.client;

import com.example.shared.messages.LocationMessage;
import com.example.shared.messages.OpenBuildingResponse;
import com.example.shared.messages.PlayerListMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class GameClientHandler extends SimpleChannelInboundHandler<Object> {
    private final GameApp gameApp;

    public GameClientHandler(GameApp gameApp) { this.gameApp = gameApp; }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof LocationMessage loc) {
            gameApp.onLocation(loc);
        } else if (msg instanceof PlayerListMessage list) {
            gameApp.onPlayerList(list);
        } else if (msg instanceof OpenBuildingResponse ob) {
            gameApp.onOpenBuildingResponse(ob);
        } else if (msg instanceof com.example.shared.messages.PlayerStatsMessage s) {
            gameApp.onPlayerStats(s);
        } else if (msg instanceof com.example.shared.messages.BattleStartResponse r) {
            gameApp.enqueue(() -> {
                if (!r.isOk()) {
                    gameApp.toast(r.getMessage());
                    return;
                }
                // створюємо бойову сцену й повну заміну
                var st = new com.example.client.screens.BattleState3D(r.getBattleId(),
                        com.example.client.screens.BattleState3D.Arena.MINE_TUNNEL,
                        // або мапни з r.getArena()
                        gameApp::leaveSubScene
                        // ⟵ викличеться при виході з бою
                );
                gameApp.enterSubScene(st);
            });
        } else if (msg instanceof com.example.shared.messages.LootGainedMessage loot) {
            gameApp.enqueue(() -> gameApp.toast(
                    "Лут: " + loot.getItemId() + " x" + loot.getAmount() + " (EXP +" + loot.getExp()
                            + ")"));
        } else if (msg instanceof com.example.shared.messages.PlayerStatsMessage s) {
            gameApp.onPlayerStats(s); // HUD оновиться
        } else if (msg instanceof com.example.shared.messages.BattleStartRequest req) {
            String battleId = java.util.UUID.randomUUID().toString();
            var enemies = java.util.Arrays.asList("rat","bat"); // мок
            ctx.writeAndFlush(new com.example.shared.messages.BattleStartResponse(
                    true, battleId, "Fight!", enemies
            ));
        }

        else if (msg instanceof com.example.shared.messages.BattleEndReport rep) {
            // мок лут/exp
            String item = "ore_copper";
            int amount = 1 + new java.util.Random().nextInt(3);
            int exp = 8;

            // TODO: inventoryRepo.addItem(playerId, item, amount);
            // TODO: levelService.addExp(playerId, exp);

            ctx.writeAndFlush(new com.example.shared.messages.LootGainedMessage(item, amount, exp));

            // відправимо нову статистику для HUD (якщо є сервіс — підстав свій)
            // тут просто мок:
            ctx.writeAndFlush(new com.example.shared.messages.PlayerStatsMessage(
                    50, 100, 1, 25, 100
            ));
        }
    }
}
