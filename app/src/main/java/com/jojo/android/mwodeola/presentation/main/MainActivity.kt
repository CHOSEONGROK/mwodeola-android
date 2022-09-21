package com.jojo.android.mwodeola.presentation.main

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.autofill.AutofillManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.jojo.android.mwodeola.autofill.AutofillHelper
import com.jojo.android.mwodeola.databinding.ActivityMainBinding
import com.jojo.android.mwodeola.model.common.CommonRepository
import com.jojo.android.mwodeola.presentation.BaseActivity
import com.jojo.android.mwodeola.presentation.drawer.DrawerActivity
import com.jojo.android.mwodeola.presentation.settings.SettingsActivity
import kotlin.concurrent.timer

class MainActivity : BaseActivity(), MainContract.View {
    companion object {
        const val TAG = "MainActivity"
        const val START_FLAG = "start_flag"
        const val START_FLAG_SIGN_UP = 0
        const val START_FLAG_SIGN_IN = 1
        const val START_FLAG_SIGN_IN_AUTO = 2

        fun start(activity: Activity, startFlag: Int) {
            activity.startActivity(Intent(activity, MainActivity::class.java).apply {
                putExtra(START_FLAG, startFlag)
            })
        }
    }

    override val isScreenLockEnabled: Boolean = true
    override val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    private val presenter: MainContract.Presenter
            by lazy { MainPresenter(this, CommonRepository(this)) }

    private var backPressedFlag: Boolean = false

    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        Log.w(TAG, "onCreate(): changingConfigurations=$changingConfigurations")
        Log.w(TAG, "onCreate(): isChangingConfigurations=$isChangingConfigurations")
        Log.w(TAG, "onCreate(): lastNonConfigurationInstance=$lastNonConfigurationInstance")
        Log.w(TAG, "onCreate(): CONFIG_KEYBOARD=${changingConfigurations or ActivityInfo.CONFIG_KEYBOARD}")
        Log.w(TAG, "onCreate(): CONFIG_KEYBOARD_HIDDEN=${changingConfigurations or ActivityInfo.CONFIG_KEYBOARD_HIDDEN}")
        Log.w(TAG, "onCreate(): CONFIG_SCREEN_LAYOUT=${changingConfigurations or ActivityInfo.CONFIG_SCREEN_LAYOUT}")
        Log.w(TAG, "onCreate(): CONFIG_SMALLEST_SCREEN_SIZE=${changingConfigurations or ActivityInfo.CONFIG_SMALLEST_SCREEN_SIZE}")

        initView()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }

    override fun onResume() {
        super.onResume()
        presenter.countAllData()
        checkEnabledAutofillService()
    }

    override fun onBackPressed() {
        if (backPressedFlag) {
            super.onBackPressed()
        } else {
            showToast("'뒤로' 버튼을 한 번 더 누르시면 종료됩니다.")
            backPressedFlag = true
            timer(period = 3000, initialDelay = 3000) {
                backPressedFlag = false
                cancel()
            }
        }
    }

    override fun updateNumberOfUserAccountData(count: Int) {
        binding.tvAccountLabel.text = "계정 ($count)"
    }

    override fun updateNumberOfCreditCardData(count: Int) {
        binding.tvCreditCardLabel.text = "체크/신용카드 ($count)"
    }

    override fun showToast(message: String?) {
        Toast.makeText(baseContext, message, Toast.LENGTH_LONG).show()
    }

    private fun initView(): Unit = with (binding) {
        if (Build.VERSION.SDK_INT >= 26) {
            btnAutofillSetting.setOnClickListener {
                AutofillHelper.requestAutofillServiceSettings(this@MainActivity)
            }
        }

        containerShowAccount.setOnClickListener {
            startDrawerActivity(DrawerActivity.CONTENT_ACCOUNT)
        }
        containerShowCreditCard.setOnClickListener {
            //startDrawerActivity(DrawerActivity.CONTENT_CREDIT_CARD)
        }

        btnSettings.setOnClickListener {
            startActivity(SettingsActivity::class.java)
        }

    }

    private fun initProperties() {
        val startFlag = intent.getIntExtra(START_FLAG, START_FLAG_SIGN_UP)
//        when (startFlag) {
//            START_FLAG_SIGN_UP ->
//        }
    }

    private fun startDrawerActivity(type: Int) {
        startActivity(Intent(this, DrawerActivity::class.java).apply {
            putExtra(DrawerActivity.EXTRA_CONTENT_TYPE, type)
        })
    }

    private fun startActivity(cls: Class<*>) {
        startActivity(Intent(this, cls))
    }

    private fun checkEnabledAutofillService(): Unit = with (binding) {
        if (Build.VERSION.SDK_INT >= 26) {
            val autofillManager = getSystemService(AutofillManager::class.java)
            val isSupported = autofillManager.isAutofillSupported
            val isEnabled = autofillManager.hasEnabledAutofillServices()

            containerAutofillSetting.visibility =
                if (isSupported && !isEnabled) View.VISIBLE
                else View.GONE
        } else {
            containerAutofillSetting.visibility = View.GONE
        }
    }
}
