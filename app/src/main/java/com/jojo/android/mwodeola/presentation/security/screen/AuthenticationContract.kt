package com.jojo.android.mwodeola.presentation.security.screen

import android.util.Log

interface AuthenticationContract {

    abstract class AuthenticationCallback {
        abstract fun onSucceed()
        abstract fun onIncorrectPassword(count: Int, limit: Int, isExceed: Boolean)
        abstract fun onLockedUser()
        open fun onUnknownError(errString: String?) {
            Log.w("AuthenticationContract", "onUnknownError(): errString=$errString")
        }
    }

    interface View {
        fun showBiometricConfirmButton()

        fun showBiometricEnrollChangedDialog()
        fun showCompletelySignUpDialog()
        fun showCompletelyCreationDialog()
        fun showCompletelyChangeDialog()
        fun showCompletelyDeletionDialog()

        fun showAuthenticationExceededDialog()
        fun showDormantUserDialog()
        fun showLockedUserDialog()
        fun showNotFoundUserDialog()

        fun finishForSucceed()
        fun finishForFailure()
        fun finishForAuthenticationExceeded()
    }

    interface Presenter {
        var userName: String?
        var userEmail: String?
        var userPhoneNumber: String?

        val authFailureCount: Int
        val authFailureLimit: Int

        var oldPasswordPin5: String
        var newPassword: String

        fun loadAuthFailureCount()
        fun checkBiometricEnroll()
        fun changeAuthTypeToBiometric()

        fun signUp(password: String)
        fun signIn(password: String, callback: AuthenticationCallback)
        fun createPassword()
        fun changePassword()
        fun deletePassword()

        fun checkPassword(password: String, callback: AuthenticationCallback)
    }
}