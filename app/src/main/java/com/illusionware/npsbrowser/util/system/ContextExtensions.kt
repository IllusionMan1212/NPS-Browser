package com.illusionware.npsbrowser.util.system

import android.app.ActivityManager
import android.content.Context
import android.os.PowerManager
import android.widget.Toast
import androidx.core.content.getSystemService

fun Context.isServiceRunning(serviceClass: Class<*>): Boolean {
    val className = serviceClass.name
    val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    return manager.getRunningServices(Integer.MAX_VALUE).any { className == it.service.className }
}

val Context.powerManager: PowerManager
    get() = getSystemService()!!

fun Context.acquireWakeLock(tag: String): PowerManager.WakeLock {
    val wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "$tag:WakeLock")
    wakeLock.acquire()
    return wakeLock
}

fun Context.toast(text: String?, duration: Int = Toast.LENGTH_SHORT, block: (Toast) -> Unit = {}): Toast {
    return Toast.makeText(applicationContext, text.orEmpty(), duration).also {
        block(it)
        it.show()
    }
}
