package com.example.server;

import com.example.server.mapper.PlayerMapper;
import com.example.server.model.BuildingEntity;
import com.example.server.model.LocationEntity;
import com.example.server.model.PlayerEntity;
import com.example.server.model.id.LocationId;
import com.example.server.repo.LocationJpaRepository;
import com.example.server.repo.PlayerJpaRepository;
import com.example.server.session.ClientSession;
import com.example.server.util.HibernateUtil;
import com.example.shared.messages.LocationMessage;
import com.example.shared.messages.LoginMessage;
import com.example.shared.messages.OpenBuildingRequest;
import com.example.shared.messages.OpenBuildingResponse;
import com.example.shared.messages.PlayerHpSync;
import com.example.shared.messages.PlayerListMessage;
import com.example.shared.messages.PlayerPositionMessage;
import com.example.shared.messages.PlayerStatsMessage;
import com.example.shared.model.Player;
import com.example.shared.model.WorldLocation;
import io.netty.channel.*;

import org.hibernate.Session;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class GameServerHandler extends SimpleChannelInboundHandler<Object> {

    private final GameServer server;

    public GameServerHandler(GameServer server) {
        this.server = server;
    }

    // ========= Netty lifecycle =========

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        server.sessions().put(ctx.channel(), new ClientSession());
        System.out.println("🔵 Connect: " + ctx.channel().id().asShortText());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        ClientSession sess = server.sessions().remove(ctx.channel());
        if (sess != null && sess.getPlayer() != null) {
            WorldLocation loc = sess.getPlayer().getLocation();
            if (loc != null) {
                unsubscribe(ctx.channel(), loc);
                broadcastPlayerList(loc);
            }
            System.out.println("🔴 Disconnect: " + sess.getPlayer().getName());
        }
    }

    // ========= Messages =========

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof LoginMessage login) {
            handleLogin(ctx, login);
        } else if (msg instanceof PlayerPositionMessage pos) {
            handleMove(ctx, pos);
        } else if (msg instanceof OpenBuildingRequest req) {
            handleOpenBuilding(ctx, req);
        } else if (msg instanceof com.example.shared.messages.BattleStartRequest req) {
            System.out.println(
                    "[SERVER] BattleStartRequest at " + req.getX() + "," + req.getY() + " arena="
                            + req.getArena());
            String battleId = java.util.UUID.randomUUID().toString();
            var enemies = java.util.Arrays.asList("rat", "bat"); // мок
            ctx.writeAndFlush(
                    new com.example.shared.messages.BattleStartResponse(true, battleId, "Fight!",
                            enemies));
        } else if (msg instanceof com.example.shared.messages.PlayerHpSync sync) {
            System.out.println("[HP-SYNC] incoming " + sync.hp + "/" + sync.hpMax);
            var session = server.sessions().get(ctx.channel());
            if (session == null || session.getPlayer() == null) {
                return;
            }
            String name = session.getPlayer().getName();

            int hpMax = Math.max(1, Math.min(sync.hpMax, 9999));
            int hp    = Math.max(0, Math.min(sync.hp, hpMax));

            var repo = new com.example.server.repo.PlayerJpaRepository();
            var pe = repo.findByName(name);
            if (pe == null) {
                System.out.println("[HP-SYNC] skip: no player entity for '" + name + "'");
                return;
            }

            System.out.println("[HP-SYNC] before DB " + pe.getNickname() + " " + pe.getHp() + "/" + pe.getHpMax());
            pe.setHp(hp);
            pe.setHpMax(hpMax);
            repo.saveOrUpdate(pe); // усередині merge+commit

            // Перечитаємо одразу (для діагностики)
            var pe2 = repo.findByName(name);
            System.out.println("[HP-SYNC] after DB  " + pe2.getNickname() + " " + pe2.getHp() + "/" + pe2.getHpMax());

            ctx.writeAndFlush(new com.example.shared.messages.PlayerStatsMessage(
                    pe2.getHp(), pe2.getHpMax(), 1, 0, 100
            ));
        }
    }

    // ========= Handlers =========

    private void handleLogin(ChannelHandlerContext ctx, LoginMessage login) {
        final String name = Optional.ofNullable(login.getPlayerName()).filter(s -> !s.isBlank())
                                    .orElse("Player_" + ctx.channel().id().asShortText());

        PlayerJpaRepository playerRepo = server.players();
        LocationJpaRepository locationRepo = server.locations();

        // знайти/створити гравця
        var pe = playerRepo.findByName(name);
        System.out.println("[LOGIN] findByName('" + name + "') -> " + (pe==null ? "null" : pe.getId()));
        if (pe == null) {
            var loc00 = locationRepo.getOrCreate(0, 0);
            pe = new PlayerEntity(UUID.randomUUID().toString(), name, loc00); // тут hp=100/100 тільки для нових
            playerRepo.saveOrUpdate(pe);
        }

        // DTO для сесії/клієнта
        Player dto = PlayerMapper.toDto(pe);
        server.sessions().get(ctx.channel()).setPlayer(dto);

        // підписка на її канал + перше завантаження будівель
        subscribe(ctx.channel(), dto.getLocation());
        List<String> buildingNames = fetchBuildingNames(dto.getLocation().getX(),
                dto.getLocation().getY());

        // відправляємо локацію клієнту
        ctx.writeAndFlush(new LocationMessage(dto.getLocation(), buildingNames));

        // розсилаємо список гравців у цій локації
        broadcastPlayerList(dto.getLocation());

        // надіслати поточні стати (HP з БД) клієнту для HUD/контексту
        // Гарантовано шлемо HP з БД після логіну
        System.out.println("[LOGIN] -> PlayerStatsMessage " + pe.getHp() + "/" + pe.getHpMax());
        ctx.writeAndFlush(new com.example.shared.messages.PlayerStatsMessage(
                pe.getHp(), pe.getHpMax(), 1, 0, 100
        ));

        System.out.println("👤 Login ok: " + dto.getName() + " @ " + dto.getLocation().getX() + ","
                                   + dto.getLocation().getY());
    }

    private void handleMove(ChannelHandlerContext ctx, PlayerPositionMessage pos) {
        ClientSession sess = server.sessions().get(ctx.channel());
        if (sess == null || sess.getPlayer() == null) {
            return;
        }

        Player dto = sess.getPlayer();
        WorldLocation oldLoc = dto.getLocation();
        WorldLocation newLoc = new WorldLocation(pos.getX(), pos.getY());

        if (oldLoc.getX() == newLoc.getX() && oldLoc.getY() == newLoc.getY()) {
            // координати ті самі — нічого не робимо
            return;
        }

        // оновлюємо в БД: підв'язуємо до нової LocationEntity
        LocationEntity newLocEntity = server.locations().getOrCreate(newLoc.getX(), newLoc.getY());
        PlayerEntity pe = server.players().findByName(dto.getName());
        if (pe != null) {
            pe.setLocation(newLocEntity);
            server.players().saveOrUpdate(pe);
        }

        // оновлюємо DTO у сесії
        dto.setLocation(newLoc);

        // переоформляємо підписки
        unsubscribe(ctx.channel(), oldLoc);
        subscribe(ctx.channel(), newLoc);

        // відправляємо новий опис локації (ландшафт + будівлі)
        List<String> buildingNames = fetchBuildingNames(newLoc.getX(), newLoc.getY());
        ctx.writeAndFlush(new LocationMessage(newLoc, buildingNames));

        // оновлюємо списки гравців у старій і новій локаціях
        broadcastPlayerList(oldLoc);
        broadcastPlayerList(newLoc);

        System.out.println(
                "➡️ Move: " + dto.getName() + " " + oldLoc.getX() + "," + oldLoc.getY() + " -> "
                        + newLoc.getX() + "," + newLoc.getY());
    }

    // ========= Helpers =========

    private static String key(WorldLocation loc) {
        return loc.getX() + ":" + loc.getY();
    }

    private static String key(int x, int y) {
        return x + ":" + y;
    }

    private void subscribe(Channel ch, WorldLocation loc) {
        server.locationChannels().computeIfAbsent(key(loc), k -> ConcurrentHashMap.newKeySet()).add(
                ch);
    }

    private void unsubscribe(Channel ch, WorldLocation loc) {
        Set<Channel> set = server.locationChannels().get(key(loc));
        if (set != null) {
            set.remove(ch);
        }
    }

    private void broadcastPlayerList(WorldLocation loc) {
        Set<Channel> set = server.locationChannels().get(key(loc));
        if (set == null || set.isEmpty()) {
            return;
        }

        List<String> names = server.sessions().values().stream().map(ClientSession::getPlayer)
                                     .filter(Objects::nonNull).filter(
                        p -> p.getLocation() != null && p.getLocation().getX() == loc.getX()
                                     && p.getLocation().getY() == loc.getY()).map(Player::getName)
                                     .sorted().collect(Collectors.toList());

        PlayerListMessage msg = new PlayerListMessage(names);
        for (Channel ch : set)
            ch.writeAndFlush(msg);
    }

    /**
     * Витягує список назв будівель у локації (x,y) через HQL.
     * Робимо окремий session, щоб не тягнути LAZY-поля поза транзакцією.
     */
    private List<String> fetchBuildingNames(int x, int y) {
        try (Session s = HibernateUtil.getSessionFactory().openSession()) {
            return s.createQuery("select b.name from BuildingEntity b "
                                         + "where b.location.id.x = :x and b.location.id.y = :y "
                                         + "order by b.name", String.class).setParameter("x", x)
                           .setParameter("y", y).getResultList();
        }
    }

    private void handleOpenBuilding(ChannelHandlerContext ctx, OpenBuildingRequest req) {
        var sess = server.sessions().get(ctx.channel());
        if (sess == null || sess.getPlayer() == null) {
            return;
        }

        int x = req.getX(), y = req.getY();
        String name = req.getBuildingName();

        // Завантажуємо будівлю з БД
        try (var s = com.example.server.util.HibernateUtil.getSessionFactory().openSession()) {
            var b = s.createQuery(
                            "from BuildingEntity b where b.location.id.x=:x and b.location.id.y=:y and b.name=:n",
                            com.example.server.model.BuildingEntity.class).setParameter("x", x)
                            .setParameter("y", y).setParameter("n", name).uniqueResult();

            if (b == null) {
                ctx.writeAndFlush(
                        new OpenBuildingResponse(false, "Building not found", name, null));
                return;
            }

            var svc = new com.example.server.service.BuildingAccessService();
            boolean ok = svc.hasAccess(b,
                    sess.getPlayer().getId()); // id = uuid у твоєму DTO Player
            if (!ok) {
                ctx.writeAndFlush(new OpenBuildingResponse(false, "Access denied", name, null));
                return;
            }

            // Мапимо type → enum
            com.example.shared.model.BuildingType bt;
            String t = b.getType() != null ? b.getType().toUpperCase() : "";
            switch (t) {
                case "MINE" -> bt = com.example.shared.model.BuildingType.MINE;
                case "SHOP" -> bt = com.example.shared.model.BuildingType.SHOP;
                case "WAREHOUSE" -> bt = com.example.shared.model.BuildingType.WAREHOUSE;
                case "FACTORY" -> bt = com.example.shared.model.BuildingType.FACTORY;
                default -> bt = com.example.shared.model.BuildingType.SHOP; // дефолт
            }

            ctx.writeAndFlush(new OpenBuildingResponse(true, null, name, bt));
        }
    }
}
