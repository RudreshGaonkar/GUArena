//package com.example.guarena.activities;
//
//import android.content.Context;
//import android.content.Intent;
//import android.content.SharedPreferences;
//import android.os.Bundle;
//import android.widget.Toast;
//
//import androidx.annotation.Nullable;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.example.guarena.R;
//import com.example.guarena.adapters.EventAdapter;
//import com.example.guarena.database.DatabaseHelper;
//import com.example.guarena.models.Event;
//import com.google.android.material.floatingactionbutton.FloatingActionButton;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class EventsActivity extends AppCompatActivity implements EventAdapter.OnEventClickListener {
//
//    private RecyclerView recyclerViewEvents;
//    private EventAdapter eventAdapter;
//    private List<Event> eventList;
//    private DatabaseHelper dbHelper;
//    private FloatingActionButton fabAddEvent;
//    private String userRole;
//
//    @Override
//    protected void onCreate(@Nullable Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_events);
//
//        // Get user role from session
//        getUserRole();
//
//        // Initialize components
//        dbHelper = new DatabaseHelper(this);
//        recyclerViewEvents = findViewById(R.id.recyclerViewEvents);
//        fabAddEvent = findViewById(R.id.fabAddEvent);
//
//        // Setup RecyclerView
//        recyclerViewEvents.setLayoutManager(new LinearLayoutManager(this));
//
//        // Setup FAB click listener
//        fabAddEvent.setOnClickListener(v -> {
//            // TODO: Intent to open AddEditEventActivity
//            Toast.makeText(this, "Add Event feature coming soon!", Toast.LENGTH_SHORT).show();
//        });
//
//        // Show/hide FAB based on user role
//        if (userRole.equals("coach") || userRole.equals("admin")) {
//            fabAddEvent.setVisibility(android.view.View.VISIBLE);
//        } else {
//            fabAddEvent.setVisibility(android.view.View.GONE);
//        }
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        loadEvents();
//    }
//
//    private void getUserRole() {
//        SharedPreferences sharedPref = getSharedPreferences("UserSession", Context.MODE_PRIVATE);
//        userRole = sharedPref.getString("role", "student");
//    }
//
//    private void loadEvents() {
//        eventList = dbHelper.getAllEvents();
//        if (eventAdapter == null) {
//            // ✅ FIXED: Pass all 3 required parameters
//            eventAdapter = new EventAdapter(eventList, this, userRole);
//            recyclerViewEvents.setAdapter(eventAdapter);
//        } else {
//            eventAdapter.updateEvents(eventList);
//        }
//    }
//
//    // ✅ IMPLEMENT OnEventClickListener METHODS
//    @Override
//    public void onEventClick(Event event) {
//        // Handle event click - show event details
//        Toast.makeText(this, "Event: " + event.getTitle(), Toast.LENGTH_SHORT).show();
//        // TODO: Open event details activity
//    }
//
//    @Override
//    public void onEventEdit(Event event) {
//        // Handle event edit
//        Toast.makeText(this, "Edit: " + event.getTitle(), Toast.LENGTH_SHORT).show();
//        // TODO: Open edit event activity
//    }
//
//    @Override
//    public void onEventDelete(Event event) {
//        // Handle event delete with confirmation
//        new androidx.appcompat.app.AlertDialog.Builder(this)
//                .setTitle("Delete Event")
//                .setMessage("Are you sure you want to delete \"" + event.getTitle() + "\"?")
//                .setPositiveButton("Delete", (dialog, which) -> {
//                    if (dbHelper.deleteEvent(event.getId())) {
//                        Toast.makeText(this, "Event deleted successfully", Toast.LENGTH_SHORT).show();
//                        loadEvents(); // Refresh the list
//                    } else {
//                        Toast.makeText(this, "Failed to delete event", Toast.LENGTH_SHORT).show();
//                    }
//                })
//                .setNegativeButton("Cancel", null)
//                .show();
//    }
//}
package com.example.guarena.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.guarena.R;
import com.example.guarena.adapters.EventAdapter;
import com.example.guarena.database.DatabaseHelper;
import com.example.guarena.models.Event;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class EventsActivity extends AppCompatActivity implements EventAdapter.OnEventClickListener {

    private RecyclerView recyclerViewEvents;
    private EventAdapter eventAdapter;
    private List<Event> eventList;
    private DatabaseHelper dbHelper;
    private FloatingActionButton fabAddEvent;
    private MaterialToolbar toolbar;

    private String userRole;
    private int currentUserId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events);

        // Get user session data
        getUserSessionData();

        // Initialize components
        initViews();
        setupToolbar();
        setupRecyclerView();
        setupFAB();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadEvents();
    }

    private void getUserSessionData() {
        SharedPreferences sharedPref = getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        userRole = sharedPref.getString("role", "student");
        currentUserId = sharedPref.getInt("userId", -1);
    }

    private void initViews() {
        dbHelper = new DatabaseHelper(this);
        recyclerViewEvents = findViewById(R.id.recyclerViewEvents);
        fabAddEvent = findViewById(R.id.fabAddEvent);
        toolbar = findViewById(R.id.toolbar);
        eventList = new ArrayList<>();
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Events");
        }

        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupRecyclerView() {
        recyclerViewEvents.setLayoutManager(new LinearLayoutManager(this));
        eventAdapter = new EventAdapter(eventList, this, userRole);
        recyclerViewEvents.setAdapter(eventAdapter);
    }

    private void setupFAB() {
        // Show FAB only for coaches and admins
        if (userRole.equals("coach") || userRole.equals("admin")) {
            fabAddEvent.setVisibility(View.VISIBLE);
            fabAddEvent.setOnClickListener(v -> openEventScheduleActivity("create"));
        } else {
            fabAddEvent.setVisibility(View.GONE);
        }
    }

    private void loadEvents() {
        // Load events based on user role - PRODUCTION READY
        switch (userRole) {
            case "admin":
                // Admin sees all events
                eventList = dbHelper.getAllEvents();
                break;
            case "coach":
                // Coach sees only their events
                eventList = dbHelper.getCoachEvents(currentUserId);
                break;
            case "student":
                // Student sees only events for their teams
                eventList = dbHelper.getStudentEvents(currentUserId);
                break;
            default:
                eventList = new ArrayList<>();
        }

        if (eventAdapter == null) {
            eventAdapter = new EventAdapter(eventList, this, userRole);
            recyclerViewEvents.setAdapter(eventAdapter);
        } else {
            eventAdapter.updateEvents(eventList);
        }

        // Show appropriate message if no events
        if (eventList.isEmpty()) {
            showNoEventsMessage();
        }
    }

    private void showNoEventsMessage() {
        String message;
        switch (userRole) {
            case "coach":
                message = "No events created yet. Tap + to create your first event.";
                break;
            case "student":
                message = "No events for your teams yet. Join a team to see events!";
                break;
            default:
                message = "No events available.";
        }
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void openEventScheduleActivity(String action) {
        Intent intent = new Intent(this, EventScheduleActivity.class);
        intent.putExtra("action", action);
        startActivityForResult(intent, 100);
    }

    private void openEventScheduleActivity(String action, int eventId) {
        Intent intent = new Intent(this, EventScheduleActivity.class);
        intent.putExtra("action", action);
        intent.putExtra("eventId", eventId);
        startActivityForResult(intent, 100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK) {
            // Refresh events list when returning from EventScheduleActivity
            loadEvents();
        }
    }

    // EventAdapter.OnEventClickListener methods
    @Override
    public void onEventClick(Event event) {
        // Open event details in view mode
        openEventScheduleActivity("view", event.getId());
    }

    @Override
    public void onEventEdit(Event event) {
        // Only coaches and admins can edit
        if (userRole.equals("coach") || userRole.equals("admin")) {
            // Coaches can only edit their own events
            if (userRole.equals("coach") && event.getCreatedBy() != currentUserId) {
                Toast.makeText(this, "You can only edit events you created", Toast.LENGTH_SHORT).show();
                return;
            }
            openEventScheduleActivity("edit", event.getId());
        } else {
            Toast.makeText(this, "You don't have permission to edit events", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onEventDelete(Event event) {
        // Only coaches can delete their own events, admins can delete any
        boolean canDelete = false;

        if (userRole.equals("admin")) {
            canDelete = true;
        } else if (userRole.equals("coach") && event.getCreatedBy() == currentUserId) {
            canDelete = true;
        }

        if (canDelete) {
            showDeleteConfirmation(event);
        } else {
            Toast.makeText(this, "You don't have permission to delete this event", Toast.LENGTH_SHORT).show();
        }
    }

    private void showDeleteConfirmation(Event event) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Delete Event")
                .setMessage("Are you sure you want to delete \"" + event.getTitle() + "\"?\n\nThis will affect all team members who were participating.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    if (dbHelper.deleteEvent(event.getId())) {
                        Toast.makeText(this, "Event deleted successfully", Toast.LENGTH_SHORT).show();
                        loadEvents(); // Refresh the list
                    } else {
                        Toast.makeText(this, "Failed to delete event", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
}
