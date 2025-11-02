package com.example.guarena.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.guarena.R;
import com.example.guarena.activities.DietPlanActivity;
import com.example.guarena.activities.GymExercisesActivity;
import com.example.guarena.activities.LoginActivity;
import com.example.guarena.activities.PerformanceActivity;
import com.example.guarena.activities.ProfileActivity;
import com.example.guarena.database.DatabaseHelper;
import com.example.guarena.models.User;
import com.example.guarena.utils.ImageUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.io.IOException;

public class ProfileFragment extends Fragment {

    private View rootView;
    private MaterialCardView cardQuickStats;
    private ImageView ivProfilePicture, ivEditProfilePic;
    private TextView tvUserName, tvUserRole, tvUserEmail;
    private TextView tvTeamsCount, tvEventsCount, tvPerformanceScore;
    private LinearLayout llPersonalInfo, llMyPerformance, llDietPlan, llExercisePlan;
    private LinearLayout llNotifications, llPrivacy, llChangePassword;
    private SwitchMaterial switchNotifications;
    private MaterialButton btnLogout;

    private DatabaseHelper databaseHelper;
    private String currentUserRole;
    private int currentUserId;
    private User currentUser;

    // Image picker launcher
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_profile, container, false);

        // Initialize database
        databaseHelper = new DatabaseHelper(getContext());

        // Get user session data
        getUserSessionData();

        // Initialize views
        initViews();

        // Setup image picker
        setupImagePicker();

        // Setup UI based on role
        setupUIForRole();

        // Load user data
        loadUserData();

        // Setup click listeners
        setupClickListeners();

        return rootView;
    }

    private void getUserSessionData() {
        SharedPreferences sharedPref = getActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        currentUserId = sharedPref.getInt("userId", -1);
        currentUserRole = sharedPref.getString("role", "student");
    }

    private void initViews() {
        cardQuickStats = rootView.findViewById(R.id.card_quick_stats);
        ivProfilePicture = rootView.findViewById(R.id.iv_profile_picture);
        ivEditProfilePic = rootView.findViewById(R.id.iv_edit_profile_pic);
        tvUserName = rootView.findViewById(R.id.tv_user_name);
        tvUserRole = rootView.findViewById(R.id.tv_user_role);
        tvUserEmail = rootView.findViewById(R.id.tv_user_email);
        tvTeamsCount = rootView.findViewById(R.id.tv_teams_count);
        tvEventsCount = rootView.findViewById(R.id.tv_events_count);
        tvPerformanceScore = rootView.findViewById(R.id.tv_performance_score);

        llPersonalInfo = rootView.findViewById(R.id.ll_personal_info);
        llMyPerformance = rootView.findViewById(R.id.ll_my_performance);
        llDietPlan = rootView.findViewById(R.id.ll_diet_plan);
        llExercisePlan = rootView.findViewById(R.id.ll_exercise_plan);
        llNotifications = rootView.findViewById(R.id.ll_notifications);
        llPrivacy = rootView.findViewById(R.id.ll_privacy);
        llChangePassword = rootView.findViewById(R.id.ll_change_password);

        switchNotifications = rootView.findViewById(R.id.switch_notifications);
        btnLogout = rootView.findViewById(R.id.btn_logout);
    }

    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null) {
                            updateProfilePicture(imageUri);
                        }
                    }
                }
        );
    }

    private void setupUIForRole() {
        // Show quick stats only for students
        if (currentUserRole.equals("student")) {
            cardQuickStats.setVisibility(View.VISIBLE);
            loadQuickStats();
        } else {
            cardQuickStats.setVisibility(View.GONE);
        }

        // Update role text
        String roleText = currentUserRole.substring(0, 1).toUpperCase() + currentUserRole.substring(1);
        tvUserRole.setText(roleText);
    }

    private void loadUserData() {
        currentUser = databaseHelper.getUserById(currentUserId);
        if (currentUser != null) {
            tvUserName.setText(currentUser.getFullName());
            tvUserEmail.setText(currentUser.getEmail());

            // Load profile picture if exists
            if (currentUser.getProfileImagePath() != null && !currentUser.getProfileImagePath().isEmpty()) {
                Glide.with(this)
                        .load(currentUser.getProfileImagePath())
                        .circleCrop()
                        .placeholder(R.drawable.ic_profile)
                        .error(R.drawable.ic_profile)
                        .into(ivProfilePicture);
            }
        }

        // Load notification preference
        SharedPreferences prefs = getActivity().getSharedPreferences("AppSettings", Context.MODE_PRIVATE);
        boolean notificationsEnabled = prefs.getBoolean("notifications_enabled", true);
        switchNotifications.setChecked(notificationsEnabled);
    }

    private void loadQuickStats() {
        if (currentUserRole.equals("student")) {
            // Load teams count
            int teamsCount = databaseHelper.getUserTeamsCount(currentUserId, currentUserRole);
            tvTeamsCount.setText(String.valueOf(teamsCount));

            // Load events count
            int eventsCount = databaseHelper.getUserEventsCount(currentUserId, currentUserRole);
            tvEventsCount.setText(String.valueOf(eventsCount));

            // Load performance score
            double performanceScore = databaseHelper.getUserPerformanceScore(currentUserId);
            tvPerformanceScore.setText(String.format("%.0f%%", performanceScore));
        }
    }

    private void setupClickListeners() {
        // Profile picture edit
        ivEditProfilePic.setOnClickListener(v -> showImagePickerDialog());

        // Menu options
        llPersonalInfo.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ProfileActivity.class);
            startActivity(intent);
        });

        llMyPerformance.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), PerformanceActivity.class);
            intent.putExtra("playerId", currentUserId);
            startActivity(intent);
        });

        llDietPlan.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), DietPlanActivity.class);
            startActivity(intent);
        });

        llExercisePlan.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), GymExercisesActivity.class);
            startActivity(intent);
        });

        // Settings
        llNotifications.setOnClickListener(v -> {
            switchNotifications.setChecked(!switchNotifications.isChecked());
        });

        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveNotificationPreference(isChecked);
            String message = isChecked ? "Notifications enabled" : "Notifications disabled";
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        });

        llPrivacy.setOnClickListener(v -> {
            showPrivacyDialog();
        });

        llChangePassword.setOnClickListener(v -> {
            showChangePasswordDialog();
        });

        // Logout
        btnLogout.setOnClickListener(v -> {
            showLogoutDialog();
        });
    }

    private void showImagePickerDialog() {
        String[] options = {"Gallery", "Remove Photo", "Cancel"};

        new MaterialAlertDialogBuilder(getContext())
                .setTitle("Profile Picture")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0: // Gallery
                            openGallery();
                            break;
                        case 1: // Remove Photo
                            removeProfilePicture();
                            break;
                        case 2: // Cancel
                            dialog.dismiss();
                            break;
                    }
                })
                .show();
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void updateProfilePicture(Uri imageUri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), imageUri);

            // Compress and save image
            String imagePath = ImageUtils.saveImageToInternalStorage(
                    getContext(), bitmap, "profiles", "profile_" + currentUserId);

            if (imagePath != null) {
                // Update database
                if (databaseHelper.updateUserProfilePicture(currentUserId, imagePath)) {
                    // Update UI
                    Glide.with(this)
                            .load(imagePath)
                            .circleCrop()
                            .into(ivProfilePicture);

                    Toast.makeText(getContext(), "Profile picture updated", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Failed to update profile picture", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (IOException e) {
            Toast.makeText(getContext(), "Error loading image", Toast.LENGTH_SHORT).show();
        }
    }

    private void removeProfilePicture() {
        if (databaseHelper.updateUserProfilePicture(currentUserId, null)) {
            ivProfilePicture.setImageResource(R.drawable.ic_profile);
            Toast.makeText(getContext(), "Profile picture removed", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "Failed to remove profile picture", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveNotificationPreference(boolean enabled) {
        SharedPreferences prefs = getActivity().getSharedPreferences("AppSettings", Context.MODE_PRIVATE);
        prefs.edit().putBoolean("notifications_enabled", enabled).apply();
    }

    private void showPrivacyDialog() {
        new MaterialAlertDialogBuilder(getContext())
                .setTitle("Privacy Settings")
                .setMessage("Privacy settings will allow you to control who can see your profile and performance data.")
                .setPositiveButton("OK", null)
                .show();
    }

    private void showChangePasswordDialog() {
        // TODO: Implement change password dialog
        Toast.makeText(getContext(), "Change password feature coming soon!", Toast.LENGTH_SHORT).show();
    }

    private void showLogoutDialog() {
        new MaterialAlertDialogBuilder(getContext())
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
        SharedPreferences sharedPref = getActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.clear();
        editor.apply();

        // Navigate to login
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        getActivity().finish();

        Toast.makeText(getContext(), "Logged out successfully", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh user data when fragment becomes visible
        loadUserData();
        if (currentUserRole.equals("student")) {
            loadQuickStats();
        }
    }
}
