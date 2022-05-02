package com.jojo.android.mwodeola.model.common

import android.util.Log
import com.jojo.android.mwodeola.data.common.SnsInfo

interface CommonSource {
    companion object { private const val TAG = "CommonSource" }

    abstract class BaseCallback<T> {
        abstract fun onSucceed(data: T)
        abstract fun onFailure(errString: String?)
    }

    abstract class DataCountCallback {
        abstract fun onSucceed(countOfAccounts: Int, countOfCreditCards: Int)
        abstract fun onFailure()
        open fun onUnknownError(errString: String?) {
            Log.w(TAG, "onUnknownError(): $errString")
        }
    }

    fun getSnsInfo(callback: BaseCallback<List<SnsInfo>>)

    fun getAllDataCount(callback: DataCountCallback)
}