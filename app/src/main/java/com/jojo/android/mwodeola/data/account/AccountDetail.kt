package com.jojo.android.mwodeola.data.account

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class AccountDetail(
    @SerializedName("group")
    val group_id: String,
    val id: String,
    var user_id: String?,
    var user_password: String?,
    var user_password_pin4: String?,
    var user_password_pin6: String?,
    var user_password_pattern: String?,
    var memo: String?,
    val created_at: String,
    val last_confirmed_at: String,
    val views: Int
): Comparable<AccountDetail>, Serializable {

    override fun compareTo(other: AccountDetail): Int =
        (this.user_id ?: "").compareTo(other.user_id ?: "")

    companion object {
        fun empty(): AccountDetail =
            AccountDetail(
                "", "", null, null,
                null, null, null, null,
                "", "", 0
            )

        fun forAutofill(user_id: String, user_password: String, memo: String): AccountDetail =
            AccountDetail(
                "", "", user_id, user_password,
                null, null, null,
                memo, "", "", 0
            )
    }
}