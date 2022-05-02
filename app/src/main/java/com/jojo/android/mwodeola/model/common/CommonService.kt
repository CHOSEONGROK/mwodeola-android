package com.jojo.android.mwodeola.model.common

import com.jojo.android.mwodeola.data.common.SnsInfo
import retrofit2.Call
import retrofit2.http.GET

interface CommonService {

    @GET("api/sns/info")
    fun getSnsInfo(): Call<List<SnsInfo>>

    @GET("api/data/all/count")
    fun getAllDataCount(): Call<Map<String, Map<String, Int>>>
}