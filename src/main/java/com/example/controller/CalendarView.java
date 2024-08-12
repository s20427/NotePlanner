package com.example.controller;

import java.util.ResourceBundle;

public enum CalendarView {
    MONTH("calendar.month"),
    WEEK("calendar.week"),
    DAY("calendar.day");

    private final String resourceKey;

    CalendarView(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public static CalendarView fromLocalizedName(String localizedName, ResourceBundle resources) {
        for (CalendarView view : CalendarView.values()) {
            if (resources.getString(view.getResourceKey()).equals(localizedName)) {
                return view;
            }
        }
        return null;
    }
}
