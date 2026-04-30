package com.illusionware.npsbrowser.data.download

import android.content.Context
import androidx.core.app.NotificationCompat
import com.illusionware.npsbrowser.R
import com.illusionware.npsbrowser.data.notification.Notifications
import com.illusionware.npsbrowser.util.system.cancelNotification
import com.illusionware.npsbrowser.util.system.notificationBuilder
import com.illusionware.npsbrowser.util.system.notify

/**
 * DownloadNotifier is used to show notifications when downloading one or multiple chapters.
 *
 * @param context context of application
 */
internal class DownloadNotifier(private val context: Context) {

    private val progressNotificationBuilder by lazy {
        context.notificationBuilder(Notifications.CHANNEL_DOWNLOADER) {
            setSmallIcon(R.drawable.baseline_cloud_download_24)
            setCategory(NotificationCompat.CATEGORY_PROGRESS)
            setAutoCancel(false)
            setOnlyAlertOnce(true)
            setSilent(true)
            setOngoing(true)
            setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
        }
    }

    private val errorNotificationBuilder by lazy {
        context.notificationBuilder(Notifications.CHANNEL_DOWNLOADER) {
            setSmallIcon(R.drawable.baseline_cloud_download_24)
            setCategory(NotificationCompat.CATEGORY_STATUS)
            setAutoCancel(true)
            setSilent(true)
        }
    }

    /**
     * Status of download. Used for correct notification icon.
     */
    private var isDownloading = false

    /**
     * Shows a notification from this builder.
     *
     * @param id the id of the notification.
     */
    private fun NotificationCompat.Builder.show(id: Int) {
        context.notify(id, build())
    }

    /**
     * Dismiss the downloader's notification. Downloader error notifications use a different id, so
     * those can only be dismissed by the user.
     */
    private fun dismissProgress() {
        context.cancelNotification(Notifications.ID_DOWNLOADER_GROUP)
    }

    private fun dismissError() {
        context.cancelNotification(Notifications.ID_DOWNLOADER_ERROR)
    }

    /**
     * Called when download progress changes.
     *
     * @param download download object containing download information.
     */
    fun onProgressChange(size: Long, progress: Long) {
        with(progressNotificationBuilder) {
            if (!isDownloading) {
                clearActions()
                isDownloading = true
            }

            setContentTitle("Downloading packages")
            setContentText("${progress / 1024 / 1024}MB / ${size / 1024 / 1024}MB")
            setProgress((size / 1024).toInt(), (progress / 1024).toInt(), false)
            dismissError()

            show(Notifications.ID_DOWNLOADER_GROUP)
        }
    }

    fun onPaused(reason: String? = null) {
        with(progressNotificationBuilder) {
            setContentTitle("Downloads paused")
            setContentText(reason ?: "Waiting to resume downloads")
            setProgress(0, 0, false)
            setOngoing(false)

            show(Notifications.ID_DOWNLOADER_GROUP)
        }

        isDownloading = false
    }

    /**
     * Resets the state once downloads are completed.
     */
    fun onComplete() {
        dismissProgress()
        dismissError()

        // Reset states to default
        isDownloading = false
    }

    fun onError(error: String) {
        with(errorNotificationBuilder) {
            setContentTitle("Download issue")
            setStyle(NotificationCompat.BigTextStyle().bigText(error))
            setContentText(error)
            setProgress(0, 0, false)

            show(Notifications.ID_DOWNLOADER_ERROR)
        }

        isDownloading = false
    }
}