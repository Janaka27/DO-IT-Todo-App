package com.example.todo_app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class SettingsFragment extends Fragment {
    public SettingsFragment() {
        super(R.layout.fragment_settings);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        View myProfileCard = view.findViewById(R.id.myProfileCard);
        View devInfoCard = view.findViewById(R.id.devInfoCard);

        myProfileCard.setOnClickListener(v -> startActivity(new Intent(requireContext(), ProfileActivity.class)));
        devInfoCard.setOnClickListener(v -> startActivity(new Intent(requireContext(), DevInfoActivity.class)));
    }
}
