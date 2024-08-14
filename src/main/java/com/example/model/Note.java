package com.example.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

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
        this.content = content;
        this.title = (title != null && !title.isEmpty()) ? title : extractTitleFromContent(content);
        this.tags = new ArrayList<>();
        if (tags != null && !tags.isEmpty()) {
            for (String tag : tags.split(",")) {
                this.tags.add(tag.trim());
            }
        }
        this.category = category;
    }

    // Getters and Setters

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
        // Automatically set the title based on the first line of content if title is not explicitly set
        if (this.title == null || this.title.isEmpty()) {
            this.title = extractTitleFromContent(content);
        }
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
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

    private String extractTitleFromContent(String content) {
        if (content != null && !content.isEmpty()) {
            return content.split("\n", 2)[0];  // Get the first line of the content
        }
        return "";
    }

    @Override
    public String toString() {
        return title + "\n" + content;
    }
}