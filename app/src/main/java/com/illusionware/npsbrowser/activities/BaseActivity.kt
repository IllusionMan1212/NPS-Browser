package com.illusionware.npsbrowser.activities

import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import com.illusionware.npsbrowser.R

open class BaseActivity : AppCompatActivity() {

    private lateinit var sharedPreferences : SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        PreferenceManager.setDefaultValues(this, R.xml.root_preferences, false)
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val decor: View = window.decorView

        AppCompatDelegate.setDefaultNightMode(when ((sharedPreferences.getString("app_theme", "2")?.toInt())) {
            0 ->  {
                decor.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                AppCompatDelegate.MODE_NIGHT_NO
            }
            1 -> {
                decor.systemUiVisibility = 0;
                AppCompatDelegate.MODE_NIGHT_YES
            }
            2 -> {
                decor.systemUiVisibility = if (resources.configuration.uiMode and
                    Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES) 0 else View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }
            else -> AppCompatDelegate.MODE_NIGHT_UNSPECIFIED
        })
    }
}