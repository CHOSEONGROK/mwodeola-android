package com.jojo.android.mwodeola.presentation.settings

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.autofill.AutofillManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.fragment.findNavController
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.jojo.android.mwodeola.R
import com.jojo.android.mwodeola.autofill.AutofillHelper

class SettingsFragment : PreferenceFragmentCompat(), SettingsSupportFragment {

    override val toolBarTitle: String = "설정"

    private val launcherForAutofill = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        findPreference<SwitchPreferenceCompat>("autofill_service")?.isChecked = (it.resultCode == RESULT_OK)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings_fragment_prefs, rootKey)

        findPreference<Preference>("user_info")?.setOnPreferenceClickListener {
            findNavController().navigate(R.id.action_settingsFragment_to_settingsUserInfoFragment)
            true
        }

        findPreference<Preference>("auth_and_security")?.setOnPreferenceClickListener {
            findNavController().navigate(R.id.action_settingsFragment_to_settingsAuthAndSecurityFragment)
            true
        }

        if (Build.VERSION.SDK_INT >= 26) {
            findPreference<SwitchPreferenceCompat>("autofill_service")?.setOnPreferenceChangeListener { _, _ ->
                requestAutofillServiceSettings()
                false
            }
        }
    }

    override fun onResume() {
        super.onResume()
        checkEnabledAutofillService()
    }

    private fun requestAutofillServiceSettings() {
        if (Build.VERSION.SDK_INT >= 26) {
            val activity = requireActivity()
            val intent = Intent(Settings.ACTION_REQUEST_SET_AUTOFILL_SERVICE)
            if (AutofillHelper.hasEnabledAutofillServices(activity).not()) {
                intent.data = Uri.parse("package:" + activity.packageName)
            } else {
                intent.data = Uri.parse("package:")
            }
            launcherForAutofill.launch(intent)
        }
    }

    private fun checkEnabledAutofillService() {
        if (Build.VERSION.SDK_INT >= 26) {
            val autofillManager = requireActivity().getSystemService(AutofillManager::class.java)
            val isSupported = autofillManager.isAutofillSupported
            val isEnabled = autofillManager.hasEnabledAutofillServices()

            findPreference<SwitchPreferenceCompat>("autofill_service")?.let {
                it.isVisible = isSupported
                it.isChecked = isEnabled
            }
        } else {
            findPreference<SwitchPreferenceCompat>("autofill_service")?.isVisible = false
        }
    }
}