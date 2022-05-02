package com.jojo.android.mwodeola.presentation.settings.authenticationAndSecurity

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Toast
import androidx.core.widget.TextViewCompat
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.jojo.android.mwodeola.R
import com.jojo.android.mwodeola.databinding.ActivitySettingsScreenLockModeBinding
import com.jojo.android.mwodeola.databinding.BottomSheetScreenLockTimeoutBinding
import com.jojo.android.mwodeola.presentation.BaseActivity
import com.jojo.android.mwodeola.presentation.security.BiometricHelper
import com.jojo.android.mwodeola.presentation.security.SecurityManager
import com.jojo.android.mwodeola.presentation.security.SecurityManager.SharedPref.Companion.APP_CREDENTIAL
import com.jojo.android.mwodeola.presentation.security.SecurityManager.SharedPref.Companion.DEVICE_CREDENTIAL


class SettingsScreenLockModeActivity : BaseActivity() {

    override val isScreenLockEnabled: Boolean = true
    override val binding by lazy { ActivitySettingsScreenLockModeBinding.inflate(layoutInflater) }

    private val securitySharedPref by lazy { SecurityManager.SharedPref(this) }

    private val appThemeColor by lazy { resources.getColor(R.color.app_theme_color, null) }
    private val textDefaultColor by lazy { resources.getColor(R.color.text_view_text_default_color, null) }
    private val textBlackColor = Color.BLACK
    private val disabledColor by lazy { resources.getColor(R.color.gray400, null) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        initView()
    }

    private fun initView(): Unit = with(binding) {
        btnBack.setOnClickListener { onBackPressed() }

        val isScreenLockEnabled = securitySharedPref.isScreenLockEnabled()
        val screenLockCredential = securitySharedPref.screenLockCredential()

        switchScreenLock.setOnCheckedChangeListener { buttonView, isChecked ->
            buttonView.text =
                if (isChecked) "화면 잠금 사용 중"
                else "화면 잠금 사용 안 함"
            buttonView.setTextColor(
                if (isChecked) appThemeColor
                else textBlackColor
            )
            tvSwitchScreenLockHelpText.text =
                if (isChecked) "모든 화면에서"
                else "앱 시작 시에만"

            securitySharedPref.isScreenLockEnabled(isChecked)

            if (isChecked) {
                settingsContainer.animate().setInterpolator(AccelerateDecelerateInterpolator())
                    .setDuration(150)
                    .scaleY(1f)
                    .alpha(1f)
                    .withStartAction { settingsContainer.visibility = View.VISIBLE }
                    .start()
            } else {
                settingsContainer.animate().setInterpolator(AccelerateDecelerateInterpolator())
                    .setDuration(150)
                    .scaleY(0.8f)
                    .alpha(0f)
                    .withEndAction { settingsContainer.visibility = View.GONE }
                    .start()
            }

            if (isChecked.not()) {
                radioGroupAuthenticationType.check(toggleAuthTypeAppCredential.id)
            }
        }
        switchScreenLock.isChecked = isScreenLockEnabled

        settingsContainer.visibility =
            if (isScreenLockEnabled) View.VISIBLE
            else View.GONE
        settingsContainer.scaleY =
            if (isScreenLockEnabled) 1f
            else 0.8f
        settingsContainer.alpha =
            if (isScreenLockEnabled) 1f
            else 0f

        if (BiometricHelper.isAuthentication.not()) {
            toggleAuthTypeDeviceCredential.setTextColor(disabledColor)
            TextViewCompat.setCompoundDrawableTintList(toggleAuthTypeDeviceCredential, ColorStateList.valueOf(disabledColor))
        }

        when (screenLockCredential) {
            APP_CREDENTIAL -> radioGroupAuthenticationType.check(toggleAuthTypeAppCredential.id)
            DEVICE_CREDENTIAL -> radioGroupAuthenticationType.check(toggleAuthTypeDeviceCredential.id)
        }

        radioGroupAuthenticationType.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                toggleAuthTypeAppCredential.id ->
                    securitySharedPref.screenLockCredential(APP_CREDENTIAL)
                toggleAuthTypeDeviceCredential.id -> {
                    if (BiometricHelper.isAuthentication) {
                        securitySharedPref.screenLockCredential(DEVICE_CREDENTIAL)
                    } else {
                        showToast("먼저 휴대폰에 지문 등록을 하셔야 합니다")
                        toggleAuthTypeAppCredential.isChecked = true
                    }
                }
            }
        }

        updateTimeoutLabel(securitySharedPref.screenLockTimeout())

        btnScreenLockTimeout.setOnClickListener {
            ScreenLockTimeoutBottomSheet()
                .show()
        }
    }

    private fun updateTimeoutLabel(timeout: Int) {
        binding.tvScreenLockTimeout.text = when (timeout) {
            0 -> "즉시"
            in 1 until 60 -> "${timeout}초"
            60 -> "1분"
            else -> ""
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    inner class ScreenLockTimeoutBottomSheet : BottomSheetDialog(this) {

        private val bottomSheetBinding = BottomSheetScreenLockTimeoutBinding.inflate(layoutInflater)

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(bottomSheetBinding.root)

            val timeout = securitySharedPref.screenLockTimeout()

            bottomSheetBinding.toggleGroup.findViewWithTag<MaterialButton>(timeout.toString()).isChecked = true
            bottomSheetBinding.toggleGroup.addOnButtonCheckedListener { group, checkedId, isChecked ->
                if (isChecked) {
                    val newTimeout = (group.findViewById<MaterialButton>(checkedId).tag as String).toIntOrNull()
                        ?: return@addOnButtonCheckedListener

                    securitySharedPref.screenLockTimeout(newTimeout)
                    updateTimeoutLabel(newTimeout)
                    dismiss()
                }
            }
        }
    }
}