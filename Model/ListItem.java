package com.example.a202sgitodoapp.Model;

// This class acts as a blueprint for items in our mixed RecyclerView list.
public abstract class ListItem {

    // Define constants for our two types of views
    public static final int TYPE_HEADER = 1;
    public static final int TYPE_TASK = 2;

    // An abstract method to force subclasses to declare their type
    abstract public int getType();

    // A static inner class to represent a Header (e.g., "Work", "Personal")
    public static class HeaderItem extends ListItem {
        private String categoryName;

        public HeaderItem(String categoryName) {
            this.categoryName = categoryName;
        }

        public String getCategoryName() {
            return categoryName;
        }

        @Override
        public int getType() {
            return TYPE_HEADER;
        }
    }

    // A static inner class to represent a Task
    public static class TaskItem extends ListItem {
        private ToDoModel task;

        public TaskItem(ToDoModel task) {
            this.task = task;
        }

        public ToDoModel getTask() {
            return task;
        }

        @Override
        public int getType() {
            return TYPE_TASK;
        }
    }
}