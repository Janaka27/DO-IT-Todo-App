package com.example.todo_app;

public class TaskItem {
    public int id;
    public int userId;
    public String title;
    public String date;
    public String time;

    public TaskItem(int id, int userId, String title, String date, String time) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.date = date;
        this.time = time;
    }

    public TaskItem(String title, String date, String time) {
        this(-1, -1, title, date, time);
    }
}
