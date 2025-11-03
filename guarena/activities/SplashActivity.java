package com.example.guarena.activities;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.guarena.R;

public class SplashActivity extends AppCompatActivity {

    private TextView tvAppName, tvTagline;
    private ProgressBar progressBar;
    private static final int SPLASH_DURATION = 3000; // 3 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Initialize views
        initViews();

        // Start animations
        startAnimations();

        // Check login status after splash
        checkLoginStatus();
    }

    private void initViews() {
        tvAppName = findViewById(R.id.tv_app_name);
        tvTagline = findViewById(R.id.tv_tagline);
        progressBar = findViewById(R.id.progress_bar);
    }

    private void startAnimations() {
        // App name fade in animation
        ObjectAnimator appNameAnim = ObjectAnimator.ofFloat(tvAppName, "alpha", 0f, 1f);
        appNameAnim.setDuration(1000);
        appNameAnim.setInterpolator(new DecelerateInterpolator());
        appNameAnim.start();

        // Tagline fade in animation (delayed)
        new Handler().postDelayed(() -> {
            ObjectAnimator taglineAnim = ObjectAnimator.ofFloat(tvTagline, "alpha", 0f, 1f);
            taglineAnim.setDuration(800);
            taglineAnim.setInterpolator(new DecelerateInterpolator());
            taglineAnim.start();
        }, 500);

        // Progress bar fade in animation (more delayed)
        new Handler().postDelayed(() -> {
            ObjectAnimator progressAnim = ObjectAnimator.ofFloat(progressBar, "alpha", 0f, 1f);
            progressAnim.setDuration(600);
            progressAnim.setInterpolator(new DecelerateInterpolator());
            progressAnim.start();
        }, 1000);
    }

    private void checkLoginStatus() {
        new Handler().postDelayed(() -> {
            SharedPreferences sharedPref = getSharedPreferences("UserSession", MODE_PRIVATE);
            boolean isLoggedIn = sharedPref.getBoolean("isLoggedIn", false);

            Intent intent;
            if (isLoggedIn) {
                // User is logged in, go to Dashboard
                intent = new Intent(SplashActivity.this, DashboardActivity.class);
            } else {
                // User not logged in, go to Login
                intent = new Intent(SplashActivity.this, LoginActivity.class);
            }

            startActivity(intent);
            finish();

            // Add smooth transition
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

        }, SPLASH_DURATION);
    }

    @Override
    public void onBackPressed() {
        // Disable back button on splash screen
        super.onBackPressed();
    }
}
