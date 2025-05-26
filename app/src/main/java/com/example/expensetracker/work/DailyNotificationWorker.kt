package com.example.expensetracker.work

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.ContextParams
import android.content.Intent
import androidx.work.Worker
import androidx.work.WorkerParameters
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.expensetracker.MainActivity
import com.example.expensetracker.R

class DailyNotificationWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context , workerParams)
{

    override fun doWork(): Result {
        Log.d("DailyNotificationWorker", "Worker started!")
        showNotification()
        return Result.success()
    }

    private fun showNotification(){
        val channelid = "expense_reminder_channel"
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        //Intent that opens a new screen
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        //1. Create a notification channel(for android 8+)
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val channel = NotificationChannel(
                channelid,
                "Daily Expense Reminder",
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.description = "Reminders to enter your daily expenses"
            notificationManager.createNotificationChannel(channel)
        }

        //2. Build the notification
        val notification = NotificationCompat.Builder(applicationContext,channelid)
            .setContentTitle("Expense Tracker")
            .setContentText("Don't forget to enter your expenses or income")
            .setSmallIcon(R.drawable.ic_notification)   // you need an icon in drawable
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        //3. Show the notification
        notificationManager.notify(1, notification)
        Log.d("DailyNotificationWorker", "Notification displayed!")
    }

}