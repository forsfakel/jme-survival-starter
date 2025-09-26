package com.example.shared.model;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "buildings")
public class Building implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String type;   // Наприклад: SHOP, STORAGE, FACTORY, MINE
    private String name;   // Локальна назва будівлі (наприклад "Магазин зброї")
    private String description;

    public Building() {
    }

    public Building(String type, String name, String description) {
        this.type = type;
        this.name = name;
        this.description = description;
    }

    public Long getId() { return id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    @Override
    public String toString() {
        return "Building{" +
                       "id=" + id +
                       ", type='" + type + '\'' +
                       ", name='" + name + '\'' +
                       ", description='" + description + '\'' +
                       '}';
    }
}
