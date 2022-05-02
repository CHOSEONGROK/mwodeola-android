package com.jojo.android.mwodeola.presentation.settings.authenticationAndSecurity

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import com.jojo.android.mwodeola.R
import com.jojo.android.mwodeola.presentation.BaseActivity
import com.jojo.android.mwodeola.databinding.ActivitySettingsAuthAndSecurityBinding
import com.jojo.android.mwodeola.presentation.security.Authenticators
import com.jojo.android.mwodeola.presentation.security.SecurityManager

class SettingsAuthAndSecurityActivity : BaseActivity() {
    companion object { const val TAG = "SettingsAuthAndSecurityActivity" }

    override val isScreenLockEnabled: Boolean = true
    override val binding by lazy { ActivitySettingsAuthAndSecurityBinding.inflate(layoutInflater) }

    private val activity: SettingsAuthAndSecurityActivity
        get() = this

    private val securitySharedPref by lazy { SecurityManager.SharedPref(this) }
    private val authenticationListener = MyAuthenticationListener()

    private val appThemeColor by lazy { resources.getColor(R.color.app_theme_color, null) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        initView()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        android.R.id.home -> { finish(); true; }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        binding.tvSubtitlePasswordAuthType.text = securitySharedPref.authType().name

        updateScreenLockTimeoutWidget()
    }

    private fun initView(): Unit = with(binding) {
        setSupportActionBar(toolBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        btnPasswordAuthType.setOnClickListener {
            authenticationListener.setActivity(SettingsAuthTypeActivity::class.java)
            Authenticators.BottomSheetBuilder(activity)
                .callback(authenticationListener)
                .execute()
        }

        updateScreenLockTimeoutWidget()
        btnScreenLockType.setOnClickListener {
            authenticationListener.setActivity(SettingsScreenLockModeActivity::class.java)
            Authenticators.BottomSheetBuilder(activity)
                .callback(authenticationListener)
                .execute()
        }
    }

    private fun updateScreenLockTimeoutWidget() {
        val screenLockEnabled = securitySharedPref.isScreenLockEnabled()

        binding.switchScreenLockType.isChecked = screenLockEnabled

        binding.labelScreenLockType.text = if (screenLockEnabled) {
            StringBuilder("모든 화면에서").apply {
                when (securitySharedPref.screenLockCredential()) {
                    SecurityManager.SharedPref.APP_CREDENTIAL -> append(", 앱 잠금 방식")
                    SecurityManager.SharedPref.DEVICE_CREDENTIAL -> append(", 휴대폰 잠금 방식")
                }
                when (val timeout = securitySharedPref.screenLockTimeout()) {
                    0 -> append(", 즉시")
                    in 1 until 60 -> append(", ${timeout}초")
                    60 -> append(", 1분")
                    else -> {}
                }
            }
        } else {
            "앱 시작 시에만"
        }
    }

    inner class MyAuthenticationListener : Authenticators.AuthenticationCallback() {
        private var startedActivity: Class<*>? = null

        override fun onSucceed() {
            startActivity(Intent(activity, startedActivity))
        }

        override fun onFailure() {

        }

        override fun onExceedAuthLimit(limit: Int) {

        }

        fun setActivity(activity: Class<*>) {
            this.startedActivity = activity
        }
    }
}