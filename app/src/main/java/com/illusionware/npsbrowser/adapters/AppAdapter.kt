package com.illusionware.npsbrowser.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import com.github.wrdlbrnft.modularadapter.ModularAdapter
import com.github.wrdlbrnft.sortedlistadapter.SortedListAdapter
import com.illusionware.npsbrowser.AppData
import com.illusionware.npsbrowser.R
import com.illusionware.npsbrowser.activities.MainActivity
import com.illusionware.npsbrowser.fragments.SingleAppFragment
import com.illusionware.npsbrowser.fragments.mainactivity.MainActivityFragment
import java.util.Comparator

private val comparator: Comparator<AppData> = SortedListAdapter.ComparatorBuilder<AppData>()
    .setOrderForModel(AppData::class.java) { a, b -> a.title?.compareTo(b.title!!)!! }
    .build()

class AppAdapter(val context: Context) : SortedListAdapter<AppData>(context, AppData::class.java, comparator) {
    private val LIST_ITEM = 0
    private val GRID_ITEM = 1
    private var isSwitchView = true

    inner class AppViewHolder(itemView: View) :
        SortedListAdapter.ViewHolder<AppData?>(itemView) {
        var appTitle: TextView = itemView.findViewById(R.id.appTitle)
        var appRegion: TextView = itemView.findViewById(R.id.appRegion)
        var appMinFW: TextView = itemView.findViewById(R.id.appMinFW)
        var appLastDate: TextView = itemView.findViewById(R.id.appLastDate)

        override fun performBind(item: AppData) {
            appTitle.text = item.title
            appTitle.isSelected = true
            appRegion.text =  item.region
            appMinFW.text =  item.minFW
            appMinFW.background = null;

            if (appMinFW.text.isNotBlank()) {
                val drawable = ResourcesCompat.getDrawable(
                    context.resources,
                    R.drawable.rect_red,
                    null
                )
                appMinFW.background = drawable;
            }
            appLastDate.text =  item.lastDateTime
        }
    }

    override fun onCreateViewHolder(inflater: LayoutInflater, parent: ViewGroup, viewType: Int): AppViewHolder {
        return if (viewType == LIST_ITEM) {
            AppViewHolder(
                inflater.inflate(
                    R.layout.recycler_item_list,
                    parent,
                    false
                )
            )
        } else {
            AppViewHolder(
                inflater.inflate(
                    R.layout.recycler_item_grid,
                    parent,
                    false
                )
            )
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (isSwitchView) {
            LIST_ITEM
        } else {
            GRID_ITEM
        }
    }

    fun toggleItemViewType(type: Int): Boolean {
        isSwitchView = type == 0
        return isSwitchView
    }
}
