package com.example.guarena.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.guarena.R;
import com.example.guarena.activities.TeamManagementActivity;
import com.example.guarena.adapters.TeamAdapter;
import com.example.guarena.database.DatabaseHelper;
import com.example.guarena.models.Team;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class TeamsFragment extends Fragment implements TeamAdapter.OnTeamClickListener {

    private View rootView;
    private MaterialCardView cardUserStats;
    private TextView tvMyTeamsCount, tvTotalPoints, tvEmptyMessage;
    private TextInputEditText etSearch;
    private MaterialButton btnFilter, btnCreateFirstTeam;
    private ChipGroup chipGroupSports;
    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView rvTeams;
    private LinearLayout llEmptyState;
    private FloatingActionButton fabAddTeam;

    private DatabaseHelper databaseHelper;
    private TeamAdapter teamAdapter;
    private List<Team> allTeams;
    private List<Team> filteredTeams;

    private String currentUserRole;
    private int currentUserId;
    private String searchQuery = "";
    private String selectedSport = "All";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_teams, container, false);

        // Initialize database
        databaseHelper = new DatabaseHelper(getContext());

        // Get user session data
        getUserSessionData();

        // Initialize views
        initViews();

        // Setup UI based on role
        setupUIForRole();

        // Setup functionality
        setupRecyclerView();
        setupSearch();
        setupChips();
        setupSwipeRefresh();
        setupClickListeners();

        // Load data
        loadTeams();

        return rootView;
    }

    private void getUserSessionData() {
        SharedPreferences sharedPref = getActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        currentUserId = sharedPref.getInt("userId", -1);
        currentUserRole = sharedPref.getString("role", "student");
    }

    private void initViews() {
        cardUserStats = rootView.findViewById(R.id.card_user_stats);
        tvMyTeamsCount = rootView.findViewById(R.id.tv_my_teams_count);
        tvTotalPoints = rootView.findViewById(R.id.tv_total_points);
        etSearch = rootView.findViewById(R.id.et_search);
        btnFilter = rootView.findViewById(R.id.btn_filter);
        btnCreateFirstTeam = rootView.findViewById(R.id.btn_create_first_team);
        chipGroupSports = rootView.findViewById(R.id.chip_group_sports);
        swipeRefresh = rootView.findViewById(R.id.swipe_refresh);
        rvTeams = rootView.findViewById(R.id.rv_teams);
        llEmptyState = rootView.findViewById(R.id.ll_empty_state);
        tvEmptyMessage = rootView.findViewById(R.id.tv_empty_message);
        fabAddTeam = rootView.findViewById(R.id.fab_add_team);

        // Initialize lists
        allTeams = new ArrayList<>();
        filteredTeams = new ArrayList<>();
    }

    private void setupUIForRole() {
        if (currentUserRole.equals("student")) {
            // Show user stats card for students
            cardUserStats.setVisibility(View.VISIBLE);
            loadUserStats();

            // Hide FAB for students
            fabAddTeam.hide();

            // Update empty message for students
            tvEmptyMessage.setText("You haven't joined any teams yet");
            btnCreateFirstTeam.setText("Browse Teams");
        } else {
            // Hide user stats for coaches/admins
            cardUserStats.setVisibility(View.GONE);

            // Show FAB for coaches/admins
            fabAddTeam.show();

            // Update empty message for coaches/admins
            tvEmptyMessage.setText("Create your first team to get started");
            btnCreateFirstTeam.setText("Create Team");
        }
    }

    private void loadUserStats() {
        if (currentUserRole.equals("student")) {
            int teamCount = databaseHelper.getUserTeamsCount(currentUserId, currentUserRole);
            int totalPoints = databaseHelper.getUserTotalPoints(currentUserId);

            tvMyTeamsCount.setText(teamCount + " Teams");
            tvTotalPoints.setText(String.valueOf(totalPoints));
        }
    }

    private void setupRecyclerView() {
        teamAdapter = new TeamAdapter(filteredTeams, this, currentUserRole);
        rvTeams.setLayoutManager(new LinearLayoutManager(getContext()));
        rvTeams.setAdapter(teamAdapter);
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                searchQuery = s.toString().trim();
                filterTeams();
            }
        });
    }

    private void setupChips() {
        chipGroupSports.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                // If no chips selected, default to "All"
                selectedSport = "All";
                rootView.findViewById(R.id.chip_all).setSelected(true);
            } else {
                // Get selected chip text
                Chip selectedChip = rootView.findViewById(checkedIds.get(0));
                selectedSport = selectedChip.getText().toString();
            }
            filterTeams();
        });
    }

    private void setupSwipeRefresh() {
        swipeRefresh.setColorSchemeResources(R.color.primary);
        swipeRefresh.setOnRefreshListener(() -> {
            loadTeams();
        });
    }

    private void setupClickListeners() {
        fabAddTeam.setOnClickListener(v -> {
            if (isCoachOrAdmin()) {
                createNewTeam();
            } else {
                Toast.makeText(getContext(), "Only coaches and admins can create teams", Toast.LENGTH_SHORT).show();
            }
        });

        btnCreateFirstTeam.setOnClickListener(v -> {
            if (currentUserRole.equals("student")) {
                // Show available teams to join
                showAvailableTeams();
            } else if (isCoachOrAdmin()) {
                createNewTeam();
            }
        });

        btnFilter.setOnClickListener(v -> {
            showFilterBottomSheet();
        });
    }

    private void loadTeams() {
        swipeRefresh.setRefreshing(true);

        // Load teams based on user role
        if (currentUserRole.equals("admin")) {
            allTeams = databaseHelper.getAllTeams();
        } else if (currentUserRole.equals("coach")) {
            allTeams = databaseHelper.getCoachTeams(currentUserId);
        } else {
            // Student - show teams they're part of
            allTeams = databaseHelper.getStudentTeams(currentUserId);
        }

        filterTeams();
        swipeRefresh.setRefreshing(false);

        // Update user stats if student
        if (currentUserRole.equals("student")) {
            loadUserStats();
        }
    }

    private void filterTeams() {
        filteredTeams.clear();

        for (Team team : allTeams) {
            boolean matchesSport = selectedSport.equals("All") ||
                    team.getSport().equalsIgnoreCase(selectedSport);

            boolean matchesSearch = TextUtils.isEmpty(searchQuery) ||
                    team.getName().toLowerCase().contains(searchQuery.toLowerCase()) ||
                    team.getSport().toLowerCase().contains(searchQuery.toLowerCase());

            if (matchesSport && matchesSearch) {
                filteredTeams.add(team);
            }
        }

        teamAdapter.notifyDataSetChanged();
        updateEmptyState();
    }

    private void updateEmptyState() {
        if (filteredTeams.isEmpty()) {
            llEmptyState.setVisibility(View.VISIBLE);
            rvTeams.setVisibility(View.GONE);
            swipeRefresh.setVisibility(View.GONE);

            // Update empty message based on user role and context
            if (currentUserRole.equals("student")) {
                if (allTeams.isEmpty()) {
                    tvEmptyMessage.setText("You haven't joined any teams yet");
                    btnCreateFirstTeam.setText("Browse Teams");
                } else {
                    tvEmptyMessage.setText("No teams match your search");
                    btnCreateFirstTeam.setVisibility(View.GONE);
                }
            } else {
                if (allTeams.isEmpty()) {
                    tvEmptyMessage.setText("Create your first team to get started");
                    btnCreateFirstTeam.setText("Create Team");
                    btnCreateFirstTeam.setVisibility(View.VISIBLE);
                } else {
                    tvEmptyMessage.setText("No teams match your search");
                    btnCreateFirstTeam.setVisibility(View.GONE);
                }
            }
        } else {
            llEmptyState.setVisibility(View.GONE);
            rvTeams.setVisibility(View.VISIBLE);
            swipeRefresh.setVisibility(View.VISIBLE);
        }
    }

    private void createNewTeam() {
        Intent intent = new Intent(getActivity(), TeamManagementActivity.class);
        intent.putExtra("action", "create");
        startActivity(intent);
    }

    private void showAvailableTeams() {
        // TODO: Show bottom sheet with available teams to join
        Toast.makeText(getContext(), "Browse teams feature coming soon!", Toast.LENGTH_SHORT).show();
    }

    private void showFilterBottomSheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getContext());
        View bottomSheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_team_filter, null);
        bottomSheetDialog.setContentView(bottomSheetView);

        // Setup filter options
        MaterialButton btnMyTeams = bottomSheetView.findViewById(R.id.btn_my_teams);
        MaterialButton btnAllTeams = bottomSheetView.findViewById(R.id.btn_all_teams);
        MaterialButton btnActiveTeams = bottomSheetView.findViewById(R.id.btn_active_teams);
        MaterialButton btnClearFilter = bottomSheetView.findViewById(R.id.btn_clear_filter);

        btnMyTeams.setOnClickListener(v -> {
            // Filter to show only user's teams
            if (currentUserRole.equals("student")) {
                allTeams = databaseHelper.getStudentTeams(currentUserId);
            } else if (currentUserRole.equals("coach")) {
                allTeams = databaseHelper.getCoachTeams(currentUserId);
            }
            filterTeams();
            bottomSheetDialog.dismiss();
        });

        btnAllTeams.setOnClickListener(v -> {
            // Show all teams (admin privilege)
            if (currentUserRole.equals("admin")) {
                allTeams = databaseHelper.getAllTeams();
                filterTeams();
            } else {
                Toast.makeText(getContext(), "You can only view your own teams", Toast.LENGTH_SHORT).show();
            }
            bottomSheetDialog.dismiss();
        });

        btnActiveTeams.setOnClickListener(v -> {
            // Filter to show only active teams
            Toast.makeText(getContext(), "Active teams filter coming soon!", Toast.LENGTH_SHORT).show();
            bottomSheetDialog.dismiss();
        });

        btnClearFilter.setOnClickListener(v -> {
            searchQuery = "";
            selectedSport = "All";
            etSearch.setText("");
            chipGroupSports.check(R.id.chip_all);
            loadTeams();
            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog.show();
    }

    private boolean isCoachOrAdmin() {
        return currentUserRole.equals("coach") || currentUserRole.equals("admin");
    }

    // TeamAdapter.OnTeamClickListener implementation
    @Override
    public void onTeamClick(Team team) {
        Intent intent = new Intent(getActivity(), TeamManagementActivity.class);
        intent.putExtra("teamId", team.getId());
        intent.putExtra("action", "view");
        startActivity(intent);
    }

    @Override
    public void onTeamEdit(Team team) {
        if (canEditTeam(team)) {
            Intent intent = new Intent(getActivity(), TeamManagementActivity.class);
            intent.putExtra("teamId", team.getId());
            intent.putExtra("action", "edit");
            startActivity(intent);
        } else {
            Toast.makeText(getContext(), "You don't have permission to edit this team", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onTeamDelete(Team team) {
        if (canEditTeam(team)) {
            showDeleteConfirmation(team);
        } else {
            Toast.makeText(getContext(), "You don't have permission to delete this team", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean canEditTeam(Team team) {
        return currentUserRole.equals("admin") ||
                (currentUserRole.equals("coach") && team.getCoachId() == currentUserId);
    }

    private void showDeleteConfirmation(Team team) {
        new androidx.appcompat.app.AlertDialog.Builder(getContext())
                .setTitle("Delete Team")
                .setMessage("Are you sure you want to delete '" + team.getName() + "'? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    if (databaseHelper.deleteTeam(team.getId())) {
                        Toast.makeText(getContext(), "Team deleted successfully", Toast.LENGTH_SHORT).show();
                        loadTeams();
                    } else {
                        Toast.makeText(getContext(), "Failed to delete team", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh teams when fragment becomes visible
        loadTeams();
    }
}
