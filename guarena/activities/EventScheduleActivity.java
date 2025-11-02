package com.example.guarena.activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.guarena.R;
import com.example.guarena.database.DatabaseHelper;
import com.example.guarena.models.Event;
import com.example.guarena.models.Team;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class EventScheduleActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private TextInputLayout tilEventTitle, tilEventType, tilEventDescription;
    private TextInputLayout tilEventDate, tilEventTime, tilEventLocation, tilEventTeam;
    private TextInputEditText etEventTitle, etEventDescription, etEventDate, etEventTime, etEventLocation;
    private AutoCompleteTextView etEventType, etEventTeam;
    private MaterialButton btnSaveEvent;

    // ✅ NEW: Brochure upload UI elements
    private MaterialCardView cardBrochure;
    private LinearLayout llBrochureSection;
    private MaterialButton btnUploadBrochure, btnRemoveBrochure;
    private TextView tvBrochureFileName, tvBrochureInfo;

    private DatabaseHelper databaseHelper;
    private String userRole;
    private int currentUserId;
    private String action; // "create", "edit", "view"
    private int eventId = -1;
    private Event currentEvent;

    private Calendar selectedDateTime;
    private List<Team> availableTeams;

    // ✅ NEW: Brochure file handling
    private Uri selectedBrochureUri;
    private String brochurePath;
    private ActivityResultLauncher<Intent> brochurePickerLauncher;

    // Event types
    private String[] eventTypes = {"Practice", "Match", "Tournament", "Meeting"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_schedule);

        // Initialize database
        databaseHelper = new DatabaseHelper(this);

        // ✅ NEW: Initialize brochure picker
        initBrochurePicker();

        // Get user session and intent data
        getUserSessionData();
        getIntentData();

        // Initialize views
        initViews();

        // Setup UI
        setupToolbar();
        setupEventTypeDropdown();
        setupTeamDropdown();
        setupDateTimePickers();
        setupBrochureUpload(); // ✅ NEW
        setupSaveButton();

        // Load data if editing
        if (action.equals("edit") || action.equals("view")) {
            loadEventData();
        }

        // Setup UI based on action
        setupUIForAction();
    }

    // ✅ NEW: Initialize brochure file picker
    // ✅ Initialize brochure file picker
    // ✅ Initialize brochure file picker
    private void initBrochurePicker() {
        brochurePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    Log.d("EventScheduleActivity", "File picker result code: " + result.getResultCode());

                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        selectedBrochureUri = result.getData().getData();
                        Log.d("EventScheduleActivity", "Selected URI: " + selectedBrochureUri);

                        if (selectedBrochureUri != null) {
                            // ✅ IMPORTANT: Take persistent permission
                            try {
                                getContentResolver().takePersistableUriPermission(
                                        selectedBrochureUri,
                                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                                );
                                Log.d("EventScheduleActivity", "Persistent permission granted");
                            } catch (Exception e) {
                                Log.e("EventScheduleActivity", "Failed to take persistent permission: " + e.getMessage());
                            }

                            brochurePath = selectedBrochureUri.toString();
                            Log.d("EventScheduleActivity", "Brochure path set to: " + brochurePath);
                            displaySelectedBrochure();
                        } else {
                            Log.e("EventScheduleActivity", "Selected URI is null!");
                        }
                    } else {
                        Log.e("EventScheduleActivity", "File picker cancelled or failed");
                    }
                }
        );
    }


    private void getUserSessionData() {
        SharedPreferences sharedPref = getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        currentUserId = sharedPref.getInt("userId", -1);
        userRole = sharedPref.getString("role", "student");
    }

    private void getIntentData() {
        Intent intent = getIntent();
        action = intent.getStringExtra("action");
        eventId = intent.getIntExtra("eventId", -1);
        if (action == null) action = "create";
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tilEventTitle = findViewById(R.id.til_event_title);
        tilEventType = findViewById(R.id.til_event_type);
        tilEventDescription = findViewById(R.id.til_event_description);
        tilEventDate = findViewById(R.id.til_event_date);
        tilEventTime = findViewById(R.id.til_event_time);
        tilEventLocation = findViewById(R.id.til_event_location);
        tilEventTeam = findViewById(R.id.til_event_team);

        etEventTitle = findViewById(R.id.et_event_title);
        etEventType = findViewById(R.id.et_event_type);
        etEventDescription = findViewById(R.id.et_event_description);
        etEventDate = findViewById(R.id.et_event_date);
        etEventTime = findViewById(R.id.et_event_time);
        etEventLocation = findViewById(R.id.et_event_location);
        etEventTeam = findViewById(R.id.et_event_team);

        btnSaveEvent = findViewById(R.id.btn_save_event);

        // ✅ NEW: Brochure UI elements
        cardBrochure = findViewById(R.id.card_brochure);
        llBrochureSection = findViewById(R.id.ll_brochure_section);
        btnUploadBrochure = findViewById(R.id.btn_upload_brochure);
        btnRemoveBrochure = findViewById(R.id.btn_remove_brochure);
        tvBrochureFileName = findViewById(R.id.tv_brochure_file_name);
        tvBrochureInfo = findViewById(R.id.tv_brochure_info);

        // Initialize calendar
        selectedDateTime = Calendar.getInstance();
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
                toolbar.setTitle("Create Event");
                break;
            case "edit":
                toolbar.setTitle("Edit Event");
                break;
            case "view":
                toolbar.setTitle("Event Details");
                break;
        }
    }

    private void setupEventTypeDropdown() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, eventTypes);
        etEventType.setAdapter(adapter);
    }

    private void setupTeamDropdown() {
        // Load teams based on user role
        if (userRole.equals("admin")) {
            availableTeams = databaseHelper.getAllTeams();
        } else if (userRole.equals("coach")) {
            availableTeams = databaseHelper.getCoachTeams(currentUserId);
        } else {
            availableTeams = new ArrayList<>(); // Students can't create events
        }

        List<String> teamNames = new ArrayList<>();
        teamNames.add("No Team"); // Option for general events
        for (Team team : availableTeams) {
            teamNames.add(team.getName() + " (" + team.getSport() + ")");
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, teamNames);
        etEventTeam.setAdapter(adapter);

        // Show message if coach has no teams
        if (userRole.equals("coach") && availableTeams.isEmpty()) {
            Toast.makeText(this, "Create a team first to schedule events", Toast.LENGTH_LONG).show();
        }
    }

    private void setupDateTimePickers() {
        // Date picker
        etEventDate.setOnClickListener(v -> showDatePicker());

        // Time picker
        etEventTime.setOnClickListener(v -> showTimePicker());

        // Set default date and time (current time + 1 hour)
        selectedDateTime.add(Calendar.HOUR_OF_DAY, 1);
        updateDateTimeFields();
    }

    // ✅ NEW: Setup brochure upload functionality
    private void setupBrochureUpload() {
        btnUploadBrochure.setOnClickListener(v -> openFilePicker());
        btnRemoveBrochure.setOnClickListener(v -> removeBrochure());

        // Initially hide the brochure section
        llBrochureSection.setVisibility(View.GONE);
    }

    // ✅ NEW: Open file picker for PDF
    // ✅ NEW: Open file picker for PDF with persistable permissions
    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT); // ✅ Changed from GET_CONTENT
        intent.setType("application/pdf");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); // ✅ Add this
        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION); // ✅ Add this

        try {
            brochurePickerLauncher.launch(intent); // ✅ No chooser needed
        } catch (Exception e) {
            Toast.makeText(this, "Please install a file manager", Toast.LENGTH_SHORT).show();
        }
    }


    // ✅ NEW: Display selected brochure file
    private void displaySelectedBrochure() {
        if (selectedBrochureUri != null) {
            String fileName = getFileName(selectedBrochureUri);
            tvBrochureFileName.setText(fileName);
            llBrochureSection.setVisibility(View.VISIBLE);
            Toast.makeText(this, "Brochure selected: " + fileName, Toast.LENGTH_SHORT).show();
        }
    }

    // ✅ NEW: Get file name from URI
    private String getFileName(Uri uri) {
        String fileName = "brochure.pdf";
        try {
            String path = uri.getPath();
            if (path != null && path.contains("/")) {
                fileName = path.substring(path.lastIndexOf("/") + 1);
            }
        } catch (Exception e) {
            fileName = "event_brochure.pdf";
        }
        return fileName;
    }

    // ✅ NEW: Remove selected brochure
    private void removeBrochure() {
        selectedBrochureUri = null;
        brochurePath = null;
        llBrochureSection.setVisibility(View.GONE);
        Toast.makeText(this, "Brochure removed", Toast.LENGTH_SHORT).show();
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    selectedDateTime.set(Calendar.YEAR, year);
                    selectedDateTime.set(Calendar.MONTH, month);
                    selectedDateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    updateDateTimeFields();
                },
                selectedDateTime.get(Calendar.YEAR),
                selectedDateTime.get(Calendar.MONTH),
                selectedDateTime.get(Calendar.DAY_OF_MONTH)
        );

        // Don't allow past dates for new events
        if (action.equals("create")) {
            datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        }

        datePickerDialog.show();
    }

    private void showTimePicker() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    selectedDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    selectedDateTime.set(Calendar.MINUTE, minute);
                    updateDateTimeFields();
                },
                selectedDateTime.get(Calendar.HOUR_OF_DAY),
                selectedDateTime.get(Calendar.MINUTE),
                false
        );
        timePickerDialog.show();
    }

    private void updateDateTimeFields() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());

        etEventDate.setText(dateFormat.format(selectedDateTime.getTime()));
        etEventTime.setText(timeFormat.format(selectedDateTime.getTime()));
    }

    private void setupSaveButton() {
        btnSaveEvent.setOnClickListener(v -> saveEvent());
    }

    private void setupUIForAction() {
        if (action.equals("view")) {
            // Make all fields read-only
            setFieldsReadOnly(true);
            btnSaveEvent.setText("Edit Event");
            btnSaveEvent.setOnClickListener(v -> {
                action = "edit";
                setFieldsReadOnly(false);
                btnSaveEvent.setText("Update Event");
                btnSaveEvent.setOnClickListener(v1 -> saveEvent());
                toolbar.setTitle("Edit Event");
            });
        } else if (action.equals("edit")) {
            btnSaveEvent.setText("Update Event");
        }

        // Hide team selection for students
        if (userRole.equals("student")) {
            tilEventTeam.setVisibility(View.GONE);
            cardBrochure.setVisibility(View.GONE); // ✅ Students can't upload brochures
        }
    }

    private void setFieldsReadOnly(boolean readOnly) {
        etEventTitle.setEnabled(!readOnly);
        etEventType.setEnabled(!readOnly);
        etEventDescription.setEnabled(!readOnly);
        etEventDate.setEnabled(!readOnly);
        etEventTime.setEnabled(!readOnly);
        etEventLocation.setEnabled(!readOnly);
        etEventTeam.setEnabled(!readOnly);
        btnUploadBrochure.setEnabled(!readOnly); // ✅ NEW
    }

    private void loadEventData() {
        if (eventId != -1) {
            currentEvent = databaseHelper.getEventById(eventId);

            if (currentEvent != null) {
                populateFields();
            } else {
                Toast.makeText(this, "Event not found", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void populateFields() {
        if (currentEvent != null) {
            etEventTitle.setText(currentEvent.getTitle());
            etEventType.setText(currentEvent.getType(), false);
            etEventDescription.setText(currentEvent.getDescription());
            etEventLocation.setText(currentEvent.getLocation());

            // Parse and set date time
            try {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                selectedDateTime.setTime(format.parse(currentEvent.getDateTime()));
                updateDateTimeFields();
            } catch (Exception e) {
                selectedDateTime = Calendar.getInstance();
                updateDateTimeFields();
            }

            // Set team selection
            if (currentEvent.getTeamName() != null) {
                etEventTeam.setText(currentEvent.getTeamName(), false);
            }

            // ✅ NEW: Load brochure if exists
            if (currentEvent.getBrochurePath() != null && !currentEvent.getBrochurePath().isEmpty()) {
                brochurePath = currentEvent.getBrochurePath();
                selectedBrochureUri = Uri.parse(brochurePath);
                tvBrochureFileName.setText(getFileName(selectedBrochureUri));
                llBrochureSection.setVisibility(View.VISIBLE);
            }
        }
    }

    private void saveEvent() {
        if (!validateInputs()) {
            return;
        }

        // Disable save button during save
        btnSaveEvent.setEnabled(false);
        btnSaveEvent.setText(action.equals("create") ? "Creating..." : "Updating...");

        // Create event object
        Event event = new Event();
        if (action.equals("edit")) {
            event.setId(eventId);
        }

        event.setTitle(etEventTitle.getText().toString().trim());
        event.setType(etEventType.getText().toString());
        event.setDescription(etEventDescription.getText().toString().trim());
        event.setLocation(etEventLocation.getText().toString().trim());

        // Format date time for database
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        event.setDateTime(format.format(selectedDateTime.getTime()));

        // Set team ID if selected
        String selectedTeam = etEventTeam.getText().toString();
        if (!selectedTeam.equals("No Team") && !TextUtils.isEmpty(selectedTeam)) {
            for (Team team : availableTeams) {
                String teamDisplay = team.getName() + " (" + team.getSport() + ")";
                if (teamDisplay.equals(selectedTeam)) {
                    event.setTeamId(team.getId());
                    break;
                }
            }
        }

        event.setCreatedBy(currentUserId);

        // ✅ NEW: Set brochure path
        event.setBrochurePath(brochurePath);

        // Save to database
        boolean success;
        if (action.equals("create")) {
            success = databaseHelper.createEventWithGlobalNotifications(this, event) > 0;
        } else {
            success = databaseHelper.updateEvent(event);
        }

        if (success) {
            String message = action.equals("create") ? "Event created and team notified!" : "Event updated successfully!";
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

            // Return to previous screen
            setResult(RESULT_OK);
            finish();
        } else {
            String message = action.equals("create") ? "Failed to create event" : "Failed to update event";
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

            // Re-enable save button
            btnSaveEvent.setEnabled(true);
            btnSaveEvent.setText(action.equals("create") ? "Create Event" : "Update Event");
        }
    }

    private boolean validateInputs() {
        // Clear previous errors
        tilEventTitle.setError(null);
        tilEventType.setError(null);
        tilEventLocation.setError(null);

        String title = etEventTitle.getText().toString().trim();
        String type = etEventType.getText().toString();
        String location = etEventLocation.getText().toString().trim();

        // Validate title
        if (TextUtils.isEmpty(title)) {
            tilEventTitle.setError("Event title is required");
            etEventTitle.requestFocus();
            return false;
        }

        if (title.length() < 3) {
            tilEventTitle.setError("Event title must be at least 3 characters");
            etEventTitle.requestFocus();
            return false;
        }

        // Validate type
        if (TextUtils.isEmpty(type)) {
            tilEventType.setError("Please select event type");
            etEventType.requestFocus();
            return false;
        }

        // Validate location
        if (TextUtils.isEmpty(location)) {
            tilEventLocation.setError("Location is required");
            etEventLocation.requestFocus();
            return false;
        }

        // Check if date/time is in the future (for new events)
        if (action.equals("create") && selectedDateTime.getTimeInMillis() < System.currentTimeMillis()) {
            Toast.makeText(this, "Please select a future date and time", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (action.equals("edit") || action.equals("view")) {
            getMenuInflater().inflate(R.menu.menu_event_edit, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (item.getItemId() == R.id.action_delete_event) {
            showDeleteConfirmation();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showDeleteConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Event")
                .setMessage("Are you sure you want to delete this event? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    if (databaseHelper.deleteEvent(eventId)) {
                        Toast.makeText(this, "Event deleted successfully", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        Toast.makeText(this, "Failed to delete event", Toast.LENGTH_SHORT).show();
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
//import android.app.DatePickerDialog;
//import android.app.TimePickerDialog;
//import android.content.Context;
//import android.content.Intent;
//import android.content.SharedPreferences;
//import android.os.Bundle;
//import android.text.TextUtils;
//import android.view.Menu;
//import android.view.MenuItem;
//import android.widget.ArrayAdapter;
//import android.widget.AutoCompleteTextView;
//import android.widget.Toast;
//
//import androidx.appcompat.app.AlertDialog;
//import androidx.appcompat.app.AppCompatActivity;
//
//import com.example.guarena.R;
//import com.example.guarena.database.DatabaseHelper;
//import com.example.guarena.models.Event;
//import com.example.guarena.models.Team;
//import com.google.android.material.appbar.MaterialToolbar;
//import com.google.android.material.button.MaterialButton;
//import com.google.android.material.textfield.TextInputEditText;
//import com.google.android.material.textfield.TextInputLayout;
//
//import java.text.SimpleDateFormat;
//import java.util.ArrayList;
//import java.util.Calendar;
//import java.util.List;
//import java.util.Locale;
//
//public class EventScheduleActivity extends AppCompatActivity {
//
//    private MaterialToolbar toolbar;
//    private TextInputLayout tilEventTitle, tilEventType, tilEventDescription;
//    private TextInputLayout tilEventDate, tilEventTime, tilEventLocation, tilEventTeam;
//    private TextInputEditText etEventTitle, etEventDescription, etEventDate, etEventTime, etEventLocation;
//    private AutoCompleteTextView etEventType, etEventTeam;
//    private MaterialButton btnSaveEvent;
//
//    private DatabaseHelper databaseHelper;
//    private String userRole;
//    private int currentUserId;
//    private String action; // "create", "edit", "view"
//    private int eventId = -1;
//    private Event currentEvent;
//
//    private Calendar selectedDateTime;
//    private List<Team> availableTeams;
//
//    // Event types
//    private String[] eventTypes = {"Practice", "Match", "Tournament"};
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_event_schedule);
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
//        setupEventTypeDropdown();
//        setupTeamDropdown();
//        setupDateTimePickers();
//        setupSaveButton();
//
//        // Load data if editing
//        if (action.equals("edit") || action.equals("view")) {
//            loadEventData();
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
//        eventId = intent.getIntExtra("eventId", -1);
//
//        if (action == null) action = "create";
//    }
//
//    private void initViews() {
//        toolbar = findViewById(R.id.toolbar);
//        tilEventTitle = findViewById(R.id.til_event_title);
//        tilEventType = findViewById(R.id.til_event_type);
//        tilEventDescription = findViewById(R.id.til_event_description);
//        tilEventDate = findViewById(R.id.til_event_date);
//        tilEventTime = findViewById(R.id.til_event_time);
//        tilEventLocation = findViewById(R.id.til_event_location);
//        tilEventTeam = findViewById(R.id.til_event_team);
//
//        etEventTitle = findViewById(R.id.et_event_title);
//        etEventType = findViewById(R.id.et_event_type);
//        etEventDescription = findViewById(R.id.et_event_description);
//        etEventDate = findViewById(R.id.et_event_date);
//        etEventTime = findViewById(R.id.et_event_time);
//        etEventLocation = findViewById(R.id.et_event_location);
//        etEventTeam = findViewById(R.id.et_event_team);
//
//        btnSaveEvent = findViewById(R.id.btn_save_event);
//
//        // Initialize calendar
//        selectedDateTime = Calendar.getInstance();
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
//                toolbar.setTitle("Create Event");
//                break;
//            case "edit":
//                toolbar.setTitle("Edit Event");
//                break;
//            case "view":
//                toolbar.setTitle("Event Details");
//                break;
//        }
//    }
//
//    private void setupEventTypeDropdown() {
//        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
//                android.R.layout.simple_dropdown_item_1line, eventTypes);
//        etEventType.setAdapter(adapter);
//    }
//    private void setupTeamDropdown() {
//        // Load teams based on user role - PRODUCTION READY
//        if (userRole.equals("admin")) {
//            availableTeams = databaseHelper.getAllTeams();
//        } else if (userRole.equals("coach")) {
//            availableTeams = databaseHelper.getCoachTeams(currentUserId);
//        } else {
//            availableTeams = new ArrayList<>(); // Students can't create events
//        }
//
//        List<String> teamNames = new ArrayList<>();
//        teamNames.add("No Team"); // Option for general events
//        for (Team team : availableTeams) {
//            teamNames.add(team.getName() + " (" + team.getSport() + ")");
//        }
//
//        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
//                android.R.layout.simple_dropdown_item_1line, teamNames);
//        etEventTeam.setAdapter(adapter);
//
//        // Show message if coach has no teams
//        if (userRole.equals("coach") && availableTeams.isEmpty()) {
//            Toast.makeText(this, "Create a team first to schedule events", Toast.LENGTH_LONG).show();
//        }
//    }
//
////    private void setupTeamDropdown() {
////        // Load teams based on user role
////        if (userRole.equals("admin")) {
////            availableTeams = databaseHelper.getAllTeams();
////        } else if (userRole.equals("coach")) {
////            availableTeams = databaseHelper.getCoachTeams(currentUserId);
////        } else {
////            availableTeams = new ArrayList<>(); // Students can't create events
////        }
////
////        List<String> teamNames = new ArrayList<>();
////        teamNames.add("No Team"); // Option for general events
////
////        for (Team team : availableTeams) {
////            teamNames.add(team.getName());
////        }
////
////        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
////                android.R.layout.simple_dropdown_item_1line, teamNames);
////        etEventTeam.setAdapter(adapter);
////    }
//
//    private void setupDateTimePickers() {
//        // Date picker
//        etEventDate.setOnClickListener(v -> showDatePicker());
//
//        // Time picker
//        etEventTime.setOnClickListener(v -> showTimePicker());
//
//        // Set default date and time (current time + 1 hour)
//        selectedDateTime.add(Calendar.HOUR_OF_DAY, 1);
//        updateDateTimeFields();
//    }
//
//    private void showDatePicker() {
//        DatePickerDialog datePickerDialog = new DatePickerDialog(
//                this,
//                (view, year, month, dayOfMonth) -> {
//                    selectedDateTime.set(Calendar.YEAR, year);
//                    selectedDateTime.set(Calendar.MONTH, month);
//                    selectedDateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
//                    updateDateTimeFields();
//                },
//                selectedDateTime.get(Calendar.YEAR),
//                selectedDateTime.get(Calendar.MONTH),
//                selectedDateTime.get(Calendar.DAY_OF_MONTH)
//        );
//
//        // Don't allow past dates for new events
//        if (action.equals("create")) {
//            datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
//        }
//
//        datePickerDialog.show();
//    }
//
//    private void showTimePicker() {
//        TimePickerDialog timePickerDialog = new TimePickerDialog(
//                this,
//                (view, hourOfDay, minute) -> {
//                    selectedDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
//                    selectedDateTime.set(Calendar.MINUTE, minute);
//                    updateDateTimeFields();
//                },
//                selectedDateTime.get(Calendar.HOUR_OF_DAY),
//                selectedDateTime.get(Calendar.MINUTE),
//                false
//        );
//
//        timePickerDialog.show();
//    }
//
//    private void updateDateTimeFields() {
//        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
//        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
//
//        etEventDate.setText(dateFormat.format(selectedDateTime.getTime()));
//        etEventTime.setText(timeFormat.format(selectedDateTime.getTime()));
//    }
//
//    private void setupSaveButton() {
//        btnSaveEvent.setOnClickListener(v -> saveEvent());
//    }
//
//    private void setupUIForAction() {
//        if (action.equals("view")) {
//            // Make all fields read-only
//            setFieldsReadOnly(true);
//            btnSaveEvent.setText("Edit Event");
//            btnSaveEvent.setOnClickListener(v -> {
//                action = "edit";
//                setFieldsReadOnly(false);
//                btnSaveEvent.setText("Update Event");
//                btnSaveEvent.setOnClickListener(v1 -> saveEvent());
//                toolbar.setTitle("Edit Event");
//            });
//        } else if (action.equals("edit")) {
//            btnSaveEvent.setText("Update Event");
//        }
//
//        // Hide team selection for students
//        if (userRole.equals("student")) {
//            tilEventTeam.setVisibility(android.view.View.GONE);
//        }
//    }
//
//    private void setFieldsReadOnly(boolean readOnly) {
//        etEventTitle.setEnabled(!readOnly);
//        etEventType.setEnabled(!readOnly);
//        etEventDescription.setEnabled(!readOnly);
//        etEventDate.setEnabled(!readOnly);
//        etEventTime.setEnabled(!readOnly);
//        etEventLocation.setEnabled(!readOnly);
//        etEventTeam.setEnabled(!readOnly);
//    }
//    private void loadEventData() {
//        if (eventId != -1) {
//            // PRODUCTION READY: Load actual event from database
//            currentEvent = databaseHelper.getEventById(eventId);
//
//            if (currentEvent != null) {
//                populateFields();
//            } else {
//                Toast.makeText(this, "Event not found", Toast.LENGTH_SHORT).show();
//                finish();
//            }
//        }
//    }
//
////    private void loadEventData() {
////        if (eventId != -1) {
////            // TODO: Load event from database
////            // currentEvent = databaseHelper.getEventById(eventId);
////            // For now, create a dummy event
////            currentEvent = new Event();
////            currentEvent.setId(eventId);
////            currentEvent.setTitle("Sample Event");
////            currentEvent.setEventType("Practice");
////            currentEvent.setDescription("Sample description");
////            currentEvent.setLocation("Main Ground");
////            currentEvent.setDateTime("2024-12-15 16:00");
////
////            populateFields();
////        }
////    }
//
//    private void populateFields() {
//        if (currentEvent != null) {
//            etEventTitle.setText(currentEvent.getTitle());
//            etEventType.setText(currentEvent.getEventType(), false);
//            etEventDescription.setText(currentEvent.getDescription());
//            etEventLocation.setText(currentEvent.getLocation());
//
//            // Parse and set date time
//            try {
//                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
//                selectedDateTime.setTime(format.parse(currentEvent.getDateTime()));
//                updateDateTimeFields();
//            } catch (Exception e) {
//                // Use current time if parsing fails
//                selectedDateTime = Calendar.getInstance();
//                updateDateTimeFields();
//            }
//        }
//    }
//
//    private void saveEvent() {
//        if (!validateInputs()) {
//            return;
//        }
//
//        // Disable save button during save
//        btnSaveEvent.setEnabled(false);
//        btnSaveEvent.setText(action.equals("create") ? "Creating..." : "Updating...");
//
//        // Create event object
//        Event event = new Event();
//        if (action.equals("edit")) {
//            event.setId(eventId);
//        }
//
//        event.setTitle(etEventTitle.getText().toString().trim());
//        event.setEventType(etEventType.getText().toString());
//        event.setDescription(etEventDescription.getText().toString().trim());
//        event.setLocation(etEventLocation.getText().toString().trim());
//
//        // Format date time for database
//        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
//        event.setDateTime(format.format(selectedDateTime.getTime()));
//
//        // Set team ID if selected
//        String selectedTeam = etEventTeam.getText().toString();
//        if (!selectedTeam.equals("No Team") && !TextUtils.isEmpty(selectedTeam)) {
//            for (Team team : availableTeams) {
//                if (team.getName().equals(selectedTeam)) {
//                    event.setTeamId(team.getId());
//                    break;
//                }
//            }
//        }
//
//        event.setCreatedBy(currentUserId);
//
//        // Save to database
//        boolean success;
//        if (action.equals("create")) {
//            success = databaseHelper.createEvent(event) > 0;
//        } else {
//            success = databaseHelper.updateEvent(event);
//        }
//
//        if (success) {
//            String message = action.equals("create") ? "Event created successfully!" : "Event updated successfully!";
//            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
//
//            // Return to previous screen
//            setResult(RESULT_OK);
//            finish();
//        } else {
//            String message = action.equals("create") ? "Failed to create event" : "Failed to update event";
//            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
//
//            // Re-enable save button
//            btnSaveEvent.setEnabled(true);
//            btnSaveEvent.setText(action.equals("create") ? "Create Event" : "Update Event");
//        }
//    }
//
//    private boolean validateInputs() {
//        // Clear previous errors
//        tilEventTitle.setError(null);
//        tilEventType.setError(null);
//        tilEventLocation.setError(null);
//
//        String title = etEventTitle.getText().toString().trim();
//        String type = etEventType.getText().toString();
//        String location = etEventLocation.getText().toString().trim();
//
//        // Validate title
//        if (TextUtils.isEmpty(title)) {
//            tilEventTitle.setError("Event title is required");
//            etEventTitle.requestFocus();
//            return false;
//        }
//
//        if (title.length() < 3) {
//            tilEventTitle.setError("Event title must be at least 3 characters");
//            etEventTitle.requestFocus();
//            return false;
//        }
//
//        // Validate type
//        if (TextUtils.isEmpty(type)) {
//            tilEventType.setError("Please select event type");
//            etEventType.requestFocus();
//            return false;
//        }
//
//        // Validate location
//        if (TextUtils.isEmpty(location)) {
//            tilEventLocation.setError("Location is required");
//            etEventLocation.requestFocus();
//            return false;
//        }
//
//        // Check if date/time is in the future (for new events)
//        if (action.equals("create") && selectedDateTime.getTimeInMillis() < System.currentTimeMillis()) {
//            Toast.makeText(this, "Please select a future date and time", Toast.LENGTH_SHORT).show();
//            return false;
//        }
//
//        return true;
//    }
//
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        if (action.equals("edit") || action.equals("view")) {
//            getMenuInflater().inflate(R.menu.menu_event_edit, menu);
//        }
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        if (item.getItemId() == android.R.id.home) {
//            onBackPressed();
//            return true;
//        } else if (item.getItemId() == R.id.action_delete_event) {
//            showDeleteConfirmation();
//            return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }
//
//    private void showDeleteConfirmation() {
//        new AlertDialog.Builder(this)
//                .setTitle("Delete Event")
//                .setMessage("Are you sure you want to delete this event? This action cannot be undone.")
//                .setPositiveButton("Delete", (dialog, which) -> {
//                    if (databaseHelper.deleteEvent(eventId)) {
//                        Toast.makeText(this, "Event deleted successfully", Toast.LENGTH_SHORT).show();
//                        setResult(RESULT_OK);
//                        finish();
//                    } else {
//                        Toast.makeText(this, "Failed to delete event", Toast.LENGTH_SHORT).show();
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
