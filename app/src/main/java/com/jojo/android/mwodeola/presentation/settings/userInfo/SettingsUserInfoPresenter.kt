package com.jojo.android.mwodeola.presentation.settings.userInfo

import android.content.Context
import com.jojo.android.mwodeola.data.users.UserInfo
import com.jojo.android.mwodeola.model.sign.SignUpRepository
import com.jojo.android.mwodeola.model.sign.SignUpSource
import com.jojo.android.mwodeola.model.token.TokenSharedPref
import com.jojo.android.mwodeola.presentation.security.SecurityManager

class SettingsUserInfoPresenter(
    private val view: SettingsUserInfoContract.View,
    private val repository: SignUpSource,
    private val sharedPref: SecurityManager.SharedPref
): SettingsUserInfoContract.Presenter {

    override fun loadUserInfo() {
        repository.loadUserInfo(object : SignUpSource.LoadDataCallback<UserInfo>() {
            override fun onSucceed(data: UserInfo) {
                view.showUserInfo(data)
            }

            override fun onFailure() {
                view.showToast("Failed.")
            }
        })
    }

    override fun signOut(context: Context) {
        repository.signOut(object : SignUpSource.BaseCallback() {
            override fun onSucceed() {
                sharedPref.clearAll()
                TokenSharedPref.removeToken(context)
                view.restartApp()
            }

            override fun onFailure() {
                view.showToast("Failed.")
            }
        })
    }

    override fun withdrawal() {
        //repository.withdrawal()
    }
}