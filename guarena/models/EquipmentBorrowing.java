package com.example.guarena.models;

public class EquipmentBorrowing {
    private int id;
    private int equipmentId;
    private String equipmentName;
    private int userId;
    private String userName;
    private String userEmail;
    private String borrowedDate;
    private String dueDate;
    private String returnedDate;
    private String status; // borrowed, returned, overdue
    private int durationDays;

    // Constructors
    public EquipmentBorrowing() {}

    public EquipmentBorrowing(int equipmentId, int userId, String borrowedDate, String dueDate, int durationDays) {
        this.equipmentId = equipmentId;
        this.userId = userId;
        this.borrowedDate = borrowedDate;
        this.dueDate = dueDate;
        this.durationDays = durationDays;
        this.status = "borrowed";
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getEquipmentId() { return equipmentId; }
    public void setEquipmentId(int equipmentId) { this.equipmentId = equipmentId; }

    public String getEquipmentName() { return equipmentName; }
    public void setEquipmentName(String equipmentName) { this.equipmentName = equipmentName; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getBorrowedDate() { return borrowedDate; }
    public void setBorrowedDate(String borrowedDate) { this.borrowedDate = borrowedDate; }

    public String getDueDate() { return dueDate; }
    public void setDueDate(String dueDate) { this.dueDate = dueDate; }

    public String getReturnedDate() { return returnedDate; }
    public void setReturnedDate(String returnedDate) { this.returnedDate = returnedDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getDurationDays() { return durationDays; }
    public void setDurationDays(int durationDays) { this.durationDays = durationDays; }

    // Helper methods
    public boolean isOverdue() {
        return "overdue".equals(status);
    }

    public boolean isReturned() {
        return "returned".equals(status);
    }

    public boolean isBorrowed() {
        return "borrowed".equals(status);
    }
}
