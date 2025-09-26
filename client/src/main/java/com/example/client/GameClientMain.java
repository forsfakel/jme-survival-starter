package com.example.client;

public class GameClientMain {
    public static void main(String[] args) throws InterruptedException {
        GameApp app = new GameApp();
        new Thread(app::start, "jme").start();

        // Дай jME запуститись (краще зробити сигналізацію, але хай так)
        Thread.sleep(800);

        GameClient client = new GameClient("localhost", 9000);
        client.start("Alice", app);

    }
    
}
