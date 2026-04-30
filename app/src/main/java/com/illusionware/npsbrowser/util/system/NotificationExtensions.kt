package com.illusionware.npsbrowser.util.system

import android.Manifest
import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.compose.ui.graphics.toArgb
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationChannelGroupCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.NotificationManagerCompat.NotificationWithIdAndTag
import androidx.core.content.PermissionChecker
import androidx.core.content.getSystemService
import com.illusionware.npsbrowser.ui.theme.ColorAccent

val Context.notificationManager: NotificationManager
    get() = getSystemService()!!

fun Context.notify(id: Int, channelId: String, block: (NotificationCompat.Builder.() -> Unit)? = null) {
    val notification = notificationBuilder(channelId, block).build()
    this.notify(id, notification)
}

fun Context.notify(id: Int, notification: Notification) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && PermissionChecker.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PermissionChecker.PERMISSION_GRANTED) {
        return
    }

    NotificationManagerCompat.from(this).notify(id, notification)
}

fun Context.notify(notificationWithIdAndTags: List<NotificationWithIdAndTag>) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && PermissionChecker.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PermissionChecker.PERMISSION_GRANTED) {
        return
    }

    NotificationManagerCompat.from(this).notify(notificationWithIdAndTags)
}

fun Context.cancelNotification(id: Int) {
    NotificationManagerCompat.from(this).cancel(id)
}

/**
 * Helper method to create a notification builder.
 *
 * @param channelId the channel id.
 * @param block the function that will execute inside the builder.
 * @return a notification to be displayed or updated.
 */
fun Context.notificationBuilder(channelId: String, block: (NotificationCompat.Builder.() -> Unit)? = null): NotificationCompat.Builder {
    val builder = NotificationCompat.Builder(this, channelId)
        .setColor(ColorAccent.toArgb())
    if (block != null) {
        builder.block()
    }
    return builder
}