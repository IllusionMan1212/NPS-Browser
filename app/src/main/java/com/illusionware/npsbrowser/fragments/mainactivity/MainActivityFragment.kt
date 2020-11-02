package com.illusionware.npsbrowser.fragments.mainactivity

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.illusionware.npsbrowser.AppData
import com.illusionware.npsbrowser.R
import com.illusionware.npsbrowser.RecyclerTouchListener
import com.illusionware.npsbrowser.fragments.SingleAppFragment
import com.illusionware.npsbrowser.adapters.AppAdapter
import com.illusionware.npsbrowser.fragments.SettingsFragment
import java.io.InputStream

class MainActivityFragment : Fragment() {

    var recyclerView : RecyclerView? = null
    var viewAdapter: AppAdapter? = null
    var filteredModelList: ArrayList<AppData>? = null

    companion object {
        var apps : ArrayList<AppData>? = arrayListOf()
    }

    private val tsvReader = csvReader {
        charset = "UTF-8"
        quoteChar = '\\'
        escapeChar = '\\'
        delimiter = '\t'
        skipEmptyLine = true
        skipMissMatchedRow = true
    }

    private lateinit var viewModel: MainActivityViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setHasOptionsMenu(true)
        activity?.title = resources.getString(R.string.app_name)

        val view = inflater.inflate(R.layout.main_fragment, container, false)

