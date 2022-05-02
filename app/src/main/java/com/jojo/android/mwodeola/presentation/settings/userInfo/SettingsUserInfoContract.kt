package com.jojo.android.mwodeola.presentation.settings.userInfo

import android.content.Context
import com.jojo.android.mwodeola.data.users.UserInfo

interface SettingsUserInfoContract {
    interface View {
        fun showUserInfo(userInfo: UserInfo)

        fun showSignOutConfirmDialog()

        fun showWithdrawalBottomSheet()

        fun showToast(message: String?)

        fun restartApp()
    }
    interface Presenter {
        fun loadUserInfo()

        fun signOut(context: Context)
        fun withdrawal()
    }
}