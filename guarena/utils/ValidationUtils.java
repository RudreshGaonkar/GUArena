package com.example.guarena.utils;

import android.text.TextUtils;
import android.util.Patterns;
import java.util.regex.Pattern;

/**
 * Utility class for input validation
 */
public class ValidationUtils {

    // Regex patterns
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,20}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[+]?[0-9]{10,15}$");
    private static final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-Z\\s]{2,50}$");

    /**
     * Validate email address
     * @param email Email to validate
     * @return true if valid email
     */
    public static boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) &&
                Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    /**
     * Validate username
     * @param username Username to validate
     * @return true if valid username
     */
    public static boolean isValidUsername(String username) {
        return !TextUtils.isEmpty(username) &&
                username.length() >= Constants.MIN_USERNAME_LENGTH &&
                USERNAME_PATTERN.matcher(username).matches();
    }

    /**
     * Validate password
     * @param password Password to validate
     * @return true if valid password
     */
    public static boolean isValidPassword(String password) {
        return !TextUtils.isEmpty(password) &&
                password.length() >= Constants.MIN_PASSWORD_LENGTH;
    }

    /**
     * Validate password strength
     * @param password Password to validate
     * @return true if password meets strength requirements
     */
    public static boolean isStrongPassword(String password) {
        if (TextUtils.isEmpty(password) || password.length() < 8) {
            return false;
        }

        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasDigit = false;
        boolean hasSpecial = false;

        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            else if (Character.isLowerCase(c)) hasLower = true;
            else if (Character.isDigit(c)) hasDigit = true;
            else if (!Character.isLetterOrDigit(c)) hasSpecial = true;
        }

        return hasUpper && hasLower && hasDigit && hasSpecial;
    }

    /**
     * Validate phone number
     * @param phone Phone number to validate
     * @return true if valid phone number
     */
    public static boolean isValidPhone(String phone) {
        return !TextUtils.isEmpty(phone) &&
                phone.length() >= Constants.MIN_PHONE_LENGTH &&
                PHONE_PATTERN.matcher(phone).matches();
    }

    /**
     * Validate full name
     * @param name Name to validate
     * @return true if valid name
     */
    public static boolean isValidName(String name) {
        return !TextUtils.isEmpty(name) &&
                name.trim().length() >= Constants.MIN_FULLNAME_LENGTH &&
                NAME_PATTERN.matcher(name.trim()).matches();
    }

    /**
     * Validate team name
     * @param teamName Team name to validate
     * @return true if valid team name
     */
    public static boolean isValidTeamName(String teamName) {
        return !TextUtils.isEmpty(teamName) &&
                teamName.trim().length() >= 3 &&
                teamName.trim().length() <= 50;
    }

    /**
     * Validate event title
     * @param title Event title to validate
     * @return true if valid title
     */
    public static boolean isValidEventTitle(String title) {
        return !TextUtils.isEmpty(title) &&
                title.trim().length() >= 3 &&
                title.trim().length() <= 100;
    }

    /**
     * Validate performance score
     * @param score Performance score to validate
     * @return true if valid score
     */
    public static boolean isValidPerformanceScore(double score) {
        return score >= Constants.MIN_PERFORMANCE_SCORE &&
                score <= Constants.MAX_PERFORMANCE_SCORE;
    }

    /**
     * Validate jersey number
     * @param number Jersey number to validate
     * @return true if valid jersey number
     */
    public static boolean isValidJerseyNumber(int number) {
        return number > 0 && number <= 99;
    }

    /**
     * Validate height (in cm)
     * @param height Height to validate
     * @return true if valid height
     */
    public static boolean isValidHeight(double height) {
        return height >= 120.0 && height <= 250.0; // Reasonable range
    }

    /**
     * Validate weight (in kg)
     * @param weight Weight to validate
     * @return true if valid weight
     */
    public static boolean isValidWeight(double weight) {
        return weight >= 30.0 && weight <= 200.0; // Reasonable range
    }

    /**
     * Validate calories count
     * @param calories Calories to validate
     * @return true if valid calories
     */
    public static boolean isValidCalories(int calories) {
        return calories >= 1000 && calories <= 5000; // Daily calorie range
    }

    /**
     * Validate equipment name
     * @param name Equipment name to validate
     * @return true if valid name
     */
    public static boolean isValidEquipmentName(String name) {
        return !TextUtils.isEmpty(name) &&
                name.trim().length() >= 2 &&
                name.trim().length() <= 50;
    }

    /**
     * Check if string contains only letters and spaces
     * @param input String to check
     * @return true if contains only letters and spaces
     */
    public static boolean isOnlyLettersAndSpaces(String input) {
        if (TextUtils.isEmpty(input)) return false;
        return input.matches("^[a-zA-Z\\s]+$");
    }

    /**
     * Check if string contains only numbers
     * @param input String to check
     * @return true if contains only numbers
     */
    public static boolean isOnlyNumbers(String input) {
        if (TextUtils.isEmpty(input)) return false;
        return input.matches("^[0-9]+$");
    }

    /**
     * Validate URL
     * @param url URL to validate
     * @return true if valid URL
     */
    public static boolean isValidUrl(String url) {
        return !TextUtils.isEmpty(url) &&
                Patterns.WEB_URL.matcher(url).matches();
    }

    /**
     * Get password strength level
     * @param password Password to check
     * @return Strength level (0=Weak, 1=Fair, 2=Good, 3=Strong)
     */
    public static int getPasswordStrength(String password) {
        if (TextUtils.isEmpty(password)) return 0;

        int score = 0;

        // Length check
        if (password.length() >= 8) score++;

        // Character variety checks
        boolean hasUpper = false, hasLower = false, hasDigit = false, hasSpecial = false;

        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            else if (Character.isLowerCase(c)) hasLower = true;
            else if (Character.isDigit(c)) hasDigit = true;
            else if (!Character.isLetterOrDigit(c)) hasSpecial = true;
        }

        if (hasLower) score++;
        if (hasUpper && hasDigit) score++;
        if (hasSpecial && password.length() >= 10) score++;

        return Math.min(score, 3); // Max level 3
    }

    /**
     * Get password strength description
     * @param password Password to check
     * @return Strength description
     */
    public static String getPasswordStrengthDescription(String password) {
        int strength = getPasswordStrength(password);
        switch (strength) {
            case 0:
            case 1:
                return "Weak";
            case 2:
                return "Fair";
            case 3:
                return "Good";
            default:
                return "Strong";
        }
    }

    /**
     * Sanitize input string (remove potentially harmful characters)
     * @param input Input string
     * @return Sanitized string
     */
    public static String sanitizeInput(String input) {
        if (TextUtils.isEmpty(input)) return "";

        // Remove potentially harmful characters
        return input.replaceAll("[<>\"'&]", "").trim();
    }

    /**
     * Validate search query
     * @param query Search query to validate
     * @return true if valid search query
     */
    public static boolean isValidSearchQuery(String query) {
        return !TextUtils.isEmpty(query) &&
                query.trim().length() >= 1 &&
                query.trim().length() <= 100;
    }
}
