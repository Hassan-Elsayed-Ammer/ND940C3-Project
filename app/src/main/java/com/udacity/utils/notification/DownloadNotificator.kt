package com.udacity.utils.notification

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import com.udacity.R
import com.udacity.utils.DownloadStatus

class DownloadNotificator(
    private val context: Context,
    private val lifecycle: Lifecycle
) : LifecycleObserver {

    init {
        lifecycle.addObserver(this)
    }

    fun notify(
        fileName: String,
        downloadStatus: DownloadStatus
    ) {
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            Toast.makeText(
                context,
                context.getString(R.string.download_completed),
                Toast.LENGTH_SHORT
            ).show()
        }
        with(context.applicationContext) {
            getNotificationManager().run {
                createChannel(applicationContext)
                sendCompleteNotification(
                    fileName,
                    downloadStatus,
                    applicationContext
                )
            }
        }
    }

}