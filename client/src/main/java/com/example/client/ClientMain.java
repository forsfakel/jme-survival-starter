package com.example.client;

import com.example.shared.messages.LocationSyncMsg;
import com.example.shared.model.WorldLocation;
import com.example.shared.net.LocationMessage;
import com.jme3.app.SimpleApplication;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import io.netty.channel.Channel;

public class ClientMain extends SimpleApplication {

    private Channel channel; // Netty канал
    private WorldLocation currentLocation = new WorldLocation(0, 0);

    public static void main(String[] args) {
        ClientMain app = new ClientMain();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        // Тут ми вже повинні створити channel до сервера
        channel = NetworkClientInitializer.connect("localhost", 8080);

        // Реєструємо клавішу "L"
        inputManager.addMapping("NextLocation", new KeyTrigger(KeyInput.KEY_L));
        inputManager.addListener(actionListener, "NextLocation");
    }

    private final ActionListener actionListener = new ActionListener() {
        @Override
        public void onAction(String name, boolean keyPressed, float tpf) {
            if (name.equals("NextLocation") && !keyPressed) {
                // Приклад: переміщаємось на +1 по X
                currentLocation = new WorldLocation(
                        currentLocation.getX() + 1,
                        currentLocation.getY()
                );

                System.out.println("Moving to location: " + currentLocation);

                // Надсилаємо повідомлення серверу
                if (channel != null && channel.isActive()) {
                    channel.writeAndFlush(new LocationMessage("player1", currentLocation));
                }
            }
        }
    };
    public void showLocation(LocationSyncMsg msg) {
        // Очистити старі об’єкти
        rootNode.detachAllChildren();

        // Створити простий фон/террейн
        Box ground = new Box(20, 0.1f, 20);
        Geometry groundGeo = new Geometry("Ground", ground);
        groundGeo.setMaterial(assetManager.loadMaterial("Common/Materials/WhiteColor.j3m"));
        rootNode.attachChild(groundGeo);

        // Додати будівлі як примітивні бокси
        int offset = 0;
        for (String building : msg.getBuildings()) {
            Box box = new Box(1, 2, 1);
            Geometry geo = new Geometry(building, box);
            geo.setLocalTranslation(offset * 3, 1, 0);
            geo.setMaterial(assetManager.loadMaterial("Common/Materials/RedColor.j3m"));
            rootNode.attachChild(geo);
            offset++;
        }
    }
}
