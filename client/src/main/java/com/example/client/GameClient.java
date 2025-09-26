package com.example.client;

import com.example.shared.messages.LoginMessage;
import com.example.shared.messages.PlayerPositionMessage;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

public class GameClient {
    private final String host;
    private final int port;
    private Channel channel;

    public GameClient(String host, int port) {
        this.host = host; this.port = port;
    }

    public void start(String playerName, GameApp gameApp) throws InterruptedException {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new GameClientInitializer(gameApp))
                    .option(ChannelOption.TCP_NODELAY, true);

            ChannelFuture f = b.connect(host, port).sync();
            this.channel = f.channel();
            System.out.println("[NET] connected: " + channel);

            // ВАЖЛИВО: відразу передаємо канал у GameApp
            gameApp.setChannel(channel);

            // Логін одразу після конекту
            channel.writeAndFlush(new LoginMessage(playerName));

            f.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully();
        }
    }

    public void sendMove(int x, int y) {
        if (channel != null && channel.isActive()) {
            channel.writeAndFlush(new PlayerPositionMessage("self", x, y));
        }
    }
}