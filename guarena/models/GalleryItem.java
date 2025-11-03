package com.example.guarena.models;

public class GalleryItem {
    private int id;
    private String title;
    private String eventName;
    private String description;
    private String imagePath;
    private int uploadedBy;
    private String uploadedDate;

    // Constructors
    public GalleryItem() {}

    public GalleryItem(String title, String eventName, String description, String imagePath, int uploadedBy) {
        this.title = title;
        this.eventName = eventName;
        this.description = description;
        this.imagePath = imagePath;
        this.uploadedBy = uploadedBy;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getEventName() { return eventName; }
    public void setEventName(String eventName) { this.eventName = eventName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    public int getUploadedBy() { return uploadedBy; }
    public void setUploadedBy(int uploadedBy) { this.uploadedBy = uploadedBy; }

    public String getUploadedDate() { return uploadedDate; }
    public void setUploadedDate(String uploadedDate) { this.uploadedDate = uploadedDate; }
}
