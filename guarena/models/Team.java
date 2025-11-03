package com.example.guarena.models;

public class Team {
    private int id;
    private String name;
    private String sport;
    private int coachId;
    private String description;
    private String createdAt;
    private int playerCount;
    private String coachName;

    // Constructors
    public Team() {}

    public Team(String name, String sport, int coachId, String description) {
        this.name = name;
        this.sport = sport;
        this.coachId = coachId;
        this.description = description;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSport() { return sport; }
    public void setSport(String sport) { this.sport = sport; }

    public int getCoachId() { return coachId; }
    public void setCoachId(int coachId) { this.coachId = coachId; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }


    public int getPlayerCount() { return playerCount; }
    public void setPlayerCount(int playerCount) { this.playerCount = playerCount; }

    public String getCoachName() { return coachName; }
    public void setCoachName(String coachName) { this.coachName = coachName; }

    @Override
    public String toString() {
        return "Team{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", sport='" + sport + '\'' +
                ", coachId=" + coachId +
                ", description='" + description + '\'' +
                ", playerCount=" + playerCount +
                '}';
    }
    // Add these methods to your Team.java class
    public String getCreatedDate() {
        return createdAt; // Use existing field
    }

    public void setCreatedDate(String createdDate) {
        this.createdAt = createdDate; // Use existing field
    }

}


