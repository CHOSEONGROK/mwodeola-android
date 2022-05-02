package com.jojo.android.mwodeola.model.token

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

object TokenSharedPref {
    const val BEARER = "Bearer "

    enum class Key {
        REFRESH_TOKEN, ACCESS_TOKEN
    }

    private var _refresh_token: String? = null
    val REFRESH_TOKEN: String?
        get() = _refresh_token

    private var _access_token: String? = null
    val ACCESS_TOKEN: String?
        get() = _access_token

    fun init(context: Context) {
        val refresh = getRefreshToken(context)
        if (refresh != null) {
            _refresh_token = BEARER + refresh
        }
        val access = getAccessToken(context)
        if (access != null) {
            _access_token = BEARER + access
        }
    }

    fun getRefreshToken(context: Context): String? =
        getSharedPreferences(context).getString(Key.REFRESH_TOKEN.name, null)

    fun setRefreshToken(context: Context, token: String) =
        getSharedPreferences(context).edit {
            putString(Key.REFRESH_TOKEN.name, token)
            commit()
            _refresh_token = BEARER + token
        }

    fun getAccessToken(context: Context): String? =
        getSharedPreferences(context).getString(Key.ACCESS_TOKEN.name, null)

    fun setAccessToken(context: Context, token: String) =
        getSharedPreferences(context).edit {
            putString(Key.ACCESS_TOKEN.name, token)
            commit()
            _access_token = BEARER + token
        }

    fun removeToken(context: Context) =
        getSharedPreferences(context).edit {
            _refresh_token = null
            _access_token = null
            remove(Key.REFRESH_TOKEN.name)
            remove(Key.ACCESS_TOKEN.name)
            commit()
        }

    private fun getSharedPreferences(context: Context): SharedPreferences =
        context.getSharedPreferences(context.packageName + ".Token", Context.MODE_PRIVATE)
}