package com.jojo.android.mwodeola.presentation.security

import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.biometric.BiometricManager
import androidx.fragment.app.FragmentActivity
import com.jojo.android.mwodeola.presentation.common.BottomUpDialog
import com.jojo.android.mwodeola.presentation.security.bottmSheet.AuthenticationPatternBottomSheet
import com.jojo.android.mwodeola.presentation.security.bottmSheet.AuthenticationPin5BottomSheet
import com.jojo.android.mwodeola.presentation.security.bottmSheet.AuthenticationPin6BottomSheet
import com.jojo.android.mwodeola.presentation.security.screen.BaseAuthenticationActivity.Companion.EXTRA_PURPOSE
import com.jojo.android.mwodeola.presentation.security.screen.BaseAuthenticationActivity.Companion.EXTRA_USER_EMAIL
import com.jojo.android.mwodeola.presentation.security.screen.BaseAuthenticationActivity.Companion.EXTRA_USER_NAME
import com.jojo.android.mwodeola.presentation.security.screen.BaseAuthenticationActivity.Companion.EXTRA_USER_PHONE_NUMBER
import com.jojo.android.mwodeola.presentation.security.screen.BaseAuthenticationActivity.Companion.PURPOSE_AUTH
import com.jojo.android.mwodeola.presentation.security.screen.BaseAuthenticationActivity.Companion.PURPOSE_CREATE
import com.jojo.android.mwodeola.presentation.security.screen.BaseAuthenticationActivity.Companion.PURPOSE_SCREEN_LOCK
import com.jojo.android.mwodeola.presentation.security.screen.BaseAuthenticationActivity.Companion.PURPOSE_SIGN_IN
import com.jojo.android.mwodeola.presentation.security.screen.BaseAuthenticationActivity.Companion.PURPOSE_SIGN_UP
import com.jojo.android.mwodeola.presentation.security.screen.pattern.PatternAuthenticationActivity
import com.jojo.android.mwodeola.presentation.security.screen.pin5.Pin5AuthenticationActivity
import com.jojo.android.mwodeola.presentation.security.screen.pin6.Pin6AuthenticationActivity
import com.jojo.android.mwodeola.util.Log2


object Authenticators {

    private const val TAG = "Authenticators"

    abstract class AuthenticationCallback {
        abstract fun onSucceed()
        abstract fun onFailure()
        abstract fun onExceedAuthLimit(limit: Int)
        open fun onDeviceHasNotBiometricError(errString: String) {
            Log.w(TAG, "onDeviceHasNotBiometricError(): $errString")
        }
        open fun onDeviceHasNewBiometricEnrolledError() {
            Log.w(TAG, "onDeviceHasNotBiometricError()")
        }
        open fun onBiometricError(errCode: Int, errString: String) {
            Log.w(TAG, "onBiometricError(errCode=$errCode): $errString")
        }
    }

    class BottomSheetBuilder(private val activity: FragmentActivity) {
        private var forAutofillService: Boolean = false
        private var callback: AuthenticationCallback? = null

        fun callback(callback: AuthenticationCallback) = apply {
            this.callback = callback
        }

        fun autofillService() = apply {
            forAutofillService = true
        }

        fun execute() {
            val securityManager = SecurityManager.SharedPref(activity)
            var authType = securityManager.authType()
            if (!securityManager.isExistsPassword(authType)) {
                callback?.onFailure()
                return
            }

            if (authType == AuthType.BIOMETRIC) {
                if (BiometricHelper.isAuthentication && BiometricHelper.hasNewBiometricEnrolled) {
                    authType = AuthType.PIN_5
                } else if (BiometricHelper.isAuthentication.not()) {
                    authType = AuthType.PIN_5
                }
            }

            if (forAutofillService && authType == AuthType.PATTERN) {
                // AutofillAuthActivity 에서 View 를 Inflate 할 때 InflateException 문제 발생
                // 추측: Material 관련 뷰만 들어가면 해당 이슈 발생함.
                authType = AuthType.PIN_5
            }

            when (authType) {
                AuthType.PIN_5 ->
                    AuthenticationPin5BottomSheet(activity, callback, forAutofillService)
                        .show()
                AuthType.PIN_6 ->
                    AuthenticationPin6BottomSheet(activity, callback, forAutofillService)
                        .show()
                AuthType.PATTERN ->
                    AuthenticationPatternBottomSheet(activity, callback, forAutofillService)
                        .show()
                AuthType.BIOMETRIC ->
                    if (callback != null) {
                        BiometricHelper.Authenticator(activity, callback!!)
                            .appCredential()
                            .execute()
                    }
            }
        }
    }

    class ScreenBuilder(private val activity: FragmentActivity) {
        private var purpose = PURPOSE_AUTH
        private var authType: AuthType? = null
        private var biometricCallback: AuthenticationCallback? = null
        private var launcher: ActivityResultLauncher<Intent>? = null
        private var hasNewBiometricEnrolled = false

