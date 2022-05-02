package com.jojo.android.mwodeola.presentation.users.sign

import com.jojo.android.mwodeola.presentation.users.SmsRetrieveHelper

interface SignContract {

    // Activity
    interface ParentView : BaseChildView {
        val presenter: Presenter
        val smsRetrieveHelper: SmsRetrieveHelper

        fun initView()

        fun moveNextPage()

        fun enabledNextButton(isEnabled: Boolean)
        fun updateNextButton(text: String)

        fun startAppLockActivityForSignUp(name: String, email: String, phoneNumber: String)
        fun startAppLockActivityForSignIn(phoneNumber: String)

        fun showSnackBar(message: String)
        fun showToast(message: String?)
        fun showDialog()
    }

    // BaseFragment
    interface BaseChildView {
        // Common
        fun onSelectedPage(isInitial: Boolean)
        fun onClickedNextButton()

        // SignInputPhoneFragment
        fun setPhoneNumber(phoneNumber: String)
        fun showSmsAuthInputWidget()
        fun requestFocusSmsCode()
        fun setSmsCode(code: String)
        fun clearSmsCode()
        fun showPhoneNumberError()
        fun showIncorrectSmsCodeError()
        fun updateResendButtonText(remainingTimeString: String)

        // SignInputUserNameFragment

        // SignInputEmailFragment
        fun setEmail(email: String)
        fun showEmailExistError()
    }

    interface Presenter {
        val isTimeOut: Boolean

        val sendingUserName: String
        val sendingEmail: String
        val sendingPhoneNumber: String
        val sendingRawPhoneNumber: String

        fun setUserName(name: String)

        fun send(phoneNumber: String)
        fun resend(phoneNumber: String)
        fun authenticateSmsCode(smsCode: String)

        fun signUpVerifyEmail(email: String)
        fun signUpVerifyPhoneNumber(phoneNumber: String)
    }
}