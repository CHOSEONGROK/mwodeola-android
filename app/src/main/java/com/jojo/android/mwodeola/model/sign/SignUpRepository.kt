package com.jojo.android.mwodeola.model.sign

import android.content.Context
import com.jojo.android.mwodeola.data.users.UserInfo

class SignUpRepository(context: Context) : SignUpSource {

    private val dataSource: SignUpSource = SignUpDataSource(context)

    override fun signUpVerifyEmail(email: String, callback: SignUpSource.BaseCallback) {
        dataSource.signUpVerifyEmail(email, callback)
    }

    override fun signUpVerifyPhone(phoneNumber: String, callback: SignUpSource.BaseCallback) {
        dataSource.signUpVerifyPhone(phoneNumber, callback)
    }

    override fun signUp(name: String, email: String, phoneNumber: String, password: String,
                        callback: SignUpSource.BaseCallback) {
        dataSource.signUp(name, email, phoneNumber, password, callback)
    }

    override fun signInVerify(phoneNumber: String, callback: SignUpSource.BaseCallback) {
        dataSource.signInVerify(phoneNumber, callback)
    }

    override fun signIn(phoneNumber: String, password: String,
                        callback: SignUpSource.SignInCallback) {
        dataSource.signIn(phoneNumber, password, callback)
    }

    override fun signInAuto(callback: SignUpSource.SignInAutoCallback) {
        dataSource.signInAuto(callback)
    }

    override fun signOut(callback: SignUpSource.BaseCallback) {
        dataSource.signOut(callback)
    }

    override fun loadUserInfo(callback: SignUpSource.LoadDataCallback<UserInfo>) {
        dataSource.loadUserInfo(callback)
    }

    override fun getAuthFailureCount(callback: SignUpSource.AuthFailureCountCallback) {
        dataSource.getAuthFailureCount(callback)
    }

    override fun postAuthFailureCount(authFailureCount: Int) {
        dataSource.postAuthFailureCount(authFailureCount)
    }

    override fun withdrawal(phoneNumber: String, password: String,
                            callback: SignUpSource.WithdrawalCallback) {
        dataSource.withdrawal(phoneNumber, password, callback)
    }

    override fun lockUser(callback: SignUpSource.BaseCallback) {
        dataSource.lockUser(callback)
    }

    override fun authPassword(password: String, callback: SignUpSource.AuthPasswordCallback) {
        dataSource.authPassword(password, callback)
    }

    override fun changePassword(oldPassword: String, newPassword: String,
                                callback: SignUpSource.AuthPasswordCallback) {
        dataSource.changePassword(oldPassword, newPassword, callback)
    }

    override fun refreshToken(callback: SignUpSource.BaseCallback) {
        dataSource.refreshToken(callback)
    }
}