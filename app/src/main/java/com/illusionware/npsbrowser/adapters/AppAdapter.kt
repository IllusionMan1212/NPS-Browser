package com.illusionware.npsbrowser.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.illusionware.npsbrowser.AppData
import com.illusionware.npsbrowser.R

class AppAdapter(private val appsList: ArrayList<AppData>) : RecyclerView.Adapter<AppAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var appTitle: TextView = view.findViewById(R.id.appItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder( LayoutInflater.from(parent.context).inflate(R.layout.list_item, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.appTitle.text =  appsList[position].Title
    }

    override fun getItemCount(): Int {
        return appsList.size
    }

    fun updateData(apps: ArrayList<AppData>) {
        appsList.clear()
        notifyDataSetChanged()
        appsList.addAll(apps)
        notifyDataSetChanged()

    }
}