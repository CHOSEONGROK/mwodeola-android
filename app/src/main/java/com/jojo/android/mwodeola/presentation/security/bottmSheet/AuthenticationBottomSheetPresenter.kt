package com.jojo.android.mwodeola.presentation.security.bottmSheet

import com.google.android.material.bottomsheet.BottomSheetDialog
import com.jojo.android.mwodeola.model.sign.SignUpSource
import com.jojo.android.mwodeola.model.token.TokenSharedPref
import com.jojo.android.mwodeola.presentation.security.AuthType
import com.jojo.android.mwodeola.presentation.security.BiometricHelper
import com.jojo.android.mwodeola.presentation.security.SecurityManager


class AuthenticationBottomSheetPresenter(
    private val authType: AuthType,
    private val view: AuthenticationBottomSheetContract.View,
    private val sharedPref: SecurityManager.SharedPref,
    private val signUpRepository: SignUpSource
) : AuthenticationBottomSheetContract.Presenter {

    override var hasNewBiometricEnrolled: Boolean = false

    override var authFailureCount: Int = 0
    override var authFailureLimit: Int = 10

    override fun loadAuthFailureCount() {
        if (authType == AuthType.PIN_5)
            return

        signUpRepository.getAuthFailureCount(object : SignUpSource.AuthFailureCountCallback() {
            override fun onSucceed(authFailureCount: Int, limit: Int) {
                this@AuthenticationBottomSheetPresenter.authFailureCount = authFailureCount
                this@AuthenticationBottomSheetPresenter.authFailureLimit = limit
            }
        })
    }

    override fun checkBiometricEnroll() {
        if (sharedPref.isExistsPassword(AuthType.BIOMETRIC)) {
            if (BiometricHelper.isAuthentication && BiometricHelper.hasNewBiometricEnrolled) {
                // 새로운 생체 인증 정보가 추가된 경우
                if (sharedPref.authType() == AuthType.BIOMETRIC) {
                    sharedPref.authType(this.authType)
                    view.showBiometricConfirmButton()
                }
                sharedPref.deletePassword(AuthType.BIOMETRIC)
                view.showBiometricEnrollChangedDialog()
            } else if (
                !BiometricHelper.isAuthentication && !BiometricHelper.hasNewBiometricEnrolled
            ) { // 생체 인증 정보가 모두 삭제된 경우
                sharedPref.authType(this.authType)
                sharedPref.deletePassword(AuthType.BIOMETRIC)
                view.showBiometricEnrollChangedDialog()
            }
        }
    }

    override fun authenticate(password: String,
                              callback: AuthenticationBottomSheetContract.AuthenticationCallback) {
        if (authType == AuthType.PIN_5) {
            authenticateRemote(password, callback)
        } else {
            authenticateLocal(password, callback)
        }
    }

    override fun changeAuthTypeToBiometric() {
        BiometricHelper.generateSecretKey()
        sharedPref.authType(AuthType.BIOMETRIC)
        sharedPref.registerBiometric()
    }

    private fun authenticateRemote(password: String,
                                   callback: AuthenticationBottomSheetContract.AuthenticationCallback) {
        signUpRepository.authPassword(password, object : SignUpSource.AuthPasswordCallback() {
            override fun onSucceed() {
                callback.onSucceed()
                view.dismissForSucceed()
            }

            override fun onIncorrectPassword(count: Int, limit: Int, isExceed: Boolean) {
                callback.onIncorrectPassword(count, limit, isExceed)

                if (isExceed) {
                    sharedPref.clearAll()
                    TokenSharedPref.removeToken((view as BottomSheetDialog).context)
                    view.showExceedAuthLimitDialog()
                }
            }

            override fun onLockedUser() {
                view.showLockedUserDialog()
            }
        })
    }

    private fun authenticateLocal(password: String,
                                  callback: AuthenticationBottomSheetContract.AuthenticationCallback) {
        val isValid = when (authType) {
            AuthType.PIN_6 ->
                sharedPref.signaturePin6(password)
            AuthType.PATTERN ->
                sharedPref.signaturePattern(password)
            else -> false
        }

        if (isValid) {
            callback.onSucceed()
            view.dismissForSucceed()
            signUpRepository.postAuthFailureCount(0)
        } else {
            val isExceed = (authFailureCount >= authFailureLimit)

            callback.onIncorrectPassword(++authFailureCount, authFailureLimit, isExceed)

            signUpRepository.postAuthFailureCount(authFailureCount)

            if (isExceed) {
                sharedPref.clearAll()
                TokenSharedPref.removeToken((view as BottomSheetDialog).context)
                view.showExceedAuthLimitDialog()
            }
        }
    }
}