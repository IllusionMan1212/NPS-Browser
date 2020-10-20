package com.illusionware.npsbrowser.fragments.mainactivity

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
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
import com.illusionware.npsbrowser.activities.SingleAppActivity
import com.illusionware.npsbrowser.activities.MainActivity
import com.illusionware.npsbrowser.adapters.AppAdapter
import com.illusionware.npsbrowser.fragments.SettingsFragment
import kotlinx.android.synthetic.main.appbar.*
import java.io.InputStream

class MainActivityFragment : Fragment(), FragmentManager.OnBackStackChangedListener {

    var recyclerView : RecyclerView? = null
    var viewAdapter: AppAdapter? = null

    companion object {

        fun newInstance() = MainActivityFragment()
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.supportFragmentManager?.addOnBackStackChangedListener(this);
        setHasOptionsMenu(true)
    }

    override fun onBackStackChanged() {
        // enable Up button only  if there are entries on the backstack
        if (requireActivity().supportFragmentManager.backStackEntryCount < 1) {
            (activity as MainActivity?)?.hideUpButton()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        activity?.title = resources.getString(R.string.app_name)

        val view = inflater.inflate(R.layout.main_fragment, container, false)

        recyclerView = view?.findViewById(R.id.appsRecycler)

        if (apps != null) {
            viewAdapter = AppAdapter(apps!!, requireContext())
        }
        viewAdapter?.notifyDataSetChanged()
        val orientation = this.resources.configuration.orientation
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val isViewAsList = prefs.getString("layout_type", "0")
        if (isViewAsList == "0") {
            viewAdapter?.toggleItemViewType(0)
            recyclerView?.layoutManager = LinearLayoutManager(context)
            viewAdapter?.notifyDataSetChanged()
        } else {
            viewAdapter?.toggleItemViewType(1)
            recyclerView?.layoutManager = GridLayoutManager(context,
                    if (orientation == Configuration.ORIENTATION_LANDSCAPE) 4 else 2)
            viewAdapter?.notifyDataSetChanged()
        }
        recyclerView?.adapter = viewAdapter

        recyclerView?.addOnItemTouchListener(RecyclerTouchListener(requireContext(), recyclerView!!, object : RecyclerTouchListener.ClickListener {
            override fun onClick(view: View?, position: Int) {
                val myIntent = Intent(context, SingleAppActivity::class.java)
                SingleAppActivity.app = apps!![position]

                startActivity(myIntent)
            }

            override fun onLongClick(view: View?, recyclerView: RecyclerView?, position: Int) {
                Toast.makeText(context, "TODO: Long Click ", Toast.LENGTH_SHORT).show()
            }
        }
        ))
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        (activity as MainActivity?)!!.setSupportActionBar(toolbar)

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
    }

    override fun onCreateOptionsMenu(menu : Menu, menuInflater : MenuInflater) {
        menuInflater.inflate(R.menu.toolbar_main, menu)
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
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            resultData?.data?.also { uri ->
                Log.d("file_location", uri.toString())
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
                    val app = AppData(id, region, name, link ?: "MISSING", license ?: "MISSING",
                        contentID ?: "MISSING", lastDateTime ?: "MISSING",
                        fileSize, sha256 ?: "MISSING", minFW)

                    apps?.add(app)
                }
                apps?.sortBy { it.title }
                recyclerView = view?.findViewById(R.id.appsRecycler)
                viewAdapter = AppAdapter(apps!!, requireContext())
                val orientation = this.resources.configuration.orientation
                val prefs = PreferenceManager.getDefaultSharedPreferences(context)
                val isViewAsList = prefs.getString("layout_type", "0")

                if (isViewAsList == "0") {
                    viewAdapter?.toggleItemViewType(0)
                    recyclerView?.layoutManager = LinearLayoutManager(context)
                    viewAdapter?.notifyDataSetChanged()
                } else {
                    viewAdapter?.toggleItemViewType(1)
                    recyclerView?.layoutManager = GridLayoutManager(context,
                            if (orientation == Configuration.ORIENTATION_LANDSCAPE) 4 else 2)
                    viewAdapter?.notifyDataSetChanged()
                }

                recyclerView?.adapter = viewAdapter
            }
        }
    }

    private fun isStoragePermissionGranted(): Boolean {
        return  if (checkSelfPermission(this.context!!, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Log.v("perms", "Permission is granted")
            true
        } else {
            Log.v("perms", "Permission is revoked")
            requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1)
            false
        }
    }
}
