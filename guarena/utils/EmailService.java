package com.example.guarena.utils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.text.Html;
import android.util.Log;

public class EmailService {

    private static final String TAG = "EmailService";

    public interface EmailCallback {
        void onSuccess();
        void onFailure(String error);
    }

    // TEAM ADDITION EMAIL - SIMPLIFIED
    public static void sendTeamAdditionEmail(Context context, String studentEmail,
                                             String studentName, String teamName,
                                             String coachName, String sport,
                                             EmailCallback callback) {

        String subject = "🎉 Welcome to " + teamName + "!";
        String body = createTeamAdditionEmailBody(studentName, teamName, coachName, sport);

        sendEmailViaIntent(context, studentEmail, subject, body, callback);
    }

    // GLOBAL EVENT NOTIFICATION EMAIL - SIMPLIFIED
    public static void sendGlobalEventNotification(Context context, String userEmail,
                                                   String userName, String eventTitle,
                                                   String eventDate, String eventLocation,
                                                   String teamName, String creatorName,
                                                   boolean isParticipating,
                                                   EmailCallback callback) {

        String subject = "🏆 New Event: " + eventTitle;
        String participationStatus = isParticipating ?
                "🎯 You're participating in this event!" :
                "📢 Open event - anyone can join!";

        String body = createGlobalEventEmailBody(userName, eventTitle, eventDate,
                eventLocation, teamName, creatorName,
                participationStatus);

        sendEmailViaIntent(context, userEmail, subject, body, callback);
    }

    //  SIMPLE EMAIL INTENT METHOD
    private static void sendEmailViaIntent(Context context, String toEmail, String subject, String body, EmailCallback callback) {
        try {
            Intent emailIntent = new Intent(Intent.ACTION_SEND);
            emailIntent.setType("message/rfc822"); // Only email apps
            emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{toEmail});
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
            emailIntent.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(body));
            emailIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            context.startActivity(Intent.createChooser(emailIntent, "Send Event Notification"));

            Log.d(TAG, "Email intent created for: " + toEmail);

            // Call success callback
            if (callback != null) {
                callback.onSuccess();
            }

        } catch (Exception e) {
            Log.e(TAG, "Could not open email app: " + e.getMessage());

            // Call failure callback
            if (callback != null) {
                callback.onFailure("Could not open email app: " + e.getMessage());
            }
        }
    }

    // EMAIL BODY CREATORS - SIMPLIFIED HTML
    private static String createTeamAdditionEmailBody(String studentName, String teamName,
                                                      String coachName, String sport) {
        return "<html><body>" +
                "<h2 style='color: #2196F3;'>🎉 Welcome to the Team! 🎉</h2>" +
                "<p>Dear " + studentName + ",</p>" +
                "<p>Congratulations! You have been added to the <b>" + teamName + "</b> team.</p>" +
                "<div style='background-color: #f5f5f5; padding: 15px; margin: 20px 0;'>" +
                "<h3 style='color: #2196F3;'>Team Details:</h3>" +
                "<p><b>Team:</b> " + teamName + "</p>" +
                "<p><b>Sport:</b> " + sport + "</p>" +
                "<p><b>Coach:</b> " + coachName + "</p>" +
                "</div>" +
                "<p>You can now:</p>" +
                "<ul>" +
                "<li>View team events and schedules</li>" +
                "<li>Track your performance</li>" +
                "<li>Connect with teammates</li>" +
                "</ul>" +
                "<p>Please log into the GuArena app to see your team dashboard.</p>" +
                "<p>Best regards,<br><b>GuArena Sports Team</b></p>" +
                "</body></html>";
    }

    private static String createGlobalEventEmailBody(String userName, String eventTitle,
                                                     String eventDate, String eventLocation,
                                                     String teamName, String creatorName,
                                                     String participationStatus) {
        return "<html><body>" +
                "<h2 style='color: #FF9800;'>🏆 New Event Alert! 🏆</h2>" +
                "<p>Hi " + userName + ",</p>" +
                "<p>A new event has been scheduled in GuArena Sports system.</p>" +
                "<div style='background-color: #fff3e0; padding: 15px; margin: 20px 0;'>" +
                "<h3 style='color: #FF9800;'>Event Details:</h3>" +
                "<p><b>Event:</b> " + eventTitle + "</p>" +
                "<p><b>Date & Time:</b> " + eventDate + "</p>" +
                "<p><b>Location:</b> " + eventLocation + "</p>" +
                "<p><b>Team/Category:</b> " + teamName + "</p>" +
                "<p><b>Organized by:</b> " + creatorName + "</p>" +
                "</div>" +
                "<div style='background-color: #e3f2fd; padding: 15px; margin: 20px 0;'>" +
                "<h3 style='color: #1976d2;'>Your Status:</h3>" +
                "<p><b>" + participationStatus + "</b></p>" +
                "</div>" +
                "<p><b>What you can do:</b></p>" +
                "<ul>" +
                "<li>Open the GuArena app to view full event details</li>" +
                "<li>Check your participation status</li>" +
                "<li>Connect with other participants</li>" +
                "</ul>" +
                "<p>Don't miss out on this exciting event!</p>" +
                "<p>Best regards,<br><b>" + creatorName + " (GuArena Sports)</b></p>" +
                "</body></html>";
    }
}



