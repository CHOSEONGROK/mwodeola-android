package com.jojo.android.mwodeola.model.sign

import android.util.Log
import com.jojo.android.mwodeola.data.users.UserInfo

interface SignUpSource {

    abstract class BaseCallback {
        abstract fun onSucceed()
        abstract fun onFailure()
        open fun onUnknownError(errString: String?) {
            Log.w("SignUpSource", "onUnknownError(): errString=$errString")
        }
    }

    abstract class LoadDataCallback<T> {
        abstract fun onSucceed(data: T)
        abstract fun onFailure()
        open fun onUnknownError(errString: String?) {
            Log.w("SignUpSource", "onUnknownError(): errString=$errString")
        }
    }

    abstract class SignInCallback {
        abstract fun onSucceed()
        abstract fun onIncorrectPassword(count: Int, limit: Int, isExceed: Boolean)
        abstract fun onLockedUser()
        abstract fun onDormantUser()
        abstract fun onNotFoundUser()
        open fun onUnknownError(errString: String?) {
            Log.w("SignUpSource", "onUnknownError(): errString=$errString")
        }
    }

    abstract class SignInAutoCallback {
        abstract fun onSucceed()
        abstract fun onExpiredToken()
        abstract fun onLockedUser()
        abstract fun onDormantUser()
        abstract fun onNotFoundUser()
        open fun onUnknownError(errString: String?) {
            Log.w("SignUpSource", "onUnknownError(): errString=$errString")
        }
    }

    abstract class WithdrawalCallback {
        abstract fun onSucceed()
        abstract fun onIncorrectPassword(count: Int, limit: Int, isExceed: Boolean)
        abstract fun onLockedUser()
        open fun onUnknownError(errString: String?) {
            Log.w("SignUpSource", "onUnknownError(): errString=$errString")
        }
    }

    abstract class AuthPasswordCallback : WithdrawalCallback()

    abstract class AuthFailureCountCallback {
        abstract fun onSucceed(authFailureCount: Int, limit: Int)
        open fun onUnknownError(errString: String?) {
            Log.w("SignUpSource", "onUnknownError(): errString=$errString")
        }
    }

    fun signUpVerifyEmail(email: String, callback: BaseCallback)
    fun signUpVerifyPhone(phoneNumber: String, callback: BaseCallback)
    fun signUp(name: String, email: String, phoneNumber: String, password: String, callback: BaseCallback)

    fun signInVerify(phoneNumber: String, callback: BaseCallback)
    fun signIn(phoneNumber: String, password: String, callback: SignInCallback)
    fun signInAuto(callback: SignInAutoCallback)

    fun signOut(callback: BaseCallback)

    fun loadUserInfo(callback: LoadDataCallback<UserInfo>)

    fun getAuthFailureCount(callback: AuthFailureCountCallback)
    fun postAuthFailureCount(authFailureCount: Int)

    fun withdrawal(phoneNumber: String, password: String, callback: WithdrawalCallback)

    fun lockUser(callback: BaseCallback)

    fun authPassword(password: String, callback: AuthPasswordCallback)
    fun changePassword(oldPassword: String, newPassword: String, callback: AuthPasswordCallback)

    fun refreshToken(callback: BaseCallback)
}