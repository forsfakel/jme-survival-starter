package com.example.shared.messages;

import java.io.Serializable;

public class OpenBuildingRequest implements Serializable {
    private int x;
    private int y;
    private String buildingName; // унікальна в межах локації

    public OpenBuildingRequest() {}
    public OpenBuildingRequest(int x, int y, String buildingName) {
        this.x = x; this.y = y; this.buildingName = buildingName;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public String getBuildingName() { return buildingName; }
}
