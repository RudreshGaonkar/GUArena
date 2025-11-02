package com.example.guarena.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.guarena.models.ActivityItem;
import com.example.guarena.models.DietPlan;
import com.example.guarena.models.Equipment;
import com.example.guarena.models.EquipmentBorrowing;
import com.example.guarena.models.Event;
import com.example.guarena.models.Exercise;
import com.example.guarena.models.GalleryItem;
import com.example.guarena.models.Performance;
import com.example.guarena.models.Player;
import com.example.guarena.models.Team;
import com.example.guarena.models.User;
import com.example.guarena.utils.DateUtils;
import com.example.guarena.utils.EmailService;
import com.example.guarena.utils.EquipmentReminderWorker;
import com.example.guarena.utils.NotificationService;
import com.example.guarena.utils.PasswordUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;



public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "sports_management.db";
    private static final int DATABASE_VERSION = 7;

    // Table Names
    private static final String TABLE_USERS = "users";
    private static final String TABLE_TEAMS = "teams";
    private static final String TABLE_PLAYERS = "players";
    private static final String TABLE_EVENTS = "events";
    private static final String TABLE_EQUIPMENT = "equipment";
    private static final String TABLE_PERFORMANCE = "performance";
    private static final String TABLE_GALLERY = "gallery";
    private static final String TABLE_DIET_PLANS = "diet_plans";
    private static final String TABLE_EXERCISES = "exercises";

    // Users Table Columns
    private static final String COL_USER_ID = "id";
    private static final String COL_USER_USERNAME = "username";
    private static final String COL_USER_EMAIL = "email";
    private static final String COL_USER_PASSWORD = "password";
    private static final String COL_USER_ROLE = "role";
    private static final String COL_USER_PROFILE_IMAGE = "profile_image_path";
    private static final String COL_USER_FULL_NAME = "full_name";
    private static final String COL_USER_PHONE = "phone";
    private static final String COL_USER_CREATED_AT = "created_at";

    // Teams Table Columns
    private static final String COL_TEAM_ID = "id";
    private static final String COL_TEAM_NAME = "name";
    private static final String COL_TEAM_SPORT = "sport";
    private static final String COL_TEAM_COACH_ID = "coach_id";
    private static final String COL_TEAM_DESCRIPTION = "description";
    private static final String COL_TEAM_CREATED_DATE = "created_at";

    // Players Table Columns
    private static final String COL_PLAYER_ID = "id";
    private static final String COL_PLAYER_USER_ID = "user_id";
    private static final String COL_PLAYER_TEAM_ID = "team_id";
    private static final String COL_PLAYER_JERSEY_NUMBER = "jersey_number";
    private static final String COL_PLAYER_POSITION = "position";
    private static final String COL_PLAYER_HEIGHT = "height";
    private static final String COL_PLAYER_WEIGHT = "weight";

    // Events Table Columns - ✅ FIXED: Consistent naming
    private static final String COL_EVENT_ID = "id";
    private static final String COL_EVENT_TITLE = "title";
    private static final String COL_EVENT_DESCRIPTION = "description";
    private static final String COL_EVENT_TYPE = "event_type";
    private static final String COL_EVENT_DATETIME = "datetime";  // ✅ Primary column
    private static final String COL_EVENT_DATE_TIME = COL_EVENT_DATETIME;  // ✅ Alias for compatibility
    private static final String COL_EVENT_LOCATION = "location";
    private static final String COL_EVENT_TEAM_ID = "team_id";
    private static final String COL_EVENT_CREATED_BY = "created_by";
    private static final String COL_EVENT_CREATED_AT = "created_at";

    // Equipment Table Columns
    private static final String COL_EQUIPMENT_ID = "id";
    private static final String COL_EQUIPMENT_NAME = "name";
    private static final String COL_EQUIPMENT_CATEGORY = "category";
    private static final String COL_EQUIPMENT_STATUS = "status";
    private static final String COL_EQUIPMENT_DESCRIPTION = "description";
    private static final String COL_EQUIPMENT_CHECKED_OUT_BY = "checked_out_by";
    private static final String COL_EQUIPMENT_CHECKED_OUT_DATE = "checked_out_date";

    private static final String COL_EQUIPMENT_QUANTITY = "quantity";
    private static final String COL_EQUIPMENT_DUE_DATE = "due_date";

    private static final String COL_EQUIPMENT_AVAILABLE_QUANTITY = "available_quantity";

    private static final String TABLE_EQUIPMENT_BORROWINGS = "equipment_borrowings";
    private static final String COL_BORROWING_ID = "id";
    private static final String COL_BORROWING_EQUIPMENT_ID = "equipment_id";
    private static final String COL_BORROWING_USER_ID = "user_id";
    private static final String COL_BORROWING_BORROWED_DATE = "borrowed_date";
    private static final String COL_BORROWING_DUE_DATE = "due_date";
    private static final String COL_BORROWING_RETURNED_DATE = "returned_date";
    private static final String COL_BORROWING_STATUS = "status";
    private static final String COL_BORROWING_DURATION_DAYS = "duration_days";

//    private static final String COL_EQUIPMENT_DUE_DATE = "due_date";

    // Performance Table Columns
    private static final String COL_PERFORMANCE_ID = "id";
    private static final String COL_PERFORMANCE_PLAYER_ID = "player_id";
    private static final String COL_PERFORMANCE_EVENT_ID = "event_id";
    private static final String COL_PERFORMANCE_STATS = "stats";
    private static final String COL_PERFORMANCE_SCORE = "score";
    private static final String COL_PERFORMANCE_DATE_RECORDED = "date_recorded";

    // Gallery Table Columns
    private static final String COL_GALLERY_ID = "id";
    private static final String COL_GALLERY_TITLE = "title";
    private static final String COL_GALLERY_IMAGE_PATH = "image_path";
    private static final String COL_GALLERY_EVENT_ID = "event_id";
    private static final String COL_GALLERY_UPLOADED_BY = "uploaded_by";
    private static final String COL_GALLERY_UPLOADED_AT = "uploaded_at";

    // Diet Plans Table Columns
    private static final String COL_DIET_PLAN_ID = "id";
    private static final String COL_DIET_PLAN_PLAYER_ID = "player_id";
    private static final String COL_DIET_PLAN_NAME = "plan_name";
    private static final String COL_DIET_PLAN_MEALS = "meals";
    private static final String COL_DIET_PLAN_CALORIES = "calories";
    private static final String COL_DIET_PLAN_CREATED_BY = "created_by";
    private static final String COL_DIET_PLAN_CREATED_AT = "created_at";

    // Exercises Table Columns
    private static final String COL_EXERCISE_ID = "id";
    private static final String COL_EXERCISE_NAME = "name";
    private static final String COL_EXERCISE_CATEGORY = "category";
    private static final String COL_EXERCISE_DESCRIPTION = "description";
    private static final String COL_EXERCISE_INSTRUCTIONS = "instructions";
    private static final String COL_EXERCISE_DURATION = "duration";
    private static final String COL_EXERCISE_REPS = "reps";
    private static final String COL_EXERCISE_CREATED_AT = "created_at";

    private static final String TABLE_EMAIL_CONFIG = "email_config";
    private static final String COL_CONFIG_ID = "config_id";
    private static final String COL_CONFIG_EMAIL = "email_address";
    private static final String COL_CONFIG_PASSWORD = "email_password";
    private static final String COL_CONFIG_SMTP_HOST = "smtp_host";
    private static final String COL_CONFIG_SMTP_PORT = "smtp_port";
    private static final String COL_CONFIG_APP_NAME = "app_name";
    private static final String COL_EVENT_BROCHURE_PATH = "brochurepath";


    // Gallery table constants (add to top of DatabaseHelper with other constants)
//    private static final String TABLE_GALLERY = "gallery";
//    private static final String COL_GALLERY_ID = "id";
//    private static final String COL_GALLERY_TITLE = "title";
    private static final String COL_GALLERY_EVENT_NAME = "event_name";
    private static final String COL_GALLERY_DESCRIPTION = "description";
