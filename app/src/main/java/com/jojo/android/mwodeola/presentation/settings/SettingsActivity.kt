package com.jojo.android.mwodeola.presentation.settings

import android.content.Context
import android.os.Bundle
import android.view.MenuItem
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import com.jojo.android.mwodeola.R
import com.jojo.android.mwodeola.databinding.ActivitySettingsBinding
import com.jojo.android.mwodeola.presentation.BaseActivity
import com.jojo.android.mwodeola.util.Log2

class SettingsActivity : BaseActivity() {

    override val binding by lazy { ActivitySettingsBinding.inflate(layoutInflater) }
    override val isScreenLockEnabled: Boolean = true

    private val fragmentObserver = FragmentLifecycleObserver()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setSupportActionBar(binding.toolBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        supportFragmentManager.registerFragmentLifecycleCallbacks(fragmentObserver, true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        android.R.id.home -> { onBackPressed(); true; }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        supportFragmentManager.unregisterFragmentLifecycleCallbacks(fragmentObserver)
        super.onDestroy()
    }

    inner class FragmentLifecycleObserver : FragmentManager.FragmentLifecycleCallbacks() {
        override fun onFragmentResumed(fm: FragmentManager, fragment: Fragment) {
            if (fragment is SettingsSupportFragment) {
                supportActionBar?.title = fragment.toolBarTitle
            }
        }
    }
}