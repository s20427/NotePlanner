package com.example.model;

import java.util.ArrayList;
import java.util.List;

public class Note {
    private int id;
    private String title;
    private String content;
    private List<String> tags;
    private Category category;

    public Note() {
        this.tags = new ArrayList<>();
    }

    public Note(int id, String title, String content, String tags, Category category) {
        this.id = id;
        this.title = title;
        this.content = content;
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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public void addTag(String tag) {
        this.tags.add(tag.trim());
    }

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
        return title + content;
    }
}
