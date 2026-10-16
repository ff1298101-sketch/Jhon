package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity

object JamesNotificationManager {
    private const val CHANNEL_MESSAGES_ID = "james_chat_messages"
    private const val CHANNEL_CALLS_ID = "james_chat_incoming_calls"
    const val INCOMING_CALL_NOTIFICATION_ID = 9999

    const val ACTION_ACCEPT_CALL = "com.example.ACTION_ACCEPT_CALL"
    const val ACTION_DECLINE_CALL = "com.example.ACTION_DECLINE_CALL"

    fun init(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Messages channel
            val messageChannel = NotificationChannel(
                CHANNEL_MESSAGES_ID,
                "James Chat Messages",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications pour nouveaux messages dans James Chat"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 200, 100, 200)
            }
            notificationManager.createNotificationChannel(messageChannel)

            // High priority calls channel
            val callChannel = NotificationChannel(
                CHANNEL_CALLS_ID,
                "James Chat Appels Entrants",
                NotificationManager.IMPORTANCE_MAX
            ).apply {
                description = "Sonneries et alertes d'appels audio et vidéo entrants"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 800, 400, 800, 400)
            }
            notificationManager.createNotificationChannel(callChannel)
        }
    }

    fun showMessageNotification(
        context: Context,
        senderName: String,
        messageText: String,
        conversationId: String,
        notificationId: Int = (System.currentTimeMillis() % 10000).toInt()
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("conversation_id", conversationId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_MESSAGES_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("💬 $senderName")
            .setContentText(messageText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(messageText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        try {
            val notificationManager = NotificationManagerCompat.from(context)
            if (notificationManager.areNotificationsEnabled()) {
                notificationManager.notify(notificationId, builder.build())
            }
        } catch (_: SecurityException) {}
    }

    fun showIncomingCallNotification(
        context: Context,
        callerId: String,
        callerName: String,
        isVideo: Boolean
    ) {
        val fullScreenIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("incoming_call_user_id", callerId)
            putExtra("incoming_call_user_name", callerName)
            putExtra("incoming_call_is_video", isVideo)
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(
            context,
            INCOMING_CALL_NOTIFICATION_ID,
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val acceptIntent = Intent(context, MainActivity::class.java).apply {
            action = ACTION_ACCEPT_CALL
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("incoming_call_user_id", callerId)
            putExtra("incoming_call_user_name", callerName)
            putExtra("incoming_call_is_video", isVideo)
        }
        val acceptPendingIntent = PendingIntent.getActivity(
            context,
            INCOMING_CALL_NOTIFICATION_ID + 1,
            acceptIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val declineIntent = Intent(context, MainActivity::class.java).apply {
            action = ACTION_DECLINE_CALL
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("incoming_call_user_id", callerId)
        }
        val declinePendingIntent = PendingIntent.getActivity(
            context,
            INCOMING_CALL_NOTIFICATION_ID + 2,
            declineIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val callTypeLabel = if (isVideo) "Appel vidéo WebRTC entrant" else "Appel audio haute définition"

        val builder = NotificationCompat.Builder(context, CHANNEL_CALLS_ID)
            .setSmallIcon(android.R.drawable.stat_sys_phone_call)
            .setContentTitle("📞 $callerName")
            .setContentText(callTypeLabel)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setOngoing(true)
            .setAutoCancel(true)
            .setContentIntent(fullScreenPendingIntent)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .addAction(android.R.drawable.ic_menu_call, "Accepter", acceptPendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Refuser", declinePendingIntent)

        try {
            val notificationManager = NotificationManagerCompat.from(context)
            if (notificationManager.areNotificationsEnabled()) {
                notificationManager.notify(INCOMING_CALL_NOTIFICATION_ID, builder.build())
            }
        } catch (_: SecurityException) {}
    }

    fun cancelCallNotification(context: Context) {
        try {
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.cancel(INCOMING_CALL_NOTIFICATION_ID)
        } catch (_: Exception) {}
    }
}
