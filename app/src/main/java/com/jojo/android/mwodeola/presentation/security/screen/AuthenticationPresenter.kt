package com.jojo.android.mwodeola.presentation.security.screen

import android.content.Context
import android.util.Log
import com.jojo.android.mwodeola.model.sign.SignUpSource
import com.jojo.android.mwodeola.model.token.TokenSharedPref
import com.jojo.android.mwodeola.presentation.security.AuthType
import com.jojo.android.mwodeola.presentation.security.BiometricHelper
import com.jojo.android.mwodeola.presentation.security.SecurityManager

class AuthenticationPresenter constructor(
    val view: AuthenticationContract.View,
    val authType: AuthType,
    val sharedPref: SecurityManager.SharedPref,
    val signUpRepository: SignUpSource
) : AuthenticationContract.Presenter {

    override var userName: String? = null
    override var userEmail: String? = null
    override var userPhoneNumber: String? = null

    override var authFailureCount: Int = 0
    override var authFailureLimit: Int = 10

    override var oldPasswordPin5: String = ""
    override var newPassword: String = ""

    override fun loadAuthFailureCount() {
        if (authType == AuthType.PIN_5)
            return

        signUpRepository.getAuthFailureCount(object : SignUpSource.AuthFailureCountCallback() {
            override fun onSucceed(authFailureCount: Int, limit: Int) {
                this@AuthenticationPresenter.authFailureCount = authFailureCount
                this@AuthenticationPresenter.authFailureLimit = limit
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
                BiometricHelper.isAuthentication.not() && BiometricHelper.hasNewBiometricEnrolled.not()
            ) { // 생체 인증 정보가 모두 삭제된 경우
                sharedPref.authType(this.authType)
                sharedPref.deletePassword(AuthType.BIOMETRIC)
                view.showBiometricEnrollChangedDialog()
            }
        }
    }

    override fun changeAuthTypeToBiometric() {
        BiometricHelper.generateSecretKey()
        sharedPref.authType(AuthType.BIOMETRIC)
        sharedPref.registerBiometric()
    }

    override fun signUp(password: String) {
        if (userName == null || userEmail == null || userPhoneNumber == null)
            return

        signUpRepository.signUp(userName!!, userEmail!!, userPhoneNumber!!, password, object : SignUpSource.BaseCallback() {
            override fun onSucceed() {
                view.showCompletelySignUpDialog()
            }

            override fun onFailure() {
                view.finishForFailure()
            }

            override fun onUnknownError(errString: String?) {
                super.onUnknownError(errString)
                view.finishForFailure()
            }
        })
    }

    override fun signIn(password: String, callback: AuthenticationContract.AuthenticationCallback) {
        if (userPhoneNumber == null)
            return

        signUpRepository.signIn(userPhoneNumber!!, password, object : SignUpSource.SignInCallback() {
            override fun onSucceed() {
                view.finishForSucceed()
            }

            override fun onIncorrectPassword(count: Int, limit: Int, isExceed: Boolean) {
                callback.onIncorrectPassword(count, limit, isExceed)

                if (isExceed) {
                    sharedPref.clearAll()
                    TokenSharedPref.removeToken(view as Context)
                    view.showAuthenticationExceededDialog()
                }
            }

            override fun onLockedUser() {
                view.showLockedUserDialog()
            }

            override fun onDormantUser() {
                view.showDormantUserDialog()
            }

            override fun onNotFoundUser() {
                view.showNotFoundUserDialog()
            }
        })
    }

    override fun createPassword() {
        when (authType) {
            AuthType.PIN_6 -> {
                sharedPref.passwordPin6(newPassword)
                sharedPref.authType(authType)
            }
            AuthType.PATTERN -> {
                sharedPref.passwordPattern(newPassword)
                sharedPref.authType(authType)
            }
            else -> {}
        }

        view.showCompletelyCreationDialog()
    }

    override fun changePassword() {
        when (authType) {
            AuthType.PIN_5 -> {
                signUpRepository.changePassword(oldPasswordPin5, newPassword, object : SignUpSource.AuthPasswordCallback() {
                    override fun onSucceed() {
                        view.showCompletelyChangeDialog()
                    }
                    override fun onIncorrectPassword(count: Int, limit: Int, isExceed: Boolean) {}
                    override fun onLockedUser() {}
                })
                return
            }
            AuthType.PIN_6 -> {
                sharedPref.passwordPin6(newPassword)
                sharedPref.authType(authType)
            }
            AuthType.PATTERN -> {
                sharedPref.passwordPattern(newPassword)
                sharedPref.authType(authType)
            }
            else -> {}
        }

        view.showCompletelyChangeDialog()
    }

    override fun deletePassword() {
        when (authType) {
            AuthType.PIN_6,
            AuthType.PATTERN -> {
                sharedPref.deletePassword(authType)
                sharedPref.authType(AuthType.PIN_5)
            }
            else -> {}
        }

        view.showCompletelyDeletionDialog()
    }

    override fun checkPassword(password: String, callback: AuthenticationContract.AuthenticationCallback) {
        when (authType) {
            AuthType.PIN_5 -> {
                signUpRepository.authPassword(password, object : SignUpSource.AuthPasswordCallback() {
                    override fun onSucceed() {
                        callback.onSucceed()
                    }

                    override fun onIncorrectPassword(count: Int, limit: Int, isExceed: Boolean) {
                        callback.onIncorrectPassword(count, limit, isExceed)

                        if (isExceed) {
                            sharedPref.clearAll()
                            TokenSharedPref.removeToken(view as Context)
                            view.showAuthenticationExceededDialog()
                        }
                    }

                    override fun onLockedUser() {
                        view.showLockedUserDialog()
                    }
                })
            }
            AuthType.PIN_6 -> {
                val isValid = sharedPref.signaturePin6(password)

                if (isValid) {
                    callback.onSucceed()
                    signUpRepository.postAuthFailureCount(0)
                } else {
                    val isExceed = (authFailureCount >= authFailureLimit)

                    callback.onIncorrectPassword(++authFailureCount, authFailureLimit, isExceed)

                    signUpRepository.postAuthFailureCount(authFailureCount)

                    if (isExceed) {
                        sharedPref.clearAll()
                        TokenSharedPref.removeToken(view as Context)
                        view.showAuthenticationExceededDialog()
                    }
                }
            }
            AuthType.PATTERN -> {
                val isValid = sharedPref.signaturePattern(password)

                if (isValid) {
                    callback.onSucceed()
                    signUpRepository.postAuthFailureCount(0)
                } else {
                    val isExceed = (authFailureCount >= authFailureLimit)

                    callback.onIncorrectPassword(++authFailureCount, authFailureLimit, isExceed)

                    signUpRepository.postAuthFailureCount(authFailureCount)

                    if (isExceed) {
                        sharedPref.clearAll()
                        TokenSharedPref.removeToken(view as Context)
                        view.showAuthenticationExceededDialog()
                    }
                }
            }
            else -> {}
        }
    }
}