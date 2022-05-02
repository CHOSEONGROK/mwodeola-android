package com.jojo.android.mwodeola.model.account

import android.util.Log
import com.jojo.android.mwodeola.data.TokenPair
import com.jojo.android.mwodeola.model.ErrorResponse
import com.jojo.android.mwodeola.model.ErrorResponse.Companion.ERROR_CODE_TOKEN_NOT_VALID
import com.jojo.android.mwodeola.model.ResponseCode.HTTP_401_UNAUTHORIZED
import com.jojo.android.mwodeola.model.RetrofitService
import com.jojo.android.mwodeola.model.token.TokenSharedPref
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

abstract class TokenBasedCallback<T> : Callback<T> {
    private var tokenErrorCount = 0

    abstract fun onNewResponse(call: Call<T>, response: Response<T>, errorResponse: ErrorResponse)
    abstract override fun onFailure(call: Call<T>, t: Throwable)

    override fun onResponse(call: Call<T>, response: Response<T>) {

        val errorResponse = ErrorResponse.createBy(response)
        if (errorResponse.responseCode == HTTP_401_UNAUTHORIZED &&
            errorResponse.code == ERROR_CODE_TOKEN_NOT_VALID) {

            if (tokenErrorCount == 0) {
                tokenErrorCount++
                refreshTokenAndReRequestCall(call, this)
            } else {
                onFailure(call, Throwable("토큰을 확인해주세요."))
            }

        } else {
            onNewResponse(call, response, errorResponse)
        }
    }

    private fun <T> refreshTokenAndReRequestCall(pendingCall: Call<T>, pendingCallback: Callback<T>) {
        val refreshToken = TokenSharedPref.REFRESH_TOKEN ?: return
        val clonePendingCall = pendingCall.clone()


    }
}