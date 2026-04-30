package com.illusionware.npsbrowser.util.system

import android.content.Context
import android.os.Build
import java.util.Locale.ENGLISH

// Borrowed from https://github.com/tachiyomiorg/tachiyomi/tree/master
fun Context.isNavigationBarNeedsScrim(): Boolean {
    return Build.VERSION.SDK_INT < Build.VERSION_CODES.Q ||
            InternalResourceHelper.getBoolean(this, "config_navBarNeedsScrim", true)
}

fun Long.prettyByte(): String {
    val kilobyte: Long = 1024
    val megabyte = kilobyte * 1024
    val gigabyte = megabyte * 1024
    val terabyte = gigabyte * 1024

    return if (this in 0 until kilobyte) {
        "$this" + "B"
    } else if (this in kilobyte until megabyte) {
        String.format(ENGLISH, "%.2f", (this.toDouble() / kilobyte.toDouble())) + "KB"
    } else if (this in megabyte until gigabyte) {
        String.format(ENGLISH, "%.2f", (this.toDouble() / megabyte.toDouble())) + "MB"
    } else if (this in gigabyte until terabyte) {
        String.format(ENGLISH, "%.2f", (this.toDouble() / gigabyte.toDouble())) + "GB"
    } else if (this >= terabyte) {
        String.format(ENGLISH, "%.2f", (this.toDouble() / terabyte.toDouble())) + "TB"
    } else {
        "$this" + "B"
    }
}