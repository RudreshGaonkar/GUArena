package com.example.guarena.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.guarena.R;
import com.example.guarena.adapters.PerformanceAdapter;
import com.example.guarena.database.DatabaseHelper;
import com.example.guarena.models.Performance;
import com.example.guarena.models.Player;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class PlayerDetailsActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private ImageView ivPlayerProfile;
    private TextView tvPlayerName, tvPlayerPosition, tvTeamName, tvPlayerEmail, tvPlayerPhone;
    private TextView tvPlayerHeight, tvPlayerWeight, tvGamesPlayed, tvPerformanceScore;
    private Chip chipJersey;
    private RecyclerView rvRecentPerformance;
    private LinearLayout llEmptyPerformance;
    private MaterialButton btnViewAllPerformance, btnDietPlan, btnExercises;
    private FloatingActionButton fabEditPlayer;

    private DatabaseHelper databaseHelper;
    private PerformanceAdapter performanceAdapter;
    private List<Performance> recentPerformance;

    private String userRole;
    private int currentUserId;
    private String action; // "view", "edit"
    private int playerId = -1;
    private Player currentPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_details);

        // Initialize database
        databaseHelper = new DatabaseHelper(this);

        // Get user session and intent data
        getUserSessionData();
        getIntentData();

        // Initialize views
        initViews();

        // Setup UI
        setupToolbar();
        setupRecyclerView();
        setupClickListeners();

        // Load player data
        loadPlayerData();

        // Setup UI based on permissions
        setupUIForPermissions();
    }

    private void getUserSessionData() {
        SharedPreferences sharedPref = getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        currentUserId = sharedPref.getInt("userId", -1);
        userRole = sharedPref.getString("role", "student");
    }

    private void getIntentData() {
        Intent intent = getIntent();
        action = intent.getStringExtra("action");
        playerId = intent.getIntExtra("playerId", -1);

        if (action == null) action = "view";
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        ivPlayerProfile = findViewById(R.id.iv_player_profile);
        tvPlayerName = findViewById(R.id.tv_player_name);
        tvPlayerPosition = findViewById(R.id.tv_player_position);
        tvTeamName = findViewById(R.id.tv_team_name);
        tvPlayerEmail = findViewById(R.id.tv_player_email);
        tvPlayerPhone = findViewById(R.id.tv_player_phone);
        tvPlayerHeight = findViewById(R.id.tv_player_height);
        tvPlayerWeight = findViewById(R.id.tv_player_weight);
        tvGamesPlayed = findViewById(R.id.tv_games_played);
        tvPerformanceScore = findViewById(R.id.tv_performance_score);
        chipJersey = findViewById(R.id.chip_jersey);
        rvRecentPerformance = findViewById(R.id.rv_recent_performance);
        llEmptyPerformance = findViewById(R.id.ll_empty_performance);
        btnViewAllPerformance = findViewById(R.id.btn_view_all_performance);
        btnDietPlan = findViewById(R.id.btn_diet_plan);
        btnExercises = findViewById(R.id.btn_exercises);
        fabEditPlayer = findViewById(R.id.fab_edit_player);

        // Initialize performance list
        recentPerformance = new ArrayList<>();
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    private void setupRecyclerView() {
        performanceAdapter = new PerformanceAdapter(recentPerformance, performance -> {
            // Navigate to detailed performance view
            Intent intent = new Intent(this, PerformanceActivity.class);
            intent.putExtra("performanceId", performance.getId());
            intent.putExtra("action", "view");
            startActivity(intent);
        });

        rvRecentPerformance.setLayoutManager(new LinearLayoutManager(this));
        rvRecentPerformance.setAdapter(performanceAdapter);
    }

    private void setupClickListeners() {
        btnViewAllPerformance.setOnClickListener(v -> {
            Intent intent = new Intent(this, PerformanceActivity.class);
            intent.putExtra("playerId", playerId);
            intent.putExtra("action", "view_all");
            startActivity(intent);
        });

        btnDietPlan.setOnClickListener(v -> {
            Intent intent = new Intent(this, DietPlanActivity.class);
            intent.putExtra("playerId", playerId);
            startActivity(intent);
        });

        btnExercises.setOnClickListener(v -> {
            Intent intent = new Intent(this, GymExercisesActivity.class);
            intent.putExtra("playerId", playerId);
            startActivity(intent);
        });

        fabEditPlayer.setOnClickListener(v -> {
            // TODO: Navigate to edit player activity/dialog
            Toast.makeText(this, "Edit player functionality coming soon!", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadPlayerData() {
        if (playerId != -1) {
            // TODO: Load player from database
            // currentPlayer = databaseHelper.getPlayerById(playerId);

            // For now, create dummy data
            currentPlayer = new Player();
            currentPlayer.setId(playerId);
            currentPlayer.setUserName("John Doe");
            currentPlayer.setUserEmail("john.doe@university.edu");
            currentPlayer.setPosition("Forward");
            currentPlayer.setJerseyNumber(10);
            currentPlayer.setHeight(175.0);
            currentPlayer.setWeight(70.0);
            currentPlayer.setTeamName("Team Titans");
            currentPlayer.setSport("Football");

            populatePlayerData();
            loadPerformanceData();
            loadPlayerStats();
        }
    }

    private void populatePlayerData() {
        if (currentPlayer != null) {
            // Basic info
            tvPlayerName.setText(currentPlayer.getUserName());
            tvPlayerPosition.setText(currentPlayer.getPosition());
            tvTeamName.setText(currentPlayer.getTeamName());
            tvPlayerEmail.setText(currentPlayer.getUserEmail());

            // Jersey number
            if (currentPlayer.getJerseyNumber() > 0) {
                chipJersey.setText("#" + currentPlayer.getJerseyNumber());
                chipJersey.setVisibility(View.VISIBLE);
            } else {
                chipJersey.setVisibility(View.GONE);
            }

            // Physical stats
            if (currentPlayer.getHeight() > 0) {
                tvPlayerHeight.setText(String.format("%.0f cm", currentPlayer.getHeight()));
            } else {
                tvPlayerHeight.setText("Not specified");
            }

            if (currentPlayer.getWeight() > 0) {
                tvPlayerWeight.setText(String.format("%.0f kg", currentPlayer.getWeight()));
            } else {
                tvPlayerWeight.setText("Not specified");
            }

            // Load profile picture if available
            // TODO: Implement profile picture loading
            // For now, use default icon

            // Update toolbar title
            toolbar.setTitle(currentPlayer.getUserName());
        }
    }

    private void loadPerformanceData() {
        // TODO: Load recent performance from database
        // recentPerformance = databaseHelper.getPlayerRecentPerformance(playerId, 5);

        // For now, create some dummy data
        recentPerformance.clear();

        if (recentPerformance.isEmpty()) {
            llEmptyPerformance.setVisibility(View.VISIBLE);
            rvRecentPerformance.setVisibility(View.GONE);
        } else {
            llEmptyPerformance.setVisibility(View.GONE);
            rvRecentPerformance.setVisibility(View.VISIBLE);
            performanceAdapter.notifyDataSetChanged();
        }
    }

    private void loadPlayerStats() {
        // TODO: Load player statistics from database
        // PlayerStats stats = databaseHelper.getPlayerStats(playerId);

        // For now, use dummy data
        tvGamesPlayed.setText("12");
        tvPerformanceScore.setText("85%");

        // TODO: Get phone number from user data
        tvPlayerPhone.setText("+1 234 567 8900");
    }

    private void setupUIForPermissions() {
        // Show edit FAB only for coaches/admins or player themselves
        if (canEditPlayer()) {
            fabEditPlayer.setVisibility(View.VISIBLE);
        } else {
            fabEditPlayer.setVisibility(View.GONE);
        }

        // Students can only view their own diet plans and exercises
        if (userRole.equals("student") && !isCurrentUserPlayer()) {
            btnDietPlan.setEnabled(false);
            btnDietPlan.setAlpha(0.5f);
            btnExercises.setEnabled(false);
            btnExercises.setAlpha(0.5f);
        }
    }

    private boolean canEditPlayer() {
        return userRole.equals("admin") ||
                userRole.equals("coach") ||
                isCurrentUserPlayer();
    }

    private boolean isCurrentUserPlayer() {
        return currentPlayer != null && currentPlayer.getUserId() == currentUserId;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (canEditPlayer()) {
            getMenuInflater().inflate(R.menu.menu_player_details, menu);

            // Hide delete option for students viewing their own profile
            if (userRole.equals("student")) {
                MenuItem deleteItem = menu.findItem(R.id.action_delete_player);
                if (deleteItem != null) {
                    deleteItem.setVisible(false);
                }
            }
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (item.getItemId() == R.id.action_edit_player) {
            // TODO: Navigate to edit player activity
            Toast.makeText(this, "Edit player functionality coming soon!", Toast.LENGTH_SHORT).show();
            return true;
        } else if (item.getItemId() == R.id.action_delete_player) {
            showDeleteConfirmation();
            return true;
        } else if (item.getItemId() == R.id.action_share_profile) {
            sharePlayerProfile();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showDeleteConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Remove Player")
                .setMessage("Are you sure you want to remove " + currentPlayer.getUserName() + " from the team?")
                .setPositiveButton("Remove", (dialog, which) -> {
                    if (databaseHelper.removePlayerFromTeam(playerId)) {
                        Toast.makeText(this, "Player removed from team successfully", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        Toast.makeText(this, "Failed to remove player from team", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void sharePlayerProfile() {
        if (currentPlayer != null) {
            String shareText = String.format(
                    "Player Profile\n\n" +
                            "Name: %s\n" +
                            "Position: %s\n" +
                            "Team: %s\n" +
                            "Jersey: #%d\n" +
                            "Games Played: %s\n" +
                            "Performance: %s\n\n" +
                            "Shared from GUArena Sports Management",
                    currentPlayer.getUserName(),
                    currentPlayer.getPosition(),
                    currentPlayer.getTeamName(),
                    currentPlayer.getJerseyNumber(),
                    tvGamesPlayed.getText().toString(),
                    tvPerformanceScore.getText().toString()
            );

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Player Profile - " + currentPlayer.getUserName());

            startActivity(Intent.createChooser(shareIntent, "Share Player Profile"));
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when returning from other activities
        loadPerformanceData();
        loadPlayerStats();
    }
}