        view?.findViewById<FloatingActionButton>(R.id.tsvChooserButton)?.setOnClickListener {
            if (!isStoragePermissionGranted())
                return@setOnClickListener
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "text/tab-separated-values"
            }
            startActivityForResult(intent, 1)
        }
        viewModel = ViewModelProviders.of(this).get(MainActivityViewModel::class.java)
        // TODO: Use the ViewModel

        recyclerView = view?.findViewById(R.id.appsRecycler)

        if (viewAdapter == null) {
            viewAdapter = AppAdapter(requireContext())
            viewAdapter?.edit()?.add(apps!!)?.commit()
        }

        if (apps != null) {
            filteredModelList = apps
        }
        viewAdapter?.notifyDataSetChanged()

        recyclerView?.adapter = viewAdapter

        val orientation = resources.configuration.orientation
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val isViewAsList = prefs.getString("layout_type", "0")
        if (isViewAsList == "0") {
            viewAdapter?.toggleItemViewType(0)
            recyclerView?.layoutManager = LinearLayoutManager(context)
            viewAdapter?.notifyDataSetChanged()
        } else {
            viewAdapter?.toggleItemViewType(1)
            recyclerView?.layoutManager = GridLayoutManager(
                context,
                if (orientation == Configuration.ORIENTATION_LANDSCAPE) 4 else 2
            )
            viewAdapter?.notifyDataSetChanged()
        }

        recyclerView?.addOnItemTouchListener(RecyclerTouchListener(
            requireContext(),
            recyclerView!!,
            object : RecyclerTouchListener.ClickListener {
                override fun onClick(view: View?, position: Int) {
                    SingleAppFragment.app = filteredModelList!![position]
                    activity?.supportFragmentManager
                        ?.beginTransaction()
                        ?.replace(R.id.container,
                            SingleAppFragment()
                        )
                        ?.addToBackStack("single app")
                        ?.commit()
                }

                override fun onLongClick(
                    view: View?,
                    recyclerView: RecyclerView?,
                    position: Int
                ) {
                    Toast.makeText(context, "TODO: Long Click $position", Toast.LENGTH_SHORT).show()
                }
            }
        ))
        return view
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            resultData?.data?.also { uri ->
                val inputStream: InputStream? = activity?.contentResolver?.openInputStream(uri)
                val entries : List<Map<String, String?>> = tsvReader.readAllWithHeader(inputStream!!)
                apps?.clear()
                apps = ArrayList(entries.size)

                entries.forEach { entry ->
                    if (entry["Name"].isNullOrEmpty()) {
                        return@forEach
                    }
                    val id = entry["Title ID"]
                    val region = entry["Region"]
                    val name = entry["Name"]
                    val link = entry["PKG direct link"]
                    val license = entry["zRIF"]
                    val contentID = entry["Content ID"]
                    var lastDateTime = entry["Last Modification Date"]
                    if (!lastDateTime.isNullOrEmpty()) {
                        lastDateTime = lastDateTime.substring(0, 10)
                    }
                    val fileSize = entry["File Size"]?.toLongOrNull()
                    val sha256 = entry["SHA256"]
                    val minFW = entry["Required FW"]
                    val app = AppData(
                        id, region, name, link ?: "MISSING", license ?: "MISSING",
                        contentID ?: "MISSING", lastDateTime ?: "MISSING",
                        fileSize, sha256 ?: "MISSING", minFW
                    )
                    apps?.add(app)
                }
                apps?.sortBy { it.title }
                filteredModelList = apps

                recyclerView = view?.findViewById(R.id.appsRecycler)
                viewAdapter = AppAdapter(requireContext())
                viewAdapter?.edit()?.add(apps!!)?.commit()
                val orientation = this.resources.configuration.orientation
                val prefs = PreferenceManager.getDefaultSharedPreferences(context)
                val isViewAsList = prefs.getString("layout_type", "0")

                if (isViewAsList == "0") {
                    viewAdapter?.toggleItemViewType(0)
                    recyclerView?.layoutManager = LinearLayoutManager(context)
                    viewAdapter?.notifyDataSetChanged()
                } else {
                    viewAdapter?.toggleItemViewType(1)
                    recyclerView?.layoutManager = GridLayoutManager(
                        context,
                        if (orientation == Configuration.ORIENTATION_LANDSCAPE) 4 else 2
                    )
                    viewAdapter?.notifyDataSetChanged()
                }

                recyclerView?.adapter = viewAdapter
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.main, menu)

        val searchItem = menu.findItem(R.id.action_search)
        val searchView: SearchView = searchItem.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(query: String): Boolean {
                filteredModelList = filter(apps!!, query) as ArrayList<AppData>?
                viewAdapter?.edit()?.replaceAll(filteredModelList!!)?.commit()
                recyclerView?.scrollToPosition(0)
                return false
            }
        })
        super.onCreateOptionsMenu(menu, menuInflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                activity?.supportFragmentManager
                    ?.beginTransaction()
                    ?.replace(
                        R.id.container,
                        SettingsFragment()
                    )
                    ?.addToBackStack("settings")
                    ?.commit()
                true
            }
            R.id.action_search -> {
                true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onPause() {
        super.onPause()
        viewAdapter?.edit()?.replaceAll(apps!!)?.commit()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        val orientation = newConfig.orientation
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val isViewAsList = prefs.getString("layout_type", "0")
        if (isViewAsList == "0") {
            val pos = (recyclerView?.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
            viewAdapter?.toggleItemViewType(0)
            recyclerView?.layoutManager = LinearLayoutManager(context)
            (recyclerView?.layoutManager as LinearLayoutManager).scrollToPosition(pos)
            viewAdapter?.notifyDataSetChanged()
        } else {
            val pos = (recyclerView?.layoutManager as GridLayoutManager).findFirstVisibleItemPosition()
            viewAdapter?.toggleItemViewType(1)
            recyclerView?.layoutManager = GridLayoutManager(
                context,
                if (orientation == Configuration.ORIENTATION_LANDSCAPE) 4 else 2
            )
            (recyclerView?.layoutManager as GridLayoutManager).scrollToPosition(pos)
            viewAdapter?.notifyDataSetChanged()
        }
    }

    private fun isStoragePermissionGranted(): Boolean {
        return  if (checkSelfPermission(
                this.requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED) {
            true
        } else {
            requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1)
            false
        }
    }

    private fun filter(models: List<AppData>, query: String): List<AppData>? {
        val lowerCaseQuery = query.toLowerCase()
        val filteredModelList: MutableList<AppData> = ArrayList()
        for (model in models) {
            val title: String = model.title?.toLowerCase()!!
            val id: String = model.titleID?.toLowerCase()!!
            if (title.contains(lowerCaseQuery) || id.contains(lowerCaseQuery)) {
                filteredModelList.add(model)
            }
        }
        return filteredModelList
    }
}
