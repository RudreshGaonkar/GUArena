package com.example.guarena.activities;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.guarena.R;
import com.example.guarena.adapters.EquipmentAdapter;
import com.example.guarena.database.DatabaseHelper;
import com.example.guarena.models.Equipment;
import com.example.guarena.models.EquipmentBorrowing;
import com.example.guarena.utils.NotificationService;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.List;

public class EquipmentActivity extends AppCompatActivity implements EquipmentAdapter.OnEquipmentClickListener {

    private MaterialToolbar toolbar;
    private RecyclerView rvEquipment;
    private SwipeRefreshLayout swipeRefresh;
    private LinearLayout llEmptyState, llBorrowedSection;
    private TextView tvEmptyMessage, tvBorrowedTitle;
    private RecyclerView rvBorrowedEquipment;
    private FloatingActionButton fabAddEquipment;

    private DatabaseHelper databaseHelper;
    private EquipmentAdapter equipmentAdapter;
    private List<Equipment> equipmentList;

    private String userRole;
    private int currentUserId;
    private String currentUserName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_equipment);

        // Initialize database
        databaseHelper = new DatabaseHelper(this);
        databaseHelper.fixExistingEquipmentQuantities();

        // Get user session
        getUserSessionData();

        // Initialize views
        initViews();

        // Setup toolbar
        setupToolbar();

        // Populate default equipment if needed
        databaseHelper.populateDefaultEquipment();

        // Load equipment
        loadEquipment();

        // Load borrowed equipment for students
        if ("student".equals(userRole)) {
            loadBorrowedEquipment();
        }

        // Setup FAB for coaches/admins
        setupFab();

        // Setup swipe refresh
        swipeRefresh.setOnRefreshListener(this::loadEquipment);
    }

    private void getUserSessionData() {
        SharedPreferences sharedPref = getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        currentUserId = sharedPref.getInt("userId", -1);
        userRole = sharedPref.getString("role", "student");
        currentUserName = sharedPref.getString("fullName", "User");
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        rvEquipment = findViewById(R.id.rv_equipment);
        swipeRefresh = findViewById(R.id.swipe_refresh);
        llEmptyState = findViewById(R.id.ll_empty_state);
        tvEmptyMessage = findViewById(R.id.tv_empty_message);
        llBorrowedSection = findViewById(R.id.ll_borrowed_section);
        tvBorrowedTitle = findViewById(R.id.tv_borrowed_title);
        rvBorrowedEquipment = findViewById(R.id.rv_borrowed_equipment);
        fabAddEquipment = findViewById(R.id.fab_add_equipment);

        rvEquipment.setLayoutManager(new LinearLayoutManager(this));
        rvBorrowedEquipment.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Equipment Management");
        }
    }

    private void setupFab() {
        if ("student".equals(userRole)) {
            fabAddEquipment.setVisibility(View.GONE);
        } else {
            fabAddEquipment.setOnClickListener(v -> showAddEquipmentDialog());
        }
    }

