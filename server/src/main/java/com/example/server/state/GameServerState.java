package com.example.server.state;

import io.netty.channel.Channel;
import io.netty.channel.ChannelId;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Глобальний стан: канали, гравці. */
public class GameServerState {

    private final ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    private final Map<ChannelId, PlayerState> players = new ConcurrentHashMap<>();

    public void addClient(Channel ch) {
        channels.add(ch);
        String pid = ch.id().asShortText();
        players.put(ch.id(), new PlayerState(pid, 0, 0)); // старт у (0,0)
    }

    public void removeClient(Channel ch) {
        channels.remove(ch);
        players.remove(ch.id());
    }

    public PlayerState player(Channel ch) { return players.get(ch.id()); }

    public Collection<PlayerState> allPlayers() { return players.values(); }

    public ChannelGroup channels() { return channels; }
}
