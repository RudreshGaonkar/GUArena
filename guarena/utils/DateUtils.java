package com.example.guarena.utils;

import android.util.Log;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Utility class for date and time operations
 */
public class DateUtils {

    private static final String TAG = "DateUtils";

    // Date format patterns
    private static final String DATABASE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final String DISPLAY_DATE_FORMAT = "MMM dd, yyyy";
    private static final String DISPLAY_TIME_FORMAT = "hh:mm a";
    private static final String DISPLAY_DATETIME_FORMAT = "MMM dd, yyyy hh:mm a";

    /**
     * Get current date and time as string for database storage
     * @return Current datetime in database format
     */
    public static String getCurrentDateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat(DATABASE_FORMAT, Locale.getDefault());
        return sdf.format(new Date());
    }

    /**
     * Get current date as string for database storage
     * @return Current date in database format
     */
    public static String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(new Date());
    }

    /**
     * Format database datetime string for display
     * @param databaseDateTime Datetime string from database
     * @return Formatted datetime string for display
     */
    public static String formatForDisplay(String databaseDateTime) {
        if (databaseDateTime == null || databaseDateTime.isEmpty()) {
            return "";
        }

        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat(DATABASE_FORMAT, Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat(DISPLAY_DATETIME_FORMAT, Locale.getDefault());

            Date date = inputFormat.parse(databaseDateTime);
            return outputFormat.format(date);
        } catch (ParseException e) {
            Log.e(TAG, "Error formatting date for display: " + e.getMessage());
            return databaseDateTime; // Return original if parsing fails
        }
    }

    /**
     * Format database date string for display (date only)
     * @param databaseDate Date string from database
     * @return Formatted date string for display
     */
    public static String formatDateForDisplay(String databaseDate) {
        if (databaseDate == null || databaseDate.isEmpty()) {
            return "";
        }

        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat(DISPLAY_DATE_FORMAT, Locale.getDefault());

            Date date = inputFormat.parse(databaseDate);
            return outputFormat.format(date);
        } catch (ParseException e) {
            Log.e(TAG, "Error formatting date for display: " + e.getMessage());
            return databaseDate;
        }
    }

    /**
     * Format time for display
     * @param databaseDateTime Datetime string from database
     * @return Formatted time string
     */
    public static String formatTimeForDisplay(String databaseDateTime) {
        if (databaseDateTime == null || databaseDateTime.isEmpty()) {
            return "";
        }

        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat(DATABASE_FORMAT, Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat(DISPLAY_TIME_FORMAT, Locale.getDefault());

            Date date = inputFormat.parse(databaseDateTime);
            return outputFormat.format(date);
        } catch (ParseException e) {
            Log.e(TAG, "Error formatting time for display: " + e.getMessage());
            return databaseDateTime;
        }
    }

    /**
     * Get relative time string (e.g., "2 hours ago", "Yesterday")
     * @param databaseDateTime Datetime string from database
     * @return Relative time string
     */
    public static String getRelativeTime(String databaseDateTime) {
        if (databaseDateTime == null || databaseDateTime.isEmpty()) {
            return "";
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat(DATABASE_FORMAT, Locale.getDefault());
            Date date = sdf.parse(databaseDateTime);
            Date now = new Date();

            long diffInMs = now.getTime() - date.getTime();
            long diffInSeconds = TimeUnit.MILLISECONDS.toSeconds(diffInMs);
            long diffInMinutes = TimeUnit.MILLISECONDS.toMinutes(diffInMs);
            long diffInHours = TimeUnit.MILLISECONDS.toHours(diffInMs);
            long diffInDays = TimeUnit.MILLISECONDS.toDays(diffInMs);

            if (diffInSeconds < 60) {
                return "Just now";
            } else if (diffInMinutes < 60) {
                return diffInMinutes + (diffInMinutes == 1 ? " minute ago" : " minutes ago");
            } else if (diffInHours < 24) {
                return diffInHours + (diffInHours == 1 ? " hour ago" : " hours ago");
            } else if (diffInDays == 1) {
                return "Yesterday";
            } else if (diffInDays < 7) {
                return diffInDays + " days ago";
            } else {
                return formatDateForDisplay(databaseDateTime);
            }

        } catch (ParseException e) {
            Log.e(TAG, "Error calculating relative time: " + e.getMessage());
            return formatForDisplay(databaseDateTime);
        }
    }

    /**
     * Check if a date is today
     * @param databaseDate Date string from database
     * @return true if date is today
     */
    public static boolean isToday(String databaseDate) {
        if (databaseDate == null || databaseDate.isEmpty()) {
            return false;
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date date = sdf.parse(databaseDate.substring(0, 10)); // Extract date part
            Date today = new Date();

            Calendar cal1 = Calendar.getInstance();
            cal1.setTime(date);
            Calendar cal2 = Calendar.getInstance();
            cal2.setTime(today);

            return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                    cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
        } catch (Exception e) {
            Log.e(TAG, "Error checking if date is today: " + e.getMessage());
            return false;
        }
    }

    /**
     * Check if a datetime is upcoming (in the future)
     * @param databaseDateTime Datetime string from database
     * @return true if datetime is in the future
     */
    public static boolean isUpcoming(String databaseDateTime) {
        if (databaseDateTime == null || databaseDateTime.isEmpty()) {
            return false;
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat(DATABASE_FORMAT, Locale.getDefault());
            Date date = sdf.parse(databaseDateTime);
            Date now = new Date();
            return date.after(now);
        } catch (ParseException e) {
            Log.e(TAG, "Error checking if date is upcoming: " + e.getMessage());
            return false;
        }
    }

    /**
     * Add days to a date
     * @param databaseDate Date string from database
     * @param days Number of days to add
     * @return New date string
     */
    public static String addDays(String databaseDate, int days) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date date = sdf.parse(databaseDate);

            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            cal.add(Calendar.DAY_OF_MONTH, days);

            return sdf.format(cal.getTime());
        } catch (ParseException e) {
            Log.e(TAG, "Error adding days to date: " + e.getMessage());
            return databaseDate;
        }
    }

    /**
     * Convert user-friendly datetime input to database format
     * @param userInput User input (e.g., "Dec 25, 2024 3:30 PM")
     * @return Database format datetime string
     */
    public static String convertToDatabase(String userInput) {
        if (userInput == null || userInput.isEmpty()) {
            return getCurrentDateTime();
        }

        // Try multiple input formats
        String[] inputFormats = {
                DISPLAY_DATETIME_FORMAT,
                "yyyy-MM-dd HH:mm",
                "MM/dd/yyyy HH:mm",
                "dd/MM/yyyy HH:mm"
        };

        for (String format : inputFormats) {
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat(format, Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat(DATABASE_FORMAT, Locale.getDefault());

                Date date = inputFormat.parse(userInput);
                return outputFormat.format(date);
            } catch (ParseException e) {
                // Try next format
                continue;
            }
        }

        Log.e(TAG, "Could not parse date input: " + userInput);
        return getCurrentDateTime(); // Fallback to current time
    }

    /**
     * Get start of day timestamp
     * @param date Date to get start of day for
     * @return Start of day timestamp
     */
    public static String getStartOfDay(String date) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date dateObj = inputFormat.parse(date);

            Calendar cal = Calendar.getInstance();
            cal.setTime(dateObj);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);

            SimpleDateFormat outputFormat = new SimpleDateFormat(DATABASE_FORMAT, Locale.getDefault());
            return outputFormat.format(cal.getTime());
        } catch (ParseException e) {
            Log.e(TAG, "Error getting start of day: " + e.getMessage());
            return date + " 00:00:00";
        }
    }

    /**
     * Get end of day timestamp
     * @param date Date to get end of day for
     * @return End of day timestamp
     */
    public static String getEndOfDay(String date) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date dateObj = inputFormat.parse(date);

            Calendar cal = Calendar.getInstance();
            cal.setTime(dateObj);
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
            cal.set(Calendar.MILLISECOND, 999);

            SimpleDateFormat outputFormat = new SimpleDateFormat(DATABASE_FORMAT, Locale.getDefault());
            return outputFormat.format(cal.getTime());
        } catch (ParseException e) {
            Log.e(TAG, "Error getting end of day: " + e.getMessage());
            return date + " 23:59:59";
        }
    }

    /**
     * Validate if a string is a valid date
     * @param dateString Date string to validate
     * @param format Expected format
     * @return true if valid date
     */
    public static boolean isValidDate(String dateString, String format) {
        if (dateString == null || dateString.isEmpty()) {
            return false;
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());
            sdf.setLenient(false); // Strict parsing
            sdf.parse(dateString);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }
}
