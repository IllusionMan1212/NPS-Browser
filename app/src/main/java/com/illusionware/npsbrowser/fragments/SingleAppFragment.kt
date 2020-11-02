package com.illusionware.npsbrowser.fragments

import android.app.DownloadManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context.CLIPBOARD_SERVICE
import android.content.Context.DOWNLOAD_SERVICE
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.illusionware.npsbrowser.AppData
import com.illusionware.npsbrowser.R
import java.lang.Exception

class SingleAppFragment : Fragment() {
    companion object {
        lateinit var app : AppData
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        activity?.title = app.title
        val view = inflater.inflate(R.layout.single_app_fragment, container, false)
        val downloadManager = requireActivity().getSystemService(DOWNLOAD_SERVICE) as DownloadManager

        view?.findViewById<TextView>(R.id.singleAppTitle)?.text = "[${app.titleID}] ${app.title}"
        if (app.fileSize != null) {
             view?.findViewById<TextView>(R.id.singleAppSize)?.text = prettifySize()
        } else {
            view?.findViewById<TextView>(R.id.singleAppSizeTitle)?.visibility = View.INVISIBLE
        }
        view?.findViewById<TextView>(R.id.singleAppContentId)?.text = app.contentID
        view?.findViewById<TextView>(R.id.singleAppFwVer)?.text = app.minFW
        // TODO: get and set app icon
        if (app.minFW.isNullOrBlank()) {
            view?.findViewById<TextView>(R.id.singleAppFwVerTitle)?.visibility = View.INVISIBLE
        }
        if (app.license == "MISSING") {
            view?.findViewById<TextView>(R.id.singleAppError)?.text = "Warning: No license was provided for this app."
        }
        if (app.link == "CART ONLY") {
            view?.findViewById<TextView>(R.id.singleAppError)?.text = "CART ONLY GAME"
            view?.findViewById<Button>(R.id.singleAppDownloadButton)?.isEnabled = false
        } else if (app.link == "MISSING") {
            view?.findViewById<TextView>(R.id.singleAppError)?.text = "Warning: No download link was provided for this app."
            view?.findViewById<Button>(R.id.singleAppDownloadButton)?.isEnabled = false
        }

        view?.findViewById<TextView>(R.id.singleAppContentId)?.setOnLongClickListener {
            val clipboardManager = requireActivity().getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = ClipData.newPlainText("Content ID", view?.findViewById<TextView>(R.id.singleAppContentId)?.text)
            clipboardManager.setPrimaryClip(clipData)
            Toast.makeText(requireContext(), "Content ID Copied to Clipboard!", Toast.LENGTH_SHORT).show()
            true
        }

        view?.findViewById<Button>(R.id.singleAppDownloadButton)?.setOnClickListener {
            try {
                var request = DownloadManager.Request(Uri.parse(app.link))
                request.setTitle(app.title)
                request.setDescription("Downloading...")
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, app.title + ".pkg") // TODO: set up preference for this
                downloadManager.enqueue(request)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "An error occurred while downloading. please try again", Toast.LENGTH_SHORT).show()
            }
        }

        view?.findViewById<Button>(R.id.singleAppDownloadButton)?.setOnLongClickListener {
            val clipboardManager = requireActivity().getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = ClipData.newPlainText("PKG URL", app.link)
            clipboardManager.setPrimaryClip(clipData)
            Toast.makeText(requireContext(), "PKG URL Copied to Clipboard", Toast.LENGTH_SHORT).show()
            true
        }
        return view
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