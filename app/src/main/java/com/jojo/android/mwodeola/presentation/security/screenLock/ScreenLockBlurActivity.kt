package com.jojo.android.mwodeola.presentation.security.screenLock

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.jojo.android.mwodeola.R
import com.jojo.android.mwodeola.databinding.ActivityScreenLockBlurBinding
import com.jojo.android.mwodeola.presentation.BaseActivity
import com.jojo.android.mwodeola.presentation.common.BottomUpDialog
import com.jojo.android.mwodeola.presentation.security.AuthType
import com.jojo.android.mwodeola.presentation.security.Authenticators
import com.jojo.android.mwodeola.presentation.security.BiometricHelper
import com.jojo.android.mwodeola.presentation.security.SecurityManager
import com.jojo.android.mwodeola.presentation.security.SecurityManager.SharedPref.Companion.APP_CREDENTIAL
import com.jojo.android.mwodeola.presentation.security.SecurityManager.SharedPref.Companion.DEVICE_CREDENTIAL
import com.jojo.android.mwodeola.presentation.security.screen.BaseAuthenticationActivity.Companion.CANCELLED
import com.jojo.android.mwodeola.presentation.security.screen.BaseAuthenticationActivity.Companion.EXTRA_RESULT
import com.jojo.android.mwodeola.presentation.security.screen.BaseAuthenticationActivity.Companion.PURPOSE_SCREEN_LOCK
import com.jojo.android.mwodeola.presentation.security.screen.BaseAuthenticationActivity.Companion.SUCCEED
import com.jojo.android.mwodeola.util.Log2

class ScreenLockBlurActivity : BaseActivity() {

    companion object {
        private const val TAG = "ScreenLockBlurActivity"
    }

    override val isScreenLockEnabled: Boolean = false
    override val binding by lazy { ActivityScreenLockBlurBinding.inflate(layoutInflater) }

    private lateinit var securitySharedPref: SecurityManager.SharedPref

    private val biometricCallback = BiometricAuthenticationCallback()

    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        Log.d(TAG ,"onActivityResult(): resultCode=${it.resultCode}, data=${it.data}")
        if (it.resultCode == RESULT_OK && it.data?.getIntExtra(EXTRA_RESULT, CANCELLED) == SUCCEED) {
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        securitySharedPref = SecurityManager.SharedPref(this)

        settingWindow()
        initView()
        executeScreenLock(securitySharedPref.authType())
    }

    override fun onBackPressed() {
        BottomUpDialog.Builder(supportFragmentManager)
            .title("앱을 종료하시겠습니까?")
            .positiveButton {
                finishAffinity()
            }
            .show()
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.activity_fade_in, R.anim.activity_fade_out)
    }

    private fun settingWindow() {
        // TODO: 버전 대응하기
        var statusBarHeight = 0
        var navigationBarHeight = 0

        with (window) {
            addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            addFlags(WindowManager.LayoutParams.FLAG_SECURE)

            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.R) {
                setDecorFitsSystemWindows(false)
            }

            statusBarColor = ContextCompat.getColor(baseContext, android.R.color.transparent)
            navigationBarColor = ContextCompat.getColor(baseContext, android.R.color.transparent)

            val statusBarId = context.resources
                .getIdentifier("status_bar_height", "dimen", "android")
            val navigationBarId = context.resources
                .getIdentifier("navigation_bar_height", "dimen", "android")

            if (statusBarId > 0 && navigationBarId > 0) {
                statusBarHeight = context.resources.getDimensionPixelSize(statusBarId)
                navigationBarHeight = context.resources.getDimensionPixelSize(navigationBarId)

                binding.statusBarGuideline.setGuidelineBegin(statusBarHeight)
                binding.navigationBarGuideline.setGuidelineEnd(navigationBarHeight)
            }
        }
    }

    private fun initView(): Unit = with(binding) {
        ivBlur.setImageBitmap(ScreenLockHandler.blurSnapshot)

        initButton(btnPin5, AuthType.PIN_5)
        initButton(btnPin6, AuthType.PIN_6)
        initButton(btnPattern, AuthType.PATTERN)
        initButton(btnBiometric, AuthType.BIOMETRIC)

        btnAppFinish.setOnClickListener { onBackPressed() }
    }

    private fun initButton(button: Button, type: AuthType) {
        if (securitySharedPref.isExistsPassword(type)) {
            button.setOnClickListener {
                executeScreenLock(type)
            }
        } else {
            button.visibility = View.GONE
        }
    }

    private fun executeScreenLock(authType: AuthType) {
        when (securitySharedPref.screenLockCredential()) {
            APP_CREDENTIAL -> {
                Authenticators.ScreenBuilder(this)
                    .purpose(PURPOSE_SCREEN_LOCK)
                    .authType(authType)
                    .biometricCallback(biometricCallback)
                    .launcher(launcher)
                    .execute()
            }
            DEVICE_CREDENTIAL -> {
                if (BiometricHelper.isAuthentication) {
                    Authenticators.DeviceCredentialBuilder(this)
                        .callback(biometricCallback)
                        .execute()
                } else {
                    securitySharedPref.screenLockCredential(APP_CREDENTIAL)
                    executeScreenLock(authType)
                }
            }
        }
    }

    inner class BiometricAuthenticationCallback : Authenticators.AuthenticationCallback() {
        override fun onSucceed() {
            finish()
        }

        override fun onFailure() {}
        override fun onExceedAuthLimit(limit: Int) {}
    }
}