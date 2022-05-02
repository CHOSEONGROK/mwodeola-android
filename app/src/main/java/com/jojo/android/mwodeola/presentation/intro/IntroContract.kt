package com.jojo.android.mwodeola.presentation.intro

interface IntroContract {
    interface View {
        fun startMainActivity()
        fun startSignActivity()
        fun showToast(message: String?)
    }
    interface Presenter {
        fun signInAuto()
    }
}