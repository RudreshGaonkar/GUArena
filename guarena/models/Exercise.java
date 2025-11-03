//package com.example.guarena.models;
//
//public class Exercise {
//    private int id;
//    private String name;
//    private String category; // strength, cardio, flexibility, sport_specific
//    private String description;
//    private String instructions;
//    private String videoUrl;
//    private String createdAt;
//    private String duration;
//    private String difficulty; // beginner, intermediate, advanced
//    private String targetMuscles;
//
//    // Constructors
//    public Exercise() {}
//
//    public Exercise(String name, String category, String description, String instructions) {
//        this.name = name;
//        this.category = category;
//        this.description = description;
//        this.instructions = instructions;
//    }
//
//    // Getters and Setters
//    public int getId() { return id; }
//    public void setId(int id) { this.id = id; }
//
//    public String getName() { return name; }
//    public void setName(String name) { this.name = name; }
//
//    public String getCategory() { return category; }
//    public void setCategory(String category) { this.category = category; }
//
//    public String getDescription() { return description; }
//    public void setDescription(String description) { this.description = description; }
//
//    public String getInstructions() { return instructions; }
//    public void setInstructions(String instructions) { this.instructions = instructions; }
//
//    public String getVideoUrl() { return videoUrl; }
//    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }
//
//    public String getCreatedAt() { return createdAt; }
//    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
//
//    public String getDuration() { return duration; }
//    public void setDuration(String duration) { this.duration = duration; }
//
//    public String getDifficulty() { return difficulty; }
//    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
//
//    public String getTargetMuscles() { return targetMuscles; }
//    public void setTargetMuscles(String targetMuscles) { this.targetMuscles = targetMuscles; }
//
//    @Override
//    public String toString() {
//        return "Exercise{" +
//                "id=" + id +
//                ", name='" + name + '\'' +
//                ", category='" + category + '\'' +
//                ", difficulty='" + difficulty + '\'' +
//                '}';
//    }
//}

package com.example.guarena.models;

public class Exercise {

    private int id;
    private String name;
    private String category; // strength, cardio, flexibility, sport_specific
    private String description;
    private String instructions;
    private String videoUrl;
    private String createdAt;
    private int duration; // ✅ CHANGE TO int (minutes)
    private String reps; // ✅ ADD MISSING FIELD
    private String difficulty; // beginner, intermediate, advanced
    private String targetMuscles;

    // Constructors
    public Exercise() {}

    public Exercise(String name, String category, String description, String instructions) {
        this.name = name;
        this.category = category;
        this.description = description;
        this.instructions = instructions;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getInstructions() { return instructions; }
    public void setInstructions(String instructions) { this.instructions = instructions; }

    public String getVideoUrl() { return videoUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    // ✅ CHANGE RETURN TYPE TO int
    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }

    // ✅ ADD MISSING METHOD
    public String getReps() { return reps; }
    public void setReps(String reps) { this.reps = reps; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public String getTargetMuscles() { return targetMuscles; }
    public void setTargetMuscles(String targetMuscles) { this.targetMuscles = targetMuscles; }

    @Override
    public String toString() {
        return "Exercise{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", category='" + category + '\'' +
                ", difficulty='" + difficulty + '\'' +
                '}';
    }
}
