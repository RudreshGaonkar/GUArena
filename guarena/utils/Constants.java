package com.example.guarena.utils;

/**
 * Application-wide constants
 */
public class Constants {

    // Database Constants
    public static final String DATABASE_NAME = "sports_management.db";
    public static final int DATABASE_VERSION = 1;

    // SharedPreferences Keys
    public static final String PREF_USER_SESSION = "UserSession";
    public static final String PREF_APP_SETTINGS = "AppPrefs";

    // Session Keys
    public static final String KEY_USER_ID = "userId";
    public static final String KEY_USERNAME = "username";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_ROLE = "role";
    public static final String KEY_FULL_NAME = "fullName";
    public static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    public static final String KEY_REMEMBER_ME = "rememberMe";
    public static final String KEY_FIRST_LAUNCH = "firstLaunch";

    // User Roles
    public static final String ROLE_ADMIN = "admin";
    public static final String ROLE_COACH = "coach";
    public static final String ROLE_STUDENT = "student";

    // Event Types
    public static final String EVENT_MATCH = "match";
    public static final String EVENT_PRACTICE = "practice";
    public static final String EVENT_TOURNAMENT = "tournament";
    public static final String EVENT_MEETING = "meeting";

    // Equipment Status
    public static final String EQUIPMENT_AVAILABLE = "available";
    public static final String EQUIPMENT_CHECKED_OUT = "checked_out";
    public static final String EQUIPMENT_MAINTENANCE = "maintenance";

    // Sports Categories
    public static final String[] SPORTS = {
            "Football", "Basketball", "Cricket", "Volleyball",
            "Badminton", "Table Tennis", "Tennis", "Hockey",
            "Swimming", "Athletics", "Baseball", "Soccer"
    };

    // Exercise Categories
    public static final String[] EXERCISE_CATEGORIES = {
            "Strength", "Cardio", "Flexibility", "Sport Specific"
    };

    // Equipment Categories
    public static final String[] EQUIPMENT_CATEGORIES = {
            "Balls", "Fitness", "Protective Gear", "Training Equipment", "Others"
    };

    // Image and File Constants
    public static final String IMAGE_DIRECTORY = "images";
    public static final String PROFILE_IMAGE_DIRECTORY = "profile_images";
    public static final String GALLERY_IMAGE_DIRECTORY = "gallery";
    public static final int MAX_IMAGE_SIZE = 1024; // KB
    public static final int PROFILE_IMAGE_SIZE = 200; // pixels

    // Validation Constants
    public static final int MIN_PASSWORD_LENGTH = 6;
    public static final int MIN_USERNAME_LENGTH = 4;
    public static final int MIN_FULLNAME_LENGTH = 3;
    public static final int MIN_PHONE_LENGTH = 10;

    // Performance Score Ranges
    public static final double MIN_PERFORMANCE_SCORE = 0.0;
    public static final double MAX_PERFORMANCE_SCORE = 10.0;

    // Pagination
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int DASHBOARD_RECENT_ITEMS = 5;

    // Date Formats
    public static final String DATE_FORMAT_DATABASE = "yyyy-MM-dd HH:mm:ss";
    public static final String DATE_FORMAT_DISPLAY = "MMM dd, yyyy";
    public static final String TIME_FORMAT_DISPLAY = "hh:mm a";
    public static final String DATETIME_FORMAT_DISPLAY = "MMM dd, yyyy hh:mm a";

    // Request Codes
    public static final int REQUEST_IMAGE_PICK = 1001;
    public static final int REQUEST_IMAGE_CAPTURE = 1002;
    public static final int REQUEST_PERMISSION_CAMERA = 1003;
    public static final int REQUEST_PERMISSION_STORAGE = 1004;

    // Admin Account
    public static final String DEFAULT_ADMIN_USERNAME = "admin";
    public static final String DEFAULT_ADMIN_PASSWORD = "admin123";
    public static final String DEFAULT_ADMIN_EMAIL = "admin@guarena.com";

    // App Info
    public static final String APP_NAME = "GUArena Sports Management";
    public static final String APP_VERSION = "1.0";
    public static final String DEVELOPER_NAME = "MCA Student";

    // Error Messages
    public static final String ERROR_NETWORK = "Network error occurred";
    public static final String ERROR_DATABASE = "Database error occurred";
    public static final String ERROR_VALIDATION = "Please check your input";
    public static final String ERROR_PERMISSION = "Permission denied";
    public static final String ERROR_FILE_NOT_FOUND = "File not found";

    // Success Messages
    public static final String SUCCESS_LOGIN = "Login successful";
    public static final String SUCCESS_REGISTER = "Registration successful";
    public static final String SUCCESS_UPDATE = "Updated successfully";
    public static final String SUCCESS_DELETE = "Deleted successfully";
    public static final String SUCCESS_CREATE = "Created successfully";

    // Prevent instantiation
    private Constants() {
        throw new UnsupportedOperationException("Constants class cannot be instantiated");
    }
}
