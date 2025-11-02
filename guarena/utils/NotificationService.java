package com.example.guarena.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.example.guarena.R;
import com.example.guarena.activities.DashboardActivity;
import com.example.guarena.activities.EquipmentActivity;

public class NotificationService {

    private static final String CHANNEL_ID = "guarena_events";
    private static final String CHANNEL_NAME = "GuArena Events";
    private static final int NOTIFICATION_ID_BASE = 1000;

    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Notifications for GuArena events and team updates");

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public static void showEventCreatedNotification(Context context, String eventTitle,
                                                    String teamName, String eventDate) {
        Intent intent = new Intent(context, DashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_events) // You need to add this icon
                .setContentTitle("🏆 New Event: " + eventTitle)
                .setContentText("Team " + teamName + " • " + eventDate)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("New event '" + eventTitle + "' has been scheduled for your team " + teamName + " on " + eventDate + ". Check the app for details!"))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID_BASE + eventTitle.hashCode(), builder.build());
    }

    public static void showTeamJoinedNotification(Context context, String teamName, String sport) {
        Intent intent = new Intent(context, DashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_teams) // You need to add this icon
                .setContentTitle("🎉 Welcome to " + teamName + "!")
                .setContentText("You've joined the " + sport + " team")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Congratulations! You have been added to " + teamName + " (" + sport + "). Check your team dashboard for events and updates."))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID_BASE + teamName.hashCode(), builder.build());
    }

    /**
     * Show equipment return reminder notification
     */
    public static void showEquipmentReturnReminder(Context context, String equipmentName,
                                                   String dueDate, int equipmentId) {
        Intent intent = new Intent(context, EquipmentActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, equipmentId, intent,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_sports)
                .setContentTitle("⏰ Equipment Return Reminder")
                .setContentText(equipmentName + " is due for return!")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Please return '" + equipmentName + "' by " + dueDate +
                                ". Return it through the Equipment section in the app."))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setVibrate(new long[]{0, 500, 250, 500}); // Vibration pattern

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID_BASE + equipmentId, builder.build());
    }

    /**
     * Show equipment borrowed confirmation notification
     */
    public static void showEquipmentBorrowedNotification(Context context, String equipmentName,
                                                         String dueDate) {
        Intent intent = new Intent(context, EquipmentActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_sports)
                .setContentTitle("✅ Equipment Borrowed")
                .setContentText("You borrowed: " + equipmentName)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Successfully borrowed '" + equipmentName + "'. Please return it by " +
                                dueDate + ". You'll receive a reminder notification."))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID_BASE + equipmentName.hashCode(), builder.build());
    }

    /**
     * Show equipment returned confirmation notification
     */
    public static void showEquipmentReturnedNotification(Context context, String equipmentName) {
        Intent intent = new Intent(context, EquipmentActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_check)
                .setContentTitle("✅ Equipment Returned")
                .setContentText("Thank you for returning " + equipmentName)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Equipment '" + equipmentName + "' has been successfully returned. " +
                                "Thank you for being responsible!"))
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID_BASE + equipmentName.hashCode(), builder.build());
    }

    /**
     * Show overdue equipment notification (for coach/admin)
     */
    public static void showOverdueEquipmentNotification(Context context, String studentName,
                                                        String equipmentName, String overdueBy) {
        Intent intent = new Intent(context, EquipmentActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_sports)
                .setContentTitle("⚠️ Overdue Equipment")
                .setContentText(studentName + " - " + equipmentName + " overdue")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Student " + studentName + " has not returned '" + equipmentName +
                                "' which was due " + overdueBy + ". Please follow up."))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setColor(0xFFF44336); // Red color for urgency

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID_BASE + equipmentName.hashCode(), builder.build());
    }
}
