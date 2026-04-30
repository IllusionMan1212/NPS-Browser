package com.illusionware.npsbrowser.data.notification

import android.content.Context
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationChannelGroupCompat
import androidx.core.app.NotificationManagerCompat

object Notifications {
    private const val GROUP_DOWNLOADER = "group_downloader"
    const val CHANNEL_DOWNLOADER = "channel_downloader"
    const val ID_DOWNLOADER_GROUP = 1
    const val ID_DOWNLOADER_ERROR = 2

    fun initialize(context: Context) {
        val notificationManager = NotificationManagerCompat.from(context)

        notificationManager.createNotificationChannelGroup(
            NotificationChannelGroupCompat.Builder(GROUP_DOWNLOADER)
                .setName("Download")
                .build()
        )

        notificationManager.createNotificationChannel(
            NotificationChannelCompat.Builder(CHANNEL_DOWNLOADER, NotificationManagerCompat.IMPORTANCE_LOW)
                .setName("Progress")
                .setDescription("Download progress and downloader status")
                .setShowBadge(false)
                .setGroup(GROUP_DOWNLOADER)
                .build()
        )
    }
}