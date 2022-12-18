package com.udacity.utils.notification

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.udacity.R
import com.udacity.ui.DetailActivity
import com.udacity.ui.DetailActivity.Companion.bundleExtrasOf
import com.udacity.utils.DOWNLOAD_COMPLETED_ID
import com.udacity.utils.DownloadStatus
import com.udacity.utils.NOTIFICATION_REQUEST_CODE


fun Context.getNotificationManager(): NotificationManager = ContextCompat.getSystemService(
    this,
    NotificationManager::class.java
) as NotificationManager

fun Context.getDownloadManager(): DownloadManager = ContextCompat.getSystemService(
    this,
    DownloadManager::class.java
) as DownloadManager

fun NotificationManager.sendCompleteNotification(
    fileName: String,
    downloadStatus: DownloadStatus,
    context: Context
) {
    val contentIntent = Intent(context, DetailActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        putExtras(bundleExtrasOf(fileName, downloadStatus))
    }
    val contentPendingIntent = PendingIntent.getActivity(
        context,
        NOTIFICATION_REQUEST_CODE,
        contentIntent,
        PendingIntent.FLAG_UPDATE_CURRENT
    )


    val checkStatusAction = NotificationCompat.Action.Builder(
        R.drawable.ic_assistant_black_24dp,
        context.getString(R.string.notification_action_check_status),
        contentPendingIntent
    ).build()


    NotificationCompat.Builder(  context,
        context.getString(R.string.notification_channel_id)
    )
        .setSmallIcon(R.drawable.ic_assistant_black_24dp)
        .setContentTitle(context.getString(R.string.notification_title))
        .setContentText(context.getString(R.string.notification_description))
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setContentIntent(contentPendingIntent)
        .setAutoCancel(true)
        .addAction(checkStatusAction)
        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        .apply {}
        .run {
            notify(DOWNLOAD_COMPLETED_ID, this.build())
        }
}

@SuppressLint("NewApi")
fun NotificationManager.createChannel(context: Context) {
    Build.VERSION.SDK_INT.takeIf { it >= Build.VERSION_CODES.O }?.run {
        NotificationChannel(
            context.getString(R.string.notification_channel_id),
            context.getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = context.getString(R.string.notification_channel_description)
            setShowBadge(true)
        }.run {
            createNotificationChannel(this)
        }
    }
}