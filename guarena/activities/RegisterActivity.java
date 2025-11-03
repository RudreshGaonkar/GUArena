package com.example.guarena.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.guarena.R;
import com.example.guarena.database.DatabaseHelper;
import com.example.guarena.models.User;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout tilFullName, tilUsername, tilEmail, tilPhone, tilRole, tilPassword, tilConfirmPassword;
    private TextInputEditText etFullName, etUsername, etEmail, etPhone, etPassword, etConfirmPassword;
    private AutoCompleteTextView etRole;
    private MaterialButton btnRegister;
    private CheckBox cbTerms;
    private TextView tvSignIn;
    private DatabaseHelper databaseHelper;

    // SECURE: Only allow student and coach registration - NO ADMIN
    private String[] roles = {"Student", "Coach"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize database
        databaseHelper = new DatabaseHelper(this);

        // Initialize views
        initViews();

        // Setup role dropdown
        setupRoleDropdown();

        // Set click listeners
        setClickListeners();
    }

    private void initViews() {
        tilFullName = findViewById(R.id.til_full_name);
        tilUsername = findViewById(R.id.til_username);
        tilEmail = findViewById(R.id.til_email);
        tilPhone = findViewById(R.id.til_phone);
        tilRole = findViewById(R.id.til_role);
        tilPassword = findViewById(R.id.til_password);
        tilConfirmPassword = findViewById(R.id.til_confirm_password);

        etFullName = findViewById(R.id.et_full_name);
        etUsername = findViewById(R.id.et_username);
        etEmail = findViewById(R.id.et_email);
        etPhone = findViewById(R.id.et_phone);
        etRole = findViewById(R.id.et_role);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);

        btnRegister = findViewById(R.id.btn_register);
        cbTerms = findViewById(R.id.cb_terms);
        tvSignIn = findViewById(R.id.tv_sign_in);
    }

    private void setupRoleDropdown() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, roles);
        etRole.setAdapter(adapter);

        // Set default to Student
        etRole.setText("Student", false);
    }

    private void setClickListeners() {
        btnRegister.setOnClickListener(v -> attemptRegistration());

        tvSignIn.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        });
    }

    private void attemptRegistration() {
        // Clear previous errors
        clearErrors();

        // Get input values
        String fullName = etFullName.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String role = etRole.getText().toString().trim();
        String password = etPassword.getText().toString();
        String confirmPassword = etConfirmPassword.getText().toString();

        // Validation
        if (!validateInputs(fullName, username, email, phone, role, password, confirmPassword)) {
            return;
        }

        // SECURITY CHECK: Prevent admin role registration
        if (role.toLowerCase().equals("admin")) {
            tilRole.setError("Admin registration not allowed");
            Toast.makeText(this, "Admin accounts cannot be created through registration", Toast.LENGTH_LONG).show();
            return;
        }

        // Check if user already exists
        if (databaseHelper.isUserExists(username, email)) {
            tilUsername.setError("Username or email already exists");
            Toast.makeText(this, "User with this username or email already exists", Toast.LENGTH_SHORT).show();
            return;
        }

        // Disable register button during registration
        btnRegister.setEnabled(false);
        btnRegister.setText("Creating Account...");

        // Create user object
        User user = new User();
        user.setFullName(fullName);
        user.setUsername(username);
        user.setEmail(email);
        user.setPhone(phone);
        user.setRole(role.toLowerCase());
        user.setPassword(password); // Password will be hashed in DatabaseHelper

        // Register user
        long userId = databaseHelper.registerUser(user);

        if (userId > 0) {
            // Registration successful
            Toast.makeText(this, "Account created successfully! Please login.", Toast.LENGTH_LONG).show();

            // Navigate to Login
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            intent.putExtra("username", username); // Pre-fill username in login
            startActivity(intent);
            finish();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        } else {
            // Registration failed
            Toast.makeText(this, "Registration failed. Please try again.", Toast.LENGTH_SHORT).show();

            // Reset button
            btnRegister.setEnabled(true);
            btnRegister.setText("Create Account");
        }
    }

    private boolean validateInputs(String fullName, String username, String email,
                                   String phone, String role, String password, String confirmPassword) {
        // Full Name validation
        if (TextUtils.isEmpty(fullName)) {
            tilFullName.setError("Full name is required");
            etFullName.requestFocus();
            return false;
        }

        if (fullName.length() < 3) {
            tilFullName.setError("Full name must be at least 3 characters");
            etFullName.requestFocus();
            return false;
        }

        // Username validation
        if (TextUtils.isEmpty(username)) {
            tilUsername.setError("Username is required");
            etUsername.requestFocus();
            return false;
        }

        if (username.length() < 4) {
            tilUsername.setError("Username must be at least 4 characters");
            etUsername.requestFocus();
            return false;
        }

        // Email validation
        if (TextUtils.isEmpty(email)) {
            tilEmail.setError("Email is required");
            etEmail.requestFocus();
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Please enter a valid email address");
            etEmail.requestFocus();
            return false;
        }

        // Phone validation
        if (TextUtils.isEmpty(phone)) {
            tilPhone.setError("Phone number is required");
            etPhone.requestFocus();
            return false;
        }

        if (phone.length() < 10) {
            tilPhone.setError("Please enter a valid phone number");
            etPhone.requestFocus();
            return false;
        }

        // Role validation
        if (TextUtils.isEmpty(role)) {
            tilRole.setError("Please select a role");
            etRole.requestFocus();
            return false;
        }

        // Password validation
        if (TextUtils.isEmpty(password)) {
            tilPassword.setError("Password is required");
            etPassword.requestFocus();
            return false;
        }

        if (password.length() < 6) {
            tilPassword.setError("Password must be at least 6 characters");
            etPassword.requestFocus();
            return false;
        }

        // Confirm Password validation
        if (TextUtils.isEmpty(confirmPassword)) {
            tilConfirmPassword.setError("Please confirm your password");
            etConfirmPassword.requestFocus();
            return false;
        }

        if (!password.equals(confirmPassword)) {
            tilConfirmPassword.setError("Passwords do not match");
            etConfirmPassword.requestFocus();
            return false;
        }

        // Terms and Conditions validation
        if (!cbTerms.isChecked()) {
            Toast.makeText(this, "Please accept Terms and Conditions", Toast.LENGTH_SHORT).show();
            cbTerms.requestFocus();
            return false;
        }

        return true;
    }

    private void clearErrors() {
        tilFullName.setError(null);
        tilUsername.setError(null);
        tilEmail.setError(null);
        tilPhone.setError(null);
        tilRole.setError(null);
        tilPassword.setError(null);
        tilConfirmPassword.setError(null);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