//    private void loadEquipment() {
//        equipmentList = databaseHelper.getAllEquipment();
//
//        if (equipmentList.isEmpty()) {
//            llEmptyState.setVisibility(View.VISIBLE);
//            rvEquipment.setVisibility(View.GONE);
//            tvEmptyMessage.setText("No equipment available");
//        } else {
//            llEmptyState.setVisibility(View.GONE);
//            rvEquipment.setVisibility(View.VISIBLE);
//
//            equipmentAdapter = new EquipmentAdapter(equipmentList, this, userRole);
//            rvEquipment.setAdapter(equipmentAdapter);
//        }
//
//        swipeRefresh.setRefreshing(false);
//    }
private void loadEquipment() {
    equipmentList = databaseHelper.getAllEquipmentForUser(currentUserId); // ✅ CHANGED

    if (equipmentList.isEmpty()) {
        llEmptyState.setVisibility(View.VISIBLE);
        rvEquipment.setVisibility(View.GONE);
        tvEmptyMessage.setText("No equipment available");
    } else {
        llEmptyState.setVisibility(View.GONE);
        rvEquipment.setVisibility(View.VISIBLE);

        equipmentAdapter = new EquipmentAdapter(equipmentList, this, userRole);
        rvEquipment.setAdapter(equipmentAdapter);
    }

    swipeRefresh.setRefreshing(false);
}


    private void loadEquipmentData() {
        equipmentList.clear();
        equipmentList.addAll(databaseHelper.getAllEquipment());
        equipmentAdapter.notifyDataSetChanged();

        if (equipmentList.isEmpty()) {
            llEmptyState.setVisibility(View.VISIBLE);
            rvEquipment.setVisibility(View.GONE);
            tvEmptyMessage.setText("No equipment available");
        } else {
            llEmptyState.setVisibility(View.GONE);
            rvEquipment.setVisibility(View.VISIBLE);
        }
    }

    private void loadBorrowedEquipment() {
        List<EquipmentBorrowing> borrowings = databaseHelper.getUserActiveBorrowings(currentUserId);

        if (borrowings.isEmpty()) {
            llBorrowedSection.setVisibility(View.GONE);
        } else {
            llBorrowedSection.setVisibility(View.VISIBLE);
            tvBorrowedTitle.setText("My Borrowed Equipment (" + borrowings.size() + ")");
            // Optionally display borrowings in rvBorrowedEquipment using another adapter
        }
    }

    @Override
    public void onEquipmentClick(Equipment equipment) {
        showEquipmentDetailsDialog(equipment);
    }

    @Override
    public void onEquipmentBorrow(Equipment equipment) {
        showBorrowDialog(equipment);
    }

    @Override
    public void onEquipmentReturn(Equipment equipment) {
        showReturnConfirmation(equipment);
    }

    @Override
    public void onEquipmentEdit(Equipment equipment) {
        showEditEquipmentDialog(equipment);
    }
    @Override
    public void onEquipmentDelete(Equipment equipment) {
        showDeleteConfirmation(equipment);
    }

    // Show Borrow Dialog with Duration Dropdown
    private void showBorrowDialog(Equipment equipment) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_borrow_equipment, null);
        builder.setView(dialogView);

        TextView tvEquipmentName = dialogView.findViewById(R.id.tv_equipment_name);
        AutoCompleteTextView dropdownDuration = dialogView.findViewById(R.id.dropdown_duration);
        MaterialButton btnBorrow = dialogView.findViewById(R.id.btn_borrow);
        MaterialButton btnCancel = dialogView.findViewById(R.id.btn_cancel);

        tvEquipmentName.setText("Borrow: " + equipment.getName());

        // Duration options
        String[] durations = {"30 seconds (Testing)", "1 Day", "2 Days", "3 Days", "4 Days", "5 Days", "6 Days", "7 Days"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, durations);
        dropdownDuration.setAdapter(adapter);
        dropdownDuration.setText(durations[0], false);

        AlertDialog dialog = builder.create();

        btnBorrow.setOnClickListener(v -> {
            String selectedDuration = dropdownDuration.getText().toString();
            int durationDays = getDurationDays(selectedDuration);

            boolean success = databaseHelper.borrowEquipment(equipment.getId(), currentUserId, durationDays);

            if (success) {
                Toast.makeText(this, "Equipment borrowed successfully!", Toast.LENGTH_SHORT).show();
                NotificationService.showEquipmentBorrowedNotification(
                        this,
                        equipment.getName(),
                        "calculated due date string"
                );
                loadEquipment();
                loadBorrowedEquipment();
                dialog.dismiss();
            }
            else {
                Toast.makeText(this, "Failed to borrow equipment", Toast.LENGTH_SHORT).show();
            }
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private int getDurationDays(String duration) {
        if (duration.contains("30 seconds")) return 0;
        if (duration.contains("1 Day")) return 1;
        if (duration.contains("2 Days")) return 2;
        if (duration.contains("3 Days")) return 3;
        if (duration.contains("4 Days")) return 4;
        if (duration.contains("5 Days")) return 5;
        if (duration.contains("6 Days")) return 6;
        if (duration.contains("7 Days")) return 7;
        return 1;
    }

    // Show Return Confirmation
    private void showReturnConfirmation(Equipment equipment) {
        new AlertDialog.Builder(this)
                .setTitle("Return Equipment")
                .setMessage("Are you sure you want to return " + equipment.getName() + "?")
                .setPositiveButton("Return", (dialog, which) -> {
                    boolean success = databaseHelper.returnEquipment(equipment.getId(), currentUserId);

                    if (success) {
                        Toast.makeText(this, "Equipment returned successfully!", Toast.LENGTH_SHORT).show();
                        NotificationService.showEquipmentReturnedNotification(
                                this,
                                equipment.getName()
                        );
                        loadEquipment();
                        loadBorrowedEquipment();
                    } else {
                        Toast.makeText(this, "Failed to return equipment", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showEquipmentDetailsDialog(Equipment equipment) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(equipment.getName());

        String details = "Category: " + equipment.getCategory() + "\n"
                + "Status: " + equipment.getStatus().toUpperCase() + "\n"
                + "Available: " + equipment.getAvailableQuantity() + "/" + equipment.getQuantity() + "\n\n"
                + equipment.getDescription();

        if ("borrowed".equals(equipment.getStatus())) {
            details += "\n\nBorrowed by: " + equipment.getBorrowedUserName();
            details += "\nDue date: " + equipment.getDueDate();
        }

        builder.setMessage(details);
        builder.setPositiveButton("OK", null);
        builder.show();
    }

    // Add Equipment Dialog
    private void showAddEquipmentDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_add_equipment);
        dialog.getWindow().setLayout(
                (int) (getResources().getDisplayMetrics().widthPixels * 0.9),
                LinearLayout.LayoutParams.WRAP_CONTENT
        );

        TextInputLayout tilName = dialog.findViewById(R.id.til_equipment_name);
        TextInputLayout tilCategory = dialog.findViewById(R.id.til_category);
        TextInputLayout tilQuantity = dialog.findViewById(R.id.til_quantity); // ✅ NEW
        TextInputEditText etName = dialog.findViewById(R.id.et_equipment_name);
        AutoCompleteTextView etCategory = dialog.findViewById(R.id.et_category);
        TextInputEditText etQuantity = dialog.findViewById(R.id.et_quantity); // ✅ NEW
        TextInputEditText etDescription = dialog.findViewById(R.id.et_description);
        MaterialButton btnCancel = dialog.findViewById(R.id.btn_cancel_add_equipment);
        MaterialButton btnAdd = dialog.findViewById(R.id.btn_add_equipment);

        String[] categories = {"Balls", "Fitness", "Protective Gear", "Training Equipment", "Others"};
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, categories);
        etCategory.setAdapter(categoryAdapter);

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnAdd.setOnClickListener(v -> {
            tilName.setError(null);
            tilCategory.setError(null);
            tilQuantity.setError(null); // ✅ NEW

            String name = etName.getText().toString().trim();
            String category = etCategory.getText().toString().trim();
            String quantityStr = etQuantity.getText().toString().trim(); // ✅ NEW
            String description = etDescription.getText().toString().trim();

            if (TextUtils.isEmpty(name)) {
                tilName.setError("Equipment name is required");
                etName.requestFocus();
                return;
            }
            if (TextUtils.isEmpty(category)) {
                tilCategory.setError("Select a category");
                etCategory.requestFocus();
                return;
            }
            // ✅ NEW: Validate quantity
            if (TextUtils.isEmpty(quantityStr)) {
                tilQuantity.setError("Quantity is required");
                etQuantity.requestFocus();
                return;
            }

            int quantity = Integer.parseInt(quantityStr);
            if (quantity < 1) {
                tilQuantity.setError("Quantity must be at least 1");
                etQuantity.requestFocus();
                return;
            }

            Equipment equipment = new Equipment();
            equipment.setName(name);
            equipment.setCategory(category);
            equipment.setDescription(description);
            equipment.setQuantity(quantity); // ✅ NEW
            equipment.setAvailableQuantity(quantity); // ✅ NEW

            boolean success = databaseHelper.addEquipment(equipment) > 0;
            if (success) {
                loadEquipmentData();
                Toast.makeText(this, "Equipment added successfully", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            } else {
                Toast.makeText(this, "Failed to add equipment", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }


    // Edit Equipment Dialog
    private void showEditEquipmentDialog(Equipment equipment) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_add_equipment);
        dialog.getWindow().setLayout(
                (int) (getResources().getDisplayMetrics().widthPixels * 0.9),
                LinearLayout.LayoutParams.WRAP_CONTENT
        );

        TextInputLayout tilName = dialog.findViewById(R.id.til_equipment_name);
        TextInputLayout tilCategory = dialog.findViewById(R.id.til_category);
        TextInputLayout tilQuantity = dialog.findViewById(R.id.til_quantity); // ✅ NEW
        TextInputEditText etName = dialog.findViewById(R.id.et_equipment_name);
        AutoCompleteTextView etCategory = dialog.findViewById(R.id.et_category);
        TextInputEditText etQuantity = dialog.findViewById(R.id.et_quantity); // ✅ NEW
        TextInputEditText etDescription = dialog.findViewById(R.id.et_description);
        MaterialButton btnCancel = dialog.findViewById(R.id.btn_cancel_add_equipment);
        MaterialButton btnAdd = dialog.findViewById(R.id.btn_add_equipment);
        btnAdd.setText("Update");

        String[] categories = {"Balls", "Fitness", "Protective Gear", "Training Equipment", "Others"};
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, categories);
        etCategory.setAdapter(categoryAdapter);

        etName.setText(equipment.getName());
        etCategory.setText(equipment.getCategory());
        etQuantity.setText(String.valueOf(equipment.getQuantity())); // ✅ NEW
        etDescription.setText(equipment.getDescription());

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnAdd.setOnClickListener(v -> {
            tilName.setError(null);
            tilCategory.setError(null);
            tilQuantity.setError(null); // ✅ NEW

            String name = etName.getText().toString().trim();
            String category = etCategory.getText().toString().trim();
            String quantityStr = etQuantity.getText().toString().trim(); // ✅ NEW
            String description = etDescription.getText().toString().trim();

            if (TextUtils.isEmpty(name)) {
                tilName.setError("Equipment name is required");
                etName.requestFocus();
                return;
            }
            if (TextUtils.isEmpty(category)) {
                tilCategory.setError("Select a category");
                etCategory.requestFocus();
                return;
            }
            // ✅ NEW: Validate quantity
            if (TextUtils.isEmpty(quantityStr)) {
                tilQuantity.setError("Quantity is required");
                etQuantity.requestFocus();
                return;
            }

            int quantity = Integer.parseInt(quantityStr);
            if (quantity < 1) {
                tilQuantity.setError("Quantity must be at least 1");
                etQuantity.requestFocus();
                return;
            }

            equipment.setName(name);
            equipment.setCategory(category);
            equipment.setDescription(description);
            equipment.setQuantity(quantity); // ✅ NEW
            // Calculate available: new_total - (old_total - old_available)
            int borrowed = equipment.getQuantity() - equipment.getAvailableQuantity();
            equipment.setAvailableQuantity(quantity - borrowed); // ✅ NEW

            boolean success = databaseHelper.updateEquipment(equipment);
            if (success) {
                loadEquipmentData();
                Toast.makeText(this, "Equipment updated successfully", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            } else {
                Toast.makeText(this, "Failed to update equipment", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }


    private void showDeleteConfirmation(Equipment equipment) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Equipment")
                .setMessage("Are you sure you want to delete \"" + equipment.getName() + "\"?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    boolean success = databaseHelper.deleteEquipment(equipment.getId());
                    if (success) {
                        loadEquipmentData();
                        Toast.makeText(this, "Deleted successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Failed to delete", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
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

//package com.example.guarena.activities;
//
//import android.app.Dialog;
//import android.content.Context;
//import android.content.SharedPreferences;
//import android.os.Bundle;
//import android.text.TextUtils;
//import android.view.View;
//import android.widget.ArrayAdapter;
//import android.widget.AutoCompleteTextView;
//import android.widget.LinearLayout;
//import android.widget.Toast;
//
//import androidx.appcompat.app.AlertDialog;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.example.guarena.R;
//import com.example.guarena.adapters.EquipmentAdapter;
//import com.example.guarena.database.DatabaseHelper;
//import com.example.guarena.models.Equipment;
//import com.google.android.material.appbar.MaterialToolbar;
//import com.google.android.material.button.MaterialButton;
//import com.google.android.material.textfield.TextInputEditText;
//import com.google.android.material.textfield.TextInputLayout;
//
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//
//public class EquipmentActivity extends AppCompatActivity implements EquipmentAdapter.OnEquipmentClickListener {
//
//    private MaterialToolbar toolbar;
//    private RecyclerView rvEquipmentList;
//    private LinearLayout llEmptyEquipment;
//    private EquipmentAdapter equipmentAdapter;
//    private List<Equipment> equipmentList;
//    private DatabaseHelper databaseHelper;
//    private String userRole;
//    private int currentUserId;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_equipment);
//
//        // Initialize database
//        databaseHelper = new DatabaseHelper(this);
//
//        // Get user session data
//        getUserSessionData();
//
//        // Initialize views
//        toolbar = findViewById(R.id.toolbar);
//        rvEquipmentList = findViewById(R.id.rv_equipment_list);
//        llEmptyEquipment = findViewById(R.id.ll_empty_equipment);
//
//        setSupportActionBar(toolbar);
//        if (getSupportActionBar() != null) {
//            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//            getSupportActionBar().setDisplayShowHomeEnabled(true);
//        }
//
//        // Setup RecyclerView
//        equipmentList = new ArrayList<>();
//        equipmentAdapter = new EquipmentAdapter(equipmentList, this, userRole);
//        rvEquipmentList.setLayoutManager(new LinearLayoutManager(this));
//        rvEquipmentList.setAdapter(equipmentAdapter);
//
//        // Load equipment data
//        loadEquipmentData();
//
//        // Setup FAB
//        findViewById(R.id.fab_add_equipment).setOnClickListener(v -> showAddEquipmentDialog());
//
//        setupUIForPermissions();
//    }
//
//    private void getUserSessionData() {
//        SharedPreferences sharedPref = getSharedPreferences("UserSession", Context.MODE_PRIVATE);
//        currentUserId = sharedPref.getInt("userId", -1);
//        userRole = sharedPref.getString("role", "student");
//    }
//
//    private void loadEquipmentData() {
//        // TODO: Load equipment list from database
//        // equipmentList = databaseHelper.getAllEquipment();
//
//        // For now, clear and show empty state
//        equipmentList.clear();
//
//        updateView();
//    }
//
//    private void updateView() {
//        if (equipmentList.isEmpty()) {
//            llEmptyEquipment.setVisibility(View.VISIBLE);
//            rvEquipmentList.setVisibility(View.GONE);
//        } else {
//            llEmptyEquipment.setVisibility(View.GONE);
//            rvEquipmentList.setVisibility(View.VISIBLE);
//        }
//
//        equipmentAdapter.updateEquipment(equipmentList);
//    }
//
//    private void setupUIForPermissions() {
//        // Only coaches/admins can add equipment
//        if (userRole.equals("coach") || userRole.equals("admin")) {
//            findViewById(R.id.fab_add_equipment).setVisibility(View.VISIBLE);
//        } else {
//            findViewById(R.id.fab_add_equipment).setVisibility(View.GONE);
//        }
//    }
//
//    private void showAddEquipmentDialog() {
//        Dialog dialog = new Dialog(this);
//        dialog.setContentView(R.layout.dialog_add_equipment);
//        dialog.getWindow().setLayout(
//                (int) (getResources().getDisplayMetrics().widthPixels * 0.9),
//                LinearLayout.LayoutParams.WRAP_CONTENT
//        );
//
//        TextInputLayout tilName = dialog.findViewById(R.id.til_equipment_name);
//        TextInputLayout tilCategory = dialog.findViewById(R.id.til_category);
//        TextInputLayout tilDescription = dialog.findViewById(R.id.til_description);
//
//        TextInputEditText etName = dialog.findViewById(R.id.et_equipment_name);
//        AutoCompleteTextView etCategory = dialog.findViewById(R.id.et_category);
//        TextInputEditText etDescription = dialog.findViewById(R.id.et_description);
//
//        MaterialButton btnCancel = dialog.findViewById(R.id.btn_cancel_add_equipment);
//        MaterialButton btnAdd = dialog.findViewById(R.id.btn_add_equipment);
//
//        String[] categories = {"Balls", "Fitness", "Protective Gear", "Training Equipment", "Others"};
//        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this,
//                android.R.layout.simple_dropdown_item_1line, categories);
//        etCategory.setAdapter(categoryAdapter);
//
//        btnCancel.setOnClickListener(v -> dialog.dismiss());
//
//        btnAdd.setOnClickListener(v -> {
//            tilName.setError(null);
//            tilCategory.setError(null);
//
//            String name = etName.getText().toString().trim();
//            String category = etCategory.getText().toString().trim();
//            String description = etDescription.getText().toString().trim();
//
//            if (TextUtils.isEmpty(name)) {
//                tilName.setError("Equipment name is required");
//                etName.requestFocus();
//                return;
//            }
//
//            if (TextUtils.isEmpty(category)) {
//                tilCategory.setError("Select a category");
//                etCategory.requestFocus();
//                return;
//            }
//
//            // Create equipment object
//            Equipment equipment = new Equipment();
//            equipment.setName(name);
//            equipment.setCategory(category);
//            equipment.setDescription(description);
//            equipment.setStatus("available");
//
//            // TODO: Save to database
//            // boolean success = databaseHelper.addEquipment(equipment);
//
//            boolean success = true; // Dummy response for now
//
//            if (success) {
//                equipmentList.add(equipment);
//                updateView();
//                Toast.makeText(this, "Equipment added successfully", Toast.LENGTH_SHORT).show();
//                dialog.dismiss();
//            } else {
//                Toast.makeText(this, "Failed to add equipment", Toast.LENGTH_SHORT).show();
//            }
//        });
//
//        dialog.show();
//    }
//
//    @Override
//    public void onEquipmentClick(Equipment equipment) {
//        // TODO: Show equipment detail
//        Toast.makeText(this, "Equipment: " + equipment.getName(), Toast.LENGTH_SHORT).show();
//    }
//
//    @Override
//    public void onEquipmentCheckOut(Equipment equipment) {
//        // TODO: Implement check-out logic
//        Toast.makeText(this, "Check out " + equipment.getName(), Toast.LENGTH_SHORT).show();
//    }
//
//    @Override
//    public void onEquipmentCheckIn(Equipment equipment) {
//        // TODO: Implement check-in logic
//        Toast.makeText(this, "Check in " + equipment.getName(), Toast.LENGTH_SHORT).show();
//    }
//
//    @Override
//    public void onEquipmentEdit(Equipment equipment) {
//        // TODO: Implement edit logic
//        Toast.makeText(this, "Edit " + equipment.getName(), Toast.LENGTH_SHORT).show();
//    }
//
//    @Override
//    public void onEquipmentDelete(Equipment equipment) {
//        new AlertDialog.Builder(this)
//                .setTitle("Delete Equipment")
//                .setMessage("Are you sure you want to delete " + equipment.getName() + "?")
//                .setPositiveButton("Delete", (dialog, which) -> {
//                    // TODO: Delete equipment from database and update list
//                    Toast.makeText(this, equipment.getName() + " deleted", Toast.LENGTH_SHORT).show();
//                })
//                .setNegativeButton("Cancel", null)
//                .show();
//    }
//}
