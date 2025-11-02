package com.example.guarena.utils;

import android.util.Log;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utility class for password hashing and verification
 * Uses SHA-256 with salt for secure password storage
 */
public class PasswordUtils {

    private static final String TAG = "PasswordUtils";
    private static final String HASH_ALGORITHM = "SHA-256";
    private static final int SALT_LENGTH = 16;

    /**
     * Hash a password with salt using SHA-256
     * @param password Plain text password
     * @return Hashed password with salt (format: salt:hash)
     */
    public static String hashPassword(String password) {
        try {
            // Generate random salt
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[SALT_LENGTH];
            random.nextBytes(salt);

            // Hash password with salt
            MessageDigest md = MessageDigest.getInstance(HASH_ALGORITHM);
            md.update(salt);
            byte[] hashedPassword = md.digest(password.getBytes("UTF-8"));

            // Combine salt and hash
            String saltString = Base64.getEncoder().encodeToString(salt);
            String hashString = Base64.getEncoder().encodeToString(hashedPassword);

            return saltString + ":" + hashString;

        } catch (Exception e) {
            Log.e(TAG, "Error hashing password: " + e.getMessage());
            // Fallback to simple hash if advanced hashing fails
            return simpleHash(password);
        }
    }

    /**
     * Verify if a password matches the hashed password
     * @param password Plain text password to verify
     * @param hashedPassword Stored hashed password (format: salt:hash)
     * @return true if password matches, false otherwise
     */
    public static boolean verifyPassword(String password, String hashedPassword) {
        try {
            // Check if it's old format (simple hash) or new format (salt:hash)
            if (!hashedPassword.contains(":")) {
                // Old simple hash format
                return hashedPassword.equals(simpleHash(password));
            }

            // New salt:hash format
            String[] parts = hashedPassword.split(":");
            if (parts.length != 2) {
                return false;
            }

            byte[] salt = Base64.getDecoder().decode(parts[0]);
            String storedHash = parts[1];

            // Hash the input password with the same salt
            MessageDigest md = MessageDigest.getInstance(HASH_ALGORITHM);
            md.update(salt);
            byte[] hashedInputPassword = md.digest(password.getBytes("UTF-8"));
            String hashString = Base64.getEncoder().encodeToString(hashedInputPassword);

            return hashString.equals(storedHash);

        } catch (Exception e) {
            Log.e(TAG, "Error verifying password: " + e.getMessage());
            // Fallback verification
            return hashedPassword.equals(simpleHash(password));
        }
    }

    /**
     * Simple hash function as fallback
     * @param password Plain text password
     * @return Simple hashed password
     */
    private static String simpleHash(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hashBytes = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "MD5 algorithm not available, using plain text");
            return password; // Last resort - not secure!
        }
    }

    /**
     * Check if password meets minimum requirements
     * @param password Password to validate
     * @return true if password is strong enough
     */
    public static boolean isPasswordStrong(String password) {
        if (password == null || password.length() < 6) {
            return false;
        }

        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasDigit = false;

        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            if (Character.isLowerCase(c)) hasLower = true;
            if (Character.isDigit(c)) hasDigit = true;
        }

        // For now, just require minimum length
        // You can make it stricter by requiring: hasUpper && hasLower && hasDigit
        return password.length() >= 6;
    }

    /**
     * Generate a random password
     * @param length Length of password to generate
     * @return Random password string
     */
    public static String generateRandomPassword(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder();

        for (int i = 0; i < length; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }

        return password.toString();
    }
}
