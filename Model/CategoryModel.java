package com.example.a202sgitodoapp.Model;

public class CategoryModel {
    private String id;
    private String name;
    private String icon;
    private int taskCount;

    public CategoryModel() {
        // Required empty constructor for Firestore
    }

    public CategoryModel(String name, String icon, int taskCount) {
        this.name = name;
        this.icon = icon;
        this.taskCount = taskCount;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public int getTaskCount() {
        return taskCount;
    }

    public void setTaskCount(int taskCount) {
        this.taskCount = taskCount;
    }
}