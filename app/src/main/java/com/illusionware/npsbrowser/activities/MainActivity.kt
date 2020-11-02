package com.illusionware.npsbrowser.activities

import android.os.Bundle
import android.view.MenuItem
import androidx.fragment.app.FragmentManager
import com.illusionware.npsbrowser.R
import com.illusionware.npsbrowser.fragments.mainactivity.MainActivityFragment

class MainActivity : BaseActivity(), FragmentManager.OnBackStackChangedListener  {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.NPSTheme)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportFragmentManager.addOnBackStackChangedListener(this);

        if (supportFragmentManager.backStackEntryCount > 0) {
            showUpButton()
        }

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, MainActivityFragment())
                .commitNow()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                supportFragmentManager.popBackStack()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackStackChanged() {
        // enable Up button only  if there are entries on the backstack
        if (supportFragmentManager.backStackEntryCount < 1) {
            hideUpButton()
        } else {
            showUpButton()
        }
    }

    private fun showUpButton() {
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
    }

    private fun hideUpButton() {
        supportActionBar!!.setDisplayHomeAsUpEnabled(false)
    }
}
