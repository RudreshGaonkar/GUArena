package com.example.guarena.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.guarena.activities.DashboardActivity;
import com.example.guarena.activities.SplashActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Always start with SplashActivity for proper app initialization
        Intent intent = new Intent(this, SplashActivity.class);
        startActivity(intent);
        finish();

        // No content view needed - MainActivity is just a launcher
    }
}
