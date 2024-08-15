package com.example.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Note {
    private String title;
    private String content;
    private List<String> tags;
    private Category category;

    public Note() {
        this.tags = new ArrayList<>();
    }

    public Note(String title, String content, String tags, Category category) {
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

    public String getTitle() {
        return title;
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