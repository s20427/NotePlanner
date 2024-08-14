package com.example.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Event {
    private int id;
    private String title;
    private LocalDateTime dateTime;
    private LocalDateTime endDateTime;
    private String description;
    private List<String> tags;
    private Category category;

    public Event() {
        this.tags = new ArrayList<>();
    }

    public Event(int id, String title, LocalDateTime dateTime, LocalDateTime endDateTime, String description, String tags, Category category) {
        this.id = id;
        this.title = title;
        this.dateTime = dateTime;
        this.endDateTime = endDateTime;
        this.description = description;
        this.tags = new ArrayList<>();
        if (tags != null && !tags.isEmpty()) {
            for (String tag : tags.split(",")) {
                this.tags.add(tag.trim());
            }
        }
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

    public LocalDateTime getEndDateTime() {
        return endDateTime;
    }

    public void setEndDateTime(LocalDateTime endDateTime) {
        this.endDateTime = endDateTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        Set<String> uniqueTags = new HashSet<>(tags);
        this.tags = new ArrayList<>(uniqueTags);
    }

    @JsonIgnore
    public String getTagsAsString() {
        return String.join(", ", tags);
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    @Override
    public String toString() {
        return title + " (" + getTagsAsString() + ")";
    }
}