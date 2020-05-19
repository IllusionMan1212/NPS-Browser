package com.illusionware.npsbrowser.activities

import android.os.Bundle
import com.illusionware.npsbrowser.AppData
import com.illusionware.npsbrowser.databinding.ActivitySingleAppBinding

class SingleAppActivity: BaseActivity() {
    companion object {
        lateinit var app : AppData
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivitySingleAppBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.titleID.text = app.titleID
        binding.gameTitle.text = app.title
    }
}