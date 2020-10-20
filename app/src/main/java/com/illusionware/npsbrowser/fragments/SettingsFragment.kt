package com.illusionware.npsbrowser.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction.TRANSIT_FRAGMENT_OPEN
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.illusionware.npsbrowser.R
import com.illusionware.npsbrowser.activities.BaseActivity
import com.illusionware.npsbrowser.activities.MainActivity
import com.illusionware.npsbrowser.fragments.mainactivity.MainActivityFragment
import kotlinx.android.synthetic.main.appbar.*

class SettingsFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (activity!!.supportFragmentManager.backStackEntryCount < 1) {
            (activity as MainActivity?)?.showUpButton()
            MainActivityFragment().recyclerView?.adapter = MainActivityFragment().viewAdapter
        }
        PreferenceManager.setDefaultValues(context, R.xml.root_preferences, false);
        activity?.supportFragmentManager
            ?.beginTransaction()
            ?.replace(
                R.id.prefs,
                SettingsFragment()
            )
            ?.setTransition(TRANSIT_FRAGMENT_OPEN)
            ?.commit()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        activity?.title = resources.getString(R.string.title_activity_settings)
        return inflater.inflate(R.layout.settings_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (activity as BaseActivity?)!!.setSupportActionBar(toolbar)
        (activity as BaseActivity?)!!.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        //viewModel = ViewModelProviders.of(this).get(MainActivity2ViewModel::class.java)
        // TODO: Use the ViewModel
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
            PreferenceManager.setDefaultValues(context, R.xml.root_preferences, false);
        }

        override fun onActivityCreated(savedInstanceState: Bundle?) {
            //(activity as BaseActivity?)!!.setSupportActionBar(toolbar)
            super.onActivityCreated(savedInstanceState)
        }
    }

}