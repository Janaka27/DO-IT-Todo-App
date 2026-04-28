package com.example.todo_app;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class AppState {
    private AppState() {}

    public static final Map<String, User> USERS = new LinkedHashMap<>();
    public static final List<TaskItem> TASKS = new ArrayList<>();
    public static User currentUser = null;

    public static boolean registerUser(String username, String email, String password) {
        String key = email.trim().toLowerCase(Locale.ROOT);
        if (USERS.containsKey(key)) {
            return false;
        }
        USERS.put(key, new User(username.trim(), key, password));
        return true;
    }

    public static boolean signIn(String email, String password) {
        String key = email.trim().toLowerCase(Locale.ROOT);
        User user = USERS.get(key);
        if (user == null || !user.password.equals(password)) {
            return false;
        }
        currentUser = user;
        return true;
    }

    public static void signOut() {
        currentUser = null;
    }
}
