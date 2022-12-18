package com.udacity.ui

import android.content.Intent
import android.os.Bundle
import androidx.annotation.ColorRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import com.udacity.BuildConfig
import com.udacity.R
import com.udacity.databinding.ActivityDetailBinding
import com.udacity.databinding.ContentDetailBinding
import com.udacity.utils.DownloadStatus

class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding

    private val fileName by lazy {
        intent?.extras?.getString(FILE_NAME_KEY, unknownText) ?: unknownText
    }
    private val downloadStatus by lazy {
        intent?.extras?.getString(EXTRA_DOWNLOAD_STATUS, unknownText) ?: unknownText
    }

    private val unknownText by lazy { getString(R.string.unknown) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_detail)
        binding.apply {
            setSupportActionBar(toolbar)
            detailContent.initializeView()
        }
        binding.detailContent.btnOk.setOnClickListener {
            finish()
            startActivity(Intent(this, MainActivity::class.java))
        }
    }

    private fun ContentDetailBinding.initializeView() {
        fileNameText.text = fileName
        tvDownloadStatus.text = downloadStatus
        changeStatus()
    }

    private fun ContentDetailBinding.changeStatus() {
        when (tvDownloadStatus.text) {
            DownloadStatus.SUCCESSFUL.statusText -> {
                changeColor(R.color.colorPrimaryDark)
            }
            DownloadStatus.FAILED.statusText -> {
                changeColor(R.color.red)
            }
        }
    }

    private fun ContentDetailBinding.changeColor(@ColorRes colorRes: Int) {
        ContextCompat.getColor(this@DetailActivity, colorRes)
            .also { color ->
                tvDownloadStatus.setTextColor(color)
            }
    }

    companion object {
        private const val FILE_NAME_KEY = "${BuildConfig.APPLICATION_ID}.FILE_NAME"
        private const val EXTRA_DOWNLOAD_STATUS = "${BuildConfig.APPLICATION_ID}.DOWNLOAD_STATUS"
        fun bundleExtrasOf(
            fileName: String,
            downloadStatus: DownloadStatus
        ) = bundleOf(
            FILE_NAME_KEY to fileName,
            EXTRA_DOWNLOAD_STATUS to downloadStatus.statusText
        )
    }


}
