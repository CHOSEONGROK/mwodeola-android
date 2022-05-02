package com.jojo.android.mwodeola.presentation.settings.authAndSecurity

import android.app.Activity
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.jojo.android.mwodeola.R
import com.jojo.android.mwodeola.databinding.BottomSheetScreenLockTimeoutBinding
import com.jojo.android.mwodeola.presentation.security.SecurityManager
import com.jojo.android.mwodeola.presentation.settings.SettingsSupportFragment
import com.jojo.android.mwodeola.presentation.settings.custom.RadioButtonPreference
import com.jojo.android.mwodeola.presentation.settings.custom.RadioGroupPreference

class SettingsScreenLockTypeSelectFragment : PreferenceFragmentCompat(), SettingsSupportFragment {

    override val toolBarTitle: String = "화면 잠금 방식"

    private val securitySharedPref by lazy { SecurityManager.SharedPref(requireContext()) }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings_screen_lock_type_select_fragment_prefs, rootKey)

        // 화면 잠금 사용/미사용 스위치
        findPreference<SwitchPreferenceCompat>("screen_lock_enabled")?.let {
            val isScreenLockEnabled = securitySharedPref.isScreenLockEnabled()
            val screenLockAuthTypeCategory = findPreference<RadioGroupPreference>("auth_type_radio_group")
            val screenLockTimeoutCategory = findPreference<Preference>("timeout_category")

            it.isChecked = isScreenLockEnabled
            screenLockAuthTypeCategory?.isVisible = isScreenLockEnabled
            screenLockTimeoutCategory?.isVisible = isScreenLockEnabled

            it.setOnPreferenceChangeListener { _, isChecked ->
                if (isChecked is Boolean) {
                    securitySharedPref.isScreenLockEnabled(isChecked)

                    screenLockAuthTypeCategory?.isVisible = isChecked
                    screenLockTimeoutCategory?.isVisible = isChecked
                }
                true
            }
        }

        // 화면 잠금 인증 방식 RadioGroup
        findPreference<RadioGroupPreference>("auth_type_radio_group")?.let {
            when (securitySharedPref.screenLockCredential()) {
                SecurityManager.SharedPref.APP_CREDENTIAL -> it.checkToggle("app_credential")
                SecurityManager.SharedPref.DEVICE_CREDENTIAL -> it.checkToggle("device_credential")
            }

            it.setToggleWatcher(object : RadioGroupPreference.ToggleWatcher() {
                override fun onToggleChanged(toggle: RadioButtonPreference, isChecked: Boolean) {
                    if (isChecked.not())
                        return

                    when (toggle.key) {
                        "app_credential" -> securitySharedPref.screenLockCredential(
                            SecurityManager.SharedPref.APP_CREDENTIAL
                        )
                        "device_credential" -> securitySharedPref.screenLockCredential(
                            SecurityManager.SharedPref.DEVICE_CREDENTIAL
                        )
                    }
                }
            })
        }

        // 화면 잠금 Timeout
        findPreference<Preference>("screen_lock_timeout")?.let {
            it.summary = securitySharedPref.screenLockTimeoutString()

            it.setOnPreferenceClickListener { _ ->
                ScreenLockTimeoutBottomSheet(requireActivity()) { timeout ->
                    it.summary = timeout
                }
                    .show()
                true
            }
        }
    }

    class ScreenLockTimeoutBottomSheet(
        activity: Activity,
        private val dismissCallback: (timeout: String) -> Unit
    ) : BottomSheetDialog(activity) {

        private val bottomSheetBinding = BottomSheetScreenLockTimeoutBinding.inflate(layoutInflater)
        private val securitySharedPref = SecurityManager.SharedPref(activity)

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

                    dismiss()
                }
            }
        }

        override fun dismiss() {
            super.dismiss()
            dismissCallback.invoke(securitySharedPref.screenLockTimeoutString())
        }
    }
}