package com.example.todo_app;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {
    private static final String TAG = "SplashActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        initializeDatabase();


        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            restoreSession();
            Class<?> next = AppState.currentUser == null ? OnboardingActivity.class : HomeActivity.class;
            startActivity(new Intent(SplashActivity.this, next));
            finish();
        }, 2200);
    }

    private void initializeDatabase() {

        try (DBHelper dbHelper = new DBHelper(this)) {
            dbHelper.getWritableDatabase();
        } catch (Exception e) {
            Log.e(TAG, "Database initialization failed", e);
        }
    }

    private void restoreSession() {
        if (AppState.currentUser != null) {
            return;
        }
        int savedUserId = SessionPrefs.getUserId(this);
        if (savedUserId <= 0) {
            return;
        }
        try (DBHelper dbHelper = new DBHelper(this)) {
            User user = dbHelper.getUserById(savedUserId);
            if (user != null) {
                AppState.currentUser = user;
            } else {
                SessionPrefs.clear(this);
            }
        }
    }
}
