package com.example.guarena.models;

public class Equipment {
    private int id;
    private String name;
    private String category;
    private String status; // available, borrowed, maintenance
    private int borrowedBy; // userId who borrowed
    private String borrowedDate; // when it was borrowed
    private String dueDate; // when it should be returned
    private String borrowedUserName;
    private String description;
    private int quantity; // Total available quantity
    private int availableQuantity; // Currently available
    private boolean isBorrowedByCurrentUser;

    // Constructors
    public Equipment() {}

    public Equipment(String name, String category, String description, int quantity) {
        this.name = name;
        this.category = category;
        this.description = description;
        this.quantity = quantity;
        this.availableQuantity = quantity;
        this.status = "available";
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getBorrowedBy() { return borrowedBy; }
    public void setBorrowedBy(int borrowedBy) { this.borrowedBy = borrowedBy; }

    // Alias methods for compatibility
    public int getCheckedOutBy() { return borrowedBy; }
    public void setCheckedOutBy(int userId) { this.borrowedBy = userId; }

    public String getBorrowedDate() { return borrowedDate; }
    public void setBorrowedDate(String borrowedDate) { this.borrowedDate = borrowedDate; }

    // Alias methods
    public String getCheckedOutDate() { return borrowedDate; }
    public void setCheckedOutDate(String date) { this.borrowedDate = date; }

    public String getDueDate() { return dueDate; }
    public void setDueDate(String dueDate) { this.dueDate = dueDate; }

    public String getBorrowedUserName() { return borrowedUserName; }
    public void setBorrowedUserName(String borrowedUserName) { this.borrowedUserName = borrowedUserName; }

    // Alias methods
    public String getCheckedOutUserName() { return borrowedUserName; }
    public void setCheckedOutUserName(String name) { this.borrowedUserName = name; }
    public String getCheckedOutByName() { return borrowedUserName; }
    public void setCheckedOutByName(String name) { this.borrowedUserName = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public int getAvailableQuantity() { return availableQuantity; }
    public void setAvailableQuantity(int availableQuantity) { this.availableQuantity = availableQuantity; }

    // Status check methods
    public boolean isAvailable() {
        return "available".equals(status) && availableQuantity > 0;
    }

    public boolean isBorrowed() {
        return "borrowed".equals(status);
    }

    public boolean isCheckedOut() {
        return isBorrowed();
    }

    public boolean isUnderMaintenance() {
        return "maintenance".equals(status);
    }

    @Override
    public String toString() {
        return "Equipment{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", category='" + category + '\'' +
                ", status='" + status + '\'' +
                ", availableQuantity=" + availableQuantity + "/" + quantity +
                '}';
    }

    public boolean isBorrowedByCurrentUser() {
        return isBorrowedByCurrentUser;
    }

    public void setIsBorrowedByCurrentUser(boolean isBorrowedByCurrentUser) {
        this.isBorrowedByCurrentUser = isBorrowedByCurrentUser;
    }
}
