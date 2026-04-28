package com.example.todo_app;

import android.content.Context;

public final class SessionPrefs {
    private static final String PREFS_NAME = "session_prefs";
    private static final String KEY_USER_ID = "user_id";

    private SessionPrefs() {}

    public static void saveUserId(Context context, int userId) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putInt(KEY_USER_ID, userId)
                .apply();
    }

    public static int getUserId(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getInt(KEY_USER_ID, -1);
    }

    public static void clear(Context context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .remove(KEY_USER_ID)
                .apply();
    }
}
