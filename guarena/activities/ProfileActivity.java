package com.example.guarena.activities;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.guarena.R;
import com.example.guarena.database.DatabaseHelper;
import com.example.guarena.models.User;
import com.example.guarena.utils.ImageUtils;
import com.example.guarena.utils.ValidationUtils;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.IOException;

public class ProfileActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private MaterialCardView cardProfilePicture;
    private ImageView ivProfilePicture;
    private TextView tvProfileName, tvProfileRole;
    private TextInputLayout tilFullName, tilUsername, tilEmail, tilPhone;
    private TextInputEditText etFullName, etUsername, etEmail, etPhone;
    private MaterialButton btnEditProfile, btnSaveChanges;
    private LinearLayout llChangePassword, llPrivacySettings, llLogout;

    private DatabaseHelper databaseHelper;
    private User currentUser;
    private String userRole;
    private int currentUserId;
    private boolean isEditMode = false;

    // Image picker launcher
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ActivityResultLauncher<String> permissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize database
        databaseHelper = new DatabaseHelper(this);

        // Get user session data
        getUserSessionData();

        // Initialize views
        initViews();

        // Setup UI
        setupToolbar();
        setupImagePicker();
        setupClickListeners();

        // Load user data
        loadUserProfile();
    }

    private void getUserSessionData() {
        SharedPreferences sharedPref = getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        currentUserId = sharedPref.getInt("userId", -1);
        userRole = sharedPref.getString("role", "student");
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        cardProfilePicture = findViewById(R.id.card_profile_picture);
        ivProfilePicture = findViewById(R.id.iv_profile_picture);
        tvProfileName = findViewById(R.id.tv_profile_name);
        tvProfileRole = findViewById(R.id.tv_profile_role);

        tilFullName = findViewById(R.id.til_full_name);
        tilUsername = findViewById(R.id.til_username);
        tilEmail = findViewById(R.id.til_email);
        tilPhone = findViewById(R.id.til_phone);

        etFullName = findViewById(R.id.et_full_name);
        etUsername = findViewById(R.id.et_username);
        etEmail = findViewById(R.id.et_email);
        etPhone = findViewById(R.id.et_phone);

        btnEditProfile = findViewById(R.id.btn_edit_profile);
        btnSaveChanges = findViewById(R.id.btn_save_changes);

        llChangePassword = findViewById(R.id.ll_change_password);
        llPrivacySettings = findViewById(R.id.ll_privacy_settings);
        llLogout = findViewById(R.id.ll_logout);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    private void setupImagePicker() {
        // Image picker launcher
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null) {
                            updateProfilePicture(imageUri);
                        }
                    }
                }
        );

        // Permission launcher
        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        openImagePicker();
                    } else {
                        Toast.makeText(this, "Permission denied to access photos", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void setupClickListeners() {
        btnEditProfile.setOnClickListener(v -> toggleEditMode());

        btnSaveChanges.setOnClickListener(v -> saveProfileChanges());

        cardProfilePicture.setOnClickListener(v -> {
            if (isEditMode) {
                checkPermissionAndPickImage();
            }
        });

        llChangePassword.setOnClickListener(v -> showChangePasswordDialog());

        llPrivacySettings.setOnClickListener(v -> {
            Toast.makeText(this, "Privacy settings coming soon!", Toast.LENGTH_SHORT).show();
        });

        llLogout.setOnClickListener(v -> showLogoutConfirmation());
    }

    private void loadUserProfile() {
        if (currentUserId != -1) {
            currentUser = databaseHelper.getUserById(currentUserId);
            if (currentUser != null) {
                populateUserData();
            } else {
                Toast.makeText(this, "Error loading profile", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void populateUserData() {
        if (currentUser != null) {
            // Profile header
            tvProfileName.setText(currentUser.getFullName());
            tvProfileRole.setText(capitalizeFirst(currentUser.getRole()));

            // Input fields
            etFullName.setText(currentUser.getFullName());
            etUsername.setText(currentUser.getUsername());
            etEmail.setText(currentUser.getEmail());
            etPhone.setText(currentUser.getPhone());

            // Load profile picture
            loadProfilePicture();
        }
    }

    private void loadProfilePicture() {
        if (currentUser.getProfileImagePath() != null && !currentUser.getProfileImagePath().isEmpty()) {
            Bitmap bitmap = ImageUtils.loadImageFromInternalStorage(currentUser.getProfileImagePath());
            if (bitmap != null) {
                ivProfilePicture.setImageBitmap(bitmap);
            } else {
                ivProfilePicture.setImageResource(R.drawable.ic_profile);
            }
        } else {
            ivProfilePicture.setImageResource(R.drawable.ic_profile);
        }
    }

    private void toggleEditMode() {
        isEditMode = !isEditMode;

        if (isEditMode) {
            // Enable editing
            setFieldsEnabled(true);
            btnEditProfile.setText("Cancel");
            btnEditProfile.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_close));
            btnSaveChanges.setVisibility(android.view.View.VISIBLE);

            // Show hint for profile picture
            Toast.makeText(this, "Tap profile picture to change", Toast.LENGTH_SHORT).show();
        } else {
            // Disable editing
            setFieldsEnabled(false);
            btnEditProfile.setText("Edit");
            btnEditProfile.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_edit));
            btnSaveChanges.setVisibility(android.view.View.GONE);

            // Restore original data
            populateUserData();
        }
    }

    private void setFieldsEnabled(boolean enabled) {
        etFullName.setEnabled(enabled);
        etUsername.setEnabled(enabled);
        etEmail.setEnabled(enabled);
        etPhone.setEnabled(enabled);
    }

    private void checkPermissionAndPickImage() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
        } else {
            openImagePicker();
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void updateProfilePicture(Uri imageUri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);

            // Compress and resize
            Bitmap compressedBitmap = ImageUtils.compressBitmap(bitmap, 400, 400);

            // Save to internal storage
            String fileName = "profile_" + currentUserId + "_" + System.currentTimeMillis();
            String savedPath = ImageUtils.saveImageToInternalStorage(
                    this, compressedBitmap, "profiles", fileName);

            if (savedPath != null) {
                // Update UI
                ivProfilePicture.setImageBitmap(compressedBitmap);

                // Update database
                if (databaseHelper.updateUserProfilePicture(currentUserId, savedPath)) {
                    currentUser.setProfileImagePath(savedPath);
                    Toast.makeText(this, "Profile picture updated!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Failed to update profile picture", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show();
            }

        } catch (IOException e) {
            Toast.makeText(this, "Error processing image", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveProfileChanges() {
        if (!validateInputs()) {
            return;
        }

        // Get updated values
        String fullName = etFullName.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();

        // Update user object
        currentUser.setFullName(fullName);
        currentUser.setUsername(username);
        currentUser.setEmail(email);
        currentUser.setPhone(phone);

        // Update database
        if (databaseHelper.updateUserProfile(currentUser)) {
            // Update session data
            updateSessionData();

            // Update UI
            tvProfileName.setText(fullName);

            Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
            toggleEditMode(); // Exit edit mode
        } else {
            Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean validateInputs() {
        // Clear previous errors
        tilFullName.setError(null);
        tilUsername.setError(null);
        tilEmail.setError(null);
        tilPhone.setError(null);

        String fullName = etFullName.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();

        // Validate full name
        if (!ValidationUtils.isValidName(fullName)) {
            tilFullName.setError("Please enter a valid name");
            etFullName.requestFocus();
            return false;
        }

        // Validate username
        if (!ValidationUtils.isValidUsername(username)) {
            tilUsername.setError("Username must be 4+ characters, letters/numbers only");
            etUsername.requestFocus();
            return false;
        }

        // Check if username is taken (by someone else)
        if (!username.equals(currentUser.getUsername()) &&
                databaseHelper.isUserExists(username, "")) {
            tilUsername.setError("Username already taken");
            etUsername.requestFocus();
            return false;
        }

        // Validate email
        if (!ValidationUtils.isValidEmail(email)) {
            tilEmail.setError("Please enter a valid email");
            etEmail.requestFocus();
            return false;
        }

        // Check if email is taken (by someone else)
        if (!email.equals(currentUser.getEmail()) &&
                databaseHelper.isUserExists("", email)) {
            tilEmail.setError("Email already registered");
            etEmail.requestFocus();
            return false;
        }

        // Validate phone
        if (!ValidationUtils.isValidPhone(phone)) {
            tilPhone.setError("Please enter a valid phone number");
            etPhone.requestFocus();
            return false;
        }

        return true;
    }

    private void updateSessionData() {
        SharedPreferences sharedPref = getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("fullName", currentUser.getFullName());
        editor.putString("username", currentUser.getUsername());
        editor.putString("email", currentUser.getEmail());
        editor.apply();
    }

    private void showChangePasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_change_password, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
//        dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_background);

        // Get views from dialog
        TextInputEditText etCurrentPassword = dialogView.findViewById(R.id.et_current_password);
        TextInputEditText etNewPassword = dialogView.findViewById(R.id.et_new_password);
        TextInputEditText etConfirmPassword = dialogView.findViewById(R.id.et_confirm_password);
        TextInputLayout tilCurrentPassword = dialogView.findViewById(R.id.til_current_password);
        TextInputLayout tilNewPassword = dialogView.findViewById(R.id.til_new_password);
        TextInputLayout tilConfirmPassword = dialogView.findViewById(R.id.til_confirm_password);
        MaterialButton btnCancel = dialogView.findViewById(R.id.btn_cancel);
        MaterialButton btnChangePassword = dialogView.findViewById(R.id.btn_change_password);

        // Set click listeners
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnChangePassword.setOnClickListener(v -> {
            // Clear previous errors
            tilCurrentPassword.setError(null);
            tilNewPassword.setError(null);
            tilConfirmPassword.setError(null);

            String currentPassword = etCurrentPassword.getText().toString().trim();
            String newPassword = etNewPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();

            if (validatePasswordChange(currentPassword, newPassword, confirmPassword,
                    tilCurrentPassword, tilNewPassword, tilConfirmPassword)) {
                // Change password
                if (changeUserPassword(currentPassword, newPassword)) {
                    Toast.makeText(this, "Password changed successfully!", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                } else {
                    tilCurrentPassword.setError("Current password is incorrect");
                }
            }
        });

        dialog.show();
    }

    private boolean validatePasswordChange(String currentPassword, String newPassword,
                                           String confirmPassword, TextInputLayout tilCurrent,
                                           TextInputLayout tilNew, TextInputLayout tilConfirm) {
        // Validate current password
        if (TextUtils.isEmpty(currentPassword)) {
            tilCurrent.setError("Please enter current password");
            return false;
        }

        // Validate new password
        if (!ValidationUtils.isValidPassword(newPassword)) {
            tilNew.setError("Password must be at least 6 characters long");
            return false;
        }

        // Check if new password is different from current
        if (currentPassword.equals(newPassword)) {
            tilNew.setError("New password must be different from current password");
            return false;
        }

        // Validate confirm password
        if (!newPassword.equals(confirmPassword)) {
            tilConfirm.setError("Passwords do not match");
            return false;
        }

        return true;
    }

    private boolean changeUserPassword(String currentPassword, String newPassword) {
        // Verify current password first
        User verifyUser = databaseHelper.authenticateUser(currentUser.getUsername(), currentPassword);
        if (verifyUser == null) {
            return false; // Current password is incorrect
        }

        // Update password in database
        return databaseHelper.updateUserPassword(currentUserId, newPassword);
    }


    private void showLogoutConfirmation() {
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
        // Clear session data
        SharedPreferences sharedPref = getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.clear();
        editor.apply();

        // Navigate to login
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();

        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
    }

    private String capitalizeFirst(String text) {
        if (TextUtils.isEmpty(text)) return "";
        return text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
    }

    @Override
    public boolean onSupportNavigateUp() {
        if (isEditMode) {
            toggleEditMode(); // Cancel edit mode
            return true;
        }
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        if (isEditMode) {
            toggleEditMode(); // Cancel edit mode
        } else {
            super.onBackPressed();
        }
    }
}
