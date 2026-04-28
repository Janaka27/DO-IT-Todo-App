package com.example.todo_app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

public class OnboardingActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        ImageButton nextButton = findViewById(R.id.nextButton);
        nextButton.setOnClickListener(v -> {
            startActivity(new Intent(OnboardingActivity.this, SignInActivity.class));
            finish();
        });
    }
}
