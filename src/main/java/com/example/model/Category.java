package com.example.model;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

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

    public String getTranslatedName(ResourceBundle bundle) {
        try {
            return bundle.getString("category." + name.toLowerCase());
        } catch (MissingResourceException e) {
            return name; // Fallback na nazwę domyślną
        }
    }
}