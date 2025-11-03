package com.example.guarena.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.guarena.R;
import com.example.guarena.fragments.EventsFragment;
import com.example.guarena.fragments.HomeFragment;
import com.example.guarena.fragments.ProfileFragment;
import com.example.guarena.fragments.TeamsFragment;
import com.example.guarena.utils.NotificationService; // ✅ ADD THIS
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class DashboardActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigation;
    private FloatingActionButton fabAdd;
    private MaterialToolbar toolbar;
    private String currentUserRole;
    private int currentUserId;
    private String currentUserName;

    // Fragments
    private HomeFragment homeFragment;
    private EventsFragment eventsFragment;
    private TeamsFragment teamsFragment;
    private ProfileFragment profileFragment;
    private Fragment currentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // ✅ Initialize notification system FIRST
        NotificationService.createNotificationChannel(this);

        // Get user session data
        getUserSessionData();

        // Initialize views
        initViews();

        // Setup toolbar
        setupToolbar();

        // Setup bottom navigation
        setupBottomNavigation();

        // Setup FAB
        setupFAB();

        // Load default fragment
        loadFragment(new HomeFragment(), "Home");

        // Register broadcast receiver
        IntentFilter filter = new IntentFilter();
        filter.addAction("SWITCH_TO_EVENTS_TAB");
        filter.addAction("SWITCH_TO_TEAMS_TAB");
        registerReceiver(tabSwitchReceiver, filter);
    }

    private BroadcastReceiver tabSwitchReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("SWITCH_TO_EVENTS_TAB".equals(action)) {
                bottomNavigation.setSelectedItemId(R.id.nav_events);
            } else if ("SWITCH_TO_TEAMS_TAB".equals(action)) {
                bottomNavigation.setSelectedItemId(R.id.nav_teams);
            }
        }
    };

    private void getUserSessionData() {
        SharedPreferences sharedPref = getSharedPreferences("UserSession", MODE_PRIVATE);
        currentUserId = sharedPref.getInt("userId", -1);
        currentUserRole = sharedPref.getString("role", "student");
        currentUserName = sharedPref.getString("fullName", "User");
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        bottomNavigation = findViewById(R.id.bottom_navigation);
        fabAdd = findViewById(R.id.fab_add);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("GuArena");
            getSupportActionBar().setSubtitle("Welcome back, " + currentUserName);
        }
    }

    private void setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            String title = "";

            if (item.getItemId() == R.id.nav_home) {
                if (homeFragment == null) homeFragment = new HomeFragment();
                selectedFragment = homeFragment;
                title = "Home";
            } else if (item.getItemId() == R.id.nav_events) {
                if (eventsFragment == null) eventsFragment = new EventsFragment();
                selectedFragment = eventsFragment;
                title = "Events";
            } else if (item.getItemId() == R.id.nav_teams) {
                if (teamsFragment == null) teamsFragment = new TeamsFragment();
                selectedFragment = teamsFragment;
                title = "Teams";
            } else if (item.getItemId() == R.id.nav_profile) {
                if (profileFragment == null) profileFragment = new ProfileFragment();
                selectedFragment = profileFragment;
                title = "Profile";
            }

            if (selectedFragment != null) {
                loadFragment(selectedFragment, title);
                return true;
            }

            return false;
        });
    }

    private void setupFAB() {
        fabAdd.setOnClickListener(v -> {
            // FAB action based on current fragment and user role
            if (currentFragment instanceof HomeFragment) {
                showQuickActionsDialog();
            } else if (currentFragment instanceof EventsFragment) {
                // Create new event
                Intent intent = new Intent(this, EventScheduleActivity.class);
                intent.putExtra("action", "create");
                startActivity(intent);
            } else if (currentFragment instanceof TeamsFragment) {
                // Create new team (Coach/Admin only)
                if (isCoachOrAdmin()) {
                    Intent intent = new Intent(this, TeamManagementActivity.class);
                    intent.putExtra("action", "create");
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "Only coaches and admins can create teams", Toast.LENGTH_SHORT).show();
                }
            } else if (currentFragment instanceof ProfileFragment) {
                // Edit profile
                Intent intent = new Intent(this, ProfileActivity.class);
                startActivity(intent);
            }
        });
    }

    private void loadFragment(Fragment fragment, String title) {
        if (fragment != null) {
            currentFragment = fragment;
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, fragment);
            transaction.commit();

            // Update toolbar title
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(title);
            }

            // Update FAB visibility based on fragment and role
            updateFABVisibility();
        }
    }

    private void updateFABVisibility() {
        // Hide FAB for students in Teams fragment
        if (currentFragment instanceof TeamsFragment && currentUserRole.equals("student")) {
            fabAdd.hide();
        } else {
            fabAdd.show();
        }
    }

    private void showQuickActionsDialog() {
        String[] actions;
        if (isCoachOrAdmin()) {
            actions = new String[]{
                    "Create Event",
                    "Add Team Member",
                    "Record Performance",
                    "Manage Equipment",
                    "Add Photos"
            };
        } else {
            actions = new String[]{
                    "View My Performance",
                    "Check Diet Plan",
                    "View Exercises",
                    "Upload Photo"
            };
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Quick Actions")
                .setItems(actions, (dialog, which) -> {
                    handleQuickAction(which);
                })
                .show();
    }

    private void handleQuickAction(int position) {
        Intent intent;
        if (isCoachOrAdmin()) {
            switch (position) {
                case 0: // Create Event
                    intent = new Intent(this, EventScheduleActivity.class);
                    intent.putExtra("action", "create");
                    startActivity(intent);
                    break;
                case 1: // Add Team Member
                    intent = new Intent(this, TeamManagementActivity.class);
                    startActivity(intent);
                    break;
                case 2: // Record Performance
                    intent = new Intent(this, PerformanceActivity.class);
                    startActivity(intent);
                    break;
                case 3: // Manage Equipment
                    // intent = new Intent(this, EquipmentActivity.class);
                    // startActivity(intent);
                    Toast.makeText(this, "Equipment management coming soon!", Toast.LENGTH_SHORT).show();
                    break;
                case 4: // Add Photos
                    intent = new Intent(this, GalleryActivity.class);
                    startActivity(intent);
                    break;
            }
        } else {
            switch (position) {
                case 0: // View My Performance
                    intent = new Intent(this, PerformanceActivity.class);
                    intent.putExtra("playerId", currentUserId);
                    startActivity(intent);
                    break;
                case 1: // Check Diet Plan
                    // intent = new Intent(this, DietPlanActivity.class);
                    // startActivity(intent);
                    Toast.makeText(this, "Diet plan feature coming soon!", Toast.LENGTH_SHORT).show();
                    break;
                case 2: // View Exercises
                    // intent = new Intent(this, GymExercisesActivity.class);
                    // startActivity(intent);
                    Toast.makeText(this, "Exercise feature coming soon!", Toast.LENGTH_SHORT).show();
                    break;
                case 3: // Upload Photo
                    intent = new Intent(this, GalleryActivity.class);
                    startActivity(intent);
                    break;
            }
        }
    }

    private boolean isCoachOrAdmin() {
        return currentUserRole.equals("coach") || currentUserRole.equals("admin");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_dashboard, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_notifications) {
            Toast.makeText(this, "Notifications feature coming soon!", Toast.LENGTH_SHORT).show();
            return true;
        } else if (item.getItemId() == R.id.action_settings) {
            Toast.makeText(this, "Settings feature coming soon!", Toast.LENGTH_SHORT).show();
            return true;
        } else if (item.getItemId() == R.id.action_logout) {
            showLogoutDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout", (dialog, which) -> {
                    logout();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void logout() {
        // Clear user session
        SharedPreferences sharedPref = getSharedPreferences("UserSession", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.clear();
        editor.apply();

        // Navigate to login
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        // If not on home fragment, go to home
        if (!(currentFragment instanceof HomeFragment)) {
            bottomNavigation.setSelectedItemId(R.id.nav_home);
        } else {
            // Show exit confirmation
            new AlertDialog.Builder(this)
                    .setTitle("Exit App")
                    .setMessage("Are you sure you want to exit?")
                    .setPositiveButton("Exit", (dialog, which) -> {
                        finishAffinity();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(tabSwitchReceiver);
    }
}


//package com.example.guarena.activities;
//
//import android.content.Intent;
//import android.content.SharedPreferences;
//import android.os.Bundle;
//import android.view.Menu;
//import android.view.MenuItem;
//import android.widget.Toast;
//
//import androidx.appcompat.app.AlertDialog;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.fragment.app.Fragment;
//import androidx.fragment.app.FragmentTransaction;
//
//import com.example.guarena.R;
//import com.example.guarena.fragments.EventsFragment;
//import com.example.guarena.fragments.HomeFragment;
//import com.example.guarena.fragments.ProfileFragment;
//import com.example.guarena.fragments.TeamsFragment;
//import com.google.android.material.appbar.MaterialToolbar;
//import com.google.android.material.bottomnavigation.BottomNavigationView;
//import com.google.android.material.floatingactionbutton.FloatingActionButton;
//
//public class DashboardActivity extends AppCompatActivity {
//
//    private BottomNavigationView bottomNavigation;
//    private FloatingActionButton fabAdd;
//    private MaterialToolbar toolbar;
//
//    private String currentUserRole;
//    private int currentUserId;
//    private String currentUserName;
//
//    // Fragments
//    private HomeFragment homeFragment;
//    private EventsFragment eventsFragment;
//    private TeamsFragment teamsFragment;
//    private ProfileFragment profileFragment;
//
//    private Fragment currentFragment;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_dashboard);
//
//        // Get user session data
//        getUserSessionData();
//
//        // Initialize views
//        initViews();
//
//        // Setup toolbar
//        setupToolbar();
//
//        // Setup bottom navigation
//        setupBottomNavigation();
//
//        // Setup FAB
//        setupFAB();
//
//        // Load default fragment
//        loadFragment(new HomeFragment(), "Home");
//    }
//
//    private void getUserSessionData() {
//        SharedPreferences sharedPref = getSharedPreferences("UserSession", MODE_PRIVATE);
//        currentUserId = sharedPref.getInt("userId", -1);
//        currentUserRole = sharedPref.getString("role", "student");
//        currentUserName = sharedPref.getString("fullName", "User");
//    }
//
//    private void initViews() {
//        toolbar = findViewById(R.id.toolbar);
//        bottomNavigation = findViewById(R.id.bottom_navigation);
//        fabAdd = findViewById(R.id.fab_add);
//    }
//
//    private void setupToolbar() {
//        setSupportActionBar(toolbar);
//        if (getSupportActionBar() != null) {
//            getSupportActionBar().setTitle("GUArena");
//            getSupportActionBar().setSubtitle("Welcome back, " + currentUserName);
//        }
//    }
//
//    private void setupBottomNavigation() {
//        bottomNavigation.setOnItemSelectedListener(item -> {
//            Fragment selectedFragment = null;
//            String title = "";
//
//            if (item.getItemId() == R.id.nav_home) {
//                if (homeFragment == null) homeFragment = new HomeFragment();
//                selectedFragment = homeFragment;
//                title = "Home";
//            } else if (item.getItemId() == R.id.nav_events) {
//                if (eventsFragment == null) eventsFragment = new EventsFragment();
//                selectedFragment = eventsFragment;
//                title = "Events";
//            } else if (item.getItemId() == R.id.nav_teams) {
//                if (teamsFragment == null) teamsFragment = new TeamsFragment();
//                selectedFragment = teamsFragment;
//                title = "Teams";
//            } else if (item.getItemId() == R.id.nav_profile) {
//                if (profileFragment == null) profileFragment = new ProfileFragment();
//                selectedFragment = profileFragment;
//                title = "Profile";
//            }
//
//            if (selectedFragment != null) {
//                loadFragment(selectedFragment, title);
//                return true;
//            }
//            return false;
//        });
//    }
//
//    private void setupFAB() {
//        fabAdd.setOnClickListener(v -> {
//            // FAB action based on current fragment and user role
//            if (currentFragment instanceof HomeFragment) {
//                showQuickActionsDialog();
//            } else if (currentFragment instanceof EventsFragment) {
//                // Create new event
//                Intent intent = new Intent(this, EventScheduleActivity.class);
//                intent.putExtra("action", "create");
//                startActivity(intent);
//            } else if (currentFragment instanceof TeamsFragment) {
//                // Create new team (Coach/Admin only)
//                if (isCoachOrAdmin()) {
//                    Intent intent = new Intent(this, TeamManagementActivity.class);
//                    intent.putExtra("action", "create");
//                    startActivity(intent);
//                } else {
//                    Toast.makeText(this, "Only coaches and admins can create teams", Toast.LENGTH_SHORT).show();
//                }
//            } else if (currentFragment instanceof ProfileFragment) {
//                // Edit profile
//                Intent intent = new Intent(this, ProfileActivity.class);
//                startActivity(intent);
//            }
//        });
//    }
//
//    private void loadFragment(Fragment fragment, String title) {
//        if (fragment != null) {
//            currentFragment = fragment;
//
//            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
//            transaction.replace(R.id.fragment_container, fragment);
//            transaction.commit();
//
//            // Update toolbar title
//            if (getSupportActionBar() != null) {
//                getSupportActionBar().setTitle(title);
//            }
//
//            // Update FAB visibility based on fragment and role
//            updateFABVisibility();
//        }
//    }
//
//    private void updateFABVisibility() {
//        // Hide FAB for students in Teams fragment
//        if (currentFragment instanceof TeamsFragment && currentUserRole.equals("student")) {
//            fabAdd.hide();
//        } else {
//            fabAdd.show();
//        }
//    }
//
//    private void showQuickActionsDialog() {
//        String[] actions;
//
//        if (isCoachOrAdmin()) {
//            actions = new String[]{
//                    "Create Event",
//                    "Add Team Member",
//                    "Record Performance",
//                    "Manage Equipment",
//                    "Add Photos"
//            };
//        } else {
//            actions = new String[]{
//                    "View My Performance",
//                    "Check Diet Plan",
//                    "View Exercises",
//                    "Upload Photo"
//            };
//        }
//
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setTitle("Quick Actions")
//                .setItems(actions, (dialog, which) -> {
//                    handleQuickAction(which);
//                })
//                .show();
//    }
//
//    private void handleQuickAction(int position) {
//        Intent intent;
//
//        if (isCoachOrAdmin()) {
//            switch (position) {
//                case 0: // Create Event
//                    intent = new Intent(this, EventScheduleActivity.class);
//                    intent.putExtra("action", "create");
//                    startActivity(intent);
//                    break;
//                case 1: // Add Team Member
//                    intent = new Intent(this, TeamManagementActivity.class);
//                    startActivity(intent);
//                    break;
//                case 2: // Record Performance
//                    intent = new Intent(this, PerformanceActivity.class);
//                    startActivity(intent);
//                    break;
//                case 3: // Manage Equipment
//                    intent = new Intent(this, EquipmentActivity.class);
//                    startActivity(intent);
//                    break;
//                case 4: // Add Photos
//                    intent = new Intent(this, GalleryActivity.class);
//                    startActivity(intent);
//                    break;
//            }
//        } else {
//            switch (position) {
//                case 0: // View My Performance
//                    intent = new Intent(this, PerformanceActivity.class);
//                    intent.putExtra("playerId", currentUserId);
//                    startActivity(intent);
//                    break;
//                case 1: // Check Diet Plan
//                    intent = new Intent(this, DietPlanActivity.class);
//                    startActivity(intent);
//                    break;
//                case 2: // View Exercises
//                    intent = new Intent(this, GymExercisesActivity.class);
//                    startActivity(intent);
//                    break;
//                case 3: // Upload Photo
//                    intent = new Intent(this, GalleryActivity.class);
//                    startActivity(intent);
//                    break;
//            }
//        }
//    }
//
//    private boolean isCoachOrAdmin() {
//        return currentUserRole.equals("coach") || currentUserRole.equals("admin");
//    }
//
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.menu_dashboard, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        if (item.getItemId() == R.id.action_notifications) {
//            // TODO: Show notifications
//            Toast.makeText(this, "Notifications feature coming soon!", Toast.LENGTH_SHORT).show();
//            return true;
//        } else if (item.getItemId() == R.id.action_settings) {
//            // TODO: Open settings
//            Toast.makeText(this, "Settings feature coming soon!", Toast.LENGTH_SHORT).show();
//            return true;
//        } else if (item.getItemId() == R.id.action_logout) {
//            showLogoutDialog();
//            return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }
//
//    private void showLogoutDialog() {
//        new AlertDialog.Builder(this)
//                .setTitle("Logout")
//                .setMessage("Are you sure you want to logout?")
//                .setPositiveButton("Logout", (dialog, which) -> {
//                    logout();
//                })
//                .setNegativeButton("Cancel", null)
//                .show();
//    }
//
//    private void logout() {
//        // Clear user session
//        SharedPreferences sharedPref = getSharedPreferences("UserSession", MODE_PRIVATE);
//        SharedPreferences.Editor editor = sharedPref.edit();
//        editor.clear();
//        editor.apply();
//
//        // Navigate to login
//        Intent intent = new Intent(this, LoginActivity.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        startActivity(intent);
//        finish();
//
//        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
//    }
//
//    @Override
//    public void onBackPressed() {
//        // If not on home fragment, go to home
//        if (!(currentFragment instanceof HomeFragment)) {
//            bottomNavigation.setSelectedItemId(R.id.nav_home);
//        } else {
//            // Show exit confirmation
//            new AlertDialog.Builder(this)
//                    .setTitle("Exit App")
//                    .setMessage("Are you sure you want to exit?")
//                    .setPositiveButton("Exit", (dialog, which) -> {
//                        finishAffinity();
//                    })
//                    .setNegativeButton("Cancel", null)
//                    .show();
//        }
//    }
//}
