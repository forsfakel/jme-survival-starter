package com.example.server;

import com.example.server.repo.LocationJpaRepository;
import com.example.server.repo.PlayerJpaRepository;
import com.example.server.session.ClientSession;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class GameServer {

    private final int port;
    // активні сесії
    final Map<Channel, ClientSession> sessions = new ConcurrentHashMap<>();
    // канали по ключу локації "x:y"
    final Map<String, Set<Channel>> locationChannels = new ConcurrentHashMap<>();

    // JPA repos
    final PlayerJpaRepository playerRepo = new PlayerJpaRepository();
    final LocationJpaRepository locationRepo = new LocationJpaRepository();

    public GameServer(int port) { this.port = port; }

    public PlayerJpaRepository players() { return playerRepo; }
    public LocationJpaRepository locations() { return locationRepo; }
    public Map<Channel, ClientSession> sessions() { return sessions; }
    public Map<String, Set<Channel>> locationChannels() { return locationChannels; }

    public void start() throws InterruptedException {
        EventLoopGroup boss = new NioEventLoopGroup(1);
        EventLoopGroup worker = new NioEventLoopGroup();

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(boss, worker)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new GameServerInitializer(this))
                    .childOption(ChannelOption.TCP_NODELAY, true);

            ChannelFuture f = b.bind(port).sync();
            System.out.println("✅ Game server started @ " + port);
            f.channel().closeFuture().sync();
        } finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }
}

