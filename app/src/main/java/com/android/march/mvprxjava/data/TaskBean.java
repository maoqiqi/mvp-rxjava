package com.android.march.mvprxjava.data;

import android.support.annotation.NonNull;

import java.util.UUID;

public class TaskBean {

    @NonNull
    private String id;

    private String title;

    private String description;

    private boolean completed;

    public TaskBean() {

    }

    public TaskBean(String title, String description, boolean completed) {
        this(UUID.randomUUID().toString(), title, description, completed);
    }

    public TaskBean(String id, String title, String description, boolean completed) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.completed = completed;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public boolean isActive() {
        return !completed;
    }

    @Override
    public String toString() {
        return "TaskBean{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", completed=" + completed +
                '}';
    }
}