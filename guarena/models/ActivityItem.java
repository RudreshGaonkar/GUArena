package com.example.guarena.models;

public class ActivityItem {

    private int id;
    private String title;
    private String description;
    private String activityType; // event_created, team_joined, performance_recorded, etc.
    private String timestamp;
    private int userId;
    private String userName;
    private String iconResource;

    // Constructors
    public ActivityItem() {}

    public ActivityItem(String title, String description, String activityType, int userId) {
        this.title = title;
        this.description = description;
        this.activityType = activityType;
        this.userId = userId;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getActivityType() { return activityType; }
    public void setActivityType(String activityType) { this.activityType = activityType; }

    // ✅ ADD MISSING METHOD - DatabaseHelper calls setType()
    public String getType() { return activityType; }
    public void setType(String type) { this.activityType = type; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getIconResource() { return iconResource; }
    public void setIconResource(String iconResource) { this.iconResource = iconResource; }

    @Override
    public String toString() {
        return "ActivityItem{" +
                "title='" + title + '\'' +
                ", activityType='" + activityType + '\'' +
                ", timestamp='" + timestamp + '\'' +
                '}';
    }
}


