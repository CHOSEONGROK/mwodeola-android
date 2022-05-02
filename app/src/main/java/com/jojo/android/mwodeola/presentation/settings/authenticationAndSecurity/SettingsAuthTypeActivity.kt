package com.jojo.android.mwodeola.presentation.settings.authenticationAndSecurity

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.BiometricManager
import androidx.constraintlayout.widget.ConstraintLayout
import com.jojo.android.mwodeola.R
import com.jojo.android.mwodeola.databinding.ActivitySettingsAuthTypeBinding
import com.jojo.android.mwodeola.presentation.BaseActivity
import com.jojo.android.mwodeola.presentation.common.BottomUpDialog
import com.jojo.android.mwodeola.presentation.security.AuthType
import com.jojo.android.mwodeola.presentation.security.Authenticators
import com.jojo.android.mwodeola.presentation.security.BiometricHelper
import com.jojo.android.mwodeola.presentation.security.SecurityManager
import com.jojo.android.mwodeola.presentation.security.screen.BaseAuthenticationActivity.Companion.EXCEEDED_AUTH_LIMIT
import com.jojo.android.mwodeola.presentation.security.screen.BaseAuthenticationActivity.Companion.EXTRA_AUTH_TYPE
import com.jojo.android.mwodeola.presentation.security.screen.BaseAuthenticationActivity.Companion.EXTRA_PURPOSE
import com.jojo.android.mwodeola.presentation.security.screen.BaseAuthenticationActivity.Companion.EXTRA_RESULT
import com.jojo.android.mwodeola.presentation.security.screen.BaseAuthenticationActivity.Companion.PURPOSE_CHANGE
import com.jojo.android.mwodeola.presentation.security.screen.BaseAuthenticationActivity.Companion.PURPOSE_CREATE
import com.jojo.android.mwodeola.presentation.security.screen.BaseAuthenticationActivity.Companion.PURPOSE_DELETE
import com.jojo.android.mwodeola.presentation.security.screen.BaseAuthenticationActivity.Companion.SUCCEED


class SettingsAuthTypeActivity : BaseActivity(), View.OnClickListener {

    companion object {
        private const val TAG = "SettingsAuthTypeActivity"
    }

    override val isScreenLockEnabled: Boolean = true
    override val binding by lazy { ActivitySettingsAuthTypeBinding.inflate(layoutInflater) }

    private val sharedPref by lazy { SecurityManager.SharedPref(this) }

    private lateinit var dataBinder: Binder

    private val biometricCallback = BiometricAuthenticationCallback()

    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        Log.d(TAG, "ActivityResult: ${it.resultCode}")

        val resultData = it.data ?: return@registerForActivityResult

        val resultCode = resultData.getIntExtra(EXTRA_RESULT, -1)
        val purpose = resultData.getIntExtra(EXTRA_PURPOSE, -1)
        val authType = resultData.getSerializableExtra(EXTRA_AUTH_TYPE) as AuthType

        if (it.resultCode == RESULT_OK && resultCode == SUCCEED && purpose != -1) {
            Log.d(TAG, "ActivityResult: RESULT_OK")

            val currentAuthType = sharedPref.authType()

            when (purpose) {
                PURPOSE_CREATE,
                PURPOSE_CHANGE -> {
                    dataBinder.checkToggle(currentAuthType)
                    dataBinder.updateSubtitle(currentAuthType, true)
                    updateButtonEnabled(true)
                }
                PURPOSE_DELETE -> {
                    dataBinder.checkToggle(currentAuthType)
                    dataBinder.updateSubtitle(currentAuthType, true)
                    dataBinder.updateSubtitle(authType, false)
                    updateButtonEnabled(false)
                }
            }
        } else if (resultCode == EXCEEDED_AUTH_LIMIT) {
            Log.e(TAG, "ActivityResult: EXCEEDED_AUTH_LIMIT")
        } else if (it.resultCode == RESULT_CANCELED) {
            Log.i(TAG, "ActivityResult: RESULT_CANCELED")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        with(binding) {
            btnBack.setOnClickListener { onBackPressed() }

            val dataBindings = mutableListOf<DataBinding>()

            dataBinder = Binder(baseContext, dataBindings)

            dataBindings.add(
                DataBinding(AuthType.PIN_5, containerPin5, tvTitlePin5, tvSubtitlePin5, radioButtonPin5))
            dataBindings.add(
                DataBinding(AuthType.PIN_6, containerPin6, tvTitlePin6, tvSubtitlePin6, radioButtonPin6))
            dataBindings.add(
                DataBinding(AuthType.PATTERN, containerPattern, tvTitlePattern, tvSubtitlePattern, radioButtonPattern))

            if (BiometricHelper.canAuthentication == BiometricManager.BIOMETRIC_SUCCESS ||
                BiometricHelper.canAuthentication == BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED ||
                BiometricHelper.canAuthentication == BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED
            ) {
                dataBindings.add(
                    DataBinding(AuthType.BIOMETRIC, containerBiometric, tvTitleBiometric, tvSubtitleBiometric, radioButtonBiometric))
            } else {
                containerBiometric.visibility = View.GONE
            }

            dataBindings.forEach {
                it.container.setOnClickListener(this@SettingsAuthTypeActivity)

                if (it.authType == sharedPref.authType()) {
                    it.toggle.isChecked = true

                    val enabled = sharedPref.authType() != AuthType.PIN_5
                    updateButtonEnabled(enabled)
                }

                dataBinder.updateSubtitle(it.authType, sharedPref.isExistsPassword(it.authType))
            }

            btnUpdate.setOnClickListener {
                val checkedAuthType = dataBinder.checkedAuthType
                if (checkedAuthType == AuthType.BIOMETRIC) {
                    biometricCallback.setPurpose(PURPOSE_CHANGE)
                }

                Authenticators.ScreenBuilder(this@SettingsAuthTypeActivity)
                    .purpose(PURPOSE_CHANGE)
                    .authType(checkedAuthType)
                    .biometricCallback(biometricCallback)
                    .launcher(launcher)
                    .execute()
            }

            btnDelete.setOnClickListener {
                val checkedAuthType = dataBinder.checkedAuthType
                if (checkedAuthType == AuthType.BIOMETRIC) {
                    biometricCallback.setPurpose(PURPOSE_DELETE)
                }

                Authenticators.ScreenBuilder(this@SettingsAuthTypeActivity)
                    .purpose(PURPOSE_DELETE)
                    .authType(checkedAuthType)
                    .biometricCallback(biometricCallback)
                    .launcher(launcher)
                    .execute()
            }
        }

    }

