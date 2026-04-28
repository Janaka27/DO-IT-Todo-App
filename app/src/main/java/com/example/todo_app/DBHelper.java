package com.example.todo_app;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DBHelper extends SQLiteOpenHelper {
    public static final String DB_NAME = "ToDo.db";
    public static final int DB_VERSION = 2;

    public static final String TABLE_USERS = "users";
    public static final String TABLE_TASKS = "tasks";

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {


        String usersTable = "CREATE TABLE " + TABLE_USERS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "userName TEXT, " +
            "email TEXT UNIQUE COLLATE NOCASE," +
                "password TEXT)";


        String tasksTable = "CREATE TABLE " + TABLE_TASKS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "task TEXT, " +
                "date TEXT, " +
                "time TEXT, " +
            "user_id INTEGER, " +
            "FOREIGN KEY(user_id) REFERENCES " + TABLE_USERS + "(id) ON DELETE CASCADE)";

        db.execSQL(usersTable);
        db.execSQL(tasksTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TASKS);
        onCreate(db);
    }


    // USERS

    public long insertUser(String name, String email, String password) {

        String normalizedEmail = normalizeEmail(email);

        if (isEmailExists(normalizedEmail)) {
            return -1;
        }

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("userName", name);
        values.put("email", normalizedEmail);
        values.put("password", password);


        return db.insert(TABLE_USERS, null, values);
    }

    public boolean isEmailExists(String email) {
        String normalizedEmail = normalizeEmail(email);
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
            "SELECT id FROM " + TABLE_USERS + " WHERE email = ? COLLATE NOCASE LIMIT 1",
            new String[]{normalizedEmail}
        );
        try {
            return cursor.moveToFirst();
        } finally {
            cursor.close();
        }
    }

    public boolean isEmailExistsForOtherUser(String email, int userId) {
        String normalizedEmail = normalizeEmail(email);
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT id FROM " + TABLE_USERS + " WHERE email = ? COLLATE NOCASE AND id != ? LIMIT 1",
                new String[]{normalizedEmail, String.valueOf(userId)}
        );
        try {
            return cursor.moveToFirst();
        } finally {
            cursor.close();
        }
    }

    @Nullable
    public User getUserByEmailAndPassword(String email, String password) {
        String normalizedEmail = normalizeEmail(email);
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT id, userName, email, password FROM " + TABLE_USERS + " WHERE email = ? COLLATE NOCASE AND password = ? LIMIT 1",
                new String[]{normalizedEmail, password}
        );
        try {
            if (!cursor.moveToFirst()) {
                return null;
            }
            int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
            String username = cursor.getString(cursor.getColumnIndexOrThrow("userName"));
            String storedEmail = cursor.getString(cursor.getColumnIndexOrThrow("email"));
            String storedPassword = cursor.getString(cursor.getColumnIndexOrThrow("password"));
            return new User(id, username, storedEmail, storedPassword);
        } finally {
            cursor.close();
        }
    }

    public boolean updateUserProfile(int userId, String username, String email) {
        String normalizedEmail = normalizeEmail(email);
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("userName", username);
        values.put("email", normalizedEmail);
        int updatedRows = db.update(
                TABLE_USERS,
                values,
                "id = ?",
                new String[]{String.valueOf(userId)}
        );
        return updatedRows > 0;
    }


    public Cursor getUsers() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_USERS, null);
    }

  
    // TASKS


    public long insertTask(String title, String date, String time, int userId) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("task", title);
        values.put("date", normalizeDateOrTime(date));
        values.put("time", normalizeDateOrTime(time));
        values.put("user_id", userId);

        return db.insert(TABLE_TASKS, null, values);
    }

    public int updateTask(int taskId, String title, String date, String time, int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("task", title);
        values.put("date", normalizeDateOrTime(date));
        values.put("time", normalizeDateOrTime(time));

        return db.update(
                TABLE_TASKS,
                values,
                "id = ? AND user_id = ?",
                new String[]{String.valueOf(taskId), String.valueOf(userId)}
        );
    }

    public int deleteTask(int taskId, int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(
                TABLE_TASKS,
                "id = ? AND user_id = ?",
                new String[]{String.valueOf(taskId), String.valueOf(userId)}
        );
    }

    public List<TaskItem> getTaskListByUser(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        List<TaskItem> tasks = new ArrayList<>();

        Cursor cursor = db.rawQuery(
                "SELECT id, task, date, time, user_id FROM " + TABLE_TASKS + " WHERE user_id = ? ORDER BY id DESC",
                new String[]{String.valueOf(userId)}
        );
        try {
            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String title = cursor.getString(cursor.getColumnIndexOrThrow("task"));
                String date = cursor.getString(cursor.getColumnIndexOrThrow("date"));
                String time = cursor.getString(cursor.getColumnIndexOrThrow("time"));
                int ownerId = cursor.getInt(cursor.getColumnIndexOrThrow("user_id"));
                tasks.add(new TaskItem(
                        id,
                        ownerId,
                        title,
                        normalizeDateOrTime(date),
                        normalizeDateOrTime(time)
                ));
            }
        } finally {
            cursor.close();
        }

        return tasks;
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeDateOrTime(String value) {
        if (value == null) {
            return "-";
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? "-" : normalized;
    }
}
