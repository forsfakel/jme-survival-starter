package com.example.client;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

public class GameClientInitializer extends ChannelInitializer<SocketChannel> {

    private final GameApp gameApp;

    public GameClientInitializer(GameApp gameApp) {
        this.gameApp = gameApp;
    }

    @Override
    protected void initChannel(SocketChannel ch) {
        ch.pipeline().addLast(
                new ObjectDecoder(ClassResolvers.cacheDisabled(null)),
                new ObjectEncoder(),
                new GameClientHandler(gameApp)
        );
    }
}
