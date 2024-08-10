package com.example.model;

import java.time.LocalDateTime;

public class Event {
    private int id;
    private String title;
    private LocalDateTime dateTime;
    private String description;
    private String tags;
    private String category;

    // Konstruktor bez argumentów
    public Event() {
    }

    // Konstruktor z pełnym zestawem argumentów
    public Event(int id, String title, LocalDateTime dateTime, String description, String tags, String category) {
        this.id = id;
        this.title = title;
        this.dateTime = dateTime;
        this.description = description;
        this.tags = tags;
        this.category = category;
    }

    // Gettery i Settery
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    @Override
    public String toString() {
        return "Event{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", dateTime=" + dateTime +
                ", description='" + description + '\'' +
                ", tags='" + tags + '\'' +
                ", category='" + category + '\'' +
                '}';
    }

    // Dodatkowe metody pomocnicze

    /**
     * Sprawdza, czy wydarzenie zawiera podany tag.
     *
     * @param tag Tag do sprawdzenia.
     * @return True, jeśli wydarzenie zawiera podany tag, w przeciwnym razie False.
     */
    public boolean hasTag(String tag) {
        if (tags == null || tags.isEmpty()) {
            return false;
        }
        String[] tagArray = tags.split(",");
        for (String t : tagArray) {
            if (t.trim().equalsIgnoreCase(tag)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Sprawdza, czy wydarzenie jest z określonej kategorii.
     *
     * @param category Kategoria do sprawdzenia.
     * @return True, jeśli wydarzenie jest z podanej kategorii, w przeciwnym razie False.
     */
    public boolean isInCategory(String category) {
        return this.category != null && this.category.equalsIgnoreCase(category);
    }
}
