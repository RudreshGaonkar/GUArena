package com.example.guarena.fragments;

import android.app.DatePickerDialog;
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
import com.example.guarena.activities.EventDetailsActivity;
import com.example.guarena.activities.EventScheduleActivity;
import com.example.guarena.adapters.EventAdapter;
import com.example.guarena.database.DatabaseHelper;
import com.example.guarena.models.Event;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class EventsFragment extends Fragment implements EventAdapter.OnEventClickListener {

    private View rootView;
    private TabLayout tabLayout;
    private TextInputEditText etSearch;
    private MaterialButton btnFilter, btnCreateFirstEvent;
    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView rvEvents;
    private LinearLayout llEmptyState;
    private TextView tvEmptyMessage;
    private FloatingActionButton fabAddEvent;

    private DatabaseHelper databaseHelper;
    private EventAdapter eventAdapter;
    private List<Event> allEvents;
    private List<Event> filteredEvents;

    private String currentUserRole;
    private int currentUserId;
    private String currentFilter = "All";
    private String searchQuery = "";
    private String dateFilter = "";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_events, container, false);

        // Initialize database
        databaseHelper = new DatabaseHelper(getContext());

        // Get user session data
        getUserSessionData();

        // Initialize views
        initViews();

        // Setup UI
        setupTabLayout();
        setupRecyclerView();
        setupSearch();
        setupSwipeRefresh();
        setupClickListeners();

        // Load data
        loadEvents();

        return rootView;
    }

    private void getUserSessionData() {
        SharedPreferences sharedPref = getActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        currentUserId = sharedPref.getInt("userId", -1);
        currentUserRole = sharedPref.getString("role", "student");
    }

    private void initViews() {
        tabLayout = rootView.findViewById(R.id.tab_layout);
        etSearch = rootView.findViewById(R.id.et_search);
        btnFilter = rootView.findViewById(R.id.btn_filter);
        btnCreateFirstEvent = rootView.findViewById(R.id.btn_create_first_event);
        swipeRefresh = rootView.findViewById(R.id.swipe_refresh);
        rvEvents = rootView.findViewById(R.id.rv_events);
        llEmptyState = rootView.findViewById(R.id.ll_empty_state);
        tvEmptyMessage = rootView.findViewById(R.id.tv_empty_message);
        fabAddEvent = rootView.findViewById(R.id.fab_add_event);

        // Initialize lists
        allEvents = new ArrayList<>();
        filteredEvents = new ArrayList<>();
    }

    private void setupTabLayout() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentFilter = tab.getText().toString();
                filterEvents();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupRecyclerView() {
        eventAdapter = new EventAdapter(filteredEvents, this, currentUserRole);
        rvEvents.setLayoutManager(new LinearLayoutManager(getContext()));
        rvEvents.setAdapter(eventAdapter);
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
                filterEvents();
            }
        });
    }

    private void setupSwipeRefresh() {
        swipeRefresh.setColorSchemeResources(R.color.primary);
        swipeRefresh.setOnRefreshListener(() -> {
            loadEvents();
        });
    }

    private void setupClickListeners() {
        fabAddEvent.setOnClickListener(v -> {
            if (isCoachOrAdmin()) {
                createNewEvent();
            } else {
                Toast.makeText(getContext(), "Only coaches and admins can create events", Toast.LENGTH_SHORT).show();
            }
        });

        btnCreateFirstEvent.setOnClickListener(v -> {
            if (isCoachOrAdmin()) {
                createNewEvent();
            } else {
                Toast.makeText(getContext(), "Only coaches and admins can create events", Toast.LENGTH_SHORT).show();
            }
        });

        btnFilter.setOnClickListener(v -> {
            showFilterBottomSheet();
        });

        // Hide FAB for students
        if (!isCoachOrAdmin()) {
            fabAddEvent.hide();
        }
    }

    // FIXED: Load events based on user role using correct methods
