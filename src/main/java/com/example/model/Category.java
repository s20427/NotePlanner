package com.example.model;

public enum Category {
    WORK("Work", "#5C361C"),
    HOME("Home", "#115C30"),
    MEETING("Meeting", "#185E8C"),
    VISIT("Visit", "#693380"),
    PRIVATE("Private", "#80251C"),
    OTHER("Other", "#666d6e");

    private final String name;
    private final String color;

    Category(String name, String color) {
        this.name = name;
        this.color = color;
    }

    public String getName() {
        return name;
    }

    public String getColor() {
        return color;
    }

    @Override
    public String toString() {
        return name;
    }
}