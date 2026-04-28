package com.example.todo_app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class HomeActivity extends AppCompatActivity {
    public static final String EXTRA_INITIAL_TAB = "initial_tab";
    private static final String TAB_HOME = "home";
    private static final String TAB_SETTINGS = "settings";
    private static final String TAG_HOME_FRAGMENT = "home_fragment";
    private static final String TAG_SETTINGS_FRAGMENT = "settings_fragment";
    private String currentTab = TAB_HOME;
    private Fragment homeFragment;
    private Fragment settingsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (AppState.currentUser == null) {
            startActivity(new Intent(this, SignInActivity.class));
            finish();
            return;
        }
        setContentView(R.layout.activity_home);

        ImageView navHomeIcon = findViewById(R.id.navHomeIcon);
        ImageView navSettingsIcon = findViewById(R.id.navSettingsIcon);
        View navHomeIndicator = findViewById(R.id.navHomeIndicator);
        View navSettingsIndicator = findViewById(R.id.navSettingsIndicator);

        findViewById(R.id.navHome).setOnClickListener(v -> showTab(TAB_HOME, navHomeIcon, navSettingsIcon, navHomeIndicator, navSettingsIndicator));
        findViewById(R.id.navSettings).setOnClickListener(v -> showTab(TAB_SETTINGS, navHomeIcon, navSettingsIcon, navHomeIndicator, navSettingsIndicator));

        if (savedInstanceState == null) {
            String initialTab = getIntent().getStringExtra(EXTRA_INITIAL_TAB);
            if (!TAB_SETTINGS.equals(initialTab)) {
                initialTab = TAB_HOME;
            }
            showTab(initialTab, navHomeIcon, navSettingsIcon, navHomeIndicator, navSettingsIndicator);
        } else {
            currentTab = savedInstanceState.getString("current_tab", TAB_HOME);
            homeFragment = getSupportFragmentManager().findFragmentByTag(TAG_HOME_FRAGMENT);
            settingsFragment = getSupportFragmentManager().findFragmentByTag(TAG_SETTINGS_FRAGMENT);
            updateNavState(currentTab, navHomeIcon, navSettingsIcon, navHomeIndicator, navSettingsIndicator);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("current_tab", currentTab);
    }

    private void showTab(
            String tab,
            ImageView navHomeIcon,
            ImageView navSettingsIcon,
            View navHomeIndicator,
            View navSettingsIndicator
    ) {
        if (tab.equals(currentTab) && getSupportFragmentManager().findFragmentById(R.id.fragmentContainer) != null) {
            return;
        }

        if (homeFragment == null) {
            homeFragment = getSupportFragmentManager().findFragmentByTag(TAG_HOME_FRAGMENT);
        }
        if (settingsFragment == null) {
            settingsFragment = getSupportFragmentManager().findFragmentByTag(TAG_SETTINGS_FRAGMENT);
        }

        Fragment targetFragment;
        Fragment otherFragment;
        String targetTag;
        if (TAB_SETTINGS.equals(tab)) {
            targetFragment = settingsFragment;
            otherFragment = homeFragment;
            targetTag = TAG_SETTINGS_FRAGMENT;
        } else {
            targetFragment = homeFragment;
            otherFragment = settingsFragment;
            targetTag = TAG_HOME_FRAGMENT;
        }

        FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction()
                .setReorderingAllowed(true)
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);

        if (targetFragment == null) {
            targetFragment = TAB_SETTINGS.equals(tab) ? new SettingsFragment() : new HomeFragment();
            transaction.add(R.id.fragmentContainer, targetFragment, targetTag);
            if (TAB_SETTINGS.equals(tab)) {
                settingsFragment = targetFragment;
            } else {
                homeFragment = targetFragment;
            }
        } else {
            transaction.show(targetFragment);
        }

        if (otherFragment != null && otherFragment.isAdded()) {
            transaction.hide(otherFragment);
        }

        transaction.commit();
        currentTab = tab;
        updateNavState(tab, navHomeIcon, navSettingsIcon, navHomeIndicator, navSettingsIndicator);
    }

    private void updateNavState(
            String activeTab,
            ImageView navHomeIcon,
            ImageView navSettingsIcon,
            View navHomeIndicator,
            View navSettingsIndicator
    ) {
        int iconColor = getColor(R.color.black);
        navHomeIcon.setColorFilter(iconColor);
        navSettingsIcon.setColorFilter(iconColor);
        navHomeIndicator.setVisibility(TAB_HOME.equals(activeTab) ? View.VISIBLE : View.INVISIBLE);
        navSettingsIndicator.setVisibility(TAB_SETTINGS.equals(activeTab) ? View.VISIBLE : View.INVISIBLE);
    }
}
