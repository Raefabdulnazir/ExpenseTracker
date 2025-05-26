package com.example.expensetracker.work

import android.content.Context
import android.util.Log
import java.util.Calendar
import java.util.concurrent.TimeUnit
import androidx.work.*

object NotificationScheduler {
    fun scheduleDailyNotification(context : Context){   //to use workmanager,we need to use Context
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY,20)//8pm
            set(Calendar.MINUTE,0)
            set(Calendar.SECOND,0)
            if (before(now)){
                add(Calendar.DAY_OF_YEAR,1)
            }
        }

        val delay = target.timeInMillis - now.timeInMillis
        Log.d("NotificationScheduler", "Calculated delay: $delay ms (${delay / (1000 * 60)} minutes)")

        //creates a request that will run notification worker repeatedly for 24 hours
        val dailyWorkRequest = PeriodicWorkRequestBuilder<DailyNotificationWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .addTag("daily_notification_tag") // Add tag for tracking
            .build()

        //gets the workmanager service and adds work request to it
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "daily_notification_work",
            ExistingPeriodicWorkPolicy.UPDATE,
            dailyWorkRequest
        )

        Log.d("NotificationScheduler", "Work scheduled with ID: ${dailyWorkRequest.id}")

    }

    fun testNotification(context: Context) {//for testing purpose
        val testWorkRequest = OneTimeWorkRequestBuilder<DailyNotificationWorker>()
            .setInitialDelay(10, TimeUnit.SECONDS) // run after 10 seconds
            .addTag("test_notification")
            .build()

        WorkManager.getInstance(context).enqueue(testWorkRequest)
    }

}