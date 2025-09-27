package com.example.server.service;

import com.example.server.GameServer;
import com.example.server.model.PlayerEntity;
import com.example.server.repo.PlayerJpaRepository;
import com.example.server.session.ClientSession;
import com.example.shared.messages.PlayerStatsMessage;
import io.netty.channel.Channel;

import java.util.Map;
import java.util.concurrent.*;

public class HpRegenService implements AutoCloseable {
    private final GameServer server;
    private final ScheduledExecutorService exec =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "hp-regen");
                t.setDaemon(true);
                return t;
            });

    public HpRegenService(GameServer server) {
        this.server = server;
    }

    public void start() {
        exec.scheduleAtFixedRate(this::tick, 60, 60, TimeUnit.SECONDS);
    }

    private void tick() {
        try {
            PlayerJpaRepository repo = server.players();
            // проходимо по онлайн-сесіях
            for (Map.Entry<Channel, ClientSession> e : server.sessions().entrySet()) {
                ClientSession sess = e.getValue();
                if (sess == null || sess.getPlayer() == null) continue;
                if (sess.isInBattle()) continue; // не регенимо в бою

                String name = sess.getPlayer().getName();
                PlayerEntity pe = repo.findByName(name);
                if (pe == null) continue;

                int hp     = pe.getHp();
                int hpMax  = pe.getHpMax();
                if (hp >= hpMax) continue;

                int add = Math.max(1, (int) Math.floor(hpMax * 0.10)); // +10% мінімум +1
                int newHp = Math.min(hp + add, hpMax);

                pe.setHp(newHp);
                repo.saveOrUpdate(pe);

                // повідомляємо клієнта
                PlayerStatsMessage msg = new PlayerStatsMessage(newHp, hpMax, 1, 0, 100);
                e.getKey().writeAndFlush(msg);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override public void close() {
        exec.shutdownNow();
    }
}
