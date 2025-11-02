package com.example.a202sgitodoapp.Model;

import java.util.Date;

public class ToDoModel {

    private String taskId;
    private String task;
    private Date due;
    private int status;
    private String categoryId;

    public ToDoModel() {}

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public ToDoModel withId(String id) {
        this.taskId = id;
        return this;
    }

    public String getTask() {
        return task;
    }

    public void setTask(String task) {
        this.task = task;
    }

    public Date getDue() {
        return due;
    }

    public void setDue(Date due) {
        this.due = due;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }
}
