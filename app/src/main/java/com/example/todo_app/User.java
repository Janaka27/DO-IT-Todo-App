package com.example.todo_app;

public class User {
    public int id;
    public String username;
    public String email;
    public String password;

    public User(int id, String username, String email, String password) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
    }

    public User(String username, String email, String password) {
        this(-1, username, email, password);
    }
}