//    private void loadEvents() {
//        swipeRefresh.setRefreshing(true);
//
//        // Load events based on user role - PRODUCTION READY
//        switch (currentUserRole) {
//            case "admin":
//                // Admin sees all events
//                allEvents = databaseHelper.getAllEvents();
//                break;
//            case "coach":
//                // Coach sees only their events
//                allEvents = databaseHelper.getCoachEvents(currentUserId);
//                break;
//            case "student":
//                // Student sees only events for their teams
//                allEvents = databaseHelper.getStudentEvents(currentUserId);
//                break;
//            default:
//                allEvents = new ArrayList<>();
//                break;
//        }
//
//        filterEvents();
//        swipeRefresh.setRefreshing(false);
//    }
    // ✅ UPDATED: Load ALL events for ALL users with participation indicators
    private void loadEvents() {
        swipeRefresh.setRefreshing(true);

        // ✅ NEW: Show ALL events to ALL users with participation status
        allEvents = databaseHelper.getAllEventsWithParticipationStatus(currentUserId, currentUserRole);

        filterEvents();
        swipeRefresh.setRefreshing(false);
    }


    private void filterEvents() {
        filteredEvents.clear();

        for (Event event : allEvents) {
            boolean matchesTab = currentFilter.equals("All") ||
                    (event.getType() != null && event.getType().equalsIgnoreCase(currentFilter));

            boolean matchesSearch = TextUtils.isEmpty(searchQuery) ||
                    (event.getTitle() != null && event.getTitle().toLowerCase().contains(searchQuery.toLowerCase())) ||
                    (event.getLocation() != null && event.getLocation().toLowerCase().contains(searchQuery.toLowerCase()));

            boolean matchesDate = TextUtils.isEmpty(dateFilter) ||
                    (event.getDateTime() != null && event.getDateTime().contains(dateFilter));

            if (matchesTab && matchesSearch && matchesDate) {
                filteredEvents.add(event);
            }
        }

        eventAdapter.notifyDataSetChanged();
        updateEmptyState();
    }

    private void updateEmptyState() {
        if (filteredEvents.isEmpty()) {
            llEmptyState.setVisibility(View.VISIBLE);
            rvEvents.setVisibility(View.GONE);
            swipeRefresh.setVisibility(View.GONE);

            // Update empty message based on user role
            if (isCoachOrAdmin()) {
                tvEmptyMessage.setText("Create your first event to get started");
                btnCreateFirstEvent.setVisibility(View.VISIBLE);
            } else {
                tvEmptyMessage.setText("No events available. Join a team to see events!");
                btnCreateFirstEvent.setVisibility(View.GONE);
            }
        } else {
            llEmptyState.setVisibility(View.GONE);
            rvEvents.setVisibility(View.VISIBLE);
            swipeRefresh.setVisibility(View.VISIBLE);
        }
    }

    private void createNewEvent() {
        Intent intent = new Intent(getActivity(), EventScheduleActivity.class);
        intent.putExtra("action", "create");
        startActivity(intent);
    }

    private void showFilterBottomSheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getContext());
        View bottomSheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_event_filter, null);
        bottomSheetDialog.setContentView(bottomSheetView);

        // Setup filter options
        MaterialButton btnToday = bottomSheetView.findViewById(R.id.btn_today);
        MaterialButton btnTomorrow = bottomSheetView.findViewById(R.id.btn_tomorrow);
        MaterialButton btnThisWeek = bottomSheetView.findViewById(R.id.btn_this_week);
        MaterialButton btnCustomDate = bottomSheetView.findViewById(R.id.btn_custom_date);
        MaterialButton btnClearFilter = bottomSheetView.findViewById(R.id.btn_clear_filter);

        btnToday.setOnClickListener(v -> {
            dateFilter = getCurrentDate();
            filterEvents();
            bottomSheetDialog.dismiss();
        });

        btnTomorrow.setOnClickListener(v -> {
            dateFilter = getTomorrowDate();
            filterEvents();
            bottomSheetDialog.dismiss();
        });

        btnThisWeek.setOnClickListener(v -> {
            // Implement this week filter
            Toast.makeText(getContext(), "This week filter coming soon!", Toast.LENGTH_SHORT).show();
            bottomSheetDialog.dismiss();
        });

        btnCustomDate.setOnClickListener(v -> {
            showDatePicker();
            bottomSheetDialog.dismiss();
        });

        btnClearFilter.setOnClickListener(v -> {
            dateFilter = "";
            searchQuery = "";
            etSearch.setText("");
            filterEvents();
            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog.show();
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                getContext(),
                (view, year, month, dayOfMonth) -> {
                    dateFilter = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth);
                    filterEvents();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private String getCurrentDate() {
        Calendar calendar = Calendar.getInstance();
        return String.format("%04d-%02d-%02d",
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH));
    }

    private String getTomorrowDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        return String.format("%04d-%02d-%02d",
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH));
    }

    private boolean isCoachOrAdmin() {
        return currentUserRole.equals("coach") || currentUserRole.equals("admin");
    }

    // EventAdapter.OnEventClickListener implementation
    @Override
    public void onEventClick(Event event) {
        // ✅ Open EventDetailsActivity instead
        Intent intent = new Intent(getActivity(), EventDetailsActivity.class);
        intent.putExtra("eventId", event.getId());
        startActivity(intent);
    }

    @Override
    public void onEventEdit(Event event) {
        if (isCoachOrAdmin()) {
            Intent intent = new Intent(getActivity(), EventScheduleActivity.class);
            intent.putExtra("eventId", event.getId());
            intent.putExtra("action", "edit");
            startActivity(intent);
        } else {
            Toast.makeText(getContext(), "You don't have permission to edit events", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onEventDelete(Event event) {
        if (isCoachOrAdmin()) {
            showDeleteConfirmation(event);
        } else {
            Toast.makeText(getContext(), "You don't have permission to delete events", Toast.LENGTH_SHORT).show();
        }
    }

    private void showDeleteConfirmation(Event event) {
        new androidx.appcompat.app.AlertDialog.Builder(getContext())
                .setTitle("Delete Event")
                .setMessage("Are you sure you want to delete '" + event.getTitle() + "'?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    if (databaseHelper.deleteEvent(event.getId())) {
                        Toast.makeText(getContext(), "Event deleted successfully", Toast.LENGTH_SHORT).show();
                        loadEvents();
                    } else {
                        Toast.makeText(getContext(), "Failed to delete event", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh events when fragment becomes visible
        loadEvents();
    }
}

