package com.example.shared.buildings;

import java.io.Serializable;

public abstract class Building implements Serializable {
    private final String id;       // Унікальний ідентифікатор
    private final String name;     // Назва будівлі

    protected Building(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() { return id; }
    public String getName() { return name; }

    @Override
    public String toString() {
        return name + " (id=" + id + ")";
    }
}
