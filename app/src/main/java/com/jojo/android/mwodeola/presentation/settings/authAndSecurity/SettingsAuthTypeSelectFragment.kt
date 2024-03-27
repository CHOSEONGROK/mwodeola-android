package com.jojo.android.mwodeola.presentation.settings.authAndSecurity

import android.os.Bundle
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.jojo.android.mwodeola.R
import com.jojo.android.mwodeola.presentation.security.AuthType
import com.jojo.android.mwodeola.presentation.security.Authenticators
import com.jojo.android.mwodeola.presentation.security.SecurityManager
import com.jojo.android.mwodeola.presentation.security.screen.BaseAuthenticationActivity
import com.jojo.android.mwodeola.presentation.settings.SettingsSupportFragment
import com.jojo.android.mwodeola.presentation.settings.custom.RadioButtonPreference
import com.jojo.android.mwodeola.presentation.settings.custom.RadioGroupPreference
import com.jojo.android.mwodeola.util.Log2

class SettingsAuthTypeSelectFragment : PreferenceFragmentCompat(), SettingsSupportFragment {

    override val toolBarTitle: String = "비밀번호 인증 방식"

    private val securitySharedPref by lazy { SecurityManager.SharedPref(requireContext()) }

    private val biometricCallback = BiometricAuthenticationCallback()
    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult(), ScreenAuthenticatorCallback())

    private val summaryOnColor by lazy { ResourcesCompat.getColor(resources, R.color.app_theme_color, null) }
    private val summaryOffColor by lazy { ResourcesCompat.getColor(resources, R.color.gray500, null) }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings_auth_type_select_fragment_prefs, rootKey)

        // 비밀번호 인증 방식 RadioGroup
        findPreference<RadioGroupPreference>("radio_group")?.let {
            it.checkToggle(securitySharedPref.authType().name)

            it.setToggleWatcher(object : RadioGroupPreference.ToggleWatcher() {
                override fun onToggleClicked(toggle: RadioButtonPreference): Boolean {
                    val authType = AuthType.valueOf(toggle.key)
                    val isExistsPassword = securitySharedPref.isExistsPassword(authType)

                    if (isExistsPassword.not()) {
                        executeAuthenticator(authType, BaseAuthenticationActivity.PURPOSE_CREATE)
                    }
                    return isExistsPassword.not()
                }

                override fun onToggleChanged(toggle: RadioButtonPreference, isChecked: Boolean) {
                    val newAuthType = AuthType.valueOf(toggle.key)

                    securitySharedPref.authType(newAuthType)
                    updateModifyCategory(newAuthType)
                }
            })
        }

        // 변경하기
        findPreference<Preference>("change")?.setOnPreferenceClickListener {
            executeAuthenticator(securitySharedPref.authType(), BaseAuthenticationActivity.PURPOSE_CHANGE)
            false
        }

        // 삭제하기
        findPreference<Preference>("delete")?.setOnPreferenceClickListener {
            executeAuthenticator(securitySharedPref.authType(), BaseAuthenticationActivity.PURPOSE_DELETE)
            false
        }

        updateSummary(AuthType.PIN_5, securitySharedPref.isExistsPassword(AuthType.PIN_5))
        updateSummary(AuthType.PIN_6, securitySharedPref.isExistsPassword(AuthType.PIN_6))
        updateSummary(AuthType.PATTERN, securitySharedPref.isExistsPassword(AuthType.PATTERN))
        updateSummary(AuthType.BIOMETRIC, securitySharedPref.isExistsPassword(AuthType.BIOMETRIC))

        updateModifyCategory(securitySharedPref.authType())
    }

    fun checkToggle(authType: AuthType) {
        findPreference<RadioGroupPreference>("radio_group")
            ?.checkToggle(authType.name)
    }

    fun updateSummary(authType: AuthType, isExistsPassword: Boolean) {
        val summary = when {
            authType == AuthType.PIN_5 -> "등록됨, 마스터 비밀번호"
            isExistsPassword -> "등록됨"
            else -> "등록 안 됨"
        }
        val summaryColor =
            if (isExistsPassword) summaryOnColor
            else summaryOffColor

        findPreference<RadioButtonPreference>(authType.name)?.let {
            it.summary = summary
            it.setSummaryColor(summaryColor)
        }
    }

    fun updateModifyCategory(authType: AuthType) {
        findPreference<Preference>("change")?.isEnabled = (authType != AuthType.BIOMETRIC)
        findPreference<Preference>("delete")?.isEnabled = (authType != AuthType.PIN_5)
    }

    fun executeAuthenticator(authType: AuthType, purpose: Int) {
        if (authType == AuthType.BIOMETRIC) {
            biometricCallback.setPurpose(purpose)
        }

        Authenticators.ScreenBuilder(requireActivity())
            .purpose(purpose)
            .authType(authType)
            .biometricCallback(biometricCallback)
            .launcher(launcher)
            .execute()
    }

    inner class ScreenAuthenticatorCallback : ActivityResultCallback<ActivityResult> {
        private val constants = BaseAuthenticationActivity

        override fun onActivityResult(result: ActivityResult) {
            if (result.resultCode == AppCompatActivity.RESULT_CANCELED)
                return

            val resultData = result.data
                ?: return

            val resultCode = resultData.getIntExtra(constants.EXTRA_RESULT, -1)
            val purpose = resultData.getIntExtra(constants.EXTRA_PURPOSE, -1)
            val authType = resultData.getSerializableExtra(constants.EXTRA_AUTH_TYPE) as AuthType

            if (result.resultCode == AppCompatActivity.RESULT_OK &&
                resultCode == constants.SUCCEED &&
                purpose != -1
            ) {
                val currentAuthType = securitySharedPref.authType()

                when (purpose) {
                    constants.PURPOSE_CREATE,
                    constants.PURPOSE_CHANGE -> {
                        checkToggle(currentAuthType)
                        updateSummary(currentAuthType, true)
                    }
                    constants.PURPOSE_DELETE -> {
                        checkToggle(currentAuthType)
                        updateSummary(currentAuthType, true)
                        updateSummary(authType, false)
                    }
                }

                updateModifyCategory(currentAuthType)
            } else if (resultCode == constants.EXCEEDED_AUTH_LIMIT) {
                Log2.e("ActivityResult: EXCEEDED_AUTH_LIMIT")
            }
        }
    }

    inner class BiometricAuthenticationCallback : Authenticators.AuthenticationCallback() {
        private val constants = BaseAuthenticationActivity
        private var purpose: Int = constants.PURPOSE_CREATE

        override fun onSucceed() {
            when (purpose) {
                constants.PURPOSE_CREATE -> {
                    securitySharedPref.authType(AuthType.BIOMETRIC)
                    securitySharedPref.registerBiometric()

                    checkToggle(AuthType.BIOMETRIC)
                    updateSummary(AuthType.BIOMETRIC, true)

                    updateModifyCategory(AuthType.BIOMETRIC)
                }
                constants.PURPOSE_CHANGE -> {}
                constants.PURPOSE_DELETE -> {
                    securitySharedPref.authType(AuthType.PIN_5)
                    checkToggle(AuthType.PIN_5)
                    updateSummary(AuthType.PIN_5, true)

                    securitySharedPref.deletePassword(AuthType.BIOMETRIC)
                    updateSummary(AuthType.BIOMETRIC, false)

                    updateModifyCategory(AuthType.PIN_5)
                }
            }
        }

        override fun onFailure() {

        }

        override fun onExceedAuthLimit(limit: Int) {

        }

        fun setPurpose(purpose: Int) {
            this.purpose = purpose
        }
    }
}