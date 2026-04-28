package com.example.todo_app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class DevInfoActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (AppState.currentUser == null) {
            startActivity(new Intent(this, SignInActivity.class));
            finish();
            return;
        }
        setContentView(R.layout.activity_dev_info);

        Button exitButton = findViewById(R.id.exitButton);
        exitButton.setOnClickListener(v -> finish());
    }
}
