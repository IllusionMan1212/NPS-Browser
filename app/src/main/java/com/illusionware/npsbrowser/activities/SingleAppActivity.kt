package com.illusionware.npsbrowser.activities

import android.app.DownloadManager
import android.content.ClipData
import android.content.ClipboardManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.Toast
import com.illusionware.npsbrowser.AppData
import com.illusionware.npsbrowser.databinding.ActivitySingleAppBinding
import java.lang.Exception

class SingleAppActivity: BaseActivity() {
    companion object {
        lateinit var app : AppData
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivitySingleAppBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager

        binding.singleAppTitle.text = "[${app.titleID}] ${app.title}"
        if (app.fileSize != null) {
            binding.singleAppSize.text = prettifySize()
        } else {
            binding.singleAppSizeTitle.visibility = View.INVISIBLE
        }
        binding.singleAppContentId.text = app.contentID
        binding.singleAppFwVer.text = app.minFW
        // TODO: get and set app icon
        if (app.minFW.isNullOrBlank()) {
            binding.singleAppFwVerTitle.visibility = View.INVISIBLE
        }
        if (app.license == "MISSING") {
            binding.singleAppError.text = "Warning: No license was provided for this app."
        }
        if (app.link == "CART ONLY") {
            binding.singleAppError.text = "CART ONLY GAME"
            binding.singleAppDownloadButton.isEnabled = false
        } else if (app.link == "MISSING") {
            binding.singleAppError.text = "Warning: No download link was provided for this app."
            binding.singleAppDownloadButton.isEnabled = false
        }

        binding.singleAppContentId.setOnLongClickListener {
            val clipboardManager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = ClipData.newPlainText("Content ID", binding.singleAppContentId.text)
            clipboardManager.setPrimaryClip(clipData)
            Toast.makeText(this, "Content ID Copied to Clipboard!", Toast.LENGTH_SHORT).show()
            true
        }

        binding.singleAppDownloadButton.setOnClickListener {
            try {
                var request = DownloadManager.Request(Uri.parse(app.link))
                request.setTitle(app.title)
                request.setDescription("Downloading...")
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, app.title + ".pkg") // TODO: set up preference for this
                downloadManager.enqueue(request)
            } catch (e: Exception) {
                Toast.makeText(this, "An error occurred while downloading. please try again", Toast.LENGTH_SHORT).show()
            }
        }

        binding.singleAppDownloadButton.setOnLongClickListener {
            val clipboardManager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = ClipData.newPlainText("PKG URL", app.link)
            clipboardManager.setPrimaryClip(clipData)
            Toast.makeText(this, "PKG URL Copied to Clipboard", Toast.LENGTH_SHORT).show()
            true
        }
    }

    private fun prettifySize(): String {
        if (app.fileSize!!.toFloat() / (1024 * 1024 * 1024) > 1) {
            return "%.1f GB".format(toGB(app.fileSize!!))
        }
        if (app.fileSize!!.toFloat() / (1024 * 1024) > 1) {
            return "%.1f MB".format(toMB(app.fileSize!!))
        }
        if (app.fileSize!!.toFloat() / 1024 > 1) {
            return "%.1f KB".format(toKB(app.fileSize!!))
        }
        return "${app.fileSize!!} Bytes";
    }

    private fun toKB(size: Number): Number {
        return size.toFloat() / 1024
    }

    private fun toMB(size: Number): Number {
        return size.toFloat() / (1024 * 1024)
    }

    private fun toGB(size: Number): Number {
        return size.toFloat() / (1024 * 1024 * 1024)
    }
}