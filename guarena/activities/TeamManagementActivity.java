package com.example.guarena.activities;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.guarena.R;
import com.example.guarena.adapters.PlayerAdapter;
import com.example.guarena.database.DatabaseHelper;
import com.example.guarena.models.Player;
import com.example.guarena.models.Team;
import com.example.guarena.models.User;
import com.example.guarena.utils.EmailService; // ✅ ADD THIS
import com.example.guarena.utils.NotificationService; // ✅ ADD THIS
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;

public class TeamManagementActivity extends AppCompatActivity implements PlayerAdapter.OnPlayerClickListener {

    private MaterialToolbar toolbar;
    private MaterialCardView cardTeamDetails, cardTeamPlayers;
    private TextInputLayout tilTeamName, tilTeamSport, tilTeamDescription;
    private TextInputEditText etTeamName, etTeamDescription;
    private AutoCompleteTextView etTeamSport;
    private MaterialButton btnSaveTeam, btnAddPlayer;
    private TextView tvPlayersCount;
    private RecyclerView rvTeamPlayers;
    private LinearLayout llEmptyPlayers;

    private DatabaseHelper databaseHelper;
    private PlayerAdapter playerAdapter;
    private List<Player> teamPlayers;
    private String userRole;
    private int currentUserId;
    private String action; // "create", "edit", "view"
    private int teamId = -1;
    private Team currentTeam;

    // Sports list
    private String[] sports = {"Football", "Basketball", "Cricket", "Volleyball", "Hockey", "Tennis", "Badminton"};

    // Position lists by sport
    private String[] footballPositions = {"Goalkeeper", "Defender", "Midfielder", "Forward"};
    private String[] basketballPositions = {"Point Guard", "Shooting Guard", "Small Forward", "Power Forward", "Center"};
    private String[] cricketPositions = {"Batsman", "Bowler", "All-Rounder", "Wicket Keeper"};
    private String[] volleyballPositions = {"Setter", "Outside Hitter", "Middle Blocker", "Opposite Hitter", "Libero"};
    private String[] defaultPositions = {"Player"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team_management);

        // Initialize database
        databaseHelper = new DatabaseHelper(this);

        // Get user session and intent data
        getUserSessionData();
        getIntentData();

        // Initialize views
        initViews();

        // Setup UI
        setupToolbar();
        setupSportDropdown();
        setupRecyclerView();
        setupSaveButton();
        setupAddPlayerButton();

        // Load data if editing
        if (action.equals("edit") || action.equals("view")) {
            loadTeamData();
            cardTeamPlayers.setVisibility(View.VISIBLE);
        }

