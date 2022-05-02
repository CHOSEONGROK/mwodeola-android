package com.jojo.android.mwodeola.presentation.main

import android.util.Log
import com.jojo.android.mwodeola.model.common.CommonSource

class MainPresenter(
    private val view: MainContract.View,
    private val repository: CommonSource
) : MainContract.Presenter {
    companion object {
        private const val TAG = "MainPresenter"
    }

    override fun countAllData() {
        repository.getAllDataCount(object : CommonSource.DataCountCallback() {
            override fun onSucceed(countOfAccounts: Int, countOfCreditCards: Int) {
                view.updateNumberOfUserAccountData(countOfAccounts)
                view.updateNumberOfCreditCardData(countOfCreditCards)
            }

            override fun onFailure() {
                Log.w(TAG, "onFailure()")
            }
        })
    }
}