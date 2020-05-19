package com.illusionware.npsbrowser.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.illusionware.npsbrowser.AppData
import com.illusionware.npsbrowser.R
import kotlin.collections.ArrayList

class AppAdapter(private val appsList: ArrayList<AppData>, val context : Context) : RecyclerView.Adapter<AppAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var appTitle: TextView = view.findViewById(R.id.appTitle)
        var appRegion: TextView = view.findViewById(R.id.appRegion)
        var appMinFW: TextView = view.findViewById(R.id.appMinFW)
        var appLastDate: TextView = view.findViewById(R.id.appLastDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder( LayoutInflater.from(parent.context).inflate(R.layout.list_item, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.appTitle.text =  appsList[position].title
        holder.appRegion.text =  appsList[position].region
        holder.appMinFW.text =  appsList[position].minFW
        holder.appMinFW.background = null;

        if (holder.appMinFW.text.isNotBlank()) {
            val drawable = ResourcesCompat.getDrawable(context.resources, R.drawable.rect_red, null)
            holder.appMinFW.background = drawable;
        }
        holder.appLastDate.text =  appsList[position].lastDateTime
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