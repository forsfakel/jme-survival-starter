package com.example.client;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.material.Materials;

public class Utils {
    public static Material makeMaterial(AssetManager assetManager, ColorRGBA color) {
        Material mat = new Material(assetManager, Materials.UNSHADED);
        mat.setColor("Color", color);
        return mat;
    }
}