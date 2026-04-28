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
            startActivity(new Intent(SplashActivity.this, OnboardingActivity.class));
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
}
