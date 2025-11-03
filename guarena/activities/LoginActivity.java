package com.example.guarena.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.guarena.R;
import com.example.guarena.database.DatabaseHelper;
import com.example.guarena.models.User;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout tilUsername, tilPassword;
    private TextInputEditText etUsername, etPassword;
    private MaterialButton btnLogin;
    private TextView tvForgotPassword, tvSignUp;
    private CheckBox cbRememberMe;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize database
        databaseHelper = new DatabaseHelper(this);

        // Initialize views
        initViews();

        // Set click listeners
        setClickListeners();

        // Show default admin credentials (first time only)
        showDefaultAdminInfo();

        // Pre-fill username if passed from registration
        Intent intent = getIntent();
        String prefilledUsername = intent.getStringExtra("username");
        if (prefilledUsername != null) {
            etUsername.setText(prefilledUsername);
        }
    }

    private void initViews() {
        tilUsername = findViewById(R.id.til_username);
        tilPassword = findViewById(R.id.til_password);
        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        tvForgotPassword = findViewById(R.id.tv_forgot_password);
        tvSignUp = findViewById(R.id.tv_sign_up);
        cbRememberMe = findViewById(R.id.cb_remember_me);
    }

    private void setClickListeners() {
        btnLogin.setOnClickListener(v -> attemptLogin());

        tvSignUp.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        tvForgotPassword.setOnClickListener(v -> {
            // TODO: Implement forgot password functionality
            Toast.makeText(this, "Forgot password feature coming soon!", Toast.LENGTH_SHORT).show();
        });
    }

    private void showDefaultAdminInfo() {
        SharedPreferences prefs = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        boolean firstLaunch = prefs.getBoolean("firstLaunch", true);

        if (firstLaunch) {
            new AlertDialog.Builder(this)
                    .setTitle("🔑 Default Admin Account")
                    .setMessage("A default admin account has been created for you:\n\n" +
                            "👤 Username: admin\n" +
                            "🔒 Password: admin123\n\n" +
                            "⚠️ Please change the password after first login!\n\n" +
                            "📝 Note: Only students and coaches can register. Admin is pre-created.")
                    .setPositiveButton("Auto-Fill", (dialog, which) -> {
                        // Auto-fill admin credentials for convenience
                        etUsername.setText("admin");
                        etPassword.setText("admin123");
                    })
                    .setNegativeButton("Skip", null)
                    .setCancelable(true)
                    .show();

            // Mark first launch as completed
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("firstLaunch", false);
            editor.apply();
        }
    }

    private void attemptLogin() {
        // Clear previous errors
        tilUsername.setError(null);
        tilPassword.setError(null);

        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Validation
        if (TextUtils.isEmpty(username)) {
            tilUsername.setError("Username is required");
            etUsername.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            tilPassword.setError("Password is required");
            etPassword.requestFocus();
            return;
        }

        // Disable login button during authentication
        btnLogin.setEnabled(false);
        btnLogin.setText("Logging in...");

        // Authenticate user
        User user = databaseHelper.authenticateUser(username, password);

        if (user != null) {
            // Login successful
            saveUserSession(user);

            // Show welcome message with role
            String welcomeMessage = "Welcome back, " + user.getFullName() + "!";
            if (user.getRole().equals("admin")) {
                welcomeMessage += " [Administrator]";
            } else if (user.getRole().equals("coach")) {
                welcomeMessage += " [Coach]";
            }

            Toast.makeText(this, welcomeMessage, Toast.LENGTH_SHORT).show();

            // Navigate to Dashboard
            Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
            startActivity(intent);
            finish();
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        } else {
            // Login failed
            tilPassword.setError("Invalid username or password");
            Toast.makeText(this, "Login failed. Please check your credentials.", Toast.LENGTH_SHORT).show();

            // Reset button
            btnLogin.setEnabled(true);
            btnLogin.setText("Login");
        }
    }

    private void saveUserSession(User user) {
        SharedPreferences sharedPref = getSharedPreferences("UserSession", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putBoolean("isLoggedIn", true);
        editor.putInt("userId", user.getId());
        editor.putString("username", user.getUsername());
        editor.putString("email", user.getEmail());
        editor.putString("role", user.getRole());
        editor.putString("fullName", user.getFullName());
        editor.putBoolean("rememberMe", cbRememberMe.isChecked());
        editor.apply();
    }

    @Override
    public void onBackPressed() {
        // Close app when back pressed from login
        super.onBackPressed();
        finishAffinity();
    }
}
