package com.jojo.android.mwodeola.presentation.intro

import android.content.Context
import com.jojo.android.mwodeola.model.sign.SignUpRepository
import com.jojo.android.mwodeola.model.sign.SignUpSource
import com.jojo.android.mwodeola.model.token.TokenSharedPref

class IntroPresenter(
    private val view: IntroContract.View
): IntroContract.Presenter {

    private val repository: SignUpSource = SignUpRepository(view as Context)

    /**
     * 자동 로그인
     * */
    override fun signInAuto() {
        if (TokenSharedPref.REFRESH_TOKEN == null) {
            view.showToast("signInAuto: token is null")
            return
        }

        repository.signInAuto(object : SignUpSource.SignInAutoCallback() {
            override fun onSucceed() {
                view.startMainActivity()
            }

            override fun onExpiredToken() {
                view.showToast("Token is expired")
            }

            override fun onLockedUser() {
                view.showToast("User is locked")
            }

            override fun onDormantUser() {
                view.showToast("User is dormant")
            }

            override fun onNotFoundUser() {
                view.showToast("User is not found")
            }

        })
    }
}