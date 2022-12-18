package com.udacity.ui

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.ContentObserver
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.databinding.DataBindingUtil
import com.udacity.R
import com.udacity.databinding.ActivityMainBinding
import com.udacity.utils.DownloadStatus
import com.udacity.utils.NO_DOWNLOAD
import com.udacity.utils.URL
import com.udacity.utils.animation.ButtonState
import com.udacity.utils.notification.DownloadNotificator
import com.udacity.utils.notification.getDownloadManager
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    private var downloadID: Long = NO_DOWNLOAD
    private lateinit var viewBinding: ActivityMainBinding
    private var downloadFileName = ""
    private var downloadContentObserver: ContentObserver? = null
    private var downloadNotificator: DownloadNotificator? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)
            .apply {
                viewBinding = this
                setSupportActionBar(toolbar)
                registerReceiver(
                    onDownloadCompletedReceiver,
                    IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
                )
                onLoadingButtonClicked()
            }
    }


    private fun ActivityMainBinding.onLoadingButtonClicked() {
        with(content) {
            customButton.setOnClickListener {
                when (rgOption.checkedRadioButtonId) {
                    View.NO_ID ->
                        Toast.makeText(
                            this@MainActivity,
                            R.string.chose_file,
                            Toast.LENGTH_SHORT
                        ).show()
                    else -> {
                        downloadFileName =
                            findViewById<RadioButton>(rgOption.checkedRadioButtonId)
                                .text.toString()
                        requestDownload()
                    }
                }
            }
        }
    }


    private fun requestDownload() {
        with(getDownloadManager()) {
            downloadID.takeIf { it != NO_DOWNLOAD }?.run {
                remove(downloadID)
                unRegisterObserver()
                downloadID = NO_DOWNLOAD
            }

            val request = DownloadManager.Request(Uri.parse(URL))
                .setTitle(getString(R.string.app_name))
                .setDescription(getString(R.string.app_description))
                .setRequiresCharging(false)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)

            downloadID = enqueue(request)

            createAndRegisterObserver()
        }
    }

    private fun DownloadManager.createAndRegisterObserver() {
        object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                downloadContentObserver?.run { queryProgress() }
            }
        }.also {
            downloadContentObserver = it
            contentResolver.registerContentObserver(
                "content://downloads/my_downloads".toUri(),
                true,
                downloadContentObserver!!
            )
        }
    }

    private fun DownloadManager.queryProgress() {
        query(DownloadManager.Query().setFilterById(downloadID)).use {
            with(it) {
                if (this != null && moveToFirst()) {
                    getInt(getColumnIndex(DownloadManager.COLUMN_ID))
                    when (getInt(getColumnIndex(DownloadManager.COLUMN_STATUS))) {
                        DownloadManager.STATUS_FAILED -> {
                            viewBinding.content.customButton.buttonState = ButtonState.Completed
                        }
                        DownloadManager.STATUS_PAUSED -> {
                        }
                        DownloadManager.STATUS_PENDING -> {
                        }
                        DownloadManager.STATUS_RUNNING -> {
                            viewBinding.content.customButton.buttonState = ButtonState.Loading
                        }
                        DownloadManager.STATUS_SUCCESSFUL -> {
                            viewBinding.content.customButton.buttonState = ButtonState.Completed
                        }
                    }
                }
            }
        }
    }

    private fun unRegisterObserver() {
        downloadContentObserver?.let {
            contentResolver.unregisterContentObserver(it)
            downloadContentObserver = null
        }
    }


    private val onDownloadCompletedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            id?.let {
                val downloadStatus = getDownloadManager().queryStatus(it)
                unRegisterObserver()
                downloadStatus.takeIf { status -> status != DownloadStatus.UNKNOWN }?.run {
                    getDownloadNotificator().notify(downloadFileName, downloadStatus)
                }
            }
        }
    }

    private fun DownloadManager.queryStatus(id: Long): DownloadStatus {
        query(DownloadManager.Query().setFilterById(id)).use {
            with(it) {
                if (this != null && moveToFirst()) {
                    return when (getInt(getColumnIndex(DownloadManager.COLUMN_STATUS))) {
                        DownloadManager.STATUS_SUCCESSFUL -> DownloadStatus.SUCCESSFUL
                        DownloadManager.STATUS_FAILED -> DownloadStatus.FAILED
                        else -> DownloadStatus.UNKNOWN
                    }
                }
                return DownloadStatus.UNKNOWN
            }
        }
    }

    private fun getDownloadNotificator(): DownloadNotificator = when (downloadNotificator) {
        null -> DownloadNotificator(this, lifecycle).also { downloadNotificator = it }
        else -> downloadNotificator!!
    }

}
