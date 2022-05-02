package com.jojo.android.mwodeola.presentation.users.sign

import android.util.Log
import com.google.firebase.FirebaseException
import com.jojo.android.mwodeola.model.sign.SignUpSource
import com.jojo.android.mwodeola.presentation.users.FirebasePhoneAuth

class SignPresenter(
    private val view: SignContract.ParentView,
    private val repository: SignUpSource,
    private val phoneAuth: FirebasePhoneAuth
) : SignContract.Presenter {
    companion object { 
        private const val TAG = "SignPresenter"
    }

    override val isTimeOut: Boolean
        get() = _isTimeOut

    override val sendingUserName: String
        get() = _sendingUserName
    override val sendingEmail: String
        get() = _sendingEmail
    override val sendingPhoneNumber: String
        get() = _sendingPhoneNumber
    override val sendingRawPhoneNumber: String
        get() = _sendingRawPhoneNumber

    private var _isTimeOut: Boolean = true
    
    private var _sendingUserName: String = ""
    private var _sendingEmail: String = ""
    private var _sendingPhoneNumber : String = ""
    private var _sendingRawPhoneNumber : String = ""

    init {
        phoneAuth.setOnPhoneAuthCallback(OnMyPhoneAuthCallback())
    }

    override fun setUserName(name: String) {
        _sendingUserName = name
    }

    override fun send(phoneNumber: String) {
//        view.smsRetrieveHelper.startSmsUserConsent("")
        phoneAuth.send(phoneNumber.formatPhone())
    }

    override fun resend(phoneNumber: String) {
//        view.smsRetrieveHelper.startSmsUserConsent("")
        phoneAuth.resend(phoneNumber.formatPhone())
    }

    override fun authenticateSmsCode(smsCode: String) {
        phoneAuth.authenticate(smsCode)
    }

    override fun signUpVerifyEmail(email: String) {
        repository.signUpVerifyEmail(email, object : SignUpSource.BaseCallback() {
            override fun onSucceed() {
                _sendingEmail = email
                view.startAppLockActivityForSignUp(sendingUserName, sendingEmail, sendingPhoneNumber)
            }

            override fun onFailure() {
                _sendingEmail = ""
                view.showEmailExistError()
            }
        })
    }

    override fun signUpVerifyPhoneNumber(phoneNumber: String) {
        repository.signUpVerifyPhone(phoneNumber, object : SignUpSource.BaseCallback() {
            override fun onSucceed() {
                view.moveNextPage()
            }

            override fun onFailure() {
                // 기존에 가입된 유저.
                view.showToast("기존에 가입된 정보로 로그인합니다.")
                view.startAppLockActivityForSignIn(sendingPhoneNumber)
            }
        })
    }

    // Format: +82-10-0000-0000
    private fun String.formatPhone(): String =
        if (this.length >= 11)
            StringBuilder("+82")
                .append(this.replace(" ", "").removePrefix("0"))
                .insert(3, '-')
                .insert(6, '-')
                .insert(11, '-')
                .toString()
        else this

    // Format: +82-10-0000-0000 to 010 0000 0000
    private fun String.rawPhone(): String =
        if (this.length == 16)
            StringBuilder("0")
                .append(this.drop(4).replace("-", " "))
                .toString()
        else this

    inner class OnMyPhoneAuthCallback : FirebasePhoneAuth.OnPhoneAuthCallback {
        override fun onVerificationCompleted(smsCode: String) {
            Log.d(TAG, "onVerificationCompleted(): smsCode=$smsCode")
            view.setSmsCode(smsCode)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            Log.d(TAG, "onVerificationFailed(): e=$e")
            _isTimeOut = true

            view.showToast(e.message)
        }

        override fun onStarted(phoneNumber: String) {
            Log.d(TAG, "onStarted()")
            _isTimeOut = false
            _sendingPhoneNumber = phoneNumber
            _sendingRawPhoneNumber = phoneNumber.rawPhone()

            view.showSnackBar("인증번호가 문자로 전송되었습니다.(최대 20초 소요)")
        }

        override fun onReStarted(phoneNumber: String) {
            Log.d(TAG, "onReStarted()")
            _isTimeOut = false
            _sendingPhoneNumber = phoneNumber
            _sendingRawPhoneNumber = phoneNumber.rawPhone()

            view.clearSmsCode()
            view.requestFocusSmsCode()
            view.enabledNextButton(false)
            view.showSnackBar("인증번호가 문자로 다시 전송되었습니다.(최대 20초 소요)")
        }

        override fun onTimer(elapsedTimeSec: Int, remainingTimeString: String) {
            Log.d(TAG, "onTimer($elapsedTimeSec): $remainingTimeString")
            view.updateResendButtonText(remainingTimeString)
        }

        override fun onTimeout() {
            Log.d(TAG, "onTimeout()")
            _isTimeOut = true

            view.enabledNextButton(false)
            view.showSnackBar("인증 시간이 초과되었습니다.")
        }

        override fun onPhoneAuthSucceed() {
            Log.d(TAG, "onPhoneAuthSucceed()")
            signUpVerifyPhoneNumber(sendingPhoneNumber)
        }

        override fun onPhoneAuthFailed() {
            Log.d(TAG, "onPhoneAuthFailed()")
            view.showIncorrectSmsCodeError()
        }
    }
}