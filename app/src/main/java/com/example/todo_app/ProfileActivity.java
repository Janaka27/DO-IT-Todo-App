package com.example.todo_app;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class ProfileActivity extends AppCompatActivity {
    private TextView usernameValue;
    private TextView emailValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (AppState.currentUser == null) {
            startActivity(new Intent(this, SignInActivity.class));
            finish();
            return;
        }
        setContentView(R.layout.activity_profile);

        usernameValue = findViewById(R.id.usernameValue);
        emailValue = findViewById(R.id.emailValue);
        Button editButton = findViewById(R.id.editProfileButton);
        Button signOutButton = findViewById(R.id.signOutButton);

        editButton.setOnClickListener(v -> showEditDialog());
        signOutButton.setOnClickListener(v -> new AlertDialog.Builder(this)
                .setTitle("Sign out")
                .setMessage("Do you want to sign out?")
                .setPositiveButton("Sign out", (dialog, which) -> {
                    AppState.signOut();
                    SessionPrefs.clear(this);
                    startActivity(new Intent(this, SignInActivity.class));
                    finishAffinity();
                })
                .setNegativeButton("Cancel", null)
                .show());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (AppState.currentUser != null) {
            usernameValue.setText(AppState.currentUser.username);
            emailValue.setText(AppState.currentUser.email);
        }
    }

    private void showEditDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_profile, null, false);
        EditText usernameInput = dialogView.findViewById(R.id.editUsernameInput);
        EditText emailInput = dialogView.findViewById(R.id.editEmailInput);
        TextView cancelAction = dialogView.findViewById(R.id.cancelEditAction);
        TextView updateAction = dialogView.findViewById(R.id.updateAction);

        usernameInput.setText(AppState.currentUser.username);
        emailInput.setText(AppState.currentUser.email);

        AlertDialog dialog = new AlertDialog.Builder(this).setView(dialogView).create();
        cancelAction.setOnClickListener(v -> dialog.dismiss());
        updateAction.setOnClickListener(v -> {
            String username = usernameInput.getText().toString().trim();
            String email = emailInput.getText().toString().trim().toLowerCase(Locale.ROOT);
            if (username.isEmpty() || email.isEmpty()) {
                return;
            }
            boolean emailChanged = !email.equals(AppState.currentUser.email);
            if (emailChanged && AppState.USERS.containsKey(email)) {
                emailInput.setError("Email already exists");
                return;
            }
            AppState.USERS.remove(AppState.currentUser.email);
            AppState.currentUser.username = username;
            AppState.currentUser.email = email;
            AppState.USERS.put(email, AppState.currentUser);
            usernameValue.setText(username);
            emailValue.setText(email);
            dialog.dismiss();
        });
        dialog.show();
    }
}
