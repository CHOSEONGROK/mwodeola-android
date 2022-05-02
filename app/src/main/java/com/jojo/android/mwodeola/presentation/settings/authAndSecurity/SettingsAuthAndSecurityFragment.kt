package com.jojo.android.mwodeola.presentation.settings.authAndSecurity

import android.os.Bundle
import androidx.navigation.fragment.findNavController
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.jojo.android.mwodeola.R
import com.jojo.android.mwodeola.presentation.security.Authenticators
import com.jojo.android.mwodeola.presentation.security.SecurityManager
import com.jojo.android.mwodeola.presentation.settings.SettingsSupportFragment
import com.jojo.android.mwodeola.presentation.settings.custom.ColorSwitchPreferenceCompat

class SettingsAuthAndSecurityFragment : PreferenceFragmentCompat(), SettingsSupportFragment {

    override val toolBarTitle: String = "인증 및 보안"

    private val securitySharedPref by lazy { SecurityManager.SharedPref(requireContext()) }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings_auth_and_security_fragment_prefs, rootKey)

        findPreference<Preference>("authentication")?.setOnPreferenceClickListener { preference ->
            Authenticators.BottomSheetBuilder(requireActivity())
                .callback(MyAuthenticationListener(preference.key))
                .execute()
            false
        }
        findPreference<ColorSwitchPreferenceCompat>("screen_lock")?.setOnPreferenceChangeListener { preference, _ ->
            Authenticators.BottomSheetBuilder(requireActivity())
                .callback(MyAuthenticationListener(preference.key))
                .execute()
            false
        }
    }

    override fun onResume() {
        super.onResume()
        findPreference<Preference>("authentication")?.summary = securitySharedPref.authType().name
        findPreference<ColorSwitchPreferenceCompat>("screen_lock")?.let {
            val isScreenLockEnabled = securitySharedPref.isScreenLockEnabled()

            if (isScreenLockEnabled) {
                it.summaryOn = StringBuilder("모든 화면에서").apply {
                    when (securitySharedPref.screenLockCredential()) {
                        SecurityManager.SharedPref.APP_CREDENTIAL -> append(", 앱 잠금 방식")
                        SecurityManager.SharedPref.DEVICE_CREDENTIAL -> append(", 휴대폰 잠금 방식")
                    }
                    append(", ${securitySharedPref.screenLockTimeoutString()}")
                }.toString()
            }

            it.isChecked = isScreenLockEnabled
        }
    }

    inner class MyAuthenticationListener(private val key: String) : Authenticators.AuthenticationCallback() {

        override fun onSucceed() {
            val actionId = when (key) {
                "authentication" -> R.id.action_settingsAuthAndSecurityFragment_to_settingsAuthTypeSelectFragment
                "screen_lock" -> R.id.action_settingsAuthAndSecurityFragment_to_settingsScreenLockTypeSelectFragment
                else -> return
            }
            findNavController().navigate(actionId)
        }

        override fun onFailure() {

        }

        override fun onExceedAuthLimit(limit: Int) {

        }
    }
}