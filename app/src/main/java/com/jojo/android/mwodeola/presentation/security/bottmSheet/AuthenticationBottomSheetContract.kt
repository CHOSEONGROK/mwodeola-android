package com.jojo.android.mwodeola.presentation.security.bottmSheet

import android.content.Context


interface AuthenticationBottomSheetContract {

    abstract class AuthenticationCallback {
        open fun onSucceed() {}
        open fun onIncorrectPassword(count: Int, limit: Int, isExceed: Boolean) {}
        open fun onLockedUser() {}
        open fun onFailure() {}
    }

    interface View {
        fun showBiometricConfirmButton()

        fun dismissForSucceed()
        fun dismissForExceedAuthLimit()

        fun initView()
        fun appendToggleAt(index: Int)
        fun removeToggleAt(index: Int)
        fun clearToggle()
        fun showAuthFailureText()
        fun showAuthFailureCountText(count: Int, limit: Int)
        fun showAuthFailure()
        fun vibrateAuthFailure()

        fun showBiometricEnrollChangedDialog()
        fun showExceedAuthLimitDialog()
        fun showLockedUserDialog()

        fun showToast(message: String)
    }

    interface Presenter {
        val hasNewBiometricEnrolled: Boolean

        val authFailureCount: Int
        val authFailureLimit: Int

        fun loadAuthFailureCount()
        fun checkBiometricEnroll()

        fun authenticate(password: String, callback: AuthenticationCallback)
        fun changeAuthTypeToBiometric()
    }
}