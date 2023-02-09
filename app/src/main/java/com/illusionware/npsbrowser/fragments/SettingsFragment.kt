package com.illusionware.npsbrowser.fragments

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction.TRANSIT_FRAGMENT_OPEN
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.illusionware.npsbrowser.R

class SettingsFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PreferenceManager.setDefaultValues(requireContext(), R.xml.root_preferences, false)
        activity?.supportFragmentManager
            ?.beginTransaction()
            ?.replace(
                R.id.prefs,
                PrefsFragment()
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

    class PrefsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
            PreferenceManager.setDefaultValues(requireContext(), R.xml.root_preferences, false)
        }
    }

}