//    private static final String COL_GALLERY_IMAGE_PATH = "image_path";
//    private static final String COL_GALLERY_UPLOADED_BY = "uploaded_by";
    private static final String COL_GALLERY_UPLOADED_DATE = "uploaded_date";

    private Context context;


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTables(db);
        insertDefaultData(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d("DatabaseHelper", "Upgrading database from version " + oldVersion + " to " + newVersion);

        // Version 2: Add brochure column to events
        if (oldVersion < 2) {
            try {
                db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + COL_EVENT_BROCHURE_PATH + " TEXT");
                Log.d("DatabaseHelper", "Brochure column added successfully");
            } catch (Exception e) {
                Log.e("DatabaseHelper", "Error adding brochure column: " + e.getMessage());
            }
        }

        // Version 3: Add equipment borrowings and update equipment table
        if (oldVersion < 3) {
            // Create equipment_borrowings table
            try {
                String CREATE_EQUIPMENT_BORROWINGS_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_EQUIPMENT_BORROWINGS + "("
                        + COL_BORROWING_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                        + COL_BORROWING_EQUIPMENT_ID + " INTEGER,"
                        + COL_BORROWING_USER_ID + " INTEGER,"
                        + COL_BORROWING_BORROWED_DATE + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                        + COL_BORROWING_DUE_DATE + " TIMESTAMP,"
                        + COL_BORROWING_RETURNED_DATE + " TIMESTAMP,"
                        + COL_BORROWING_STATUS + " TEXT DEFAULT 'borrowed',"
                        + COL_BORROWING_DURATION_DAYS + " INTEGER,"
                        + "FOREIGN KEY(" + COL_BORROWING_EQUIPMENT_ID + ") REFERENCES " + TABLE_EQUIPMENT + "(" + COL_EQUIPMENT_ID + "),"
                        + "FOREIGN KEY(" + COL_BORROWING_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COL_USER_ID + ")"
                        + ")";
                db.execSQL(CREATE_EQUIPMENT_BORROWINGS_TABLE);
                Log.d("DatabaseHelper", "Equipment borrowings table created");
            } catch (Exception e) {
                Log.e("DatabaseHelper", "Error creating borrowings table: " + e.getMessage());
            }

            // Add quantity columns
            try {
                db.execSQL("ALTER TABLE " + TABLE_EQUIPMENT + " ADD COLUMN " + COL_EQUIPMENT_QUANTITY + " INTEGER DEFAULT 1");
                Log.d("DatabaseHelper", "Quantity column added");
            } catch (Exception e) {
                Log.e("DatabaseHelper", "Quantity column may already exist: " + e.getMessage());
            }

            try {
                db.execSQL("ALTER TABLE " + TABLE_EQUIPMENT + " ADD COLUMN " + COL_EQUIPMENT_AVAILABLE_QUANTITY + " INTEGER DEFAULT 1");
                Log.d("DatabaseHelper", "Available quantity column added");
            } catch (Exception e) {
                Log.e("DatabaseHelper", "Available quantity column may already exist: " + e.getMessage());
            }

            // Add due_date column
            try {
                db.execSQL("ALTER TABLE " + TABLE_EQUIPMENT + " ADD COLUMN " + COL_EQUIPMENT_DUE_DATE + " TIMESTAMP");
                Log.d("DatabaseHelper", "Due date column added");
            } catch (Exception e) {
                Log.e("DatabaseHelper", "Due date column may already exist: " + e.getMessage());
            }
        }

        if (oldVersion < 8) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_GALLERY);
            Log.d("DatabaseHelper", "Old gallery table dropped");
            // Create gallery table
            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_GALLERY + " (" +
                    COL_GALLERY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_GALLERY_TITLE + " TEXT NOT NULL, " +
                    COL_GALLERY_EVENT_NAME + " TEXT, " +
                    COL_GALLERY_DESCRIPTION + " TEXT, " +
                    COL_GALLERY_IMAGE_PATH + " TEXT NOT NULL, " +
                    COL_GALLERY_UPLOADED_BY + " INTEGER, " +
                    COL_GALLERY_UPLOADED_DATE + " DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY(" + COL_GALLERY_UPLOADED_BY + ") REFERENCES " + TABLE_USERS + "(" + COL_USER_ID + ")" +
                    ")");
        }

    }


    private void createTables(SQLiteDatabase db) {
        // Users Table
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + "("
                + COL_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COL_USER_USERNAME + " TEXT UNIQUE NOT NULL,"
                + COL_USER_EMAIL + " TEXT UNIQUE NOT NULL,"
                + COL_USER_PASSWORD + " TEXT NOT NULL,"
                + COL_USER_ROLE + " TEXT NOT NULL,"
                + COL_USER_PROFILE_IMAGE + " TEXT,"
                + COL_USER_FULL_NAME + " TEXT,"
                + COL_USER_PHONE + " TEXT,"
                + COL_USER_CREATED_AT + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
                + ")";


        // Teams Table
        String CREATE_TEAMS_TABLE = "CREATE TABLE " + TABLE_TEAMS + "("
                + COL_TEAM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COL_TEAM_NAME + " TEXT NOT NULL,"
                + COL_TEAM_SPORT + " TEXT NOT NULL,"
                + COL_TEAM_COACH_ID + " INTEGER,"
                + COL_TEAM_DESCRIPTION + " TEXT,"
                + COL_TEAM_CREATED_DATE + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                + "FOREIGN KEY(" + COL_TEAM_COACH_ID + ") REFERENCES " + TABLE_USERS + "(" + COL_USER_ID + ")"
                + ")";

        // Players Table
        String CREATE_PLAYERS_TABLE = "CREATE TABLE " + TABLE_PLAYERS + "("
                + COL_PLAYER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COL_PLAYER_USER_ID + " INTEGER,"
                + COL_PLAYER_TEAM_ID + " INTEGER,"
                + COL_PLAYER_JERSEY_NUMBER + " INTEGER,"
                + COL_PLAYER_POSITION + " TEXT,"
                + COL_PLAYER_HEIGHT + " REAL,"
                + COL_PLAYER_WEIGHT + " REAL,"
                + "FOREIGN KEY(" + COL_PLAYER_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COL_USER_ID + "),"
                + "FOREIGN KEY(" + COL_PLAYER_TEAM_ID + ") REFERENCES " + TABLE_TEAMS + "(" + COL_TEAM_ID + ")"
                + ")";

        // Events Table
        String CREATE_EVENTS_TABLE = "CREATE TABLE " + TABLE_EVENTS + "("
                + COL_EVENT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COL_EVENT_TITLE + " TEXT NOT NULL,"
                + COL_EVENT_DESCRIPTION + " TEXT,"
                + COL_EVENT_TYPE + " TEXT,"
                + COL_EVENT_DATETIME + " TIMESTAMP NOT NULL,"
                + COL_EVENT_LOCATION + " TEXT,"
                + COL_EVENT_TEAM_ID + " INTEGER,"
                + COL_EVENT_CREATED_BY + " INTEGER,"
                + COL_EVENT_BROCHURE_PATH + " TEXT," // ✅ NEW: Brochure path
                + COL_EVENT_CREATED_AT + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                + "FOREIGN KEY(" + COL_EVENT_TEAM_ID + ") REFERENCES " + TABLE_TEAMS + "(" + COL_TEAM_ID + "),"
                + "FOREIGN KEY(" + COL_EVENT_CREATED_BY + ") REFERENCES " + TABLE_USERS + "(" + COL_USER_ID + ")"
                + ")";

        // Equipment Table
        String CREATE_EQUIPMENT_TABLE = "CREATE TABLE " + TABLE_EQUIPMENT + "("
                + COL_EQUIPMENT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COL_EQUIPMENT_NAME + " TEXT NOT NULL,"
                + COL_EQUIPMENT_CATEGORY + " TEXT,"
                + COL_EQUIPMENT_STATUS + " TEXT DEFAULT 'available',"
                + COL_EQUIPMENT_DESCRIPTION + " TEXT,"
                + COL_EQUIPMENT_QUANTITY + " INTEGER DEFAULT 1,"
                + COL_EQUIPMENT_AVAILABLE_QUANTITY + " INTEGER DEFAULT 1,"
                + COL_EQUIPMENT_CHECKED_OUT_BY + " INTEGER,"
                + COL_EQUIPMENT_CHECKED_OUT_DATE + " TIMESTAMP,"
                + COL_EQUIPMENT_DUE_DATE + " TIMESTAMP,"
                + "FOREIGN KEY(" + COL_EQUIPMENT_CHECKED_OUT_BY + ") REFERENCES " + TABLE_USERS + "(" + COL_USER_ID + ")"
                + ")";

// Equipment Borrowings Table
        String CREATE_EQUIPMENT_BORROWINGS_TABLE = "CREATE TABLE " + TABLE_EQUIPMENT_BORROWINGS + "("
                + COL_BORROWING_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COL_BORROWING_EQUIPMENT_ID + " INTEGER,"
                + COL_BORROWING_USER_ID + " INTEGER,"
                + COL_BORROWING_BORROWED_DATE + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                + COL_BORROWING_DUE_DATE + " TIMESTAMP,"
                + COL_BORROWING_RETURNED_DATE + " TIMESTAMP,"
                + COL_BORROWING_STATUS + " TEXT DEFAULT 'borrowed',"
                + COL_BORROWING_DURATION_DAYS + " INTEGER,"
                + "FOREIGN KEY(" + COL_BORROWING_EQUIPMENT_ID + ") REFERENCES " + TABLE_EQUIPMENT + "(" + COL_EQUIPMENT_ID + "),"
                + "FOREIGN KEY(" + COL_BORROWING_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COL_USER_ID + ")"
                + ")";


        // Performance Table
        String CREATE_PERFORMANCE_TABLE = "CREATE TABLE " + TABLE_PERFORMANCE + "("
                + COL_PERFORMANCE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COL_PERFORMANCE_PLAYER_ID + " INTEGER,"
                + COL_PERFORMANCE_EVENT_ID + " INTEGER,"
                + COL_PERFORMANCE_STATS + " TEXT,"
                + COL_PERFORMANCE_SCORE + " REAL,"
                + COL_PERFORMANCE_DATE_RECORDED + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                + "FOREIGN KEY(" + COL_PERFORMANCE_PLAYER_ID + ") REFERENCES " + TABLE_PLAYERS + "(" + COL_PLAYER_ID + "),"
                + "FOREIGN KEY(" + COL_PERFORMANCE_EVENT_ID + ") REFERENCES " + TABLE_EVENTS + "(" + COL_EVENT_ID + ")"
                + ")";

        // Gallery Table
//        String CREATE_GALLERY_TABLE = "CREATE TABLE " + TABLE_GALLERY + "("
//                + COL_GALLERY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
//                + COL_GALLERY_TITLE + " TEXT,"
//                + COL_GALLERY_IMAGE_PATH + " TEXT NOT NULL,"
//                + COL_GALLERY_EVENT_ID + " INTEGER,"
//                + COL_GALLERY_UPLOADED_BY + " INTEGER,"
//                + COL_GALLERY_UPLOADED_AT + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
//                + "FOREIGN KEY(" + COL_GALLERY_EVENT_ID + ") REFERENCES " + TABLE_EVENTS + "(" + COL_EVENT_ID + "),"
//                + "FOREIGN KEY(" + COL_GALLERY_UPLOADED_BY + ") REFERENCES " + TABLE_USERS + "(" + COL_USER_ID + ")"
//                + ")";

        String CREATE_GALLERY_TABLE =
                "CREATE TABLE " + TABLE_GALLERY + " (" +
                        COL_GALLERY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COL_GALLERY_TITLE + " TEXT NOT NULL, " +
                        COL_GALLERY_EVENT_NAME + " TEXT, " +
                        COL_GALLERY_DESCRIPTION + " TEXT, " +
                        COL_GALLERY_IMAGE_PATH + " TEXT NOT NULL, " +
                        COL_GALLERY_UPLOADED_BY + " INTEGER, " +
                        COL_GALLERY_UPLOADED_DATE + " DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                        "FOREIGN KEY(" + COL_GALLERY_UPLOADED_BY + ") REFERENCES " + TABLE_USERS + "(" + COL_USER_ID + ")" +
                        ")";


        // Diet Plans Table
        String CREATE_DIET_PLANS_TABLE = "CREATE TABLE " + TABLE_DIET_PLANS + "("
                + COL_DIET_PLAN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COL_DIET_PLAN_PLAYER_ID + " INTEGER,"
                + COL_DIET_PLAN_NAME + " TEXT NOT NULL,"
                + COL_DIET_PLAN_MEALS + " TEXT,"
                + COL_DIET_PLAN_CALORIES + " INTEGER,"
                + COL_DIET_PLAN_CREATED_BY + " INTEGER,"
                + COL_DIET_PLAN_CREATED_AT + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                + "FOREIGN KEY(" + COL_DIET_PLAN_PLAYER_ID + ") REFERENCES " + TABLE_PLAYERS + "(" + COL_PLAYER_ID + "),"
                + "FOREIGN KEY(" + COL_DIET_PLAN_CREATED_BY + ") REFERENCES " + TABLE_USERS + "(" + COL_USER_ID + ")"
                + ")";

        // Exercises Table
        String CREATE_EXERCISES_TABLE = "CREATE TABLE " + TABLE_EXERCISES + "("
                + COL_EXERCISE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COL_EXERCISE_NAME + " TEXT NOT NULL,"
                + COL_EXERCISE_CATEGORY + " TEXT,"
                + COL_EXERCISE_DESCRIPTION + " TEXT,"
                + COL_EXERCISE_INSTRUCTIONS + " TEXT,"
                + COL_EXERCISE_DURATION + " INTEGER,"
                + COL_EXERCISE_REPS + " TEXT,"
                + COL_EXERCISE_CREATED_AT + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
                + ")";
        String CREATE_EMAIL_CONFIG_TABLE = "CREATE TABLE " + TABLE_EMAIL_CONFIG + "("
                + COL_CONFIG_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COL_CONFIG_EMAIL + " TEXT NOT NULL,"
                + COL_CONFIG_PASSWORD + " TEXT NOT NULL,"
                + COL_CONFIG_SMTP_HOST + " TEXT DEFAULT 'smtp.gmail.com',"
                + COL_CONFIG_SMTP_PORT + " TEXT DEFAULT '587',"
                + COL_CONFIG_APP_NAME + " TEXT DEFAULT 'GuArena Sports'"
                + ")";



        // Execute table creation
        db.execSQL(CREATE_USERS_TABLE);
        db.execSQL(CREATE_TEAMS_TABLE);
        db.execSQL(CREATE_PLAYERS_TABLE);
        db.execSQL(CREATE_EVENTS_TABLE);
        db.execSQL(CREATE_EQUIPMENT_TABLE);
        db.execSQL(CREATE_EQUIPMENT_BORROWINGS_TABLE);
        db.execSQL(CREATE_PERFORMANCE_TABLE);
        db.execSQL(CREATE_GALLERY_TABLE);
        db.execSQL(CREATE_DIET_PLANS_TABLE);
        db.execSQL(CREATE_EXERCISES_TABLE);
        db.execSQL(CREATE_EMAIL_CONFIG_TABLE);

        Log.d("DatabaseHelper", "All tables created successfully");
    }

    private void insertDefaultData(SQLiteDatabase db) {
        createDefaultAdmin(db);

        // Insert sample coach
        ContentValues coachUser = new ContentValues();
        coachUser.put(COL_USER_USERNAME, "coach1");
        coachUser.put(COL_USER_EMAIL, "coach1@guarena.com");
        coachUser.put(COL_USER_PASSWORD, PasswordUtils.hashPassword("coach123"));
        coachUser.put(COL_USER_ROLE, "coach");
        coachUser.put(COL_USER_FULL_NAME, "John Coach");
        coachUser.put(COL_USER_PHONE, "9876543210");
        long coachId = db.insert(TABLE_USERS, null, coachUser);

        // Insert sample student
        ContentValues studentUser = new ContentValues();
        studentUser.put(COL_USER_USERNAME, "student1");
        studentUser.put(COL_USER_EMAIL, "student1@guarena.com");
        studentUser.put(COL_USER_PASSWORD, PasswordUtils.hashPassword("student123"));
        studentUser.put(COL_USER_ROLE, "student");
        studentUser.put(COL_USER_FULL_NAME, "John Student");
        studentUser.put(COL_USER_PHONE, "5555555555");
        long studentId = db.insert(TABLE_USERS, null, studentUser);

        // Insert sample team
        ContentValues team = new ContentValues();
        team.put(COL_TEAM_NAME, "Football Tigers");
        team.put(COL_TEAM_SPORT, "Football");
        team.put(COL_TEAM_COACH_ID, coachId);
        team.put(COL_TEAM_DESCRIPTION, "University football team");
        long teamId = db.insert(TABLE_TEAMS, null, team);

        // Insert sample player
        ContentValues player = new ContentValues();
        player.put(COL_PLAYER_USER_ID, studentId);
        player.put(COL_PLAYER_TEAM_ID, teamId);
        player.put(COL_PLAYER_JERSEY_NUMBER, 10);
        player.put(COL_PLAYER_POSITION, "Forward");
        player.put(COL_PLAYER_HEIGHT, 175.0);
        player.put(COL_PLAYER_WEIGHT, 70.0);
        db.insert(TABLE_PLAYERS, null, player);

        insertSampleEquipment(db);
        insertSampleExercises(db);
        insertDefaultEmailConfig(db);

        Log.d("DatabaseHelper", "Default data inserted successfully");
    }

    private void createDefaultAdmin(SQLiteDatabase db) {
        try {
            String checkAdminQuery = "SELECT COUNT(*) FROM " + TABLE_USERS + " WHERE " + COL_USER_ROLE + " = 'admin'";
            Cursor cursor = db.rawQuery(checkAdminQuery, null);
            cursor.moveToFirst();
            int adminCount = cursor.getInt(0);
            cursor.close();

            if (adminCount == 0) {
                ContentValues adminValues = new ContentValues();
                adminValues.put(COL_USER_USERNAME, "admin");
                adminValues.put(COL_USER_EMAIL, "admin@guarena.com");
                adminValues.put(COL_USER_FULL_NAME, "System Administrator");
                adminValues.put(COL_USER_PHONE, "1234567890");
                adminValues.put(COL_USER_PASSWORD, PasswordUtils.hashPassword("admin123"));
                adminValues.put(COL_USER_ROLE, "admin");

                long adminId = db.insert(TABLE_USERS, null, adminValues);
                if (adminId > 0) {
                    Log.d("DatabaseHelper", "Default admin created successfully");
                    Log.d("DatabaseHelper", "Username: admin | Password: admin123");
                }
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error creating default admin: " + e.getMessage());
        }
    }

    private void insertSampleEquipment(SQLiteDatabase db) {
        String[] equipmentNames = {"Football", "Basketball", "Tennis Racket", "Volleyball", "Cricket Bat"};
        String[] categories = {"Balls", "Balls", "Rackets", "Balls", "Bats"};

        for (int i = 0; i < equipmentNames.length; i++) {
            ContentValues equipment = new ContentValues();
            equipment.put(COL_EQUIPMENT_NAME, equipmentNames[i]);
            equipment.put(COL_EQUIPMENT_CATEGORY, categories[i]);
            equipment.put(COL_EQUIPMENT_STATUS, "available");
            equipment.put(COL_EQUIPMENT_DESCRIPTION, "Sample " + equipmentNames[i].toLowerCase());
            db.insert(TABLE_EQUIPMENT, null, equipment);
        }
    }

    private void insertSampleExercises(SQLiteDatabase db) {
        String[][] exercises = {
                {"Push-ups", "Strength", "Upper body strength exercise", "Start in plank position. Lower body until chest nearly touches floor. Push back up.", "15", "3x10"},
                {"Running", "Cardio", "Cardiovascular exercise for endurance", "Maintain steady pace. Focus on breathing and form.", "30", ""},
                {"Stretching", "Flexibility", "Improve flexibility and prevent injury", "Hold each stretch for 30 seconds. Don't bounce.", "10", ""},
                {"Squats", "Strength", "Lower body strength exercise", "Stand with feet shoulder-width apart. Lower as if sitting in chair.", "20", "3x12"}
        };

        for (String[] exercise : exercises) {
            ContentValues values = new ContentValues();
            values.put(COL_EXERCISE_NAME, exercise[0]);
            values.put(COL_EXERCISE_CATEGORY, exercise[1]);
            values.put(COL_EXERCISE_DESCRIPTION, exercise[2]);
            values.put(COL_EXERCISE_INSTRUCTIONS, exercise[3]);
            values.put(COL_EXERCISE_DURATION, Integer.parseInt(exercise[4]));
            values.put(COL_EXERCISE_REPS, exercise[5]);
            db.insert(TABLE_EXERCISES, null, values);
        }
    }

    // ✅ FIXED: Helper method for current date time
    private String getCurrentDateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }

    // ==================== USER AUTHENTICATION METHODS ====================

    public long registerUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        long result = -1;

        try {
            if (user.getRole().equals("admin")) {
                Log.w("DatabaseHelper", "Admin registration attempt blocked");
                return -1;
            }

            ContentValues values = new ContentValues();
            values.put(COL_USER_USERNAME, user.getUsername());
            values.put(COL_USER_EMAIL, user.getEmail());
            values.put(COL_USER_PASSWORD, PasswordUtils.hashPassword(user.getPassword()));
            values.put(COL_USER_ROLE, user.getRole());
            values.put(COL_USER_FULL_NAME, user.getFullName());
            values.put(COL_USER_PHONE, user.getPhone());

            result = db.insert(TABLE_USERS, null, values);
            Log.d("DatabaseHelper", "User registered with ID: " + result);
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error registering user: " + e.getMessage());
        } finally {
            db.close();
        }

        return result;
    }

    public User authenticateUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        User user = null;

        try {
            String[] columns = {COL_USER_ID, COL_USER_USERNAME, COL_USER_EMAIL,
                    COL_USER_ROLE, COL_USER_FULL_NAME, COL_USER_PHONE,
                    COL_USER_PROFILE_IMAGE, COL_USER_PASSWORD};
            String selection = COL_USER_USERNAME + " = ?";
            String[] selectionArgs = {username};

            Cursor cursor = db.query(TABLE_USERS, columns, selection, selectionArgs,
                    null, null, null);

            if (cursor.moveToFirst()) {
                String hashedPassword = cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_PASSWORD));
                if (PasswordUtils.verifyPassword(password, hashedPassword)) {
                    user = new User();
                    user.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_USER_ID)));
                    user.setUsername(cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_USERNAME)));
                    user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_EMAIL)));
                    user.setRole(cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_ROLE)));
                    user.setFullName(cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_FULL_NAME)));
                    user.setPhone(cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_PHONE)));
                    user.setProfileImagePath(cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_PROFILE_IMAGE)));

                    Log.d("DatabaseHelper", "User authenticated: " + username + " [" + user.getRole() + "]");
                }
            }
            cursor.close();
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error authenticating user: " + e.getMessage());
        } finally {
            db.close();
        }

        return user;
    }

    public boolean isUserExists(String username, String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        boolean exists = false;

        try {
            String selection = COL_USER_USERNAME + " = ? OR " + COL_USER_EMAIL + " = ?";
            String[] selectionArgs = {username, email};
            Cursor cursor = db.query(TABLE_USERS, new String[]{COL_USER_ID},
                    selection, selectionArgs, null, null, null);
            exists = cursor.getCount() > 0;
            cursor.close();
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error checking user existence: " + e.getMessage());
        } finally {
            db.close();
        }

        return exists;
    }

    public User getUserById(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        User user = null;

        try {
            String[] columns = {COL_USER_ID, COL_USER_USERNAME, COL_USER_EMAIL,
                    COL_USER_ROLE, COL_USER_FULL_NAME, COL_USER_PHONE,
                    COL_USER_PROFILE_IMAGE};
            String selection = COL_USER_ID + " = ?";
            String[] selectionArgs = {String.valueOf(userId)};

            Cursor cursor = db.query(TABLE_USERS, columns, selection, selectionArgs,
                    null, null, null);

            if (cursor.moveToFirst()) {
                user = new User();
                user.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_USER_ID)));
                user.setUsername(cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_USERNAME)));
                user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_EMAIL)));
                user.setRole(cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_ROLE)));
                user.setFullName(cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_FULL_NAME)));
                user.setPhone(cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_PHONE)));
                user.setProfileImagePath(cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_PROFILE_IMAGE)));
            }
            cursor.close();
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error getting user by ID: " + e.getMessage());
        } finally {
            db.close();
        }

        return user;
    }

    // ==================== EVENT CRUD OPERATIONS ====================

    public long createEvent(Event event) {
        SQLiteDatabase db = this.getWritableDatabase();
        long result = -1;

        try {
            ContentValues values = new ContentValues();
            values.put(COL_EVENT_TITLE, event.getTitle());
            values.put(COL_EVENT_DESCRIPTION, event.getDescription());
            values.put(COL_EVENT_TYPE, event.getType());
            values.put(COL_EVENT_DATETIME, event.getDateTime());
            values.put(COL_EVENT_LOCATION, event.getLocation());
            values.put(COL_EVENT_TEAM_ID, event.getTeamId());
            values.put(COL_EVENT_CREATED_BY, event.getCreatedBy());
            values.put(COL_EVENT_BROCHURE_PATH, event.getBrochurePath());

            result = db.insert(TABLE_EVENTS, null, values);
            Log.d("DatabaseHelper", "Event created with ID: " + result);
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error creating event: " + e.getMessage());
        } finally {
            db.close();
        }

        return result;
    }

    public List<Event> getAllEvents() {
        List<Event> eventList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT e.*, t.name as team_name, u.full_name as created_by_name FROM " + TABLE_EVENTS + " e " +
                "LEFT JOIN " + TABLE_TEAMS + " t ON e." + COL_EVENT_TEAM_ID + " = t." + COL_TEAM_ID + " " +
                "LEFT JOIN " + TABLE_USERS + " u ON e." + COL_EVENT_CREATED_BY + " = u." + COL_USER_ID + " " +
                "ORDER BY e." + COL_EVENT_DATETIME + " ASC";

        Cursor cursor = db.rawQuery(query, null);
        while (cursor.moveToNext()) {
            Event event = createEventFromCursor(cursor);
            eventList.add(event);
        }

        cursor.close();
        db.close();
        return eventList;
    }

    public boolean updateEvent(Event event) {
        SQLiteDatabase db = this.getWritableDatabase();
        boolean success = false;

        try {
            ContentValues values = new ContentValues();
            values.put(COL_EVENT_TITLE, event.getTitle());
            values.put(COL_EVENT_DESCRIPTION, event.getDescription());
            values.put(COL_EVENT_TYPE, event.getType());
            values.put(COL_EVENT_DATETIME, event.getDateTime());
            values.put(COL_EVENT_LOCATION, event.getLocation());
            values.put(COL_EVENT_TEAM_ID, event.getTeamId());
            values.put(COL_EVENT_BROCHURE_PATH, event.getBrochurePath());

            String whereClause = COL_EVENT_ID + " = ?";
            String[] whereArgs = {String.valueOf(event.getId())};

            int rowsAffected = db.update(TABLE_EVENTS, values, whereClause, whereArgs);
            success = rowsAffected > 0;
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error updating event: " + e.getMessage());
        } finally {
            db.close();
        }

        return success;
    }

    public boolean deleteEvent(int eventId) {
        SQLiteDatabase db = this.getWritableDatabase();
        boolean success = false;

        try {
            String whereClause = COL_EVENT_ID + " = ?";
            String[] whereArgs = {String.valueOf(eventId)};

            int rowsAffected = db.delete(TABLE_EVENTS, whereClause, whereArgs);
            success = rowsAffected > 0;
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error deleting event: " + e.getMessage());
        } finally {
            db.close();
        }

        return success;
    }

    // ==================== TEAM CRUD OPERATIONS ====================

    public long createTeam(Team team) {
        SQLiteDatabase db = this.getWritableDatabase();
        long result = -1;

        try {
            ContentValues values = new ContentValues();
            values.put(COL_TEAM_NAME, team.getName());
            values.put(COL_TEAM_SPORT, team.getSport());
            values.put(COL_TEAM_COACH_ID, team.getCoachId());
            values.put(COL_TEAM_DESCRIPTION, team.getDescription());

            result = db.insert(TABLE_TEAMS, null, values);
            Log.d("DatabaseHelper", "Team created with ID: " + result);
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error creating team: " + e.getMessage());
        } finally {
            db.close();
        }

        return result;
    }

    public List<Team> getAllTeams() {
        List<Team> teamList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT * FROM " + TABLE_TEAMS + " ORDER BY " + COL_TEAM_NAME + " ASC";
        Cursor cursor = db.rawQuery(query, null);

        while (cursor.moveToNext()) {
            Team team = new Team();
            team.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_TEAM_ID)));
            team.setName(cursor.getString(cursor.getColumnIndexOrThrow(COL_TEAM_NAME)));
            team.setSport(cursor.getString(cursor.getColumnIndexOrThrow(COL_TEAM_SPORT)));
            team.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(COL_TEAM_DESCRIPTION)));
            team.setCoachId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_TEAM_COACH_ID)));
            team.setCreatedDate(cursor.getString(cursor.getColumnIndexOrThrow(COL_TEAM_CREATED_DATE)));
            teamList.add(team);
        }

        cursor.close();
        db.close();
        return teamList;
    }

    // ==================== PRODUCTION READY METHODS FOR HomeFragment ====================

    // ✅ Get students for a specific team
    public List<User> getStudentsForTeam(int teamId) {
        List<User> studentList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT u.* FROM " + TABLE_USERS + " u " +
                "INNER JOIN " + TABLE_PLAYERS + " p ON u." + COL_USER_ID + " = p." + COL_PLAYER_USER_ID + " " +
                "WHERE p." + COL_PLAYER_TEAM_ID + " = ? AND u." + COL_USER_ROLE + " = 'student'";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(teamId)});

        while (cursor.moveToNext()) {
            User student = new User();
            student.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_USER_ID)));
            student.setUsername(cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_USERNAME)));
            student.setFullName(cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_FULL_NAME)));
            student.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_EMAIL)));
            student.setPhone(cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_PHONE)));
            student.setRole(cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_ROLE)));
            student.setProfileImagePath(cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_PROFILE_IMAGE)));
            studentList.add(student);
        }

        cursor.close();
        db.close();
        return studentList;
    }

    // ✅ Get teams that a coach manages
    public List<Team> getCoachTeams(int coachId) {
        List<Team> teamList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT * FROM " + TABLE_TEAMS + " WHERE " + COL_TEAM_COACH_ID + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(coachId)});

        while (cursor.moveToNext()) {
            Team team = new Team();
            team.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_TEAM_ID)));
            team.setName(cursor.getString(cursor.getColumnIndexOrThrow(COL_TEAM_NAME)));
            team.setSport(cursor.getString(cursor.getColumnIndexOrThrow(COL_TEAM_SPORT)));
            team.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(COL_TEAM_DESCRIPTION)));
            team.setCoachId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_TEAM_COACH_ID)));
            team.setCreatedDate(cursor.getString(cursor.getColumnIndexOrThrow(COL_TEAM_CREATED_DATE)));
            teamList.add(team);
        }

        cursor.close();
        db.close();
        return teamList;
    }

    // ✅ Get teams that a student is part of
    public List<Team> getStudentTeams(int studentId) {
        List<Team> teamList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT t.* FROM " + TABLE_TEAMS + " t " +
                "INNER JOIN " + TABLE_PLAYERS + " p ON t." + COL_TEAM_ID + " = p." + COL_PLAYER_TEAM_ID + " " +
                "WHERE p." + COL_PLAYER_USER_ID + " = ?";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(studentId)});

        while (cursor.moveToNext()) {
            Team team = new Team();
            team.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_TEAM_ID)));
            team.setName(cursor.getString(cursor.getColumnIndexOrThrow(COL_TEAM_NAME)));
            team.setSport(cursor.getString(cursor.getColumnIndexOrThrow(COL_TEAM_SPORT)));
            team.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(COL_TEAM_DESCRIPTION)));
            team.setCoachId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_TEAM_COACH_ID)));
            team.setCreatedDate(cursor.getString(cursor.getColumnIndexOrThrow(COL_TEAM_CREATED_DATE)));
            teamList.add(team);
        }

        cursor.close();
        db.close();
        return teamList;
    }

    // ✅ Get events for student based on their teams
    public List<Event> getStudentEvents(int studentId) {
        List<Event> eventList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT DISTINCT e.*, t.name as team_name, u.full_name as created_by_name FROM " + TABLE_EVENTS + " e " +
                "LEFT JOIN " + TABLE_TEAMS + " t ON e." + COL_EVENT_TEAM_ID + " = t." + COL_TEAM_ID + " " +
                "LEFT JOIN " + TABLE_USERS + " u ON e." + COL_EVENT_CREATED_BY + " = u." + COL_USER_ID + " " +
                "INNER JOIN " + TABLE_PLAYERS + " p ON e." + COL_EVENT_TEAM_ID + " = p." + COL_PLAYER_TEAM_ID + " " +
                "WHERE p." + COL_PLAYER_USER_ID + " = ? " +
                "ORDER BY e." + COL_EVENT_DATETIME + " ASC";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(studentId)});

        while (cursor.moveToNext()) {
            Event event = createEventFromCursor(cursor);
            eventList.add(event);
        }

        cursor.close();
        db.close();
        return eventList;
    }

    // ✅ Get events created by a coach
    public List<Event> getCoachEvents(int coachId) {
        List<Event> eventList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT e.*, t.name as team_name, u.full_name as created_by_name FROM " + TABLE_EVENTS + " e " +
                "LEFT JOIN " + TABLE_TEAMS + " t ON e." + COL_EVENT_TEAM_ID + " = t." + COL_TEAM_ID + " " +
                "LEFT JOIN " + TABLE_USERS + " u ON e." + COL_EVENT_CREATED_BY + " = u." + COL_USER_ID + " " +
                "WHERE e." + COL_EVENT_CREATED_BY + " = ? " +
                "ORDER BY e." + COL_EVENT_DATETIME + " ASC";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(coachId)});

        while (cursor.moveToNext()) {
            Event event = createEventFromCursor(cursor);
            eventList.add(event);
        }

        cursor.close();
        db.close();
        return eventList;
    }

    // ✅ Helper method to create Event from cursor (NO DUPLICATES)
    private Event createEventFromCursor(Cursor cursor) {
        Event event = new Event();
        event.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_EVENT_ID)));
        event.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(COL_EVENT_TITLE)));
        event.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(COL_EVENT_DESCRIPTION)));
        event.setType(cursor.getString(cursor.getColumnIndexOrThrow(COL_EVENT_TYPE)));
        event.setDateTime(cursor.getString(cursor.getColumnIndexOrThrow(COL_EVENT_DATETIME)));
        event.setLocation(cursor.getString(cursor.getColumnIndexOrThrow(COL_EVENT_LOCATION)));
        event.setTeamId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_EVENT_TEAM_ID)));
        event.setCreatedBy(cursor.getInt(cursor.getColumnIndexOrThrow(COL_EVENT_CREATED_BY)));

        int brochureIndex = cursor.getColumnIndex(COL_EVENT_BROCHURE_PATH);
        if (brochureIndex != -1) {
            String brochurePath = cursor.getString(brochureIndex);
            event.setBrochurePath(brochurePath);
            Log.d("DatabaseHelper", "Brochure path retrieved: " + brochurePath); // ✅ DEBUG
        } else {
            Log.d("DatabaseHelper", "Brochure column not found!"); // ✅ DEBUG
        }

        // Set additional info
        int teamNameIndex = cursor.getColumnIndex("team_name");
        int createdByNameIndex = cursor.getColumnIndex("created_by_name");

        if (teamNameIndex != -1) {
            event.setTeamName(cursor.getString(teamNameIndex));
        }
        if (createdByNameIndex != -1) {
            event.setCreatedByName(cursor.getString(createdByNameIndex));
        }

        return event;
    }

    // ✅ Get event by ID with full details
    public Event getEventById(int eventId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Event event = null;

        String query = "SELECT e.*, t.name as team_name, u.full_name as created_by_name FROM " + TABLE_EVENTS + " e " +
                "LEFT JOIN " + TABLE_TEAMS + " t ON e." + COL_EVENT_TEAM_ID + " = t." + COL_TEAM_ID + " " +
                "LEFT JOIN " + TABLE_USERS + " u ON e." + COL_EVENT_CREATED_BY + " = u." + COL_USER_ID + " " +
                "WHERE e." + COL_EVENT_ID + " = ?";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(eventId)});

        if (cursor.moveToFirst()) {
            event = createEventFromCursor(cursor);
        }

        cursor.close();
        db.close();
        return event;
    }

    // ✅ Get today's events count for dashboard
    public int getTodayEventsCount(int userId, String userRole) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query;
        String[] params;

        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        switch (userRole) {
            case "admin":
                query = "SELECT COUNT(*) FROM " + TABLE_EVENTS + " WHERE DATE(" + COL_EVENT_DATETIME + ") = ?";
                params = new String[]{today};
                break;
            case "coach":
                query = "SELECT COUNT(*) FROM " + TABLE_EVENTS + " WHERE DATE(" + COL_EVENT_DATETIME + ") = ? AND " + COL_EVENT_CREATED_BY + " = ?";
                params = new String[]{today, String.valueOf(userId)};
                break;
            case "student":
                query = "SELECT COUNT(DISTINCT e." + COL_EVENT_ID + ") FROM " + TABLE_EVENTS + " e " +
                        "INNER JOIN " + TABLE_PLAYERS + " p ON e." + COL_EVENT_TEAM_ID + " = p." + COL_PLAYER_TEAM_ID + " " +
                        "WHERE DATE(e." + COL_EVENT_DATETIME + ") = ? AND p." + COL_PLAYER_USER_ID + " = ?";
                params = new String[]{today, String.valueOf(userId)};
                break;
            default:
                return 0;
        }

        Cursor cursor = db.rawQuery(query, params);
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        db.close();
        return count;
    }

    // ✅ Get user teams count for dashboard
    public int getUserTeamsCount(int userId, String userRole) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query;
        String[] params;

        switch (userRole) {
            case "admin":
                query = "SELECT COUNT(*) FROM " + TABLE_TEAMS;
                params = new String[]{};
                break;
            case "coach":
                query = "SELECT COUNT(*) FROM " + TABLE_TEAMS + " WHERE " + COL_TEAM_COACH_ID + " = ?";
                params = new String[]{String.valueOf(userId)};
                break;
            case "student":
                query = "SELECT COUNT(*) FROM " + TABLE_TEAMS + " t " +
                        "INNER JOIN " + TABLE_PLAYERS + " p ON t." + COL_TEAM_ID + " = p." + COL_PLAYER_TEAM_ID + " " +
                        "WHERE p." + COL_PLAYER_USER_ID + " = ?";
                params = new String[]{String.valueOf(userId)};
                break;
            default:
                return 0;
        }

        Cursor cursor = db.rawQuery(query, params);
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        db.close();
        return count;
    }

    // ✅ Get upcoming events for dashboard
    public List<Event> getUpcomingEvents(int userId, String userRole, int limit) {
        List<Event> eventList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String currentDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        String query;
        String[] params;

        switch (userRole) {
            case "admin":
                query = "SELECT e.*, t.name as team_name, u.full_name as created_by_name FROM " + TABLE_EVENTS + " e " +
                        "LEFT JOIN " + TABLE_TEAMS + " t ON e." + COL_EVENT_TEAM_ID + " = t." + COL_TEAM_ID + " " +
                        "LEFT JOIN " + TABLE_USERS + " u ON e." + COL_EVENT_CREATED_BY + " = u." + COL_USER_ID + " " +
                        "WHERE e." + COL_EVENT_DATETIME + " > ? " +
                        "ORDER BY e." + COL_EVENT_DATETIME + " ASC LIMIT ?";
                params = new String[]{currentDateTime, String.valueOf(limit)};
                break;
            case "coach":
                query = "SELECT e.*, t.name as team_name, u.full_name as created_by_name FROM " + TABLE_EVENTS + " e " +
                        "LEFT JOIN " + TABLE_TEAMS + " t ON e." + COL_EVENT_TEAM_ID + " = t." + COL_TEAM_ID + " " +
                        "LEFT JOIN " + TABLE_USERS + " u ON e." + COL_EVENT_CREATED_BY + " = u." + COL_USER_ID + " " +
                        "WHERE e." + COL_EVENT_DATETIME + " > ? AND e." + COL_EVENT_CREATED_BY + " = ? " +
                        "ORDER BY e." + COL_EVENT_DATETIME + " ASC LIMIT ?";
                params = new String[]{currentDateTime, String.valueOf(userId), String.valueOf(limit)};
                break;
            case "student":
                query = "SELECT DISTINCT e.*, t.name as team_name, u.full_name as created_by_name FROM " + TABLE_EVENTS + " e " +
                        "LEFT JOIN " + TABLE_TEAMS + " t ON e." + COL_EVENT_TEAM_ID + " = t." + COL_TEAM_ID + " " +
                        "LEFT JOIN " + TABLE_USERS + " u ON e." + COL_EVENT_CREATED_BY + " = u." + COL_USER_ID + " " +
                        "INNER JOIN " + TABLE_PLAYERS + " p ON e." + COL_EVENT_TEAM_ID + " = p." + COL_PLAYER_TEAM_ID + " " +
                        "WHERE e." + COL_EVENT_DATETIME + " > ? AND p." + COL_PLAYER_USER_ID + " = ? " +
                        "ORDER BY e." + COL_EVENT_DATETIME + " ASC LIMIT ?";
                params = new String[]{currentDateTime, String.valueOf(userId), String.valueOf(limit)};
                break;
            default:
                return eventList;
        }

        Cursor cursor = db.rawQuery(query, params);
        while (cursor.moveToNext()) {
            Event event = createEventFromCursor(cursor);
            eventList.add(event);
        }

        cursor.close();
        db.close();
        return eventList;
    }

    // ✅ Get recent activity for dashboard
    public List<ActivityItem> getRecentActivity(int userId, String userRole, int limit) {
        List<ActivityItem> activityList = new ArrayList<>();

        // Get recent events as activities
        List<Event> recentEvents = getUpcomingEvents(userId, userRole, limit / 2);
        for (Event event : recentEvents) {
            ActivityItem activity = new ActivityItem();
            activity.setTitle("Upcoming Event");
            activity.setDescription(event.getTitle() + " - " + event.getLocation());
            activity.setTimestamp(event.getDateTime());
            activity.setType("event");
            activityList.add(activity);
        }

        // Add some sample activities for demo
        if (activityList.size() < limit) {
            ActivityItem activity1 = new ActivityItem();
            activity1.setTitle("Performance Updated");
            activity1.setDescription("Your latest performance has been recorded");
            activity1.setTimestamp(getCurrentDateTime());
            activity1.setType("performance");
            activityList.add(activity1);

            if (activityList.size() < limit) {
                ActivityItem activity2 = new ActivityItem();
                activity2.setTitle("Team Practice");
                activity2.setDescription("Regular practice session completed");
                activity2.setTimestamp(getCurrentDateTime());
                activity2.setType("practice");
                activityList.add(activity2);
            }
        }

        return activityList.size() > limit ? activityList.subList(0, limit) : activityList;
    }

    // ✅ For TeamsFragment - User Points (Optional/Demo)
    public int getUserTotalPoints(int userId) {
        // Simple demo implementation - can be enhanced later
        return 0; // For now, return 0 points (not critical functionality)
    }

    // ✅ For TeamsFragment - Delete Team
    public boolean deleteTeam(int teamId) {
        SQLiteDatabase db = this.getWritableDatabase();
        boolean success = false;

        try {
            // First remove all players from this team
            String deletePlayersQuery = "DELETE FROM " + TABLE_PLAYERS + " WHERE " + COL_PLAYER_TEAM_ID + " = ?";
            db.execSQL(deletePlayersQuery, new String[]{String.valueOf(teamId)});

            // Then delete the team
            String whereClause = COL_TEAM_ID + " = ?";
            String[] whereArgs = {String.valueOf(teamId)};
            int rowsAffected = db.delete(TABLE_TEAMS, whereClause, whereArgs);
            success = rowsAffected > 0;

            if (success) {
                Log.d("DatabaseHelper", "Team deleted successfully: " + teamId);
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error deleting team: " + e.getMessage());
        } finally {
            db.close();
        }

        return success;
    }

    // ✅ For ProfileFragment - User Events Count
    public int getUserEventsCount(int userId, String userRole) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query;
        String[] params;

        switch (userRole) {
            case "admin":
                query = "SELECT COUNT(*) FROM " + TABLE_EVENTS;
                params = new String[]{};
                break;
            case "coach":
                query = "SELECT COUNT(*) FROM " + TABLE_EVENTS + " WHERE " + COL_EVENT_CREATED_BY + " = ?";
                params = new String[]{String.valueOf(userId)};
                break;
            case "student":
                query = "SELECT COUNT(DISTINCT e." + COL_EVENT_ID + ") FROM " + TABLE_EVENTS + " e " +
                        "INNER JOIN " + TABLE_PLAYERS + " p ON e." + COL_EVENT_TEAM_ID + " = p." + COL_PLAYER_TEAM_ID + " " +
                        "WHERE p." + COL_PLAYER_USER_ID + " = ?";
                params = new String[]{String.valueOf(userId)};
                break;
            default:
                return 0;
        }

        Cursor cursor = db.rawQuery(query, params);
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        db.close();
        return count;
    }

    // ✅ For ProfileFragment - User Performance Score (Optional/Demo)
    public double getUserPerformanceScore(int userId) {
        // Simple demo implementation - can be enhanced later
        return 85.5; // Return demo score (not critical functionality)
    }

    // ✅ For ProfileFragment & ProfileActivity - Update Profile Picture
    public boolean updateUserProfilePicture(int userId, String imagePath) {
        SQLiteDatabase db = this.getWritableDatabase();
        boolean success = false;

        try {
            ContentValues values = new ContentValues();
            values.put(COL_USER_PROFILE_IMAGE, imagePath);

            String whereClause = COL_USER_ID + " = ?";
            String[] whereArgs = {String.valueOf(userId)};

            int rowsAffected = db.update(TABLE_USERS, values, whereClause, whereArgs);
            success = rowsAffected > 0;

            if (success) {
                Log.d("DatabaseHelper", "Profile picture updated for user: " + userId);
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error updating profile picture: " + e.getMessage());
        } finally {
            db.close();
        }

        return success;
    }

    // ✅ For PerformanceActivity - Get Recent Events
    public List<Event> getRecentEvents() {
        List<Event> eventList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT e.*, t.name as team_name, u.full_name as created_by_name FROM " + TABLE_EVENTS + " e " +
                "LEFT JOIN " + TABLE_TEAMS + " t ON e." + COL_EVENT_TEAM_ID + " = t." + COL_TEAM_ID + " " +
                "LEFT JOIN " + TABLE_USERS + " u ON e." + COL_EVENT_CREATED_BY + " = u." + COL_USER_ID + " " +
                "ORDER BY e." + COL_EVENT_CREATED_AT + " DESC LIMIT 10";

        Cursor cursor = db.rawQuery(query, null);
        while (cursor.moveToNext()) {
            Event event = createEventFromCursor(cursor);
            eventList.add(event);
        }

        cursor.close();
        db.close();
        return eventList;
    }

    // ✅ For PerformanceActivity - Record Performance
    public boolean recordPerformance(Performance performance) {
        SQLiteDatabase db = this.getWritableDatabase();
        boolean success = false;

        try {
            ContentValues values = new ContentValues();
            values.put(COL_PERFORMANCE_PLAYER_ID, performance.getPlayerId());
            values.put(COL_PERFORMANCE_EVENT_ID, performance.getEventId());
            values.put(COL_PERFORMANCE_STATS, performance.getStats());
            values.put(COL_PERFORMANCE_SCORE, performance.getScore());

            long result = db.insert(TABLE_PERFORMANCE, null, values);
            success = result > 0;

            if (success) {
                Log.d("DatabaseHelper", "Performance recorded successfully");
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error recording performance: " + e.getMessage());
        } finally {
            db.close();
        }

        return success;
    }

    // ✅ For PlayerDetailsActivity - Remove Player from Team
    public boolean removePlayerFromTeam(int playerId) {
        SQLiteDatabase db = this.getWritableDatabase();
        boolean success = false;

        try {
            String whereClause = COL_PLAYER_ID + " = ?";
            String[] whereArgs = {String.valueOf(playerId)};

            int rowsAffected = db.delete(TABLE_PLAYERS, whereClause, whereArgs);
            success = rowsAffected > 0;

            if (success) {
                Log.d("DatabaseHelper", "Player removed from team: " + playerId);
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error removing player from team: " + e.getMessage());
        } finally {
            db.close();
        }

        return success;
    }

    // ✅ For ProfileActivity - Update User Profile
    public boolean updateUserProfile(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        boolean success = false;

        try {
            ContentValues values = new ContentValues();
            values.put(COL_USER_USERNAME, user.getUsername());
            values.put(COL_USER_EMAIL, user.getEmail());
            values.put(COL_USER_FULL_NAME, user.getFullName());
            values.put(COL_USER_PHONE, user.getPhone());

            if (user.getProfileImagePath() != null) {
                values.put(COL_USER_PROFILE_IMAGE, user.getProfileImagePath());
            }

            String whereClause = COL_USER_ID + " = ?";
            String[] whereArgs = {String.valueOf(user.getId())};

            int rowsAffected = db.update(TABLE_USERS, values, whereClause, whereArgs);
            success = rowsAffected > 0;

            if (success) {
                Log.d("DatabaseHelper", "User profile updated: " + user.getUsername());
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error updating user profile: " + e.getMessage());
        } finally {
            db.close();
        }

        return success;
    }

    // ✅ For ProfileActivity - Update User Password
    public boolean updateUserPassword(int userId, String newPassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        boolean success = false;

        try {
            ContentValues values = new ContentValues();
            values.put(COL_USER_PASSWORD, PasswordUtils.hashPassword(newPassword));

            String whereClause = COL_USER_ID + " = ?";
            String[] whereArgs = {String.valueOf(userId)};

            int rowsAffected = db.update(TABLE_USERS, values, whereClause, whereArgs);
            success = rowsAffected > 0;

            if (success) {
                Log.d("DatabaseHelper", "Password updated for user: " + userId);
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error updating password: " + e.getMessage());
        } finally {
            db.close();
        }

        return success;
    }

    // ✅ For TeamManagementActivity - Get Available Students
    public List<User> getAvailableStudents() {
        List<User> studentList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Get students who are not yet assigned to any team
        String query = "SELECT u.* FROM " + TABLE_USERS + " u " +
                "WHERE u." + COL_USER_ROLE + " = 'student' " +
                "AND u." + COL_USER_ID + " NOT IN (" +
                "SELECT p." + COL_PLAYER_USER_ID + " FROM " + TABLE_PLAYERS + " p" +
                ") ORDER BY u." + COL_USER_FULL_NAME + " ASC";

        Cursor cursor = db.rawQuery(query, null);

        while (cursor.moveToNext()) {
            User student = new User();
            student.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_USER_ID)));
            student.setUsername(cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_USERNAME)));
            student.setFullName(cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_FULL_NAME)));
            student.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_EMAIL)));
            student.setPhone(cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_PHONE)));
            student.setRole(cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_ROLE)));
            student.setProfileImagePath(cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_PROFILE_IMAGE)));
            studentList.add(student);
        }

        cursor.close();
        db.close();
        return studentList;
    }

    // ✅ For TeamManagementActivity - Add Player to Team
    public boolean addPlayerToTeam(Player player) {
        SQLiteDatabase db = this.getWritableDatabase();
        boolean success = false;

        try {
            ContentValues values = new ContentValues();
            values.put(COL_PLAYER_USER_ID, player.getUserId());
            values.put(COL_PLAYER_TEAM_ID, player.getTeamId());
            values.put(COL_PLAYER_JERSEY_NUMBER, player.getJerseyNumber());
            values.put(COL_PLAYER_POSITION, player.getPosition());
            values.put(COL_PLAYER_HEIGHT, player.getHeight());
            values.put(COL_PLAYER_WEIGHT, player.getWeight());

            long result = db.insert(TABLE_PLAYERS, null, values);
            success = result > 0;

            if (success) {
                Log.d("DatabaseHelper", "Player added to team successfully");
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error adding player to team: " + e.getMessage());
        } finally {
            db.close();
        }

        return success;
    }

    // ✅ For TeamManagementActivity - Update Team
    public boolean updateTeam(Team team) {
        SQLiteDatabase db = this.getWritableDatabase();
        boolean success = false;

        try {
            ContentValues values = new ContentValues();
            values.put(COL_TEAM_NAME, team.getName());
            values.put(COL_TEAM_SPORT, team.getSport());
            values.put(COL_TEAM_DESCRIPTION, team.getDescription());
            values.put(COL_TEAM_COACH_ID, team.getCoachId());

            String whereClause = COL_TEAM_ID + " = ?";
            String[] whereArgs = {String.valueOf(team.getId())};

            int rowsAffected = db.update(TABLE_TEAMS, values, whereClause, whereArgs);
            success = rowsAffected > 0;

            if (success) {
                Log.d("DatabaseHelper", "Team updated: " + team.getName());
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error updating team: " + e.getMessage());
        } finally {
            db.close();
        }

        return success;
    }

    // ✅ For TeamManagementActivity - Update Team Players
    public boolean updateTeamPlayers(int teamId, List<Player> players) {
        SQLiteDatabase db = this.getWritableDatabase();
        boolean success = true;

        try {
            db.beginTransaction();

            // First, remove all existing players from this team
            String deleteQuery = "DELETE FROM " + TABLE_PLAYERS + " WHERE " + COL_PLAYER_TEAM_ID + " = ?";
            db.execSQL(deleteQuery, new String[]{String.valueOf(teamId)});

            // Then add all new players
            for (Player player : players) {
                player.setTeamId(teamId);
                if (!addPlayerToTeamTransaction(db, player)) {
                    success = false;
                    break;
                }
            }

            if (success) {
                db.setTransactionSuccessful();
                Log.d("DatabaseHelper", "Team players updated successfully");
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error updating team players: " + e.getMessage());
            success = false;
        } finally {
            db.endTransaction();
            db.close();
        }

        return success;
    }

    // Helper method for updateTeamPlayers
    private boolean addPlayerToTeamTransaction(SQLiteDatabase db, Player player) {
        try {
            ContentValues values = new ContentValues();
            values.put(COL_PLAYER_USER_ID, player.getUserId());
            values.put(COL_PLAYER_TEAM_ID, player.getTeamId());
            values.put(COL_PLAYER_JERSEY_NUMBER, player.getJerseyNumber());
            values.put(COL_PLAYER_POSITION, player.getPosition());
            values.put(COL_PLAYER_HEIGHT, player.getHeight());
            values.put(COL_PLAYER_WEIGHT, player.getWeight());

            long result = db.insert(TABLE_PLAYERS, null, values);
            return result > 0;
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error in addPlayerToTeamTransaction: " + e.getMessage());
            return false;
        }
    }

    // ✅ For TeamManagementActivity - Check if Team Name Exists
    public boolean isTeamNameExists(String teamName) {
        SQLiteDatabase db = this.getReadableDatabase();
        boolean exists = false;

        try {
            String selection = COL_TEAM_NAME + " = ? COLLATE NOCASE";
            String[] selectionArgs = {teamName};
            Cursor cursor = db.query(TABLE_TEAMS, new String[]{COL_TEAM_ID},
                    selection, selectionArgs, null, null, null);
            exists = cursor.getCount() > 0;
            cursor.close();
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error checking team name existence: " + e.getMessage());
        } finally {
            db.close();
        }

        return exists;
    }


    public boolean addPlayerToTeamWithNotification(Context context, Player player, String teamName, String sport, String coachName) {
        SQLiteDatabase db = this.getWritableDatabase();
        boolean success = false;

        try {
            ContentValues values = new ContentValues();
            values.put(COL_PLAYER_USER_ID, player.getUserId());
            values.put(COL_PLAYER_TEAM_ID, player.getTeamId());
            values.put(COL_PLAYER_JERSEY_NUMBER, player.getJerseyNumber());
            values.put(COL_PLAYER_POSITION, player.getPosition());
            values.put(COL_PLAYER_HEIGHT, player.getHeight());
            values.put(COL_PLAYER_WEIGHT, player.getWeight());

            long result = db.insert(TABLE_PLAYERS, null, values);
            success = result > 0;

            if (success) {
                Log.d("DatabaseHelper", "Player added to team successfully");

                // Get student details for notifications
                User student = getUserById(player.getUserId());
                if (student != null) {
                    // Send email notification
                    EmailService.sendTeamAdditionEmail(
                            context,
                            student.getEmail(),
                            student.getFullName(),
                            teamName,
                            coachName,
                            sport,
                            new EmailService.EmailCallback() {
                                @Override
                                public void onSuccess() {
                                    Log.d("DatabaseHelper", "Team addition email sent successfully");
                                }

                                @Override
                                public void onFailure(String error) {
                                    Log.e("DatabaseHelper", "Failed to send team addition email: " + error);
                                }
                            }
                    );

                    // Show in-app notification
                    NotificationService.showTeamJoinedNotification(context, teamName, sport);
                }
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error adding player to team: " + e.getMessage());
        } finally {
            db.close();
        }

        return success;
    }

    // Enhanced createEvent with notifications to team members
    public long createEventWithNotifications(Context context, Event event) {
        SQLiteDatabase db = this.getWritableDatabase();
        long result = -1;

        try {
            ContentValues values = new ContentValues();
            values.put(COL_EVENT_TITLE, event.getTitle());
            values.put(COL_EVENT_DESCRIPTION, event.getDescription());
            values.put(COL_EVENT_TYPE, event.getType());
            values.put(COL_EVENT_DATETIME, event.getDateTime());
            values.put(COL_EVENT_LOCATION, event.getLocation());
            values.put(COL_EVENT_TEAM_ID, event.getTeamId());
            values.put(COL_EVENT_CREATED_BY, event.getCreatedBy());

            result = db.insert(TABLE_EVENTS, null, values);
            Log.d("DatabaseHelper", "Event created with ID: " + result);

            if (result > 0) {
                // Get team details
                Team team = getTeamById(event.getTeamId());
                if (team != null) {
                    // Notify all students in the team
                    List<User> students = getStudentsForTeam(event.getTeamId());
                    for (User student : students) {
                        // Send in-app notification
                        NotificationService.showEventCreatedNotification(
                                context,
                                event.getTitle(),
                                team.getName(),
                                event.getDateTime()
                        );

                        // Optionally send email for important events
                        if ("Match".equalsIgnoreCase(event.getType()) || "Tournament".equalsIgnoreCase(event.getType())) {
                            EmailService.sendGlobalEventNotification(
                                    context,
                                    student.getEmail(),
                                    student.getFullName(),
                                    event.getTitle(),
                                    event.getDateTime(),
                                    event.getLocation(),
                                    team.getName(),
                                    "Event Creator", // creatorName
                                    false, // isParticipating (default to false for non-team events)
                                    new EmailService.EmailCallback() {
                                        @Override
                                        public void onSuccess() {
                                            Log.d("DatabaseHelper", "Event email sent to: " + student.getEmail());
                                        }

                                        @Override
                                        public void onFailure(String error) {
                                            Log.e("DatabaseHelper", "Failed to send event email: " + error);
                                        }
                                    }
                            );

                        }
                    }
                }
            }

        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error creating event: " + e.getMessage());
        } finally {
            db.close();
        }

        return result;
    }

    // Helper method to get team by ID
    public Team getTeamById(int teamId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Team team = null;

        String query = "SELECT * FROM " + TABLE_TEAMS + " WHERE " + COL_TEAM_ID + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(teamId)});

        if (cursor.moveToFirst()) {
            team = new Team();
            team.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_TEAM_ID)));
            team.setName(cursor.getString(cursor.getColumnIndexOrThrow(COL_TEAM_NAME)));
            team.setSport(cursor.getString(cursor.getColumnIndexOrThrow(COL_TEAM_SPORT)));
            team.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(COL_TEAM_DESCRIPTION)));
            team.setCoachId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_TEAM_COACH_ID)));
            team.setCreatedDate(cursor.getString(cursor.getColumnIndexOrThrow(COL_TEAM_CREATED_DATE)));
        }

        cursor.close();
        db.close();
        return team;
    }

    // ==================== ENHANCED EVENT METHODS - SHOW ALL EVENTS TO EVERYONE ====================

    // ✅ NEW: Get ALL events for any user with participation status
    public List<Event> getAllEventsWithParticipationStatus(int userId, String userRole) {
        List<Event> eventList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT DISTINCT e.*, t.name as team_name, u.full_name as created_by_name, " +
                "CASE WHEN p.user_id IS NOT NULL THEN 1 ELSE 0 END as is_participating " +
                "FROM " + TABLE_EVENTS + " e " +
                "LEFT JOIN " + TABLE_TEAMS + " t ON e." + COL_EVENT_TEAM_ID + " = t." + COL_TEAM_ID + " " +
                "LEFT JOIN " + TABLE_USERS + " u ON e." + COL_EVENT_CREATED_BY + " = u." + COL_USER_ID + " " +
                "LEFT JOIN " + TABLE_PLAYERS + " p ON e." + COL_EVENT_TEAM_ID + " = p." + COL_PLAYER_TEAM_ID + " AND p." + COL_PLAYER_USER_ID + " = ? " +
                "ORDER BY e." + COL_EVENT_DATETIME + " ASC";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});

        while (cursor.moveToNext()) {
            Event event = createEventFromCursor(cursor);

            // Set participation status
            int isParticipating = cursor.getInt(cursor.getColumnIndexOrThrow("is_participating"));
            event.setIsUserParticipating(isParticipating == 1);

            // Set user role context
            event.setUserRole(userRole);
            event.setCurrentUserId(userId);

            eventList.add(event);
        }

        cursor.close();
        db.close();
        return eventList;
    }

    // ✅ ENHANCED: Send notifications to ALL registered users when event is created
    public long createEventWithGlobalNotifications(Context context, Event event) {
        SQLiteDatabase db = this.getWritableDatabase();
        long result = -1;

        try {
            ContentValues values = new ContentValues();
            values.put(COL_EVENT_TITLE, event.getTitle());
            values.put(COL_EVENT_DESCRIPTION, event.getDescription());
            values.put(COL_EVENT_TYPE, event.getType());
            values.put(COL_EVENT_DATETIME, event.getDateTime());
            values.put(COL_EVENT_LOCATION, event.getLocation());
            values.put(COL_EVENT_TEAM_ID, event.getTeamId());
            values.put(COL_EVENT_CREATED_BY, event.getCreatedBy());

            result = db.insert(TABLE_EVENTS, null, values);
            Log.d("DatabaseHelper", "Event created with ID: " + result);

            if (result > 0) {
                // Get event creator and team details
                User creator = getUserById(event.getCreatedBy());
                Team team = null;
                if (event.getTeamId() > 0) {
                    team = getTeamById(event.getTeamId());
                }

                // ✅ SEND EMAIL TO ALL REGISTERED USERS
                List<User> allUsers = getAllRegisteredUsers();
                for (User user : allUsers) {
                    // Skip the creator
                    if (user.getId() == event.getCreatedBy()) continue;

                    // Determine if user is participating
                    boolean isParticipating = false;
                    if (team != null) {
                        List<User> teamMembers = getStudentsForTeam(team.getId());
                        isParticipating = teamMembers.stream().anyMatch(member -> member.getId() == user.getId());
                    }

                    // Send email notification
                    sendEventNotificationToUser(context, user, event, team, creator, isParticipating);

                    // Send in-app notification
                    NotificationService.showEventCreatedNotification(
                            context,
                            event.getTitle(),
                            team != null ? team.getName() : "General Event",
                            event.getDateTime()
                    );
                }
            }

        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error creating event: " + e.getMessage());
        } finally {
            db.close();
        }

        return result;
    }

    // ✅ Helper method to get all registered users
    public List<User> getAllRegisteredUsers() {
        List<User> userList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT * FROM " + TABLE_USERS + " ORDER BY " + COL_USER_FULL_NAME + " ASC";
        Cursor cursor = db.rawQuery(query, null);

        while (cursor.moveToNext()) {
            User user = new User();
            user.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_USER_ID)));
            user.setUsername(cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_USERNAME)));
            user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_EMAIL)));
            user.setRole(cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_ROLE)));
            user.setFullName(cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_FULL_NAME)));
            user.setPhone(cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_PHONE)));
            user.setProfileImagePath(cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_PROFILE_IMAGE)));
            userList.add(user);
        }

        cursor.close();
        db.close();
        return userList;
    }

    // ✅ FIXED: Get team members with full details for public viewing
    public List<User> getTeamMembersWithDetails(int teamId) {
        List<User> memberList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // ✅ FIXED: Use the actual column constants instead of hardcoded names
        String query = "SELECT u.*, " +
                "p." + COL_PLAYER_JERSEY_NUMBER + ", " +
                "p." + COL_PLAYER_POSITION + ", " +
                "p." + COL_PLAYER_HEIGHT + ", " +
                "p." + COL_PLAYER_WEIGHT + " " +
                "FROM " + TABLE_USERS + " u " +
                "INNER JOIN " + TABLE_PLAYERS + " p ON u." + COL_USER_ID + " = p." + COL_PLAYER_USER_ID + " " +
                "WHERE p." + COL_PLAYER_TEAM_ID + " = ? " +
                "ORDER BY p." + COL_PLAYER_JERSEY_NUMBER + " ASC";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(teamId)});

        while (cursor.moveToNext()) {
            User member = new User();
            member.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_USER_ID)));
            member.setUsername(cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_USERNAME)));
            member.setFullName(cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_FULL_NAME)));
            member.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_EMAIL)));
            member.setPhone(cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_PHONE)));
            member.setRole(cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_ROLE)));
            member.setProfileImagePath(cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_PROFILE_IMAGE)));

            // ✅ FIXED: Use the column constants for player info
            member.setJerseyNumber(cursor.getInt(cursor.getColumnIndexOrThrow(COL_PLAYER_JERSEY_NUMBER)));
            member.setPosition(cursor.getString(cursor.getColumnIndexOrThrow(COL_PLAYER_POSITION)));
            member.setHeight(cursor.getDouble(cursor.getColumnIndexOrThrow(COL_PLAYER_HEIGHT)));
            member.setWeight(cursor.getDouble(cursor.getColumnIndexOrThrow(COL_PLAYER_WEIGHT)));

            memberList.add(member);
        }

        cursor.close();
        db.close();
        return memberList;
    }


    // ==================== EMAIL CONFIGURATION METHODS ====================

    // ✅ EmailConfig model class
    public static class EmailConfig {
        private String emailAddress;
        private String emailPassword;
        private String smtpHost;
        private String smtpPort;
        private String appName;

        // Constructors
        public EmailConfig() {}

        public EmailConfig(String emailAddress, String emailPassword, String smtpHost, String smtpPort, String appName) {
            this.emailAddress = emailAddress;
            this.emailPassword = emailPassword;
            this.smtpHost = smtpHost;
            this.smtpPort = smtpPort;
            this.appName = appName;
        }

        // Getters and setters
        public String getEmailAddress() { return emailAddress; }
        public void setEmailAddress(String emailAddress) { this.emailAddress = emailAddress; }

        public String getEmailPassword() { return emailPassword; }
        public void setEmailPassword(String emailPassword) { this.emailPassword = emailPassword; }

        public String getSmtpHost() { return smtpHost; }
        public void setSmtpHost(String smtpHost) { this.smtpHost = smtpHost; }

        public String getSmtpPort() { return smtpPort; }
        public void setSmtpPort(String smtpPort) { this.smtpPort = smtpPort; }

        public String getAppName() { return appName; }
        public void setAppName(String appName) { this.appName = appName; }
    }

    // ✅ Get email configuration from database
    public EmailConfig getEmailConfiguration() {
        SQLiteDatabase db = this.getReadableDatabase();
        EmailConfig config = null;

        String query = "SELECT * FROM " + TABLE_EMAIL_CONFIG + " LIMIT 1";
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            config = new EmailConfig();
            config.setEmailAddress(cursor.getString(cursor.getColumnIndexOrThrow(COL_CONFIG_EMAIL)));
            config.setEmailPassword(cursor.getString(cursor.getColumnIndexOrThrow(COL_CONFIG_PASSWORD)));
            config.setSmtpHost(cursor.getString(cursor.getColumnIndexOrThrow(COL_CONFIG_SMTP_HOST)));
            config.setSmtpPort(cursor.getString(cursor.getColumnIndexOrThrow(COL_CONFIG_SMTP_PORT)));
            config.setAppName(cursor.getString(cursor.getColumnIndexOrThrow(COL_CONFIG_APP_NAME)));
        }

        cursor.close();
        db.close();
        return config;
    }

    // ✅ Update email configuration
    public boolean updateEmailConfiguration(EmailConfig config) {
        SQLiteDatabase db = this.getWritableDatabase();
        boolean success = false;

        try {
            // Check if config exists
            String countQuery = "SELECT COUNT(*) FROM " + TABLE_EMAIL_CONFIG;
            Cursor cursor = db.rawQuery(countQuery, null);
            cursor.moveToFirst();
            int count = cursor.getInt(0);
            cursor.close();

            ContentValues values = new ContentValues();
            values.put(COL_CONFIG_EMAIL, config.getEmailAddress());
            values.put(COL_CONFIG_PASSWORD, config.getEmailPassword());
            values.put(COL_CONFIG_SMTP_HOST, config.getSmtpHost());
            values.put(COL_CONFIG_SMTP_PORT, config.getSmtpPort());
            values.put(COL_CONFIG_APP_NAME, config.getAppName());

            if (count > 0) {
                // Update existing config
                int rowsAffected = db.update(TABLE_EMAIL_CONFIG, values, COL_CONFIG_ID + " = (SELECT MIN(" + COL_CONFIG_ID + ") FROM " + TABLE_EMAIL_CONFIG + ")", null);
                success = rowsAffected > 0;
            } else {
                // Insert new config
                long result = db.insert(TABLE_EMAIL_CONFIG, null, values);
                success = result > 0;
            }

            Log.d("DatabaseHelper", "Email config updated: " + success);
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error updating email config: " + e.getMessage());
        } finally {
            db.close();
        }

        return success;
    }

    // ✅ Check if email is configured properly
    public boolean isEmailConfigured() {
        EmailConfig config = getEmailConfiguration();
        return config != null &&
                config.getEmailAddress() != null &&
                !config.getEmailAddress().isEmpty() &&
                !config.getEmailAddress().equals("admin@guarena.com") &&
                config.getEmailPassword() != null &&
                !config.getEmailPassword().isEmpty() &&
                !config.getEmailPassword().equals("change-this-password");
    }

    // ✅ Initialize default email config
    private void insertDefaultEmailConfig(SQLiteDatabase db) {
        ContentValues values = new ContentValues();
        values.put(COL_CONFIG_EMAIL, "admin@guarena.com"); // Default placeholder
        values.put(COL_CONFIG_PASSWORD, "change-this-password"); // Default placeholder
        values.put(COL_CONFIG_SMTP_HOST, "smtp.gmail.com");
        values.put(COL_CONFIG_SMTP_PORT, "587");
        values.put(COL_CONFIG_APP_NAME, "GuArena Sports Management");

        db.insert(TABLE_EMAIL_CONFIG, null, values);
        Log.d("DatabaseHelper", "Default email config inserted");
    }

    // ✅ FIND AND REPLACE YOUR sendEventNotificationToUser METHOD WITH THIS:
    private void sendEventNotificationToUser(Context context, User user, Event event, Team team, User creator, boolean isParticipating) {
        String participationStatus = isParticipating ? "You're participating in this event!" : "Open event - anyone can join!";
        String teamInfo = team != null ? team.getName() + " (" + team.getSport() + ")" : "General Event";

        EmailService.sendGlobalEventNotification(
                context,
                user.getEmail(),
                user.getFullName(),
                event.getTitle(),
                event.getDateTime(),
                event.getLocation(),
                teamInfo,
                creator.getFullName(),
                isParticipating,
                new EmailService.EmailCallback() {
                    @Override
                    public void onSuccess() {
                        Log.d("DatabaseHelper", "Event email sent to: " + user.getEmail());
                    }

                    @Override
                    public void onFailure(String error) {
                        Log.e("DatabaseHelper", "Failed to send event email: " + error);
                    }
                }
        );
    }

    public boolean borrowEquipment(int equipmentId, int userId, int durationDays) {
        SQLiteDatabase db = this.getWritableDatabase();
        boolean success = false;
        try {
            db.beginTransaction();

            // ✅ FIRST: Check current available quantity
            Cursor cursor = db.rawQuery(
                    "SELECT " + COL_EQUIPMENT_AVAILABLE_QUANTITY + " FROM " + TABLE_EQUIPMENT +
                            " WHERE " + COL_EQUIPMENT_ID + " = ?",
                    new String[]{String.valueOf(equipmentId)}
            );

            if (!cursor.moveToFirst() || cursor.getInt(0) <= 0) {
                cursor.close();
                return false; // No units available
            }

            int currentAvailable = cursor.getInt(0);
            cursor.close();

            // Calculate due date
            Calendar calendar = Calendar.getInstance();
            if (durationDays == 0) {
                calendar.add(Calendar.SECOND, 30); // 30 seconds for testing
            } else {
                calendar.add(Calendar.DAY_OF_MONTH, durationDays);
            }

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            String dueDate = sdf.format(calendar.getTime());
            String borrowedDate = sdf.format(new Date());

            // Create borrowing record
            ContentValues borrowingValues = new ContentValues();
            borrowingValues.put(COL_BORROWING_EQUIPMENT_ID, equipmentId);
            borrowingValues.put(COL_BORROWING_USER_ID, userId);
            borrowingValues.put(COL_BORROWING_BORROWED_DATE, borrowedDate);
            borrowingValues.put(COL_BORROWING_DUE_DATE, dueDate);
            borrowingValues.put(COL_BORROWING_STATUS, "borrowed");
            borrowingValues.put(COL_BORROWING_DURATION_DAYS, durationDays);

            long borrowingId = db.insert(TABLE_EQUIPMENT_BORROWINGS, null, borrowingValues);

            if (borrowingId > 0) {
                // ✅ FIXED: Decrease available_quantity by 1
                ContentValues equipmentValues = new ContentValues();
                equipmentValues.put(COL_EQUIPMENT_AVAILABLE_QUANTITY, currentAvailable - 1);

                // ✅ FIXED: Only set status to "borrowed" if ALL units are taken
                if (currentAvailable - 1 == 0) {
                    equipmentValues.put(COL_EQUIPMENT_STATUS, "borrowed");
                }

                equipmentValues.put(COL_EQUIPMENT_CHECKED_OUT_BY, userId);
                equipmentValues.put(COL_EQUIPMENT_CHECKED_OUT_DATE, borrowedDate);
                equipmentValues.put(COL_EQUIPMENT_DUE_DATE, dueDate);

                int updated = db.update(TABLE_EQUIPMENT, equipmentValues,
                        COL_EQUIPMENT_ID + " = ?", new String[]{String.valueOf(equipmentId)});

                if (updated > 0) {
                    db.setTransactionSuccessful();
                    success = true;
                    // Schedule notification for due date
                    scheduleEquipmentReturnNotification(equipmentId, userId, dueDate);

                    Log.d("DatabaseHelper", "Equipment borrowed. Available: " + (currentAvailable - 1));
                }
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error borrowing equipment: " + e.getMessage());
        } finally {
            db.endTransaction();
            db.close();
        }

        return success;
    }

    // Return Equipment

    public boolean returnEquipment(int equipmentId, int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        boolean success = false;
        try {
            db.beginTransaction();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            String returnedDate = sdf.format(new Date());

            // Update borrowing record
            ContentValues borrowingValues = new ContentValues();
            borrowingValues.put(COL_BORROWING_RETURNED_DATE, returnedDate);
            borrowingValues.put(COL_BORROWING_STATUS, "returned");

            int borrowingUpdated = db.update(TABLE_EQUIPMENT_BORROWINGS, borrowingValues,
                    COL_BORROWING_EQUIPMENT_ID + " = ? AND " + COL_BORROWING_USER_ID + " = ? AND " + COL_BORROWING_STATUS + " = ?",
                    new String[]{String.valueOf(equipmentId), String.valueOf(userId), "borrowed"});

            if (borrowingUpdated > 0) {
                // ✅ FIXED: Increase available_quantity by 1
                db.execSQL(
                        "UPDATE " + TABLE_EQUIPMENT +
                                " SET " + COL_EQUIPMENT_AVAILABLE_QUANTITY + " = " + COL_EQUIPMENT_AVAILABLE_QUANTITY + " + 1, " +
                                COL_EQUIPMENT_STATUS + " = 'available' " +
                                " WHERE " + COL_EQUIPMENT_ID + " = ?",
                        new String[]{String.valueOf(equipmentId)}
                );

                db.setTransactionSuccessful();
                success = true;

                Log.d("DatabaseHelper", "Equipment returned. Available increased by 1");
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error returning equipment: " + e.getMessage());
        } finally {
            db.endTransaction();
            db.close();
        }

        return success;
    }

    // Get All Active Borrowings
    public List<EquipmentBorrowing> getAllActiveBorrowings() {
        List<EquipmentBorrowing> borrowings = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT eb.*, e." + COL_EQUIPMENT_NAME + " as equipment_name, " +
                "u." + COL_USER_FULL_NAME + " as user_name, u." + COL_USER_EMAIL + " as user_email " +
                "FROM " + TABLE_EQUIPMENT_BORROWINGS + " eb " +
                "INNER JOIN " + TABLE_EQUIPMENT + " e ON eb." + COL_BORROWING_EQUIPMENT_ID + " = e." + COL_EQUIPMENT_ID + " " +
                "INNER JOIN " + TABLE_USERS + " u ON eb." + COL_BORROWING_USER_ID + " = u." + COL_USER_ID + " " +
                "WHERE eb." + COL_BORROWING_STATUS + " = 'borrowed' " +
                "ORDER BY eb." + COL_BORROWING_DUE_DATE + " ASC";

        Cursor cursor = db.rawQuery(query, null);

        while (cursor.moveToNext()) {
            EquipmentBorrowing borrowing = new EquipmentBorrowing();
            borrowing.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_BORROWING_ID)));
            borrowing.setEquipmentId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_BORROWING_EQUIPMENT_ID)));
            borrowing.setEquipmentName(cursor.getString(cursor.getColumnIndexOrThrow("equipment_name")));
            borrowing.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_BORROWING_USER_ID)));
            borrowing.setUserName(cursor.getString(cursor.getColumnIndexOrThrow("user_name")));
            borrowing.setUserEmail(cursor.getString(cursor.getColumnIndexOrThrow("user_email")));
            borrowing.setBorrowedDate(cursor.getString(cursor.getColumnIndexOrThrow(COL_BORROWING_BORROWED_DATE)));
            borrowing.setDueDate(cursor.getString(cursor.getColumnIndexOrThrow(COL_BORROWING_DUE_DATE)));
            borrowing.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(COL_BORROWING_STATUS)));
            borrowing.setDurationDays(cursor.getInt(cursor.getColumnIndexOrThrow(COL_BORROWING_DURATION_DAYS)));

            borrowings.add(borrowing);
        }

        cursor.close();
        db.close();
        return borrowings;
    }

    // Get User's Active Borrowings
    public List<EquipmentBorrowing> getUserActiveBorrowings(int userId) {
        List<EquipmentBorrowing> borrowings = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT eb.*, e." + COL_EQUIPMENT_NAME + " as equipment_name " +
                "FROM " + TABLE_EQUIPMENT_BORROWINGS + " eb " +
                "INNER JOIN " + TABLE_EQUIPMENT + " e ON eb." + COL_BORROWING_EQUIPMENT_ID + " = e." + COL_EQUIPMENT_ID + " " +
                "WHERE eb." + COL_BORROWING_USER_ID + " = ? AND eb." + COL_BORROWING_STATUS + " = 'borrowed' " +
                "ORDER BY eb." + COL_BORROWING_DUE_DATE + " ASC";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});

        while (cursor.moveToNext()) {
            EquipmentBorrowing borrowing = new EquipmentBorrowing();
            borrowing.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_BORROWING_ID)));
            borrowing.setEquipmentId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_BORROWING_EQUIPMENT_ID)));
            borrowing.setEquipmentName(cursor.getString(cursor.getColumnIndexOrThrow("equipment_name")));
            borrowing.setUserId(userId);
            borrowing.setBorrowedDate(cursor.getString(cursor.getColumnIndexOrThrow(COL_BORROWING_BORROWED_DATE)));
            borrowing.setDueDate(cursor.getString(cursor.getColumnIndexOrThrow(COL_BORROWING_DUE_DATE)));
            borrowing.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(COL_BORROWING_STATUS)));
            borrowing.setDurationDays(cursor.getInt(cursor.getColumnIndexOrThrow(COL_BORROWING_DURATION_DAYS)));

            borrowings.add(borrowing);
        }

        cursor.close();
        db.close();
        return borrowings;
    }

    // Schedule equipment return notification
    private void scheduleEquipmentReturnNotification(int equipmentId, int userId, String dueDate) {
        try {
            Context context = this.context; // error Cannot resolve symbol 'context'
            if (context == null) return;

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date dueDateObj = sdf.parse(dueDate);

            if (dueDateObj != null) {
                long delayMillis = dueDateObj.getTime() - System.currentTimeMillis();

                // If due date is in the future, schedule notification
                if (delayMillis > 0) {
                    // For WorkManager scheduling
                    scheduleWorkManagerNotification(equipmentId, userId, delayMillis);
                } else {
                    // Already overdue, send notification immediately
                    Equipment equipment = getEquipmentById(equipmentId); // error : Cannot resolve method 'getEquipmentById' in 'DatabaseHelper'
                    if (equipment != null) {
                        NotificationService.showEquipmentReturnReminder(
                                context,
                                equipment.getName(),
                                dueDate,
                                equipmentId
                        );
                    }
                }
            }

        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error scheduling notification: " + e.getMessage());
        }
    }

    // Schedule using WorkManager for reliable delivery
    private void scheduleWorkManagerNotification(int equipmentId, int userId, long delayMillis) {
        try {
            // Convert to minutes for WorkManager
            long delayMinutes = Math.max(1, delayMillis / (60 * 1000));

            Data inputData = new Data.Builder()
                    .putInt("equipmentId", equipmentId)
                    .putInt("userId", userId)
                    .build();

            OneTimeWorkRequest notificationWork = new OneTimeWorkRequest.Builder(EquipmentReminderWorker.class)
                    .setInitialDelay(delayMinutes, TimeUnit.MINUTES)
                    .setInputData(inputData)
                    .addTag("equipment_reminder_" + equipmentId)
                    .build();

            WorkManager.getInstance(context).enqueue(notificationWork);
            Log.d("DatabaseHelper", "Scheduled equipment notification in " + delayMinutes + " minutes");

        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error scheduling WorkManager: " + e.getMessage());
        }
    }


    // Pre-populate equipment data
    public void populateDefaultEquipment() {
        SQLiteDatabase db = this.getWritableDatabase();

        // Check if equipment already exists
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_EQUIPMENT, null);
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();

        if (count == 0) {
            // Add default equipment
            // error :Cannot resolve method 'addEquipment' in 'DatabaseHelper'
            addEquipment(new Equipment("Basketball", "Balls", "Official size basketball", 10));
            addEquipment(new Equipment("Football", "Balls", "FIFA standard football", 8));
            addEquipment(new Equipment("Tennis Racket", "Rackets", "Professional tennis racket", 6));
            addEquipment(new Equipment("Badminton Racket", "Rackets", "Lightweight badminton racket", 12));
            addEquipment(new Equipment("Cricket Bat", "Bats", "English willow cricket bat", 5));
            addEquipment(new Equipment("Table Tennis Paddle", "Paddles", "Professional TT paddle", 8));
            addEquipment(new Equipment("Volleyball", "Balls", "Official volleyball", 6));
            addEquipment(new Equipment("Hockey Stick", "Sticks", "Composite hockey stick", 10));
            addEquipment(new Equipment("Gym Mat", "Mats", "Exercise yoga mat", 15));
            addEquipment(new Equipment("Skipping Rope", "Training", "Speed skipping rope", 20));

            Log.d("DatabaseHelper", "Default equipment populated");
        }
    }

    // Get equipment by ID
    public Equipment getEquipmentById(int equipmentId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Equipment equipment = null;

        String query = "SELECT * FROM " + TABLE_EQUIPMENT + " WHERE " + COL_EQUIPMENT_ID + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(equipmentId)});

        if (cursor.moveToFirst()) {
            equipment = new Equipment();
            equipment.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_EQUIPMENT_ID)));
            equipment.setName(cursor.getString(cursor.getColumnIndexOrThrow(COL_EQUIPMENT_NAME)));
            equipment.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(COL_EQUIPMENT_CATEGORY)));
            equipment.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(COL_EQUIPMENT_DESCRIPTION)));
            equipment.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(COL_EQUIPMENT_STATUS)));
            equipment.setQuantity(cursor.getInt(cursor.getColumnIndexOrThrow(COL_EQUIPMENT_QUANTITY)));
            equipment.setAvailableQuantity(cursor.getInt(cursor.getColumnIndexOrThrow(COL_EQUIPMENT_AVAILABLE_QUANTITY)));

            // Get borrowed info if status is borrowed
            int borrowedByIndex = cursor.getColumnIndex(COL_EQUIPMENT_CHECKED_OUT_BY);
            if (borrowedByIndex != -1 && !cursor.isNull(borrowedByIndex)) {
                equipment.setBorrowedBy(cursor.getInt(borrowedByIndex));
            }

            int borrowedDateIndex = cursor.getColumnIndex(COL_EQUIPMENT_CHECKED_OUT_DATE);
            if (borrowedDateIndex != -1) {
                equipment.setBorrowedDate(cursor.getString(borrowedDateIndex));
            }

            int dueDateIndex = cursor.getColumnIndex(COL_EQUIPMENT_DUE_DATE);
            if (dueDateIndex != -1) {
                equipment.setDueDate(cursor.getString(dueDateIndex));
            }
        }

        cursor.close();
        db.close();
        return equipment;
    }
    // Add new equipment

    public long addEquipment(Equipment equipment) {
        SQLiteDatabase db = this.getWritableDatabase();
        long equipmentId = -1;
        try {
            ContentValues values = new ContentValues();
            values.put(COL_EQUIPMENT_NAME, equipment.getName());
            values.put(COL_EQUIPMENT_CATEGORY, equipment.getCategory());
            values.put(COL_EQUIPMENT_DESCRIPTION, equipment.getDescription());
            values.put(COL_EQUIPMENT_STATUS, "available");

            // ✅ FIXED: Set both quantity and available_quantity to the same value
            int quantity = equipment.getQuantity() > 0 ? equipment.getQuantity() : 1;
            values.put(COL_EQUIPMENT_QUANTITY, quantity);
            values.put(COL_EQUIPMENT_AVAILABLE_QUANTITY, quantity); // ✅ Same as quantity!

            equipmentId = db.insert(TABLE_EQUIPMENT, null, values);

            Log.d("DatabaseHelper", "Equipment added: " + equipment.getName() +
                    " Qty: " + quantity + " Available: " + quantity);
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error adding equipment: " + e.getMessage());
        } finally {
            db.close();
        }
        return equipmentId;
    }

    // Delete equipment
    public boolean deleteEquipment(int equipmentId) {
        SQLiteDatabase db = this.getWritableDatabase();
        boolean success = false;

        try {
            int rowsDeleted = db.delete(TABLE_EQUIPMENT,
                    COL_EQUIPMENT_ID + " = ?",
                    new String[]{String.valueOf(equipmentId)});

            success = rowsDeleted > 0;
            Log.d("DatabaseHelper", "Equipment deleted: " + equipmentId);
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error deleting equipment: " + e.getMessage());
        } finally {
            db.close();
        }

        return success;
    }
    // Get all equipment
    public List<Equipment> getAllEquipmentForUser(int currentUserId) {
        List<Equipment> equipmentList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT e.*, " +
                "u." + COL_USER_FULL_NAME + " as borrowedusername, " +
                "CASE WHEN eb." + COL_BORROWING_USER_ID + " = ? THEN 1 ELSE 0 END as isborrowedbyme " +
                "FROM " + TABLE_EQUIPMENT + " e " +
                "LEFT JOIN " + TABLE_USERS + " u ON e." + COL_EQUIPMENT_CHECKED_OUT_BY + " = u." + COL_USER_ID + " " +
                "LEFT JOIN " + TABLE_EQUIPMENT_BORROWINGS + " eb ON e." + COL_EQUIPMENT_ID + " = eb." + COL_BORROWING_EQUIPMENT_ID + " " +
                "AND eb." + COL_BORROWING_USER_ID + " = ? " +
                "AND eb." + COL_BORROWING_STATUS + " = 'borrowed' " +
                "ORDER BY e." + COL_EQUIPMENT_NAME;

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(currentUserId), String.valueOf(currentUserId)});

        while (cursor.moveToNext()) {
            Equipment equipment = new Equipment();
            equipment.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_EQUIPMENT_ID)));
            equipment.setName(cursor.getString(cursor.getColumnIndexOrThrow(COL_EQUIPMENT_NAME)));
            equipment.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(COL_EQUIPMENT_CATEGORY)));
            equipment.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(COL_EQUIPMENT_DESCRIPTION)));
            equipment.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(COL_EQUIPMENT_STATUS)));

            int quantityIndex = cursor.getColumnIndex(COL_EQUIPMENT_QUANTITY);
            if (quantityIndex != -1) {
                equipment.setQuantity(cursor.getInt(quantityIndex));
            }

            int availableIndex = cursor.getColumnIndex(COL_EQUIPMENT_AVAILABLE_QUANTITY);
            if (availableIndex != -1) {
                equipment.setAvailableQuantity(cursor.getInt(availableIndex));
            }

            int borrowedUserIndex = cursor.getColumnIndex("borrowedusername");
            if (borrowedUserIndex != -1) {
                equipment.setBorrowedUserName(cursor.getString(borrowedUserIndex));
            }

            // ✅ NEW: Check if current user borrowed this
            int isBorrowedByMeIndex = cursor.getColumnIndex("isborrowedbyme");
            if (isBorrowedByMeIndex != -1) {
                equipment.setIsBorrowedByCurrentUser(cursor.getInt(isBorrowedByMeIndex) == 1);
            }

            int dueDateIndex = cursor.getColumnIndex(COL_EQUIPMENT_DUE_DATE);
            if (dueDateIndex != -1) {
                equipment.setDueDate(cursor.getString(dueDateIndex));
            }

            equipmentList.add(equipment);
        }

        cursor.close();
        db.close();
        return equipmentList;
    }

    public List<Equipment> getAllEquipment() {
        List<Equipment> equipmentList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT e.*, u." + COL_USER_FULL_NAME + " as borrowed_user_name " +
                "FROM " + TABLE_EQUIPMENT + " e " +
                "LEFT JOIN " + TABLE_USERS + " u ON e." + COL_EQUIPMENT_CHECKED_OUT_BY + " = u." + COL_USER_ID +
                " ORDER BY e." + COL_EQUIPMENT_NAME;

        Cursor cursor = db.rawQuery(query, null);

        while (cursor.moveToNext()) {
            Equipment equipment = new Equipment();
            equipment.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_EQUIPMENT_ID)));
            equipment.setName(cursor.getString(cursor.getColumnIndexOrThrow(COL_EQUIPMENT_NAME)));
            equipment.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(COL_EQUIPMENT_CATEGORY)));
            equipment.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(COL_EQUIPMENT_DESCRIPTION)));
            equipment.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(COL_EQUIPMENT_STATUS)));

            int quantityIndex = cursor.getColumnIndex(COL_EQUIPMENT_QUANTITY);
            if (quantityIndex != -1) {
                equipment.setQuantity(cursor.getInt(quantityIndex));
            }

            int availableIndex = cursor.getColumnIndex(COL_EQUIPMENT_AVAILABLE_QUANTITY);
            if (availableIndex != -1) {
                equipment.setAvailableQuantity(cursor.getInt(availableIndex));
            }

            // Get borrowed info if available
            int borrowedByIndex = cursor.getColumnIndex(COL_EQUIPMENT_CHECKED_OUT_BY);
            if (borrowedByIndex != -1 && !cursor.isNull(borrowedByIndex)) {
                equipment.setBorrowedBy(cursor.getInt(borrowedByIndex));
            }

            int borrowedDateIndex = cursor.getColumnIndex(COL_EQUIPMENT_CHECKED_OUT_DATE);
            if (borrowedDateIndex != -1) {
                equipment.setBorrowedDate(cursor.getString(borrowedDateIndex));
            }

            int dueDateIndex = cursor.getColumnIndex(COL_EQUIPMENT_DUE_DATE);
            if (dueDateIndex != -1) {
                equipment.setDueDate(cursor.getString(dueDateIndex));
            }

            int borrowedUserIndex = cursor.getColumnIndex("borrowed_user_name");
            if (borrowedUserIndex != -1) {
                equipment.setBorrowedUserName(cursor.getString(borrowedUserIndex));
            }

            equipmentList.add(equipment);
        }

        cursor.close();
        db.close();
        return equipmentList;
    }

    public boolean updateEquipment(Equipment equipment) {
        SQLiteDatabase db = this.getWritableDatabase();
        boolean success = false;
        try {
            ContentValues values = new ContentValues();
            values.put(COL_EQUIPMENT_NAME, equipment.getName());
            values.put(COL_EQUIPMENT_CATEGORY, equipment.getCategory());
            values.put(COL_EQUIPMENT_DESCRIPTION, equipment.getDescription());
            values.put(COL_EQUIPMENT_QUANTITY, equipment.getQuantity()); // ✅ ADD THIS
            values.put(COL_EQUIPMENT_AVAILABLE_QUANTITY, equipment.getAvailableQuantity()); // ✅ ADD THIS

            String whereClause = COL_EQUIPMENT_ID + " = ?";
            String[] whereArgs = {String.valueOf(equipment.getId())};

            int rowsAffected = db.update(TABLE_EQUIPMENT, values, whereClause, whereArgs);
            success = rowsAffected > 0;

            if (success) {
                Log.d("DatabaseHelper", "Equipment updated: " + equipment.getName() +
                        " Qty: " + equipment.getQuantity() +
                        " Available: " + equipment.getAvailableQuantity());
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error updating equipment: " + e.getMessage());
        } finally {
            db.close();
        }
        return success;
    }
    // ✅ ONE-TIME FIX: Update all existing equipment to set available_quantity = quantity
    public void fixExistingEquipmentQuantities() {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            // Update all equipment where available_quantity doesn't match quantity
            db.execSQL(
                    "UPDATE " + TABLE_EQUIPMENT +
                            " SET " + COL_EQUIPMENT_AVAILABLE_QUANTITY + " = " + COL_EQUIPMENT_QUANTITY +
                            " WHERE " + COL_EQUIPMENT_STATUS + " = 'available'"
            );

            Log.d("DatabaseHelper", "Fixed existing equipment quantities");
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error fixing quantities: " + e.getMessage());
        } finally {
            db.close();
        }
    }

    public long addGalleryItem(GalleryItem item) {
        SQLiteDatabase db = this.getWritableDatabase();
        long result = -1;
        try {
            ContentValues values = new ContentValues();
            values.put(COL_GALLERY_TITLE, item.getTitle());
            values.put(COL_GALLERY_EVENT_NAME, item.getEventName());
            values.put(COL_GALLERY_DESCRIPTION, item.getDescription());
            values.put(COL_GALLERY_IMAGE_PATH, item.getImagePath());
            values.put(COL_GALLERY_UPLOADED_BY, item.getUploadedBy());

            result = db.insert(TABLE_GALLERY, null, values);
            Log.d("DatabaseHelper", "Gallery item added with ID: " + result);
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error adding gallery item: " + e.getMessage());
        } finally {
            db.close();
        }
        return result;
    }

    public List<GalleryItem> getAllGalleryItems() {
        List<GalleryItem> galleryItems = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT * FROM " + TABLE_GALLERY +
                " ORDER BY " + COL_GALLERY_UPLOADED_DATE + " DESC";

        Cursor cursor = db.rawQuery(query, null);

        while (cursor.moveToNext()) {
            GalleryItem item = new GalleryItem();
            item.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_GALLERY_ID)));
            item.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(COL_GALLERY_TITLE)));
            item.setEventName(cursor.getString(cursor.getColumnIndexOrThrow(COL_GALLERY_EVENT_NAME)));
            item.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(COL_GALLERY_DESCRIPTION)));
            item.setImagePath(cursor.getString(cursor.getColumnIndexOrThrow(COL_GALLERY_IMAGE_PATH)));
            item.setUploadedBy(cursor.getInt(cursor.getColumnIndexOrThrow(COL_GALLERY_UPLOADED_BY)));
            item.setUploadedDate(cursor.getString(cursor.getColumnIndexOrThrow(COL_GALLERY_UPLOADED_DATE)));

            galleryItems.add(item);
        }

        cursor.close();
        db.close();
        return galleryItems;
    }

    public boolean deleteGalleryItem(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        boolean success = false;
        try {
            int rowsDeleted = db.delete(TABLE_GALLERY,
                    COL_GALLERY_ID + " = ?",
                    new String[]{String.valueOf(id)});
            success = rowsDeleted > 0;
            Log.d("DatabaseHelper", "Gallery item deleted: " + success);
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error deleting gallery item: " + e.getMessage());
        } finally {
            db.close();
        }
        return success;
    }

    public String getUserFullName(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String fullName = "Unknown";

        Cursor cursor = db.rawQuery("SELECT " + COL_USER_FULL_NAME +
                        " FROM " + TABLE_USERS +
                        " WHERE " + COL_USER_ID + " = ?",
                new String[]{String.valueOf(userId)});

        if (cursor.moveToFirst()) {
            fullName = cursor.getString(0);
        }

        cursor.close();
        db.close();
        return fullName;
    }

}

