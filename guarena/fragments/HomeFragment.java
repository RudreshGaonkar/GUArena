package com.example.guarena.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.guarena.R;
import com.example.guarena.activities.EventScheduleActivity;
import com.example.guarena.activities.GalleryActivity;
import com.example.guarena.activities.EquipmentActivity; // ✅ CHANGED
import com.example.guarena.activities.TeamManagementActivity;
import com.example.guarena.adapters.UpcomingEventsAdapter;
import com.example.guarena.adapters.RecentActivityAdapter;
import com.example.guarena.database.DatabaseHelper;
import com.example.guarena.models.Event;
import com.example.guarena.models.ActivityItem;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private View rootView;
    private SwipeRefreshLayout swipeRefresh;
    private TextView tvUserName, tvUserRole, tvTodayEventsCount, tvMyTeamsCount, tvViewAllEvents;
    private ImageView ivProfilePicture;
    private LinearLayout llEventsAction, llTeamsAction, llEquipmentAction, llGalleryAction; // ✅ CHANGED
    private RecyclerView rvUpcomingEvents, rvRecentActivity;

    private DatabaseHelper databaseHelper;
    private UpcomingEventsAdapter upcomingEventsAdapter;
    private RecentActivityAdapter recentActivityAdapter;

    private String currentUserRole;
    private int currentUserId;
    private String currentUserName;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize database
        databaseHelper = new DatabaseHelper(getContext());

        // Get user session data
        getUserSessionData();

        // Initialize views
        initViews();

        // Setup UI
        setupUserInfo();
        setupQuickActions();
        setupRecyclerViews();
        setupSwipeRefresh();

        // Load data
        loadDashboardData();

        return rootView;
    }

    private void getUserSessionData() {
        SharedPreferences sharedPref = getActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        currentUserId = sharedPref.getInt("userId", -1);
        currentUserRole = sharedPref.getString("role", "student");
        currentUserName = sharedPref.getString("fullName", "User");
    }

    private void initViews() {
        swipeRefresh = rootView.findViewById(R.id.swipe_refresh);
        tvUserName = rootView.findViewById(R.id.tv_user_name);
        tvUserRole = rootView.findViewById(R.id.tv_user_role);
        tvTodayEventsCount = rootView.findViewById(R.id.tv_today_events_count);
        tvMyTeamsCount = rootView.findViewById(R.id.tv_my_teams_count);
        tvViewAllEvents = rootView.findViewById(R.id.tv_view_all_events);
        ivProfilePicture = rootView.findViewById(R.id.iv_profile_picture);

        llEventsAction = rootView.findViewById(R.id.ll_events_action);
        llTeamsAction = rootView.findViewById(R.id.ll_teams_action);
        llEquipmentAction = rootView.findViewById(R.id.ll_equipment_action); // ✅ CHANGED
        llGalleryAction = rootView.findViewById(R.id.ll_gallery_action);

        rvUpcomingEvents = rootView.findViewById(R.id.rv_upcoming_events);
        rvRecentActivity = rootView.findViewById(R.id.rv_recent_activity);
    }

    private void setupUserInfo() {
        tvUserName.setText(currentUserName);
        tvUserRole.setText(currentUserRole.substring(0, 1).toUpperCase() + currentUserRole.substring(1));
        // TODO: Load profile picture from database
        // For now, use default icon
    }

    private void setupQuickActions() {
        // Events - Students go to EventsFragment view, Coaches go to EventScheduleActivity
        llEventsAction.setOnClickListener(v -> {
            if ("student".equals(currentUserRole)) {
                // Students: Switch to Events tab to VIEW events
                if (getActivity() != null) {
                    Intent intent = new Intent("SWITCH_TO_EVENTS_TAB");
                    getActivity().sendBroadcast(intent);
                }
            } else {
                // Coaches/Admins: Go to EventScheduleActivity to MANAGE events
                Intent intent = new Intent(getActivity(), EventScheduleActivity.class);
                startActivity(intent);
            }
        });

        // Teams - Students see their team, Coaches manage all teams
        llTeamsAction.setOnClickListener(v -> {
            if ("student".equals(currentUserRole)) {
                // Students: Switch to Teams tab to VIEW their team
                if (getActivity() != null) {
                    Intent intent = new Intent("SWITCH_TO_TEAMS_TAB");
                    getActivity().sendBroadcast(intent);
                }
            } else {
                // Coaches/Admins: Go to TeamManagementActivity to MANAGE teams
                Intent intent = new Intent(getActivity(), TeamManagementActivity.class);
                startActivity(intent);
            }
        });

        // Equipment - Everyone uses the same activity
        llEquipmentAction.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), EquipmentActivity.class);
            startActivity(intent);
        });

        // Gallery - Everyone uses the same activity
        llGalleryAction.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), GalleryActivity.class);
            startActivity(intent);
        });

        tvViewAllEvents.setOnClickListener(v -> {
            // Switch to Events tab
            if (getActivity() != null) {
                Intent intent = new Intent("SWITCH_TO_EVENTS_TAB");
                getActivity().sendBroadcast(intent);
            }
        });
    }



    private void setupRecyclerViews() {
        // Upcoming Events RecyclerView
        rvUpcomingEvents.setLayoutManager(new LinearLayoutManager(getContext()));
        upcomingEventsAdapter = new UpcomingEventsAdapter(new ArrayList<>(), event -> {
            // Handle event click
            Intent intent = new Intent(getActivity(), EventScheduleActivity.class);
            intent.putExtra("eventId", event.getId());
            intent.putExtra("action", "view");
            startActivity(intent);
        });
        rvUpcomingEvents.setAdapter(upcomingEventsAdapter);

        // Recent Activity RecyclerView
        rvRecentActivity.setLayoutManager(new LinearLayoutManager(getContext()));
        recentActivityAdapter = new RecentActivityAdapter(new ArrayList<>());
        rvRecentActivity.setAdapter(recentActivityAdapter);
    }

    private void setupSwipeRefresh() {
        swipeRefresh.setColorSchemeResources(R.color.primary);
        swipeRefresh.setOnRefreshListener(() -> {
            loadDashboardData();
        });
    }

    private void loadDashboardData() {
        // Load today's events count
        int todayEventsCount = databaseHelper.getTodayEventsCount(currentUserId, currentUserRole);
        tvTodayEventsCount.setText(String.valueOf(todayEventsCount));

        // Load my teams count
        int myTeamsCount = databaseHelper.getUserTeamsCount(currentUserId, currentUserRole);
        tvMyTeamsCount.setText(String.valueOf(myTeamsCount));

        // Load upcoming events
        List<Event> upcomingEvents = databaseHelper.getUpcomingEvents(currentUserId, currentUserRole, 5);
        upcomingEventsAdapter.updateEvents(upcomingEvents);

        // Load recent activity
        List<ActivityItem> recentActivity = databaseHelper.getRecentActivity(currentUserId, currentUserRole, 10);
        recentActivityAdapter.updateActivity(recentActivity);

        // Stop refresh animation
        swipeRefresh.setRefreshing(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh data when fragment becomes visible
        loadDashboardData();
    }
}

