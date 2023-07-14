package com.illusionware.npsbrowser.util

import android.content.Context
import android.os.Build

// Borrowed from https://github.com/tachiyomiorg/tachiyomi/tree/master
fun Context.isNavigationBarNeedsScrim(): Boolean {
    return Build.VERSION.SDK_INT < Build.VERSION_CODES.Q ||
            InternalResourceHelper.getBoolean(this, "config_navBarNeedsScrim", true)
}
