package com.example.expensetracker.models;

public class Category {
    private int id;
    private String name;
    private String icon;
    private String color;

    public Category(int id, String name, String icon, String color) {
        this.id = id;
        this.name = name;
        this.icon = icon;
        this.color = color;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getIcon() { return icon; }
    public String getColor() { return color; }

    @Override
    public String toString() { return icon + " " + name; }
}
