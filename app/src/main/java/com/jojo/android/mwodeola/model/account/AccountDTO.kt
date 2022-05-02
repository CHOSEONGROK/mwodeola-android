package com.jojo.android.mwodeola.model.account

interface AccountDTO {

    data class UpdateFavorite(val account_group_id: String, val is_favorite: Boolean)
}