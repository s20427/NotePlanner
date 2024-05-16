package com.example.model;

public class Note {
    private int id;
    private String title;
    private String content;
    private String tags;
    private String category;

    public Note() {}

    public Note(int id, String title, String content, String tags, String category) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.tags = tags;
        this.category = category;
    }

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
        return title + content;
    }
}
