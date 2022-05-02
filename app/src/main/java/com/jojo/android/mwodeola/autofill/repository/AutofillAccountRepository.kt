package com.jojo.android.mwodeola.autofill.repository

import com.jojo.android.mwodeola.autofill.model.IAutofillAccount


interface AutofillAccountRepository {

     interface LoadCallback {
        fun onSucceed(accounts: List<IAutofillAccount>)
        fun onFailure()
        fun onUnknownError(errString: String?)
    }

    interface SaveCallback {
        fun onSucceed(code: String)
        fun onFailure()
        fun onUnknownError(errString: String?)
    }

    fun saveAccount(
        packageName: String, appName: String,
        userID: String?, userPassword: String,
        callback: SaveCallback
    )

    fun getAccountData(packageName: String, callback: LoadCallback)

    fun updateAccount(callback: LoadCallback)
}