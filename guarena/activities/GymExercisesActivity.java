package com.example.guarena.activities;

import android.os.Bundle;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.guarena.R;

public class GymExercisesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gym_exercises);

        // Initialize toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Gym Exercises");
        }

        toolbar.setNavigationOnClickListener(v -> finish());

        // Show "Coming Soon" message
        showComingSoon();
    }

    private void showComingSoon() {
        // Find the main container
//        LinearLayout mainContainer = findViewById(R.id.ll_main_content);
//        if (mainContainer != null) {
//            mainContainer.removeAllViews();
//
//            // Create "Coming Soon" TextView
//            TextView tvComingSoon = new TextView(this);
//            tvComingSoon.setText("Coming Soon");
//            tvComingSoon.setTextSize(24);
//            tvComingSoon.setGravity(Gravity.CENTER);
//            tvComingSoon.setTextColor(getResources().getColor(R.color.text_secondary));
//
//            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
//                    LinearLayout.LayoutParams.MATCH_PARENT,
//                    LinearLayout.LayoutParams.MATCH_PARENT
//            );
//            tvComingSoon.setLayoutParams(params);
//
//            mainContainer.addView(tvComingSoon);
//        }
    }
}
