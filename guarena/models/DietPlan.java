package com.example.guarena.models;

public class DietPlan {
    private int id;
    private int playerId;
    private String planName;
    private String meals; // JSON format for flexible meal structure
    private int calories;
    private int createdBy;
    private String createdAt;
    private String playerName;
    private String createdByName;

    // Constructors
    public DietPlan() {}

    public DietPlan(int playerId, String planName, String meals, int calories, int createdBy) {
        this.playerId = playerId;
        this.planName = planName;
        this.meals = meals;
        this.calories = calories;
        this.createdBy = createdBy;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getPlayerId() { return playerId; }
    public void setPlayerId(int playerId) { this.playerId = playerId; }

    public String getPlanName() { return planName; }
    public void setPlanName(String planName) { this.planName = planName; }

    public String getMeals() { return meals; }
    public void setMeals(String meals) { this.meals = meals; }

    public int getCalories() { return calories; }
    public void setCalories(int calories) { this.calories = calories; }

    public int getCreatedBy() { return createdBy; }
    public void setCreatedBy(int createdBy) { this.createdBy = createdBy; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }

    public String getCreatedByName() { return createdByName; }
    public void setCreatedByName(String createdByName) { this.createdByName = createdByName; }

    @Override
    public String toString() {
        return "DietPlan{" +
                "id=" + id +
                ", planName='" + planName + '\'' +
                ", playerName='" + playerName + '\'' +
                ", calories=" + calories +
                '}';
    }
}
