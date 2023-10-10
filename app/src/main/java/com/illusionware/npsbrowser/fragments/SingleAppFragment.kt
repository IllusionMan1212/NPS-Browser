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
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.illusionware.npsbrowser.AppData
import com.illusionware.npsbrowser.R

class SingleAppFragment : Fragment() {
    private val baseStoreURL = "https://store.playstation.com/store/api/chihiro/00_09_000/container"

    companion object {
        lateinit var app : AppData
    }

    // TODO: either use binding or make vars for all the views

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
        if (app.version != null) {
            view?.findViewById<TextView>(R.id.singleAppVer)?.text = app.version
        } else {
            view?.findViewById<TextView>(R.id.singleAppVerTitle)?.visibility = View.INVISIBLE
        }
        view?.findViewById<TextView>(R.id.singleAppFwVer)?.text = app.minFW
        // TODO: get and set app icon
        if (app.minFW.isNullOrBlank()) {
            view?.findViewById<TextView>(R.id.singleAppFwVerTitle)?.visibility = View.INVISIBLE
        }
        if (app.license == "MISSING") {
            view?.findViewById<TextView>(R.id.singleAppError)?.text = getString(R.string.no_license)
            view?.findViewById<Button>(R.id.singleAppCopyZRIFButton)?.isEnabled = false
        }
        if (app.link == "CART ONLY") {
            view?.findViewById<TextView>(R.id.singleAppError)?.text = getString(R.string.cart_only)
            view?.findViewById<Button>(R.id.singleAppDownloadButton)?.isEnabled = false
            view?.findViewById<Button>(R.id.singleAppCopyPKGButton)?.isEnabled = false
            view?.findViewById<Button>(R.id.singleAppCopyZRIFButton)?.isEnabled = false
        } else if (app.link == "MISSING") {
            view?.findViewById<TextView>(R.id.singleAppError)?.text = getString(R.string.no_download_link)
            view?.findViewById<Button>(R.id.singleAppDownloadButton)?.isEnabled = false
            view?.findViewById<Button>(R.id.singleAppCopyPKGButton)?.isEnabled = false
            view?.findViewById<Button>(R.id.singleAppCopyZRIFButton)?.isEnabled = false
        }

        view?.findViewById<TextView>(R.id.singleAppContentId)?.setOnLongClickListener {
            val clipboardManager = requireActivity().getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = ClipData.newPlainText(
                "Content ID",
                view.findViewById<TextView>(R.id.singleAppContentId)?.text
            )
            clipboardManager.setPrimaryClip(clipData)
            Toast.makeText(requireContext(), getString(R.string.content_id_copied), Toast.LENGTH_SHORT).show()
            true
        }

        // click listener for the download button
        view?.findViewById<Button>(R.id.singleAppDownloadButton)?.setOnClickListener {
            try {
                val request = DownloadManager.Request(Uri.parse(app.link))
                request.setTitle(app.title)
                request.setDescription(getString(R.string.downloading))
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                request.setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_DOWNLOADS,
                    app.title!!.replace("[\\\\/:*?\"<>|]".toRegex(), " ") + ".pkg"
                ) // TODO: set up preference for this
                downloadManager.enqueue(request)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), getString(R.string.download_error), Toast.LENGTH_SHORT).show()
            }
        }

        // click listener for the copy pkg button
        view?.findViewById<Button>(R.id.singleAppCopyPKGButton)?.setOnClickListener {
            val clipboardManager = requireActivity().getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = ClipData.newPlainText("PKG URL", app.link)
            clipboardManager.setPrimaryClip(clipData)
            Toast.makeText(requireContext(),getString(R.string.pkg_url_copied),Toast.LENGTH_SHORT).show()
        }

        // click listener for the copy zRIF button
        view?.findViewById<Button>(R.id.singleAppCopyZRIFButton)?.setOnClickListener {
            val clipboardManager = requireActivity().getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = ClipData.newPlainText("zRIF", app.license)
            clipboardManager.setPrimaryClip(clipData)
            Toast.makeText(requireContext(),getString(R.string.zrif_copied),Toast.LENGTH_SHORT).show()
        }

        // set the height for the error text to 0 if there's no error so that the scrollview can take more space
        if (view?.findViewById<TextView>(R.id.singleAppError)?.text.isNullOrEmpty()) {
            view?.findViewById<TextView>(R.id.singleAppError)?.layoutParams?.height = 0
        }

        if (app.contentID != null) {
            val circularProgressDrawable = CircularProgressDrawable(requireContext())
            circularProgressDrawable.setColorSchemeColors(0x43EB92)
            circularProgressDrawable.strokeWidth = 10f
            circularProgressDrawable.centerRadius = 180f
            circularProgressDrawable.start()

            val image : ImageView? = view?.findViewById(R.id.singleAppIcon)
            val iconURL = getImage(app.contentID!!)
            if (iconURL != null) {
                // TODO: make the options a requestOptions
                Glide.with(this).load(iconURL).placeholder(circularProgressDrawable).error(R.drawable.ic_games_placeholder_24dp).into(image!!)
            }
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
        return "${app.fileSize!!} Bytes"
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

    private fun getImage(contentID: String): String? {
        when (contentID.substring(0, 2)) {
            "EP" -> {
                // european games
                return "$baseStoreURL/SA/en/999/${contentID}/image"
            }
            "UP" -> {
                // united states games
                return "$baseStoreURL/US/en/999/${contentID}/image"
            }
            "JP" -> {
                // japanese games
                return "$baseStoreURL/jp/ja/999/${contentID}/image"
            }
            "KP" -> {
                // korean games
                return "$baseStoreURL/kr/ko/999/${contentID}/image"
            }
            "HP" -> {
                // asia games
                return "$baseStoreURL/HK/zh/999/${contentID}/image"
            }
        }
        return null
    }
}