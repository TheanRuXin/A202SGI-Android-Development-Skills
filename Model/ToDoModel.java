package com.example.a202sgitodoapp.Model;

import java.util.Date;
public class ToDoModel extends TaskId {

    public String TaskId;
    private String task;
    private Date due;
    private int status;
    private String categoryId;  // NEW: Link task to category

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

    public ToDoModel withId(String id) {
        this.TaskId = id;
        return this;
    }
}