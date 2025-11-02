package com.example.guarena.activities;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.guarena.R;
import com.example.guarena.adapters.TeamMemberAdapter;
import com.example.guarena.database.DatabaseHelper;
import com.example.guarena.models.Event;
import com.example.guarena.models.User;
import com.example.guarena.utils.EmailService;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EventDetailsActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private TextView tvEventTitle, tvEventDescription, tvEventDate, tvEventTime;
    private TextView tvEventLocation, tvEventType, tvTeamName, tvCoachName;
    private Chip chipEventType;
    private MaterialButton btnParticipate, btnViewBrochure, btnEdit;
    private RecyclerView rvTeamMembers;
    private LinearLayout llTeamMembersSection, llBrochureSection;
    private MaterialCardView cardTeamMembers;

    private DatabaseHelper databaseHelper;
    private TeamMemberAdapter teamMemberAdapter;

    private Event currentEvent;
    private String currentUserRole;
    private int currentUserId;
    private String currentUserName, currentUserEmail;
    private int eventId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);

        // Initialize database
        databaseHelper = new DatabaseHelper(this);

        // Get user session data
        getUserSessionData();

        // Get event ID from intent
        eventId = getIntent().getIntExtra("eventId", -1);

        if (eventId == -1) {
            Toast.makeText(this, "Invalid event", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize views
        initViews();

        // Setup toolbar
        setupToolbar();

        // Load event details
        loadEventDetails();
    }

    private void getUserSessionData() {
        SharedPreferences sharedPref = getSharedPreferences("UserSession", MODE_PRIVATE);
        currentUserId = sharedPref.getInt("userId", -1);
        currentUserRole = sharedPref.getString("role", "student");
        currentUserName = sharedPref.getString("fullName", "User");
        currentUserEmail = sharedPref.getString("email", "");
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tvEventTitle = findViewById(R.id.tv_event_title);
        tvEventDescription = findViewById(R.id.tv_event_description);
        tvEventDate = findViewById(R.id.tv_event_date);
        tvEventTime = findViewById(R.id.tv_event_time);
        tvEventLocation = findViewById(R.id.tv_event_location);
        tvEventType = findViewById(R.id.tv_event_type);
        tvTeamName = findViewById(R.id.tv_team_name);
        tvCoachName = findViewById(R.id.tv_coach_name);
        chipEventType = findViewById(R.id.chip_event_type);
        btnParticipate = findViewById(R.id.btn_participate);
        btnViewBrochure = findViewById(R.id.btn_view_brochure);
        btnEdit = findViewById(R.id.btn_edit);
        rvTeamMembers = findViewById(R.id.rv_team_members);
        llTeamMembersSection = findViewById(R.id.ll_team_members_section);
        llBrochureSection = findViewById(R.id.ll_brochure_section);
        cardTeamMembers = findViewById(R.id.card_team_members);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Event Details");
        }
    }

    private void loadEventDetails() {
        currentEvent = databaseHelper.getEventById(eventId);

        if (currentEvent == null) {
            Toast.makeText(this, "Event not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Display event information
        displayEventInfo();

        // Show/hide buttons based on user role
        setupButtonsForRole();

        // Load team members if event has a team
        if (currentEvent.getTeamId() > 0) {
            loadTeamMembers();
        } else {
            llTeamMembersSection.setVisibility(View.GONE);
        }

        // Handle brochure display
        if (currentEvent.getBrochurePath() != null && !currentEvent.getBrochurePath().isEmpty()) {
            llBrochureSection.setVisibility(View.VISIBLE);
            btnViewBrochure.setOnClickListener(v -> openBrochure());
        } else {
            llBrochureSection.setVisibility(View.GONE);
        }
    }

    private void displayEventInfo() {
        tvEventTitle.setText(currentEvent.getTitle());
        tvEventDescription.setText(currentEvent.getDescription());
        tvEventLocation.setText(currentEvent.getLocation());
        tvEventType.setText(currentEvent.getType());
        chipEventType.setText(currentEvent.getType());

        // Set chip color based on event type
        setChipColor(currentEvent.getType());

        // Parse and format date/time
        formatDateTime(currentEvent.getDateTime());

        // Display team name if available
        if (currentEvent.getTeamName() != null && !currentEvent.getTeamName().isEmpty()) {
            tvTeamName.setText(currentEvent.getTeamName());
        } else {
            tvTeamName.setText("General Event");
        }

        // Display coach/creator name
        if (currentEvent.getCreatedByName() != null) {
            tvCoachName.setText("Organized by: " + currentEvent.getCreatedByName());
        }
    }

    private void setChipColor(String eventType) {
        if (eventType == null) return;

        switch (eventType.toLowerCase()) {
            case "match":
                chipEventType.setChipBackgroundColorResource(R.color.event_match);
                break;
            case "practice":
                chipEventType.setChipBackgroundColorResource(R.color.event_practice);
                break;
            case "tournament":
                chipEventType.setChipBackgroundColorResource(R.color.event_tournament);
                break;
            case "meeting":
                chipEventType.setChipBackgroundColorResource(R.color.event_meeting);
                break;
            default:
                chipEventType.setChipBackgroundColorResource(R.color.primary);
                break;
        }
    }

    private void formatDateTime(String dateTimeString) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date date = inputFormat.parse(dateTimeString);

            if (date != null) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMM dd, yyyy", Locale.getDefault());
                SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());

                tvEventDate.setText(dateFormat.format(date));
                tvEventTime.setText(timeFormat.format(date));
            }
        } catch (ParseException e) {
            tvEventDate.setText(dateTimeString);
            tvEventTime.setText("");
        }
    }

    private void setupButtonsForRole() {
        if ("student".equals(currentUserRole)) {
            // Students see participate button, not edit button
            btnParticipate.setVisibility(View.VISIBLE);
            btnEdit.setVisibility(View.GONE);

            btnParticipate.setOnClickListener(v -> sendParticipationRequest());
        } else {
            // Coaches and admins see edit button, not participate button
            btnParticipate.setVisibility(View.GONE);
            btnEdit.setVisibility(View.VISIBLE);

            btnEdit.setOnClickListener(v -> editEvent());
        }
    }

    private void loadTeamMembers() {
        List<User> teamMembers = databaseHelper.getTeamMembersWithDetails(currentEvent.getTeamId());

        if (teamMembers != null && !teamMembers.isEmpty()) {
            llTeamMembersSection.setVisibility(View.VISIBLE);
            teamMemberAdapter = new TeamMemberAdapter(teamMembers);
            rvTeamMembers.setLayoutManager(new LinearLayoutManager(this));
            rvTeamMembers.setAdapter(teamMemberAdapter);
        } else {
            llTeamMembersSection.setVisibility(View.GONE);
        }
    }

    private void sendParticipationRequest() {
        // Get coach email
        User coach = databaseHelper.getUserById(currentEvent.getCreatedBy());

        if (coach == null || coach.getEmail() == null) {
            Toast.makeText(this, "Unable to send request. Coach information not available.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Prepare email content
        String subject = "Event Participation Request - " + currentEvent.getTitle();
        String body = createParticipationEmailBody(coach.getFullName());

        // Send email via intent
        sendEmailViaIntent(coach.getEmail(), subject, body);
    }

    private String createParticipationEmailBody(String coachName) {
        return "<!DOCTYPE html>" +
                "<html><body style='font-family: Arial, sans-serif;'>" +
                "<h2 style='color: #2196F3;'>Event Participation Request</h2>" +
                "<p>Dear " + coachName + ",</p>" +
                "<p>I would like to participate in the following event:</p>" +
                "<div style='background-color: #f5f5f5; padding: 15px; border-radius: 8px; margin: 20px 0;'>" +
                "<h3 style='margin: 0; color: #2196F3;'>Event Details</h3>" +
                "<p><strong>Event:</strong> " + currentEvent.getTitle() + "</p>" +
                "<p><strong>Date & Time:</strong> " + currentEvent.getDateTime() + "</p>" +
                "<p><strong>Location:</strong> " + currentEvent.getLocation() + "</p>" +
                "<p><strong>Type:</strong> " + currentEvent.getType() + "</p>" +
                "</div>" +
                "<div style='background-color: #e3f2fd; padding: 15px; border-radius: 8px; margin: 20px 0;'>" +
                "<h3 style='margin: 0; color: #1976d2;'>Student Information</h3>" +
                "<p><strong>Name:</strong> " + currentUserName + "</p>" +
                "<p><strong>Email:</strong> " + currentUserEmail + "</p>" +
                "</div>" +
                "<p>Please confirm my participation or contact me for more details.</p>" +
                "<p>Best regards,<br><strong>" + currentUserName + "</strong></p>" +
                "</body></html>";
    }

    private void sendEmailViaIntent(String toEmail, String subject, String body) {
        try {
            Intent emailIntent = new Intent(Intent.ACTION_SEND);
            emailIntent.setType("message/rfc822");
            emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{toEmail});
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
            emailIntent.putExtra(Intent.EXTRA_TEXT, android.text.Html.fromHtml(body));

            startActivity(Intent.createChooser(emailIntent, "Send participation request"));
            Toast.makeText(this, "Opening email app...", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Could not open email app: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void openBrochure() {
        if (currentEvent.getBrochurePath() == null || currentEvent.getBrochurePath().isEmpty()) {
            Toast.makeText(this, "No brochure available", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            Uri brochureUri = Uri.parse(currentEvent.getBrochurePath());
            Log.d("EventDetailsActivity", "Opening brochure URI: " + brochureUri);

            // ✅ Create intent with proper permissions
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(brochureUri, "application/pdf");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            try {
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                // No PDF viewer found
                Toast.makeText(this, "No PDF viewer installed. Please install Adobe Reader or Google Drive.", Toast.LENGTH_LONG).show();
            } catch (SecurityException e) {
                // Permission issue - try to re-grant permission
                Log.e("EventDetailsActivity", "SecurityException: " + e.getMessage());

                // Try opening with chooser as fallback
                try {
                    Intent chooserIntent = Intent.createChooser(intent, "Open PDF with");
                    chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(chooserIntent);
                } catch (Exception ex) {
                    Toast.makeText(this, "Unable to open brochure. Please ask the coach to re-upload it.", Toast.LENGTH_LONG).show();
                }
            }
        } catch (Exception e) {
            Log.e("EventDetailsActivity", "Error opening brochure: " + e.getMessage());
            e.printStackTrace();
            Toast.makeText(this, "Unable to open brochure", Toast.LENGTH_SHORT).show();
        }
    }




    private void editEvent() {
        Intent intent = new Intent(this, EventScheduleActivity.class);
        intent.putExtra("eventId", eventId);
        intent.putExtra("action", "edit");
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
