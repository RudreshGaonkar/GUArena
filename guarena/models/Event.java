package com.example.guarena.models;

public class Event {
    private int id;
    private String title;
    private String description;
    private String eventType;
    private String dateTime;
    private String location;
    private int teamId;
    private int createdBy;

    // Additional fields needed by DatabaseHelper
    private String teamName;
    private String createdByName;

    //  NEW: Brochure field
    private String brochurePath;

    // Add these fields to Event.java
    private boolean isUserParticipating;
    private String userRole;
    private int currentUserId;

    // Constructors
    public Event() {}

    public Event(String title, String description, String eventType, String dateTime, String location, int teamId, int createdBy) {
        this.title = title;
        this.description = description;
        this.eventType = eventType;
        this.dateTime = dateTime;
        this.location = location;
        this.teamId = teamId;
        this.createdBy = createdBy;
    }

    //  Getters and Setters for brochure
    public String getBrochurePath() {
        return brochurePath;
    }

    public void setBrochurePath(String brochurePath) {
        this.brochurePath = brochurePath;
    }

    // Add getters and setters
    public boolean isUserParticipating() {
        return isUserParticipating;
    }

    public void setIsUserParticipating(boolean userParticipating) {
        isUserParticipating = userParticipating;
    }

    public String getUserRole() {
        return userRole;
    }

    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }

    public int getCurrentUserId() {
        return currentUserId;
    }

    public void setCurrentUserId(int currentUserId) {
        this.currentUserId = currentUserId;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    // MISSING METHODS - ADD THESE FOR DatabaseHelper COMPATIBILITY
    public String getType() {
        return eventType;
    }

    public void setType(String type) {
        this.eventType = type;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getTeamId() {
        return teamId;
    }

    public void setTeamId(int teamId) {
        this.teamId = teamId;
    }

    public int getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(int createdBy) {
        this.createdBy = createdBy;
    }

    // ADDITIONAL MISSING METHODS
    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public String getCreatedByName() {
        return createdByName;
    }

    public void setCreatedByName(String createdByName) {
        this.createdByName = createdByName;
    }

    @Override
    public String toString() {
        return "Event{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", eventType='" + eventType + '\'' +
                ", dateTime='" + dateTime + '\'' +
                ", location='" + location + '\'' +
                '}';
    }
}

