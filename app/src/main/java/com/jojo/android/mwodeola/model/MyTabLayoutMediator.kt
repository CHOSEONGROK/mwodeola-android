package com.jojo.android.mwodeola.model

import android.app.Activity
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.util.Log
import android.util.TypedValue
import android.widget.TextView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.jojo.android.mwodeola.R
import com.jojo.android.mwodeola.data.account.Account
import com.jojo.android.mwodeola.data.account.AccountGroup
import com.jojo.android.mwodeola.data.account.AccountGroupAndDetails
import com.jojo.android.mwodeola.presentation.common.SquircleIcon


class MyTabLayoutMediator(
    private val activity: Activity,
    private val tabLayout: TabLayout,
    private val viewPager2: ViewPager2
): TabLayout.OnTabSelectedListener {
    private lateinit var mediator: TabLayoutMediator

    fun init(accounts: AccountGroupAndDetails) {
        mediator = TabLayoutMediator(tabLayout, viewPager2) { tab, position ->
            bindTab(tab, accounts[position], position)
        }
        tabLayout.clearOnTabSelectedListeners()
        tabLayout.addOnTabSelectedListener(this)
    }

    fun attach() {
        mediator.attach()
    }

    fun detach() {
        mediator.detach()
    }

    override fun onTabSelected(tab: TabLayout.Tab?) {
        tab?.customView?.findViewById<TextView>(R.id.text)?.let {
            it.setTypeface(null, Typeface.BOLD)
            it.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
        }
    }

    override fun onTabUnselected(tab: TabLayout.Tab?) {
        tab?.customView?.findViewById<TextView>(R.id.text)?.let {
            it.setTypeface(null, Typeface.NORMAL)
            it.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
        }
    }

    override fun onTabReselected(tab: TabLayout.Tab?) {}

    private fun bindTab(tab: TabLayout.Tab, account: Account, position: Int) {
        tab.setCustomView(R.layout.tab_custom_view)
        val tabTextView = tab.customView?.findViewById<TextView>(R.id.text)
        val tabIconView = tab.customView?.findViewById<SquircleIcon>(R.id.icon)

        tabTextView?.text = account.detail.user_id

        if (account.sns_group == null) { // 자사 계정
            when (account.own_group.icon_type) {
                AccountGroup.ICON_TYPE_TEXT ->
                    tabIconView?.setIconText(account.own_group.group_name)
                AccountGroup.ICON_TYPE_IMAGE -> {}
                AccountGroup.ICON_TYPE_INSTALLED_APP_LOGO -> {
                    val icon = getIconFromInstalledApp(account.own_group.app_package_name)
                    Log.i("MyTabLayoutMediator", "icon=$icon")
                    tabIconView?.setIconImageDrawable(icon)
                }
            }
        } else { // SNS 연동 계정
            tabIconView?.setSnsGroupIcon(account.sns_group.sns)
        }

        tab.orCreateBadge.number = position
        tab.orCreateBadge.backgroundColor = Color.RED
    }

    private fun getIconFromInstalledApp(packageName: String?): Drawable? {
        if (packageName == null)
            return null

        return try {
            activity.packageManager.getApplicationIcon(packageName)
        } catch (e: PackageManager.NameNotFoundException) {
            Log.w("MyTabLayoutMediator", "getIconFromInstalledApp(): $e")
            null
        }
    }
}