package com.jojo.android.mwodeola.model

import android.content.Context
import android.util.Log
import com.google.gson.GsonBuilder
import com.jojo.android.mwodeola.model.account.AccountService
import com.jojo.android.mwodeola.model.common.CommonService
import com.jojo.android.mwodeola.model.sign.SignUpService
import com.jojo.android.mwodeola.model.token.TokenSharedPref
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitService {
    private const val TAG = "RetrofitService"
    private const val BASE_URL = "http://ec2-52-79-191-108.ap-northeast-2.compute.amazonaws.com/"
    private const val TIME_OUT = 1500L

    private var _signUpService: SignUpService? = null
    private var _commonService: CommonService? = null
    private var _accountService: AccountService? = null

    fun getSignUpService(context: Context): SignUpService =
        _signUpService ?: getRetrofitAPI(SignUpService::class.java, context).also { _signUpService = it }

    fun getCommonService(context: Context): CommonService =
        _commonService ?: getRetrofitAPI(CommonService::class.java, context).also { _commonService = it }

    fun getAccountService(context: Context): AccountService =
        _accountService ?: getRetrofitAPI(AccountService::class.java, context).also { _accountService = it }

    private fun initSignUpService(context: Context): SignUpService =
        getRetrofitAPI(SignUpService::class.java, context).also { _signUpService = it }

    private fun initCommonService(context: Context): CommonService =
        getRetrofitAPI(CommonService::class.java, context).also { _commonService = it }

    private fun initAccountService(context: Context): AccountService =
        getRetrofitAPI(AccountService::class.java, context).also { _accountService = it }

    private fun <T> getRetrofitAPI(service: Class<T>, context: Context): T =
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(
                GsonConverterFactory.create(
                    GsonBuilder()
                        .serializeNulls()
                        .create()
                )
            )
            .client(getOkHttpClient(service, context))
            .build()
            .create(service)

    private fun <T> getOkHttpClient(service: Class<T>, context: Context): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .connectTimeout(TIME_OUT, TimeUnit.MILLISECONDS)
            .readTimeout(TIME_OUT, TimeUnit.MILLISECONDS)
            .writeTimeout(TIME_OUT, TimeUnit.MILLISECONDS)
            .retryOnConnectionFailure(false)
            .addInterceptor(getHttpLoggingInterceptor())
            .retryOnConnectionFailure(true)

        if (service !is SignUpService) {
            builder
                // access 토큰 인터셉터
                .addInterceptor(getTokenInterceptor(context))
                // access 토큰 만료 응답(code=401)이 오면 토큰 갱신 & re-request
                .authenticator(getTokenAuthenticator(context))
        }

        return builder.build()
    }

    private fun getTokenInterceptor(context: Context): Interceptor = Interceptor {
        if (TokenSharedPref.ACCESS_TOKEN == null) {
            TokenSharedPref.init(context)
        }

        val originalRequest = it.request()
        val newRequest =
            if (originalRequest.header("Authorization") == null && TokenSharedPref.ACCESS_TOKEN != null)
                it.request().newBuilder()
                    .addHeader("Connection", "close")
                    .addHeader("Authorization", TokenSharedPref.ACCESS_TOKEN!!)
                    .method(originalRequest.method, originalRequest.body)
                    .tag(1) // request 요청 횟수(Authenticator 에서 재요청 횟수를 확인하기 위함이라 중요!!)
                    .build()
            else originalRequest
        it.proceed(newRequest)
    }

    private fun getHttpLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

    private fun getTokenAuthenticator(context: Context): Authenticator = object : Authenticator {
        override fun authenticate(route: Route?, response: Response): Request? {
            Log.i(TAG, "TokenAuthenticator: response=$response")

            val errorResponse = ErrorResponse.createBy(response)
            Log.w(TAG, "TokenAuthenticator: errorResponse=${errorResponse.rawErrorString}")

            val tokenError = (errorResponse.responseCode == ResponseCode.HTTP_401_UNAUTHORIZED) &&
                    (errorResponse.code == ErrorResponse.ERROR_CODE_TOKEN_NOT_VALID ||
                            errorResponse.code == ErrorResponse.ERROR_CODE_TOKEN_NOT_VALID ||
                            errorResponse.detail == ErrorResponse.ERROR_DETAIL_TOKEN_NOT_PROVIDED)

            if (tokenError) {
                Log.i(TAG, "TokenAuthenticator.tokenError: REFRESH_TOKEN is null ?= ${TokenSharedPref.REFRESH_TOKEN == null}")
                if (TokenSharedPref.REFRESH_TOKEN == null) {
                    TokenSharedPref.init(context)
                    if (TokenSharedPref.REFRESH_TOKEN == null)
                        return null
                }

                // request.tag 는 재요청 횟수를 재기 위함.
                val tag = response.request.tag()
                Log.i(TAG, "TokenAuthenticator.tokenError: response.request.tag(재요청횟수)=$tag")
                if (tag is Int && tag < 2) {
                    getSignUpService(context).refreshToken(TokenSharedPref.REFRESH_TOKEN!!).execute()
                        .body()?.let {
                            TokenSharedPref.setAccessToken(context, it.access)
                        }

                    Log.d(TAG, "TokenAuthenticator: 토큰 갱신 완료!!")
                    return response.request.newBuilder()
                        .removeHeader("Authorization")
                        .addHeader("Authorization", TokenSharedPref.ACCESS_TOKEN!!)
                        .method(response.request.method, response.request.body)
                        .tag(tag + 1)
                        .build()
                } else {
                    Log.w(TAG, "TokenAuthenticator: [재요청횟수 초과!!] tag=$tag: request=${response.request}")
                }

                // response.close()
                return null
            } else {
                // response.close()
                return null
            }
        }
    }

    private fun String.setBearer(): String =
        if (!this.contains("Bearer "))
            "Bearer $this"
        else this
}