    override fun onClick(view: View) {
        val authType = dataBinder.list.find { it.container == view }?.authType
            ?: return

        if (authType == sharedPref.authType())
            return

        if (sharedPref.isExistsPassword(authType)) {
            sharedPref.authType(authType)
            dataBinder.checkToggle(authType)

            if (authType == AuthType.PIN_5) {
                updateButtonEnabled(false)
            } else {
                updateButtonEnabled(true)
            }
        } else {
            if (authType == AuthType.BIOMETRIC) {
                when (BiometricHelper.canAuthentication) {
                    BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                        showToast("먼저 휴대폰에 지문 등록을 하셔야 합니다")
                        return
                    }
                    BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> {
                        showToast("휴대폰의 보안 업데이트가 필요합니다")
                        return
                    }
                }

                biometricCallback.setPurpose(PURPOSE_CREATE)
            }

            Authenticators.ScreenBuilder(this@SettingsAuthTypeActivity)
                .purpose(PURPOSE_CREATE)
                .authType(authType)
                .biometricCallback(biometricCallback)
                .launcher(launcher)
                .execute()
        }
    }

    fun updateButtonEnabled(enabled: Boolean) {
        binding.btnUpdate.isEnabled = enabled
        binding.btnDelete.isEnabled = enabled
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    class DataBinding(
        val authType: AuthType,
        val container: ConstraintLayout,
        val tvTitle: TextView,
        val tvSubtitle: TextView,
        val toggle: RadioButton
    )

    class Binder(private val context: Context, val list: List<DataBinding>) {
        private val colorBlue = context.resources.getColor(R.color.blue600, null)
        private val colorGray = context.resources.getColor(R.color.gray500, null)

        val checkedAuthType: AuthType
            get() = list.find { it.toggle.isChecked }?.authType ?: AuthType.PIN_5

        fun checkToggle(type: AuthType) {
            list.filter { it.authType == type }
                .forEach { it.toggle.isChecked = true }
            list.filter { it.authType != type }
                .forEach { it.toggle.isChecked = false }
        }

        fun updateSubtitle(type: AuthType, isExists: Boolean) {
            val subtitle =
                if (type == AuthType.PIN_5) "등록됨, 마스터 비밀번호"
                else if (isExists) "등록됨"
                else "등록 안 됨"

            val subtitleColor =
                if (isExists) colorBlue
                else colorGray

            list.find { it.authType == type }
                ?.let {
                    it.tvSubtitle.text = subtitle
                    it.tvSubtitle.setTextColor(subtitleColor)
                }
        }
    }

    inner class BiometricAuthenticationCallback : Authenticators.AuthenticationCallback() {
        private var purpose: Int = PURPOSE_CREATE

        override fun onSucceed() {
            when (purpose) {
                PURPOSE_CREATE -> {
                    sharedPref.authType(AuthType.BIOMETRIC)
                    sharedPref.registerBiometric()

                    dataBinder.checkToggle(AuthType.BIOMETRIC)
                    dataBinder.updateSubtitle(AuthType.BIOMETRIC, true)
                    updateButtonEnabled(true)
                }
                PURPOSE_CHANGE -> {}
                PURPOSE_DELETE -> {
                    sharedPref.authType(AuthType.PIN_5)
                    dataBinder.checkToggle(AuthType.PIN_5)
                    dataBinder.updateSubtitle(AuthType.PIN_5, true)

                    sharedPref.deletePassword(AuthType.BIOMETRIC)
                    dataBinder.updateSubtitle(AuthType.BIOMETRIC, false)

                    updateButtonEnabled(false)
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