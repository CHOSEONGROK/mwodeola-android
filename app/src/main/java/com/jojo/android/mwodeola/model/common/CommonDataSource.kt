package com.jojo.android.mwodeola.model.common

import android.content.Context
import android.util.Log
import com.jojo.android.mwodeola.data.common.SnsInfo
import com.jojo.android.mwodeola.model.ErrorResponse
import com.jojo.android.mwodeola.model.RetrofitService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CommonDataSource(
    private val context: Context
): CommonSource {
    companion object { private const val TAG = "CommonDataSource" }

    private val service: CommonService
        get() = RetrofitService.getCommonService(context)

    override fun getSnsInfo(callback: CommonSource.BaseCallback<List<SnsInfo>>) {
        service.getSnsInfo().enqueue(object : Callback<List<SnsInfo>> {
            override fun onResponse(call: Call<List<SnsInfo>>, response: Response<List<SnsInfo>>) {
                val responseBody = response.body()

                if (response.isSuccessful && responseBody != null) {
                    callback.onSucceed(responseBody)
                } else {
                    val errorResponse = ErrorResponse.createBy(response)
                    callback.onFailure(errorResponse.toString())
                }
            }

            override fun onFailure(call: Call<List<SnsInfo>>, t: Throwable) {
                callback.onFailure(t.toString())
            }
        })
    }

    override fun getAllDataCount(callback: CommonSource.DataCountCallback) {
        service.getAllDataCount().enqueue(object : Callback<Map<String, Map<String, Int>>> {
            override fun onResponse(call: Call<Map<String, Map<String, Int>>>,
                                    response: Response<Map<String, Map<String, Int>>>) {
                Log.i(TAG, "getAllDataCount.onResponse(): response=$response")
                val responseBody = response.body()

                if (response.isSuccessful && responseBody != null) {
                    val countOfAccounts = responseBody["account"]?.get("group_count")!!
                    val countOfCreditCards = 0

                    callback.onSucceed(countOfAccounts, countOfCreditCards)
                } else {
                    val error = ErrorResponse.createBy(response)
                    callback.onUnknownError(error.toString())
                }
            }

            override fun onFailure(call: Call<Map<String, Map<String, Int>>>, t: Throwable) {
                callback.onUnknownError(t.toString())
            }
        })
    }
}