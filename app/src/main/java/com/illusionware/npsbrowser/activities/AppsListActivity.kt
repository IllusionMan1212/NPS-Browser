package com.illusionware.npsbrowser.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.illusionware.npsbrowser.AppData
import com.illusionware.npsbrowser.RecyclerTouchListener
import com.illusionware.npsbrowser.adapters.AppAdapter
import com.illusionware.npsbrowser.databinding.ActivityAppsListBinding

class AppsListActivity : AppCompatActivity() {
    companion object {
        lateinit var apps : ArrayList<AppData>
    }
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityAppsListBinding.inflate(layoutInflater);
        setContentView(binding.root)

        viewAdapter = AppAdapter(apps)

        recyclerView = binding.appsRecycler
        recyclerView.apply {
            layoutManager = LinearLayoutManager(applicationContext)
            adapter = viewAdapter
        }
    }
}