package com.example.guarena.models;

public class Performance {
    private int id;
    private int playerId;
    private int eventId;
    private String stats; // JSON format for flexible stats
    private String dateRecorded;
    private String playerName;
    private String eventTitle;
    private double score;
    private String metrics;

    // Constructors
    public Performance() {}

    public Performance(int playerId, int eventId, String stats) {
        this.playerId = playerId;
        this.eventId = eventId;
        this.stats = stats;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getPlayerId() { return playerId; }
    public void setPlayerId(int playerId) { this.playerId = playerId; }

    public int getEventId() { return eventId; }
    public void setEventId(int eventId) { this.eventId = eventId; }

    public String getStats() { return stats; }
    public void setStats(String stats) { this.stats = stats; }

    public String getDateRecorded() { return dateRecorded; }
    public void setDateRecorded(String dateRecorded) { this.dateRecorded = dateRecorded; }

    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }

    public String getEventTitle() { return eventTitle; }
    public void setEventTitle(String eventTitle) { this.eventTitle = eventTitle; }

    public double getScore() { return score; }
    public void setScore(double score) { this.score = score; }

    public String getMetrics() { return metrics; }
    public void setMetrics(String metrics) { this.metrics = metrics; }

    @Override
    public String toString() {
        return "Performance{" +
                "id=" + id +
                ", playerName='" + playerName + '\'' +
                ", eventTitle='" + eventTitle + '\'' +
                ", score=" + score +
                '}';
    }
}
