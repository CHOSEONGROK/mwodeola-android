package com.jojo.android.mwodeola.presentation.account.edit

import com.jojo.android.mwodeola.data.account.Account

interface EditAccountContract {

    interface View {
        val viewBinder: EditAccountActivityViewBinder

        fun setUserIdsForAutoComplete(userIds: List<String>)

        fun showBackDrop()
        fun hideBackDrop()

        fun cancelRemoveMode()

        fun showExitWithoutSavingConfirmDialog()
        fun showDeleteConfirmDialog()

        fun finishForUpdated(account: Account)
        fun finishForDeleted(accountId: String)
    }

    interface Presenter {
        val initialAccount: Account

        fun loadUserIds()

        fun checkDataChanged(): Boolean
        fun isValidWebUrl(url: String): Boolean
        fun isPossibleSave(): Boolean

        fun updateFavorite(isFavorite: Boolean)
        fun save()
        fun delete()
    }
}