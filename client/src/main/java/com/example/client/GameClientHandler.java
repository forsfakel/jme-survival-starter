package com.example.client;

import com.example.shared.messages.LocationMessage;
import com.example.shared.messages.LootGainedMessage;
import com.example.shared.messages.OpenBuildingResponse;
import com.example.shared.messages.PlayerListMessage;
import com.example.shared.messages.PlayerStatsMessage;
import com.example.shared.messages.BattleStartResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class GameClientHandler extends SimpleChannelInboundHandler<Object> {
    private final GameApp gameApp;

    public GameClientHandler(GameApp gameApp) {
        this.gameApp = gameApp;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof LocationMessage loc) {
            gameApp.onLocation(loc);
        } else if (msg instanceof PlayerListMessage list) {
            gameApp.onPlayerList(list);
        } else if (msg instanceof OpenBuildingResponse r) {
            gameApp.enqueue(() -> {
                if (r.isSuccess()) {
                    com.example.client.PlayerContext.get().setBuilding(r.getBuildingName(), true);
                    gameApp.onOpenBuilding(r);
                } else {
                    gameApp.onOpenBuildingError(r.getErrorMessage());
                }
            });
        } else if (msg instanceof PlayerStatsMessage s) {
            gameApp.onPlayerStats(s); // Оновити HUD/контекст
        } else if (msg instanceof BattleStartResponse r) {
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
        } else if (msg instanceof LootGainedMessage loot) {
            gameApp.enqueue(() -> gameApp.toast(
                    "Лут: " + loot.getItemId() + " x" + loot.getAmount() + " (EXP +" + loot.getExp()
                            + ")"));

        }
    }
}