        private var name: String? = null
        private var email: String? = null
        private var phoneNumber: String? = null

        init {
            if (BiometricHelper.isAuthentication && BiometricHelper.hasNewBiometricEnrolled) {
                hasNewBiometricEnrolled = BiometricHelper.hasNewBiometricEnrolled
            }
        }

        fun purpose(purpose: Int) = apply {
            this.purpose = purpose
        }

        fun authType(type: AuthType) = apply {
            this.authType = type
        }

        fun signUp(name: String, email: String, phoneNumber: String) = apply {
            this.purpose = PURPOSE_SIGN_UP
            this.name = name
            this.email = email
            this.phoneNumber = phoneNumber
        }

        fun signIn(phoneNumber: String) = apply {
            this.purpose = PURPOSE_SIGN_IN
            this.phoneNumber = phoneNumber
        }

        fun biometricCallback(callback: AuthenticationCallback) = apply {
            biometricCallback = callback
        }

        fun launcher(launcher: ActivityResultLauncher<Intent>) = apply {
            this.launcher = launcher
        }

        fun execute() {
            when (purpose) {
                PURPOSE_SIGN_UP -> {
                    launcher?.launch(Intent(activity, Pin5AuthenticationActivity::class.java).apply {
                        putExtra(EXTRA_PURPOSE, PURPOSE_SIGN_UP)
                        putExtra(EXTRA_USER_NAME, name)
                        putExtra(EXTRA_USER_EMAIL, email)
                        putExtra(EXTRA_USER_PHONE_NUMBER, phoneNumber)
                    })
                    return
                }
                PURPOSE_SIGN_IN -> {
                    launcher?.launch(Intent(activity, Pin5AuthenticationActivity::class.java).apply {
                        putExtra(EXTRA_PURPOSE, PURPOSE_SIGN_IN)
                        putExtra(EXTRA_USER_PHONE_NUMBER, phoneNumber)
                    })
                    return
                }
            }

            if (authType == AuthType.BIOMETRIC && purpose == PURPOSE_SCREEN_LOCK) {
                if (BiometricHelper.canAuthentication == BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED ||
                    BiometricHelper.hasNewBiometricEnrolled) {
                    // 생체 인증 정보가 변경되었을 경우
                    authType = AuthType.PIN_5
                }
            }

            when (authType) {
                AuthType.PIN_5 ->
                    launchActivity(Pin5AuthenticationActivity::class.java)
                AuthType.PIN_6 ->
                    launchActivity(Pin6AuthenticationActivity::class.java)
                AuthType.PATTERN ->
                    launchActivity(PatternAuthenticationActivity::class.java)
                AuthType.BIOMETRIC ->
                    if (biometricCallback != null) {
                        BiometricHelper.Authenticator(activity, biometricCallback!!)
                            .appCredential()
                            .execute()
                    }
                else -> {}
            }
        }

        private fun launchActivity(cls: Class<*>) {
            launcher?.launch(Intent(activity, cls).apply {
                putExtra(EXTRA_PURPOSE, purpose)
            })
        }
    }

    class DeviceCredentialBuilder(private val activity: FragmentActivity) {
        private var callback: AuthenticationCallback? = null

        fun callback(callback: AuthenticationCallback) = apply {
            this.callback = callback
        }

        fun execute() {
            val sharedPref = SecurityManager.SharedPref(activity)

            if (BiometricHelper.isAuthentication.not() ||
                sharedPref.isScreenLockEnabled().not() ||
                callback == null)
                return

            if (sharedPref.isExistsPassword(AuthType.BIOMETRIC)) {
                if ((BiometricHelper.isAuthentication xor BiometricHelper.hasNewBiometricEnrolled).not()) {
                    // 기존 앱에 지문 등록이 되어 있으나, 디바이스에서 지문 정보가 변경된 경우
                    // (True and True) or (False and False)
                    BottomUpDialog.Builder(activity.supportFragmentManager)
                        .title("생체 인증 정보 변경됨")
                        .subtitle("휴대폰의 생체 인증 정보가 변경되어 앱에 등록되었던 지문 인증은 사라집니다.")
                        .confirmedButton {
                            sharedPref.authType(AuthType.PIN_5)
                            sharedPref.deletePassword(AuthType.BIOMETRIC)
                            Handler(Looper.getMainLooper()).postDelayed({
                                BiometricHelper.Authenticator(activity, callback!!)
                                    .deviceCredential()
                                    .execute()
                            }, 200)
                        }
                        .show()
                } else {
                    BiometricHelper.Authenticator(activity, callback!!)
                        .deviceCredential()
                        .execute()
                }
            }
        }
    }
}