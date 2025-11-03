package com.example.guarena.activities;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.guarena.R;
import com.example.guarena.adapters.GalleryAdapter;
import com.example.guarena.database.DatabaseHelper;
import com.example.guarena.models.Event;
import com.example.guarena.models.GalleryItem;
import com.example.guarena.utils.ImageUtils;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GalleryActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private RecyclerView rvGallery;
    private SwipeRefreshLayout swipeRefresh;
    private LinearLayout llEmptyState;
    private TextView tvEmptyMessage;
    private FloatingActionButton fabAddPhoto;

    private DatabaseHelper databaseHelper;
    private GalleryAdapter galleryAdapter;
    private List<GalleryItem> galleryItems;

    private String userRole;
    private int currentUserId;
    private Uri selectedImageUri;

    private ActivityResultLauncher<Intent> pickImageLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        // Initialize database
        databaseHelper = new DatabaseHelper(this);

        // Get user session
        getUserSessionData();

        // Initialize views
        initViews();

        // Setup toolbar
        setupToolbar();

        // Setup image picker
        setupImagePicker();

        // Setup FAB (only for coaches)
        setupFAB();

        // Load gallery
        loadGallery();

        // Setup swipe refresh
        swipeRefresh.setOnRefreshListener(this::loadGallery);
    }

    private void getUserSessionData() {
        SharedPreferences sharedPref = getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        currentUserId = sharedPref.getInt("userId", -1);
        userRole = sharedPref.getString("role", "student");
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        rvGallery = findViewById(R.id.rv_gallery);
        swipeRefresh = findViewById(R.id.swipe_refresh);
        llEmptyState = findViewById(R.id.ll_empty_state);
        tvEmptyMessage = findViewById(R.id.tv_empty_message);
        fabAddPhoto = findViewById(R.id.ic_add);

        // Use LinearLayoutManager for Instagram-style vertical scroll
        rvGallery.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Gallery");
        }
    }

    private void setupImagePicker() {
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        showUploadDialog();
                    }
                }
        );
    }

    private void setupFAB() {
        if ("student".equals(userRole)) {
            fabAddPhoto.setVisibility(View.GONE); // Students can't upload
        } else {
            fabAddPhoto.setOnClickListener(v -> openImagePicker());
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickImageLauncher.launch(intent);
    }

    private void showUploadDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_upload_photo);
        dialog.getWindow().setLayout(
                (int) (getResources().getDisplayMetrics().widthPixels * 0.9),
                LinearLayout.LayoutParams.WRAP_CONTENT
        );

        TextInputLayout tilTitle = dialog.findViewById(R.id.til_title);
        TextInputLayout tilEvent = dialog.findViewById(R.id.til_event);
        TextInputLayout tilDescription = dialog.findViewById(R.id.til_description);
        TextInputEditText etTitle = dialog.findViewById(R.id.et_title);
        AutoCompleteTextView etEvent = dialog.findViewById(R.id.et_event);
        TextInputEditText etDescription = dialog.findViewById(R.id.et_description);
        MaterialButton btnCancel = dialog.findViewById(R.id.btn_cancel);
        MaterialButton btnUpload = dialog.findViewById(R.id.btn_upload);

        // Load events for dropdown
        List<Event> events = databaseHelper.getAllEvents();
        List<String> eventNames = new ArrayList<>();
        eventNames.add("General"); // Default option
        for (Event event : events) {
            eventNames.add(event.getTitle());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, eventNames);
        etEvent.setAdapter(adapter);
        etEvent.setText("General", false);

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnUpload.setOnClickListener(v -> {
            tilTitle.setError(null);

            String title = etTitle.getText().toString().trim();
            String eventName = etEvent.getText().toString().trim();
            String description = etDescription.getText().toString().trim();

            if (TextUtils.isEmpty(title)) {
                tilTitle.setError("Title is required");
                etTitle.requestFocus();
                return;
            }

            if (selectedImageUri != null) {
                // Save image and create gallery item
//                String imagePath = ImageUtils.saveImageToInternalStorage(this, selectedImageUri, "gallery");

//                if (imagePath != null) {
//                    GalleryItem item = new GalleryItem();
//                    item.setTitle(title);
//                    item.setEventName(eventName);
//                    item.setDescription(description);
//                    item.setImagePath(imagePath);
//                    item.setUploadedBy(currentUserId);
//
//                    long result = databaseHelper.addGalleryItem(item);
//                    if (result > 0) {
//                        Toast.makeText(this, "Photo uploaded successfully!", Toast.LENGTH_SHORT).show();
//                        loadGallery();
//                        dialog.dismiss();
//                    } else {
//                        Toast.makeText(this, "Failed to upload photo", Toast.LENGTH_SHORT).show();
//                    }
//                } else {
//                    Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show();
//                }
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);
                    String imagePath = ImageUtils.saveImageToInternalStorage(this, bitmap, "gallery_" + System.currentTimeMillis(), "gallery");

                    if (imagePath != null) {
                        GalleryItem item = new GalleryItem();
                        item.setTitle(title);
                        item.setEventName(eventName);
                        item.setDescription(description);
                        item.setImagePath(imagePath);
                        item.setUploadedBy(currentUserId);

                        long result = databaseHelper.addGalleryItem(item);
                        if (result > 0) {
                            Toast.makeText(this, "Photo uploaded successfully!", Toast.LENGTH_SHORT).show();
                            loadGallery();
                            dialog.dismiss();
                        } else {
                            Toast.makeText(this, "Failed to upload photo", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException e) {
                    Log.e("GalleryActivity", "Error loading image: " + e.getMessage());
                    Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
                }

            }
        });

        dialog.show();
    }

    private void loadGallery() {
        galleryItems = databaseHelper.getAllGalleryItems();

        if (galleryItems.isEmpty()) {
            llEmptyState.setVisibility(View.VISIBLE);
            rvGallery.setVisibility(View.GONE);
            tvEmptyMessage.setText("No photos yet");
        } else {
            llEmptyState.setVisibility(View.GONE);
            rvGallery.setVisibility(View.VISIBLE);

            galleryAdapter = new GalleryAdapter(galleryItems, this, userRole);
            rvGallery.setAdapter(galleryAdapter);
        }

        swipeRefresh.setRefreshing(false);
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
