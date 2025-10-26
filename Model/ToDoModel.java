package com.example.a202sgitodoapp.Model;

public class ToDoModel extends TaskId {

    public String TaskId;
    private String task;
    private String due;
    private int status;

    public String getTask() {
        return task;
    }

    public String getDue() {
        return due;
    }

    public int getStatus() {
        return status;
    }

    public ToDoModel withId(String id) {
        this.TaskId = id;
        return this;
    }
}
