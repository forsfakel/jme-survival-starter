package com.example.server;


public class GameServerMain {
    public static void main(String[] args) throws Exception {
        new GameServer(9000).start();
    }
}
