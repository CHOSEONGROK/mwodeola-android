package com.jojo.android.mwodeola.presentation.main

interface MainContract {

    interface View {
        fun updateNumberOfUserAccountData(count: Int)
        fun updateNumberOfCreditCardData(count: Int)

        fun showToast(message: String?)
    }

    interface Presenter {
        fun countAllData()
    }
}