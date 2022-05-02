package com.jojo.android.mwodeola.model.sign

import com.jojo.android.mwodeola.data.TokenPair
import com.jojo.android.mwodeola.data.users.UserInfo
import retrofit2.Call
import retrofit2.http.*

interface SignUpService {

    @POST("users/sign_up/verify/email")
    fun signUpVerifyEmail(@Body body: SignUpDTO.SignUpVerifyEmail): Call<Any>

    @POST("users/sign_up/verify/phone")
    fun signUpVerifyPhone(@Body body: SignUpDTO.SignUpVerifyPhone): Call<Any>

    @POST("users/sign_up")
    fun signUp(@Body body: SignUpDTO.SignUp): Call<TokenPair>

    @POST("users/sign_in/verify")
    fun signInVerify(@Body body: Map<String, String>): Call<Any>

    @POST("users/sign_in")
    fun signIn(@Body body: SignUpDTO.SignIn): Call<TokenPair>

    @GET("users/sign_in/auto")
    fun signInAuto(@Header("Authorization") refresh_token: String): Call<TokenPair>

    @PUT("users/sign_out")
    fun signOut(@Header("Authorization") refresh_token: String): Call<Any>

    @GET("users/info")
    fun loadUserInfo(@Header("Authorization") refresh_token: String): Call<UserInfo>

    @GET("users/auth_failed_count")
    fun getAuthFailureCount(
        @Header("Authorization") refresh_token: String
    ): Call<HashMap<String, Int>>

    @POST("users/auth_failed_count")
    fun postAuthFailureCount(
        @Header("Authorization") refresh_token: String,
        @Body body: HashMap<String, Int>
    ) : Call<Any>

    @FormUrlEncoded
    @HTTP(method="DELETE", hasBody=true, path="users/withdrawal")
    fun withdrawal(
        @Header("Authorization") refresh_token: String,
        @Field("phone_number") phone_number: String,
        @Field("password") password: String
    ): Call<Any>

    @POST("users/lock")
    fun lockUser(@Header("Authorization") refresh_token: String): Call<Any>

    @POST("users/password/auth")
    fun authPassword(@Header("Authorization") refresh_token: String,
                     @Body body: SignUpDTO.PasswordAuth): Call<Any>

    @PUT("users/password/change")
    fun changePassword(@Header("Authorization") refresh_token: String,
                       @Body body: SignUpDTO.PasswordChange): Call<Any>

    @GET("users/token/refresh")
    fun refreshToken(@Header("Authorization") refresh_token: String): Call<TokenPair>
}