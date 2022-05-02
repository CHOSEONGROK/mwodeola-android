package com.jojo.android.mwodeola.data.account

import androidx.viewpager.widget.ViewPager
import java.io.Serializable

data class AccountGroupAndDetails constructor(
    val own_group: AccountGroup,
    val accounts: MutableList<Account>
): Serializable {
    val size: Int
        get() = accounts.size

    operator fun get(index: Int): Account = accounts[index]

    fun getOrNull(index: Int): Account? = accounts.getOrNull(index)

    fun updateFavorite(isFavorite: Boolean) {
        own_group.is_favorite = isFavorite
        accounts.forEach { it.own_group.is_favorite = isFavorite }
    }

    companion object {
        fun empty(): AccountGroupAndDetails =
            AccountGroupAndDetails(AccountGroup.empty(), mutableListOf())
    }
}