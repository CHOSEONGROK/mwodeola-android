package com.jojo.android.mwodeola.presentation.intro

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.safetynet.SafetyNetAppCheckProviderFactory
import com.jojo.android.mwodeola.data.local.SearchHistory
import com.jojo.android.mwodeola.databinding.ActivityIntroBinding
import com.jojo.android.mwodeola.model.local.SearchHistoryRepository
import com.jojo.android.mwodeola.model.local.SearchHistorySource
import com.jojo.android.mwodeola.model.token.TokenSharedPref
import com.jojo.android.mwodeola.presentation.BaseActivity
import com.jojo.android.mwodeola.presentation.main.MainActivity
import com.jojo.android.mwodeola.presentation.security.BiometricHelper
import com.jojo.android.mwodeola.presentation.security.screen.BaseAuthenticationActivity.Companion.CANCELLED
import com.jojo.android.mwodeola.presentation.security.screen.BaseAuthenticationActivity.Companion.EXTRA_PURPOSE
import com.jojo.android.mwodeola.presentation.security.screen.BaseAuthenticationActivity.Companion.EXTRA_RESULT
import com.jojo.android.mwodeola.presentation.security.screen.BaseAuthenticationActivity.Companion.PURPOSE_SIGN_IN
import com.jojo.android.mwodeola.presentation.security.screen.BaseAuthenticationActivity.Companion.PURPOSE_SIGN_UP
import com.jojo.android.mwodeola.presentation.security.screen.BaseAuthenticationActivity.Companion.SUCCEED
import com.jojo.android.mwodeola.presentation.users.sign.SignActivity
import com.jojo.android.mwodeola.presentation.security.SecurityManager
import com.jojo.android.mwodeola.presentation.security.screenLock.ScreenLockHandler
import kotlin.concurrent.timer

class IntroActivity : BaseActivity(), IntroContract.View {
    companion object { const val TAG = "IntroActivity" }

    override val isScreenLockEnabled: Boolean = false
    override val binding by lazy { ActivityIntroBinding.inflate(layoutInflater) }

    private val presenter: IntroContract.Presenter = IntroPresenter(this)

    private val securitySharedPref = SecurityManager.SharedPref(this)

    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val resultData = result.data
            ?: return@registerForActivityResult
        val resultExtra = resultData.getIntExtra(EXTRA_RESULT, CANCELLED)
        val purpose = resultData.getIntExtra(EXTRA_PURPOSE, -1)

        if (result.resultCode == RESULT_OK && resultExtra == SUCCEED) {
            when (purpose) {
                PURPOSE_SIGN_UP -> {
                    ScreenLockHandler.signUpFlag = true
                    startMainActivity()
                }
                PURPOSE_SIGN_IN -> {
                    ScreenLockHandler.signInFlag = true
                    startMainActivity()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        if (SecurityManager.hasNotSecretKey()) {
            SecurityManager.generateSecretKey()
        }

        Log.d(TAG, "onCreate(): authType=${securitySharedPref.authType()}")
        // securitySharedPref.screenLockCredential(APP_CREDENTIAL)

        Log.d(TAG, "onCreate(): canAuthentication=${BiometricHelper.canAuthentication}")
        Log.d(TAG, "onCreate(): canAuthenticationString=${BiometricHelper.canAuthenticationString}")
        Log.d(TAG, "onCreate(): isAuthentication=${BiometricHelper.isAuthentication}")
        Log.d(TAG, "onCreate(): hasNewBiometricEnrolled=${BiometricHelper.hasNewBiometricEnrolled}")

        // Firebase initialize
        FirebaseApp.initializeApp(this)
        val firebaseAppCheck = FirebaseAppCheck.getInstance()
        firebaseAppCheck.installAppCheckProviderFactory(
            SafetyNetAppCheckProviderFactory.getInstance()
        )

        initializeWindowSettings()
        initView()

        // 토큰 세팅
        TokenSharedPref.init(this)
        if (TokenSharedPref.REFRESH_TOKEN != null) {
            binding.btnStart.isVisible = false
            timer(period = 500, initialDelay = 500) {
                presenter.signInAuto()
                cancel()
            }
        } else {
            binding.btnStart.isVisible = true
        }
    }

    override fun startMainActivity() {
        startActivity(Intent(baseContext, MainActivity::class.java))
        finish()
    }

    override fun startSignActivity() {
        launcher.launch(Intent(this, SignActivity::class.java))
    }

    override fun showToast(message: String?) {
        Toast.makeText(baseContext, message, Toast.LENGTH_SHORT).show()
    }

    private fun initView(): Unit = with (binding) {
//        btnSignInAuto.setOnClickListener {
//            presenter.signInAuto()
//        }
        btnStart.setOnClickListener {
            startSignActivity()
        }
    }

    private fun initializeWindowSettings() {
        window?.run {
            addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            addFlags(WindowManager.LayoutParams.FLAG_SECURE)

//            clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
//            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)

            if (Build.VERSION_CODES.M <= Build.VERSION.SDK_INT && Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                // 버전 대응하기(테스트 X)
                decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_IMMERSIVE or
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                        View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                setDecorFitsSystemWindows(false)
                statusBarColor = ContextCompat.getColor(baseContext, android.R.color.transparent)
//                navigationBarColor = ContextCompat.getColor(baseContext, R.color.app_theme_color)
                val statusBarId = resources.getIdentifier("status_bar_height", "dimen", "android")
                val navigationBarId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
                if (statusBarId > 0 && navigationBarId > 0) {
                    val statusBarHeight = resources.getDimensionPixelSize(statusBarId)
                    val navigationBarHeight = resources.getDimensionPixelSize(navigationBarId)
                    binding.bottomGuideLine.setGuidelineEnd(navigationBarHeight)
                }
            }
        }
    }
}