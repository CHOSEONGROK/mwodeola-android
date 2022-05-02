package com.jojo.android.mwodeola.model.sign

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.jojo.android.mwodeola.data.TokenPair
import com.jojo.android.mwodeola.data.users.UserInfo
import com.jojo.android.mwodeola.model.ErrorResponse
import com.jojo.android.mwodeola.model.RetrofitService
import com.jojo.android.mwodeola.model.token.TokenSharedPref
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SignUpDataSource(private val context: Context) : SignUpSource {
    companion object { private const val TAG = "SignUpDataSource" }

    private val service: SignUpService
        get() = RetrofitService.getSignUpService(context)

    override fun signUpVerifyEmail(email: String, callback: SignUpSource.BaseCallback) {
        val body = SignUpDTO.SignUpVerifyEmail(email)

        service.signUpVerifyEmail(body).enqueue(object : Callback<Any> {
            override fun onResponse(call: Call<Any>, response: Response<Any>) {
                Log.i(TAG, "onResponse(): response=$response")

                if (response.isSuccessful) {
                    callback.onSucceed()
                } else {
                    val errorResponse = ErrorResponse.createBy(response)

                    if (errorResponse.code == "already_registered_email") {
                        callback.onFailure()
                    } else {
                        callback.onUnknownError(errorResponse.toString())
                    }
                }
            }

            override fun onFailure(call: Call<Any>, t: Throwable) {
                callback.onUnknownError(t.toString())
            }
        })
    }

    override fun signUpVerifyPhone(phoneNumber: String, callback: SignUpSource.BaseCallback) {
        val body = SignUpDTO.SignUpVerifyPhone(phoneNumber)
        Log.i(TAG, "signUpVerifyPhone(): body=$body")

        service.signUpVerifyPhone(body).enqueue(object : Callback<Any> {
            override fun onResponse(call: Call<Any>, response: Response<Any>) {
                Log.d(TAG, "signUpVerify.onResponse(): response=$response")

                if (response.isSuccessful) {
                    callback.onSucceed()
                } else if (response.errorBody() != null) {
                    val errorResponse = ErrorResponse.createBy(response)

                    if (errorResponse.code == "already_registered_phone_number") {
                        callback.onFailure()
                    } else {
                        callback.onUnknownError(errorResponse.toString())
                    }
                }
            }

            override fun onFailure(call: Call<Any>, t: Throwable) {
                callback.onUnknownError(t.toString())
            }
        })
    }

    override fun signUp(name: String, email: String, phoneNumber: String, password: String,
                        callback: SignUpSource.BaseCallback) {
        val body = SignUpDTO.SignUp(name, email, phoneNumber, password)
        Log.i(TAG, "signUp(): body=$body")

        service.signUp(body).enqueue(object : Callback<TokenPair> {
            override fun onResponse(call: Call<TokenPair>, response: Response<TokenPair>) {
                Log.i(TAG, "signUp.onResponse(): response=$response")

                val responseBody = response.body()
                if (response.isSuccessful && responseBody != null) {
                    TokenSharedPref.setRefreshToken(context, responseBody.refresh)
                    TokenSharedPref.setAccessToken(context, responseBody.access)

                    callback.onSucceed()
                } else {
                    val errorResponse = ErrorResponse.createBy(response)

                    callback.onUnknownError(errorResponse.toString())
                }
            }

            override fun onFailure(call: Call<TokenPair>, t: Throwable) {
                callback.onUnknownError(t.toString())
            }
        })
    }

    override fun signInVerify(phoneNumber: String, callback: SignUpSource.BaseCallback) {
        val body = mapOf(Pair("phone_number", phoneNumber))

        service.signInVerify(body).enqueue(object : Callback<Any> {
            override fun onResponse(call: Call<Any>, response: Response<Any>) {
                Log.i(TAG, "onResponse(): response=$response")

                if (response.isSuccessful) {
                    callback.onSucceed()
                } else {
                    val errorResponse = ErrorResponse.createBy(response)

                    if (errorResponse.code == "unregistered_users") {
                        callback.onFailure()
                    } else {
                        callback.onUnknownError(errorResponse.toString())
                    }
                }
            }

            override fun onFailure(call: Call<Any>, t: Throwable) {
                callback.onUnknownError(t.toString())
            }
        })
    }

    override fun signIn(phoneNumber: String, password: String,
                        callback: SignUpSource.SignInCallback) {
        val body = SignUpDTO.SignIn(phoneNumber, password)

        service.signIn(body).enqueue(object : Callback<TokenPair> {
            override fun onResponse(call: Call<TokenPair>, response: Response<TokenPair>) {
                Log.i(TAG, "signIn.onResponse(): response=$response")

                val responseBody = response.body()

                if (response.isSuccessful && responseBody != null) {
                    TokenSharedPref.setRefreshToken(context, responseBody.refresh)
                    TokenSharedPref.setAccessToken(context, responseBody.access)

                    callback.onSucceed()
                } else {
                    val errorResponse = ErrorResponse.createBy(response)

                    when (errorResponse.code) {
                        ErrorResponse.ERROR_CODE_AUTHENTICATION_FAILED ->
                            callback.onIncorrectPassword(errorResponse.count!!, errorResponse.limit!!, false)
                        ErrorResponse.ERROR_CODE_AUTHENTICATION_EXCEED ->
                            callback.onIncorrectPassword(errorResponse.count!!, errorResponse.limit!!, true)
                        ErrorResponse.ERROR_CODE_USER_INACTIVE ->
                            callback.onDormantUser()
                        ErrorResponse.ERROR_CODE_USER_LOCKED ->
                            callback.onLockedUser()
                        ErrorResponse.ERROR_CODE_USER_NOT_FOUND ->
                            callback.onNotFoundUser()
                        else -> callback.onUnknownError(errorResponse.toString())
                    }
                }
            }

            override fun onFailure(call: Call<TokenPair>, t: Throwable) {
                callback.onUnknownError(t.toString())
            }
        })
    }

    override fun signInAuto(callback: SignUpSource.SignInAutoCallback) {
        val refreshToken = TokenSharedPref.REFRESH_TOKEN
        if (refreshToken == null) {
            callback.onUnknownError("REFRESH_TOKEN is null")
            return
        }

        service.signInAuto(refreshToken).enqueue(object : Callback<TokenPair> {
            override fun onResponse(call: Call<TokenPair>, response: Response<TokenPair>) {
                Log.i(TAG, "signInAuto.onResponse(): response=$response")

                val responseBody = response.body()
                if (response.isSuccessful && responseBody != null) {
                    TokenSharedPref.setRefreshToken(context, responseBody.refresh)
                    TokenSharedPref.setAccessToken(context, responseBody.access)

                    callback.onSucceed()
                } else {
                    val errorResponse = ErrorResponse.createBy(response)

                    when (errorResponse.code) {
                        // Refresh 토큰 만료 or 토큰 블랙리스트
                        ErrorResponse.ERROR_CODE_TOKEN_NOT_VALID ->
                            callback.onExpiredToken()
                        ErrorResponse.ERROR_CODE_USER_INACTIVE ->
                            callback.onDormantUser()
                        ErrorResponse.ERROR_CODE_USER_LOCKED ->
                            callback.onLockedUser()
                        ErrorResponse.ERROR_CODE_USER_NOT_FOUND ->
                            callback.onNotFoundUser()
                        else -> callback.onUnknownError(errorResponse.toString())
                    }

                    TokenSharedPref.removeToken(context)
                }
            }

            override fun onFailure(call: Call<TokenPair>, t: Throwable) {
                callback.onUnknownError(t.toString())
            }
        })
    }

    override fun signOut(callback: SignUpSource.BaseCallback) {
        val refreshToken = TokenSharedPref.REFRESH_TOKEN
        if (refreshToken == null) {
            callback.onUnknownError("REFRESH_TOKEN is null")
            return
        }

        service.signOut(refreshToken).enqueue(object : Callback<Any> {
            override fun onResponse(call: Call<Any>, response: Response<Any>) {
                Log.i(TAG, "signOut.onResponse(): response=$response")

                if (response.isSuccessful) {
                    TokenSharedPref.removeToken(context)
                    callback.onSucceed()
                } else {
                    val errorResponse = ErrorResponse.createBy(response)
                    callback.onUnknownError(errorResponse.toString())
                }
            }

            override fun onFailure(call: Call<Any>, t: Throwable) {
                callback.onUnknownError(t.toString())
            }
        })
    }

    override fun loadUserInfo(callback: SignUpSource.LoadDataCallback<UserInfo>) {
        val refreshToken = TokenSharedPref.REFRESH_TOKEN
        if (refreshToken == null) {
            callback.onUnknownError("REFRESH_TOKEN is null")
            return
        }

        service.loadUserInfo(refreshToken).enqueue(object : Callback<UserInfo> {
            override fun onResponse(call: Call<UserInfo>, response: Response<UserInfo>) {
                val responseData = response.body()

                if (response.isSuccessful && responseData != null) {
                    callback.onSucceed(responseData)
                } else {
                    val errorResponse = ErrorResponse.createBy(response)
                    callback.onUnknownError(errorResponse.toString())
                }
            }

            override fun onFailure(call: Call<UserInfo>, t: Throwable) {
                callback.onUnknownError(t.toString())
            }
        })
    }

    override fun getAuthFailureCount(callback: SignUpSource.AuthFailureCountCallback) {
        val refreshToken = TokenSharedPref.REFRESH_TOKEN
        if (refreshToken == null) {
            callback.onUnknownError("REFRESH_TOKEN is null")
            return
        }

        service.getAuthFailureCount(refreshToken).enqueue(object : Callback<HashMap<String, Int>> {
            override fun onResponse(
                call: Call<HashMap<String, Int>>,
                response: Response<HashMap<String, Int>>
            ) {
                val responseBody = response.body()
                val authFailureCount = responseBody?.get("auth_failed_count")
                val limit = responseBody?.get("limit")

                if (authFailureCount == null || limit == null) {
                    callback.onUnknownError("not valid response data")
                } else if (response.isSuccessful.not()) {
                    val errorResponse = ErrorResponse.createBy(response)

                    callback.onUnknownError(errorResponse.toString())
                } else {
                    callback.onSucceed(authFailureCount, limit)
                }
            }

            override fun onFailure(call: Call<HashMap<String, Int>>, t: Throwable) {
                callback.onUnknownError(t.toString())
            }
        })
    }

    override fun postAuthFailureCount(authFailureCount: Int) {
        val refreshToken = TokenSharedPref.REFRESH_TOKEN
            ?: return
        val body = hashMapOf<String, Int>()
        body["auth_failed_count"] = authFailureCount

        service.postAuthFailureCount(refreshToken, body).enqueue(object : Callback<Any> {
            override fun onResponse(call: Call<Any>, response: Response<Any>) {}
            override fun onFailure(call: Call<Any>, t: Throwable) {}
        })
    }

    override fun withdrawal(phoneNumber: String, password: String,
                            callback: SignUpSource.WithdrawalCallback) {
        val refreshToken = TokenSharedPref.REFRESH_TOKEN

        service.withdrawal(refreshToken!!, phoneNumber, password).enqueue(object : Callback<Any> {
            override fun onResponse(call: Call<Any>, response: Response<Any>) {
                Log.i(TAG, "withdrawal.onResponse(): response=$response")

                if (response.isSuccessful) {
                    callback.onSucceed()
                } else {
                    val errorResponse = ErrorResponse.createBy(response)

                    when (errorResponse.code) {
                        ErrorResponse.ERROR_CODE_AUTHENTICATION_FAILED ->
                            callback.onIncorrectPassword(errorResponse.count!!, errorResponse.limit!!, false)
                        ErrorResponse.ERROR_CODE_AUTHENTICATION_EXCEED ->
                            callback.onIncorrectPassword(errorResponse.count!!, errorResponse.limit!!, true)
                        ErrorResponse.ERROR_CODE_USER_LOCKED -> {
                            callback.onLockedUser()
                            TokenSharedPref.removeToken(context)
                        }
                        else -> callback.onUnknownError(errorResponse.toString())
                    }
                }
            }

            override fun onFailure(call: Call<Any>, t: Throwable) {
                callback.onUnknownError(t.toString())
            }
        })
    }

    override fun lockUser(callback: SignUpSource.BaseCallback) {
        val refreshToken = TokenSharedPref.REFRESH_TOKEN
        if (refreshToken == null) {
            callback.onUnknownError("REFRESH_TOKEN is null")
            return
        }

        service.lockUser(refreshToken).enqueue(object : Callback<Any> {
            override fun onResponse(call: Call<Any>, response: Response<Any>) {
                if (response.isSuccessful) {
                    callback.onSucceed()
                } else {
                    val errorResponse = ErrorResponse.createBy(response)

                    callback.onUnknownError(errorResponse.toString())
                }
            }

            override fun onFailure(call: Call<Any>, t: Throwable) {
                callback.onUnknownError(t.toString())
            }
        })
    }

    override fun authPassword(password: String, callback: SignUpSource.AuthPasswordCallback) {
        val header = TokenSharedPref.REFRESH_TOKEN
        val body = SignUpDTO.PasswordAuth(password)

        service.authPassword(header!!, body).enqueue(object : Callback<Any> {
            override fun onResponse(call: Call<Any>, response: Response<Any>) {
                Log.i(TAG, "authPassword.onResponse(): response=$response")

                if (response.isSuccessful) {
                    callback.onSucceed()
                } else if (response.errorBody() != null) {
                    val errorResponse = ErrorResponse.createBy(response)

                    when (errorResponse.code) {
                        ErrorResponse.ERROR_CODE_AUTHENTICATION_FAILED ->
                            callback.onIncorrectPassword(errorResponse.count!!, errorResponse.limit!!, false)
                        ErrorResponse.ERROR_CODE_AUTHENTICATION_EXCEED ->
                            callback.onIncorrectPassword(errorResponse.count!!, errorResponse.limit!!, true)
                        ErrorResponse.ERROR_CODE_USER_LOCKED -> {
                            callback.onLockedUser()
                            TokenSharedPref.removeToken(context)
                        }
                        else -> callback.onUnknownError(errorResponse.toString())
                    }
                }
            }

            override fun onFailure(call: Call<Any>, t: Throwable) {
                callback.onUnknownError(t.toString())
            }
        })
    }

    override fun changePassword(oldPassword: String, newPassword: String,
                                callback: SignUpSource.AuthPasswordCallback) {
        val header = TokenSharedPref.REFRESH_TOKEN
        val body = SignUpDTO.PasswordChange(oldPassword, newPassword)

        service.changePassword(header!!, body).enqueue(object : Callback<Any> {
            override fun onResponse(call: Call<Any>, response: Response<Any>) {
                Log.i(TAG, "changePassword.onResponse(): response=$response")

                if (response.isSuccessful) {
                    callback.onSucceed()
                } else {
                    val errorResponse = ErrorResponse.createBy(response)

                    when (errorResponse.code) {
                        ErrorResponse.ERROR_CODE_AUTHENTICATION_FAILED ->
                            callback.onIncorrectPassword(errorResponse.count!!, errorResponse.limit!!, false)
                        ErrorResponse.ERROR_CODE_AUTHENTICATION_EXCEED ->
                            callback.onIncorrectPassword(errorResponse.count!!, errorResponse.limit!!, true)
                        ErrorResponse.ERROR_CODE_USER_LOCKED -> {
                            callback.onLockedUser()
                            TokenSharedPref.removeToken(context)
                        }
                        else -> callback.onUnknownError(errorResponse.toString())
                    }
                }
            }

            override fun onFailure(call: Call<Any>, t: Throwable) {
                callback.onUnknownError(t.toString())
            }
        })
    }

    override fun refreshToken(callback: SignUpSource.BaseCallback) {
        val header = TokenSharedPref.REFRESH_TOKEN

        service.refreshToken(header!!).enqueue(object : Callback<TokenPair> {
            override fun onResponse(call: Call<TokenPair>, response: Response<TokenPair>) {
                Log.i(TAG, "refreshToken.onResponse(): response=$response")

                val responseBody = response.body()
                if (response.isSuccessful && responseBody != null) {
                    TokenSharedPref.setAccessToken(context, responseBody.access)

                    callback.onSucceed()
                } else {
                    val responseError = ErrorResponse.createBy(response)
                    callback.onUnknownError(responseError.toString())
                }
            }

            override fun onFailure(call: Call<TokenPair>, t: Throwable) {
                callback.onUnknownError(t.toString())
            }
        })
    }

    private fun showToast(message: String?) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}