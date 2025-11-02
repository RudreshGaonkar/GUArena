package com.example.guarena.utils;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.guarena.database.DatabaseHelper;
import com.example.guarena.models.EquipmentBorrowing;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EquipmentReminderWorker extends Worker {

    public EquipmentReminderWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            Context context = getApplicationContext();
            DatabaseHelper databaseHelper = new DatabaseHelper(context);

            // Get all active borrowings
            List<EquipmentBorrowing> borrowings = databaseHelper.getAllActiveBorrowings();

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date now = new Date();

            for (EquipmentBorrowing borrowing : borrowings) {
                try {
                    Date dueDate = sdf.parse(borrowing.getDueDate());

                    if (dueDate != null && dueDate.before(now)) {
                        // Equipment is overdue
                        NotificationService.showEquipmentReturnReminder(
                                context,
                                borrowing.getEquipmentName(),
                                borrowing.getDueDate(),
                                borrowing.getEquipmentId()
                        );
                    } else if (dueDate != null) {
                        // Check if due soon (within 1 hour)
                        long timeDiff = dueDate.getTime() - now.getTime();
                        long hoursDiff = timeDiff / (60 * 60 * 1000);

                        if (hoursDiff <= 1) {
                            NotificationService.showEquipmentReturnReminder(
                                    context,
                                    borrowing.getEquipmentName(),
                                    borrowing.getDueDate(),
                                    borrowing.getEquipmentId()
                            );
                        }
                    }
                } catch (Exception e) {
                    Log.e("EquipmentReminder", "Error processing borrowing: " + e.getMessage());
                }
            }

            return Result.success();

        } catch (Exception e) {
            Log.e("EquipmentReminder", "Worker failed: " + e.getMessage());
            return Result.failure();
        }
    }
}
