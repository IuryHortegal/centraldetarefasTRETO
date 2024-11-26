package com.example.tasknetwork;

public class Task {
    private final String title;
    private final String icon;

    public Task(String title, String icon) {
        this.title = title;
        this.icon = icon;
    }

    public String getTitle() {
        return title;
    }

    public String getIcon() {
        return icon;
    }
}