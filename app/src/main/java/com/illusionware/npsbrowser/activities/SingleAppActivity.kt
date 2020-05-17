package com.illusionware.npsbrowser.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.illusionware.npsbrowser.AppData
import com.illusionware.npsbrowser.databinding.ActivitySingleAppBinding

class SingleAppActivity: AppCompatActivity() {
    companion object {
        lateinit var app : AppData
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivitySingleAppBinding.inflate(layoutInflater);
        setContentView(binding.root)

        binding.titleID.text = app.TitleID
        binding.gameTitle.text = app.Title;
    }
}