        // Setup UI based on action
        setupUIForAction();
    }

    private void getUserSessionData() {
        SharedPreferences sharedPref = getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        currentUserId = sharedPref.getInt("userId", -1);
        userRole = sharedPref.getString("role", "student");
    }

    private void getIntentData() {
        Intent intent = getIntent();
        action = intent.getStringExtra("action");
        teamId = intent.getIntExtra("teamId", -1);
        if (action == null) action = "create";
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        cardTeamDetails = findViewById(R.id.card_team_details);
        cardTeamPlayers = findViewById(R.id.card_team_players);
        tilTeamName = findViewById(R.id.til_team_name);
        tilTeamSport = findViewById(R.id.til_team_sport);
        tilTeamDescription = findViewById(R.id.til_team_description);
        etTeamName = findViewById(R.id.et_team_name);
        etTeamSport = findViewById(R.id.et_team_sport);
        etTeamDescription = findViewById(R.id.et_team_description);
        btnSaveTeam = findViewById(R.id.btn_save_team);
        btnAddPlayer = findViewById(R.id.btn_add_player);
        tvPlayersCount = findViewById(R.id.tv_players_count);
        rvTeamPlayers = findViewById(R.id.rv_team_players);
        llEmptyPlayers = findViewById(R.id.ll_empty_players);

        // Initialize players list
        teamPlayers = new ArrayList<>();
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // Update title based on action
        switch (action) {
            case "create":
                toolbar.setTitle("Create Team");
                break;
            case "edit":
                toolbar.setTitle("Edit Team");
                break;
            case "view":
                toolbar.setTitle("Team Details");
                break;
        }
    }

    private void setupSportDropdown() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, sports);
        etTeamSport.setAdapter(adapter);
    }

    private void setupRecyclerView() {
        playerAdapter = new PlayerAdapter(teamPlayers, this, userRole);
        rvTeamPlayers.setLayoutManager(new LinearLayoutManager(this));
        rvTeamPlayers.setAdapter(playerAdapter);
        updatePlayersView();
    }

    private void setupSaveButton() {
        btnSaveTeam.setOnClickListener(v -> saveTeam());
    }

    private void setupAddPlayerButton() {
        btnAddPlayer.setOnClickListener(v -> showAddPlayerDialog());
    }

    private void setupUIForAction() {
        if (action.equals("view")) {
            // Make all fields read-only
            setFieldsReadOnly(true);
            btnSaveTeam.setText("Edit Team");
            btnSaveTeam.setOnClickListener(v -> {
                action = "edit";
                setFieldsReadOnly(false);
                btnSaveTeam.setText("Update Team");
                btnSaveTeam.setOnClickListener(v1 -> saveTeam());
                toolbar.setTitle("Edit Team");
            });
        } else if (action.equals("edit")) {
            btnSaveTeam.setText("Update Team");
        }
    }

    private void setFieldsReadOnly(boolean readOnly) {
        etTeamName.setEnabled(!readOnly);
        etTeamSport.setEnabled(!readOnly);
        etTeamDescription.setEnabled(!readOnly);
        btnAddPlayer.setEnabled(!readOnly);
    }

    private void loadTeamData() {
        if (teamId != -1) {
            // ✅ PRODUCTION READY: Load actual team and players from database
            currentTeam = databaseHelper.getTeamById(teamId);
            if (currentTeam != null) {
                teamPlayers = databaseHelper.getStudentsForTeam(teamId).stream()
                        .map(user -> {
                            Player player = new Player();
                            player.setUserId(user.getId());
                            player.setUserName(user.getFullName());
                            player.setUserEmail(user.getEmail());
                            player.setTeamId(teamId);
                            // You can load more player details here
                            return player;
                        })
                        .collect(java.util.stream.Collectors.toList());

                populateFields();
                updatePlayersView();
            }
        }
    }

    private void populateFields() {
        if (currentTeam != null) {
            etTeamName.setText(currentTeam.getName());
            etTeamSport.setText(currentTeam.getSport(), false);
            etTeamDescription.setText(currentTeam.getDescription());
        }
    }

    private void showAddPlayerDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_add_player);
        dialog.getWindow().setLayout(
                (int) (getResources().getDisplayMetrics().widthPixels * 0.9),
                LinearLayout.LayoutParams.WRAP_CONTENT
        );

        // Initialize dialog views
        TextInputLayout tilSearchStudent = dialog.findViewById(R.id.til_search_student);
        TextInputLayout tilJerseyNumber = dialog.findViewById(R.id.til_jersey_number);
        TextInputLayout tilPosition = dialog.findViewById(R.id.til_position);
        TextInputLayout tilHeight = dialog.findViewById(R.id.til_height);
        TextInputLayout tilWeight = dialog.findViewById(R.id.til_weight);

        AutoCompleteTextView etSearchStudent = dialog.findViewById(R.id.et_search_student);
        TextInputEditText etJerseyNumber = dialog.findViewById(R.id.et_jersey_number);
        AutoCompleteTextView etPosition = dialog.findViewById(R.id.et_position);
        TextInputEditText etHeight = dialog.findViewById(R.id.et_height);
        TextInputEditText etWeight = dialog.findViewById(R.id.et_weight);

        MaterialButton btnCancel = dialog.findViewById(R.id.btn_cancel_add);
        MaterialButton btnConfirm = dialog.findViewById(R.id.btn_confirm_add);

        // Setup student search
        List<User> availableStudents = databaseHelper.getAvailableStudents();
        List<String> studentNames = new ArrayList<>();
        for (User student : availableStudents) {
            studentNames.add(student.getFullName() + " (" + student.getEmail() + ")");
        }

        ArrayAdapter<String> studentAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, studentNames);
        etSearchStudent.setAdapter(studentAdapter);

        // Setup position dropdown based on selected sport
        setupPositionDropdown(etPosition, etTeamSport.getText().toString());

        // Button listeners
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnConfirm.setOnClickListener(v -> {
            if (validatePlayerInput(tilSearchStudent, tilJerseyNumber, tilPosition,
                    etSearchStudent, etJerseyNumber, etPosition)) {

                // Create player object
                Player player = new Player();

                // Find selected student
                String selectedStudent = etSearchStudent.getText().toString();
                User selectedUser = null;
                for (User student : availableStudents) {
                    String studentDisplay = student.getFullName() + " (" + student.getEmail() + ")";
                    if (studentDisplay.equals(selectedStudent)) {
                        selectedUser = student;
                        player.setUserId(student.getId());
                        player.setUserName(student.getFullName());
                        player.setUserEmail(student.getEmail());
                        break;
                    }
                }

                player.setTeamId(teamId);
                player.setJerseyNumber(Integer.parseInt(etJerseyNumber.getText().toString()));
                player.setPosition(etPosition.getText().toString());

                // Set physical stats if provided
                if (!TextUtils.isEmpty(etHeight.getText().toString())) {
                    player.setHeight(Double.parseDouble(etHeight.getText().toString()));
                }

                if (!TextUtils.isEmpty(etWeight.getText().toString())) {
                    player.setWeight(Double.parseDouble(etWeight.getText().toString()));
                }

                // ✅ ENHANCED: Add to database with notifications
                if (teamId != -1) {
                    String teamName = etTeamName.getText().toString();
                    String sport = etTeamSport.getText().toString();
                    User coach = databaseHelper.getUserById(currentUserId);
                    String coachName = coach != null ? coach.getFullName() : "Coach";

                    // Use enhanced method with notifications
                    if (databaseHelper.addPlayerToTeamWithNotification(this, player, teamName, sport, coachName)) {
                        // Add to local list for immediate UI update
                        teamPlayers.add(player);
                        updatePlayersView();
                        dialog.dismiss();
                        Toast.makeText(this, "Player added and notified successfully!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Failed to add player", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Just add to local list (for team creation)
                    teamPlayers.add(player);
                    updatePlayersView();
                    dialog.dismiss();
                    Toast.makeText(this, "Player added successfully", Toast.LENGTH_SHORT).show();
                }
            }
        });

        dialog.show();
    }

    private void setupPositionDropdown(AutoCompleteTextView etPosition, String sport) {
        String[] positions;
        switch (sport.toLowerCase()) {
            case "football":
                positions = footballPositions;
                break;
            case "basketball":
                positions = basketballPositions;
                break;
            case "cricket":
                positions = cricketPositions;
                break;
            case "volleyball":
                positions = volleyballPositions;
                break;
            default:
                positions = defaultPositions;
                break;
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, positions);
        etPosition.setAdapter(adapter);
    }

    private boolean validatePlayerInput(TextInputLayout tilSearchStudent, TextInputLayout tilJerseyNumber,
                                        TextInputLayout tilPosition, AutoCompleteTextView etSearchStudent,
                                        TextInputEditText etJerseyNumber, AutoCompleteTextView etPosition) {

        // Clear previous errors
        tilSearchStudent.setError(null);
        tilJerseyNumber.setError(null);
        tilPosition.setError(null);

        String student = etSearchStudent.getText().toString().trim();
        String jerseyNumberStr = etJerseyNumber.getText().toString().trim();
        String position = etPosition.getText().toString();

        // Validate student selection
        if (TextUtils.isEmpty(student)) {
            tilSearchStudent.setError("Please select a student");
            return false;
        }

        // Validate jersey number
        if (TextUtils.isEmpty(jerseyNumberStr)) {
            tilJerseyNumber.setError("Jersey number is required");
            return false;
        }

        try {
            int jerseyNumber = Integer.parseInt(jerseyNumberStr);
            if (jerseyNumber < 1 || jerseyNumber > 999) {
                tilJerseyNumber.setError("Jersey number must be between 1-999");
                return false;
            }

            // Check if jersey number already exists
            for (Player player : teamPlayers) {
                if (player.getJerseyNumber() == jerseyNumber) {
                    tilJerseyNumber.setError("Jersey number already taken");
                    return false;
                }
            }
        } catch (NumberFormatException e) {
            tilJerseyNumber.setError("Invalid jersey number");
            return false;
        }

        // Validate position
        if (TextUtils.isEmpty(position)) {
            tilPosition.setError("Please select a position");
            return false;
        }

        return true;
    }

    private void updatePlayersView() {
        tvPlayersCount.setText(teamPlayers.size() + " Players");

        if (teamPlayers.isEmpty()) {
            llEmptyPlayers.setVisibility(View.VISIBLE);
            rvTeamPlayers.setVisibility(View.GONE);
        } else {
            llEmptyPlayers.setVisibility(View.GONE);
            rvTeamPlayers.setVisibility(View.VISIBLE);
        }

        playerAdapter.notifyDataSetChanged();
    }

    private void saveTeam() {
        if (!validateTeamInputs()) {
            return;
        }

        // Disable save button during save
        btnSaveTeam.setEnabled(false);
        btnSaveTeam.setText(action.equals("create") ? "Creating..." : "Updating...");

        // Create team object
        Team team = new Team();
        if (action.equals("edit")) {
            team.setId(teamId);
        }

        team.setName(etTeamName.getText().toString().trim());
        team.setSport(etTeamSport.getText().toString());
        team.setDescription(etTeamDescription.getText().toString().trim());
        team.setCoachId(currentUserId);

        // Save to database
        boolean success;
        if (action.equals("create")) {
            long newTeamId = databaseHelper.createTeam(team);
            success = newTeamId > 0;
            if (success) {
                teamId = (int) newTeamId;
                // ✅ ENHANCED: Save players with notifications
                String teamName = team.getName();
                String sport = team.getSport();
                User coach = databaseHelper.getUserById(currentUserId);
                String coachName = coach != null ? coach.getFullName() : "Coach";

                for (Player player : teamPlayers) {
                    player.setTeamId(teamId);
                    databaseHelper.addPlayerToTeamWithNotification(this, player, teamName, sport, coachName);
                }
            }
        } else {
            success = databaseHelper.updateTeam(team);
            if (success) {
                // Update players
                databaseHelper.updateTeamPlayers(teamId, teamPlayers);
            }
        }

        if (success) {
            String message = action.equals("create") ? "Team created successfully!" : "Team updated successfully!";
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            // Return to previous screen
            setResult(RESULT_OK);
            finish();
        } else {
            String message = action.equals("create") ? "Failed to create team" : "Failed to update team";
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            // Re-enable save button
            btnSaveTeam.setEnabled(true);
            btnSaveTeam.setText(action.equals("create") ? "Create Team" : "Update Team");
        }
    }

    private boolean validateTeamInputs() {
        // Clear previous errors
        tilTeamName.setError(null);
        tilTeamSport.setError(null);

        String name = etTeamName.getText().toString().trim();
        String sport = etTeamSport.getText().toString();

        // Validate name
        if (TextUtils.isEmpty(name)) {
            tilTeamName.setError("Team name is required");
            etTeamName.requestFocus();
            return false;
        }

        if (name.length() < 3) {
            tilTeamName.setError("Team name must be at least 3 characters");
            etTeamName.requestFocus();
            return false;
        }

        // Validate sport
        if (TextUtils.isEmpty(sport)) {
            tilTeamSport.setError("Please select a sport");
            etTeamSport.requestFocus();
            return false;
        }

        // Check if team name already exists
        if (action.equals("create") && databaseHelper.isTeamNameExists(name)) {
            tilTeamName.setError("Team name already exists");
            etTeamName.requestFocus();
            return false;
        }

        return true;
    }

    // PlayerAdapter.OnPlayerClickListener implementation
    @Override
    public void onPlayerClick(Player player) {
        Intent intent = new Intent(this, PlayerDetailsActivity.class);
        intent.putExtra("playerId", player.getId());
        intent.putExtra("action", "view");
        startActivity(intent);
    }

    @Override
    public void onPlayerEdit(Player player) {
        Toast.makeText(this, "Edit player: " + player.getUserName(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPlayerRemove(Player player) {
        new AlertDialog.Builder(this)
                .setTitle("Remove Player")
                .setMessage("Are you sure you want to remove " + player.getUserName() + " from the team?")
                .setPositiveButton("Remove", (dialog, which) -> {
                    teamPlayers.remove(player);
                    updatePlayersView();
                    Toast.makeText(this, "Player removed from team", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (action.equals("edit") || action.equals("view")) {
            getMenuInflater().inflate(R.menu.menu_team_edit, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (item.getItemId() == R.id.action_delete_team) {
            showDeleteConfirmation();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showDeleteConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Team")
                .setMessage("Are you sure you want to delete this team? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    if (databaseHelper.deleteTeam(teamId)) {
                        Toast.makeText(this, "Team deleted successfully", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        Toast.makeText(this, "Failed to delete team", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}


//package com.example.guarena.activities;
//
//import android.app.Dialog;
//import android.content.Context;
//import android.content.Intent;
//import android.content.SharedPreferences;
//import android.os.Bundle;
//import android.text.Editable;
//import android.text.TextUtils;
//import android.text.TextWatcher;
//import android.view.Menu;
//import android.view.MenuItem;
//import android.view.View;
//import android.widget.ArrayAdapter;
//import android.widget.AutoCompleteTextView;
//import android.widget.LinearLayout;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.appcompat.app.AlertDialog;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.example.guarena.R;
//import com.example.guarena.adapters.PlayerAdapter;
//import com.example.guarena.database.DatabaseHelper;
//import com.example.guarena.models.Player;
//import com.example.guarena.models.Team;
//import com.example.guarena.models.User;
//import com.google.android.material.appbar.MaterialToolbar;
//import com.google.android.material.button.MaterialButton;
//import com.google.android.material.card.MaterialCardView;
//import com.google.android.material.textfield.TextInputEditText;
//import com.google.android.material.textfield.TextInputLayout;
//
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//
//public class TeamManagementActivity extends AppCompatActivity implements PlayerAdapter.OnPlayerClickListener {
//
//    private MaterialToolbar toolbar;
//    private MaterialCardView cardTeamDetails, cardTeamPlayers;
//    private TextInputLayout tilTeamName, tilTeamSport, tilTeamDescription;
//    private TextInputEditText etTeamName, etTeamDescription;
//    private AutoCompleteTextView etTeamSport;
//    private MaterialButton btnSaveTeam, btnAddPlayer;
//    private TextView tvPlayersCount;
//    private RecyclerView rvTeamPlayers;
//    private LinearLayout llEmptyPlayers;
//
//    private DatabaseHelper databaseHelper;
//    private PlayerAdapter playerAdapter;
//    private List<Player> teamPlayers;
//
//    private String userRole;
//    private int currentUserId;
//    private String action; // "create", "edit", "view"
//    private int teamId = -1;
//    private Team currentTeam;
//
//    // Sports list
//    private String[] sports = {"Football", "Basketball", "Cricket", "Volleyball", "Hockey", "Tennis", "Badminton"};
//
//    // Position lists by sport
//    private String[] footballPositions = {"Goalkeeper", "Defender", "Midfielder", "Forward"};
//    private String[] basketballPositions = {"Point Guard", "Shooting Guard", "Small Forward", "Power Forward", "Center"};
//    private String[] cricketPositions = {"Batsman", "Bowler", "All-Rounder", "Wicket Keeper"};
//    private String[] volleyballPositions = {"Setter", "Outside Hitter", "Middle Blocker", "Opposite Hitter", "Libero"};
//    private String[] defaultPositions = {"Player"};
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_team_management);
//
//        // Initialize database
//        databaseHelper = new DatabaseHelper(this);
//
//        // Get user session and intent data
//        getUserSessionData();
//        getIntentData();
//
//        // Initialize views
//        initViews();
//
//        // Setup UI
//        setupToolbar();
//        setupSportDropdown();
//        setupRecyclerView();
//        setupSaveButton();
//        setupAddPlayerButton();
//
//        // Load data if editing
//        if (action.equals("edit") || action.equals("view")) {
//            loadTeamData();
//            cardTeamPlayers.setVisibility(View.VISIBLE);
//        }
//
//        // Setup UI based on action
//        setupUIForAction();
//    }
//
//    private void getUserSessionData() {
//        SharedPreferences sharedPref = getSharedPreferences("UserSession", Context.MODE_PRIVATE);
//        currentUserId = sharedPref.getInt("userId", -1);
//        userRole = sharedPref.getString("role", "student");
//    }
//
//    private void getIntentData() {
//        Intent intent = getIntent();
//        action = intent.getStringExtra("action");
//        teamId = intent.getIntExtra("teamId", -1);
//
//        if (action == null) action = "create";
//    }
//
//    private void initViews() {
//        toolbar = findViewById(R.id.toolbar);
//        cardTeamDetails = findViewById(R.id.card_team_details);
//        cardTeamPlayers = findViewById(R.id.card_team_players);
//
//        tilTeamName = findViewById(R.id.til_team_name);
//        tilTeamSport = findViewById(R.id.til_team_sport);
//        tilTeamDescription = findViewById(R.id.til_team_description);
//
//        etTeamName = findViewById(R.id.et_team_name);
//        etTeamSport = findViewById(R.id.et_team_sport);
//        etTeamDescription = findViewById(R.id.et_team_description);
//
//        btnSaveTeam = findViewById(R.id.btn_save_team);
//        btnAddPlayer = findViewById(R.id.btn_add_player);
//        tvPlayersCount = findViewById(R.id.tv_players_count);
//        rvTeamPlayers = findViewById(R.id.rv_team_players);
//        llEmptyPlayers = findViewById(R.id.ll_empty_players);
//
//        // Initialize players list
//        teamPlayers = new ArrayList<>();
//    }
//
//    private void setupToolbar() {
//        setSupportActionBar(toolbar);
//        if (getSupportActionBar() != null) {
//            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//            getSupportActionBar().setDisplayShowHomeEnabled(true);
//        }
//
//        // Update title based on action
//        switch (action) {
//            case "create":
//                toolbar.setTitle("Create Team");
//                break;
//            case "edit":
//                toolbar.setTitle("Edit Team");
//                break;
//            case "view":
//                toolbar.setTitle("Team Details");
//                break;
//        }
//    }
//
//    private void setupSportDropdown() {
//        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
//                android.R.layout.simple_dropdown_item_1line, sports);
//        etTeamSport.setAdapter(adapter);
//    }
//
//    private void setupRecyclerView() {
//        playerAdapter = new PlayerAdapter(teamPlayers, this, userRole);
//        rvTeamPlayers.setLayoutManager(new LinearLayoutManager(this));
//        rvTeamPlayers.setAdapter(playerAdapter);
//
//        updatePlayersView();
//    }
//
//    private void setupSaveButton() {
//        btnSaveTeam.setOnClickListener(v -> saveTeam());
//    }
//
//    private void setupAddPlayerButton() {
//        btnAddPlayer.setOnClickListener(v -> showAddPlayerDialog());
//    }
//
//    private void setupUIForAction() {
//        if (action.equals("view")) {
//            // Make all fields read-only
//            setFieldsReadOnly(true);
//            btnSaveTeam.setText("Edit Team");
//            btnSaveTeam.setOnClickListener(v -> {
//                action = "edit";
//                setFieldsReadOnly(false);
//                btnSaveTeam.setText("Update Team");
//                btnSaveTeam.setOnClickListener(v1 -> saveTeam());
//                toolbar.setTitle("Edit Team");
//            });
//        } else if (action.equals("edit")) {
//            btnSaveTeam.setText("Update Team");
//        }
//    }
//
//    private void setFieldsReadOnly(boolean readOnly) {
//        etTeamName.setEnabled(!readOnly);
//        etTeamSport.setEnabled(!readOnly);
//        etTeamDescription.setEnabled(!readOnly);
//        btnAddPlayer.setEnabled(!readOnly);
//    }
//
//    private void loadTeamData() {
//        if (teamId != -1) {
//            // TODO: Load team and players from database
//            // currentTeam = databaseHelper.getTeamById(teamId);
//            // teamPlayers = databaseHelper.getTeamPlayers(teamId);
//
//            // For now, create dummy data
//            currentTeam = new Team();
//            currentTeam.setId(teamId);
//            currentTeam.setName("Sample Team");
//            currentTeam.setSport("Football");
//            currentTeam.setDescription("Sample team description");
//
//            populateFields();
//            updatePlayersView();
//        }
//    }
//
//    private void populateFields() {
//        if (currentTeam != null) {
//            etTeamName.setText(currentTeam.getName());
//            etTeamSport.setText(currentTeam.getSport(), false);
//            etTeamDescription.setText(currentTeam.getDescription());
//        }
//    }
//
//    private void showAddPlayerDialog() {
//        Dialog dialog = new Dialog(this);
//        dialog.setContentView(R.layout.dialog_add_player);
//        dialog.getWindow().setLayout(
//                (int) (getResources().getDisplayMetrics().widthPixels * 0.9),
//                LinearLayout.LayoutParams.WRAP_CONTENT
//        );
//
//        // Initialize dialog views
//        TextInputLayout tilSearchStudent = dialog.findViewById(R.id.til_search_student);
//        TextInputLayout tilJerseyNumber = dialog.findViewById(R.id.til_jersey_number);
//        TextInputLayout tilPosition = dialog.findViewById(R.id.til_position);
//        TextInputLayout tilHeight = dialog.findViewById(R.id.til_height);
//        TextInputLayout tilWeight = dialog.findViewById(R.id.til_weight);
//
//        AutoCompleteTextView etSearchStudent = dialog.findViewById(R.id.et_search_student);
//        TextInputEditText etJerseyNumber = dialog.findViewById(R.id.et_jersey_number);
//        AutoCompleteTextView etPosition = dialog.findViewById(R.id.et_position);
//        TextInputEditText etHeight = dialog.findViewById(R.id.et_height);
//        TextInputEditText etWeight = dialog.findViewById(R.id.et_weight);
//
//        MaterialButton btnCancel = dialog.findViewById(R.id.btn_cancel_add);
//        MaterialButton btnConfirm = dialog.findViewById(R.id.btn_confirm_add);
//
//        // Setup student search
//        List<User> availableStudents = databaseHelper.getAvailableStudents();
//        List<String> studentNames = new ArrayList<>();
//        for (User student : availableStudents) {
//            studentNames.add(student.getFullName() + " (" + student.getEmail() + ")");
//        }
//
//        ArrayAdapter<String> studentAdapter = new ArrayAdapter<>(this,
//                android.R.layout.simple_dropdown_item_1line, studentNames);
//        etSearchStudent.setAdapter(studentAdapter);
//
//        // Setup position dropdown based on selected sport
//        setupPositionDropdown(etPosition, etTeamSport.getText().toString());
//
//        // Button listeners
//        btnCancel.setOnClickListener(v -> dialog.dismiss());
//
//        btnConfirm.setOnClickListener(v -> {
//            if (validatePlayerInput(tilSearchStudent, tilJerseyNumber, tilPosition,
//                    etSearchStudent, etJerseyNumber, etPosition)) {
//
//                // Create player object
//                Player player = new Player();
//
//                // Find selected student
//                String selectedStudent = etSearchStudent.getText().toString();
//                for (User student : availableStudents) {
//                    String studentDisplay = student.getFullName() + " (" + student.getEmail() + ")";
//                    if (studentDisplay.equals(selectedStudent)) {
//                        player.setUserId(student.getId());
//                        player.setUserName(student.getFullName());
//                        player.setUserEmail(student.getEmail());
//                        break;
//                    }
//                }
//
//                player.setTeamId(teamId);
//                player.setJerseyNumber(Integer.parseInt(etJerseyNumber.getText().toString()));
//                player.setPosition(etPosition.getText().toString());
//
//                // Set physical stats if provided
//                if (!TextUtils.isEmpty(etHeight.getText().toString())) {
//                    player.setHeight(Double.parseDouble(etHeight.getText().toString()));
//                }
//                if (!TextUtils.isEmpty(etWeight.getText().toString())) {
//                    player.setWeight(Double.parseDouble(etWeight.getText().toString()));
//                }
//
//                // Add to list
//                teamPlayers.add(player);
//                updatePlayersView();
//
//                dialog.dismiss();
//                Toast.makeText(this, "Player added successfully", Toast.LENGTH_SHORT).show();
//            }
//        });
//
//        dialog.show();
//    }
//
//    private void setupPositionDropdown(AutoCompleteTextView etPosition, String sport) {
//        String[] positions;
//
//        switch (sport.toLowerCase()) {
//            case "football":
//                positions = footballPositions;
//                break;
//            case "basketball":
//                positions = basketballPositions;
//                break;
//            case "cricket":
//                positions = cricketPositions;
//                break;
//            case "volleyball":
//                positions = volleyballPositions;
//                break;
//            default:
//                positions = defaultPositions;
//                break;
//        }
//
//        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
//                android.R.layout.simple_dropdown_item_1line, positions);
//        etPosition.setAdapter(adapter);
//    }
//
//    private boolean validatePlayerInput(TextInputLayout tilSearchStudent, TextInputLayout tilJerseyNumber,
//                                        TextInputLayout tilPosition, AutoCompleteTextView etSearchStudent,
//                                        TextInputEditText etJerseyNumber, AutoCompleteTextView etPosition) {
//        // Clear previous errors
//        tilSearchStudent.setError(null);
//        tilJerseyNumber.setError(null);
//        tilPosition.setError(null);
//
//        String student = etSearchStudent.getText().toString().trim();
//        String jerseyNumberStr = etJerseyNumber.getText().toString().trim();
//        String position = etPosition.getText().toString();
//
//        // Validate student selection
//        if (TextUtils.isEmpty(student)) {
//            tilSearchStudent.setError("Please select a student");
//            return false;
//        }
//
//        // Validate jersey number
//        if (TextUtils.isEmpty(jerseyNumberStr)) {
//            tilJerseyNumber.setError("Jersey number is required");
//            return false;
//        }
//
//        try {
//            int jerseyNumber = Integer.parseInt(jerseyNumberStr);
//            if (jerseyNumber < 1 || jerseyNumber > 999) {
//                tilJerseyNumber.setError("Jersey number must be between 1-999");
//                return false;
//            }
//
//            // Check if jersey number already exists
//            for (Player player : teamPlayers) {
//                if (player.getJerseyNumber() == jerseyNumber) {
//                    tilJerseyNumber.setError("Jersey number already taken");
//                    return false;
//                }
//            }
//        } catch (NumberFormatException e) {
//            tilJerseyNumber.setError("Invalid jersey number");
//            return false;
//        }
//
//        // Validate position
//        if (TextUtils.isEmpty(position)) {
//            tilPosition.setError("Please select a position");
//            return false;
//        }
//
//        return true;
//    }
//
//    private void updatePlayersView() {
//        tvPlayersCount.setText(teamPlayers.size() + " Players");
//
//        if (teamPlayers.isEmpty()) {
//            llEmptyPlayers.setVisibility(View.VISIBLE);
//            rvTeamPlayers.setVisibility(View.GONE);
//        } else {
//            llEmptyPlayers.setVisibility(View.GONE);
//            rvTeamPlayers.setVisibility(View.VISIBLE);
//        }
//
//        playerAdapter.notifyDataSetChanged();
//    }
//
//    private void saveTeam() {
//        if (!validateTeamInputs()) {
//            return;
//        }
//
//        // Disable save button during save
//        btnSaveTeam.setEnabled(false);
//        btnSaveTeam.setText(action.equals("create") ? "Creating..." : "Updating...");
//
//        // Create team object
//        Team team = new Team();
//        if (action.equals("edit")) {
//            team.setId(teamId);
//        }
//
//        team.setName(etTeamName.getText().toString().trim());
//        team.setSport(etTeamSport.getText().toString());
//        team.setDescription(etTeamDescription.getText().toString().trim());
//        team.setCoachId(currentUserId);
//
//        // Save to database
//        boolean success;
//        if (action.equals("create")) {
//            long newTeamId = databaseHelper.createTeam(team);
//            success = newTeamId > 0;
//            if (success) {
//                teamId = (int) newTeamId;
//                // Save players if any
//                for (Player player : teamPlayers) {
//                    player.setTeamId(teamId);
//                    databaseHelper.addPlayerToTeam(player);
//                }
//            }
//        } else {
//            success = databaseHelper.updateTeam(team);
//            if (success) {
//                // Update players
//                databaseHelper.updateTeamPlayers(teamId, teamPlayers);
//            }
//        }
//
//        if (success) {
//            String message = action.equals("create") ? "Team created successfully!" : "Team updated successfully!";
//            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
//
//            // Return to previous screen
//            setResult(RESULT_OK);
//            finish();
//        } else {
//            String message = action.equals("create") ? "Failed to create team" : "Failed to update team";
//            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
//
//            // Re-enable save button
//            btnSaveTeam.setEnabled(true);
//            btnSaveTeam.setText(action.equals("create") ? "Create Team" : "Update Team");
//        }
//    }
//
//    private boolean validateTeamInputs() {
//        // Clear previous errors
//        tilTeamName.setError(null);
//        tilTeamSport.setError(null);
//
//        String name = etTeamName.getText().toString().trim();
//        String sport = etTeamSport.getText().toString();
//
//        // Validate name
//        if (TextUtils.isEmpty(name)) {
//            tilTeamName.setError("Team name is required");
//            etTeamName.requestFocus();
//            return false;
//        }
//
//        if (name.length() < 3) {
//            tilTeamName.setError("Team name must be at least 3 characters");
//            etTeamName.requestFocus();
//            return false;
//        }
//
//        // Validate sport
//        if (TextUtils.isEmpty(sport)) {
//            tilTeamSport.setError("Please select a sport");
//            etTeamSport.requestFocus();
//            return false;
//        }
//
//        // Check if team name already exists
//        if (action.equals("create") && databaseHelper.isTeamNameExists(name)) {
//            tilTeamName.setError("Team name already exists");
//            etTeamName.requestFocus();
//            return false;
//        }
//
//        return true;
//    }
//
//    // PlayerAdapter.OnPlayerClickListener implementation
//    @Override
//    public void onPlayerClick(Player player) {
//        Intent intent = new Intent(this, PlayerDetailsActivity.class);
//        intent.putExtra("playerId", player.getId());
//        intent.putExtra("action", "view");
//        startActivity(intent);
//    }
//
//    @Override
//    public void onPlayerEdit(Player player) {
//        // TODO: Implement player edit functionality
//        Toast.makeText(this, "Edit player: " + player.getUserName(), Toast.LENGTH_SHORT).show();
//    }
//
//    @Override
//    public void onPlayerRemove(Player player) {
//        new AlertDialog.Builder(this)
//                .setTitle("Remove Player")
//                .setMessage("Are you sure you want to remove " + player.getUserName() + " from the team?")
//                .setPositiveButton("Remove", (dialog, which) -> {
//                    teamPlayers.remove(player);
//                    updatePlayersView();
//                    Toast.makeText(this, "Player removed from team", Toast.LENGTH_SHORT).show();
//                })
//                .setNegativeButton("Cancel", null)
//                .show();
//    }
//
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        if (action.equals("edit") || action.equals("view")) {
//            getMenuInflater().inflate(R.menu.menu_team_edit, menu);
//        }
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        if (item.getItemId() == android.R.id.home) {
//            onBackPressed();
//            return true;
//        } else if (item.getItemId() == R.id.action_delete_team) {
//            showDeleteConfirmation();
//            return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }
//
//    private void showDeleteConfirmation() {
//        new AlertDialog.Builder(this)
//                .setTitle("Delete Team")
//                .setMessage("Are you sure you want to delete this team? This action cannot be undone.")
//                .setPositiveButton("Delete", (dialog, which) -> {
//                    if (databaseHelper.deleteTeam(teamId)) {
//                        Toast.makeText(this, "Team deleted successfully", Toast.LENGTH_SHORT).show();
//                        setResult(RESULT_OK);
//                        finish();
//                    } else {
//                        Toast.makeText(this, "Failed to delete team", Toast.LENGTH_SHORT).show();
//                    }
//                })
//                .setNegativeButton("Cancel", null)
//                .show();
//    }
//
//    @Override
//    public void onBackPressed() {
//        super.onBackPressed();
//    }
//}
