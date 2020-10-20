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
    private final val LIST_ITEM = 0
    private final val GRID_ITEM = 1
    var isSwitchView = true

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var appTitle: TextView = view.findViewById(R.id.appTitle)
        var appRegion: TextView = view.findViewById(R.id.appRegion)
        var appMinFW: TextView = view.findViewById(R.id.appMinFW)
        var appLastDate: TextView = view.findViewById(R.id.appLastDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return if (viewType == LIST_ITEM) {
            ViewHolder( LayoutInflater.from(parent.context).inflate(R.layout.list_item, parent, false))
        } else {
            ViewHolder( LayoutInflater.from(parent.context).inflate(R.layout.grid_item, parent, false))
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (isSwitchView) {
            LIST_ITEM
        } else {
            GRID_ITEM
        }
    }

    public fun toggleItemViewType(type: Int): Boolean {
        isSwitchView = type == 0
        return isSwitchView
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.appTitle.text =  appsList[position].title
        holder.appTitle.isSelected = true
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