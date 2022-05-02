package com.jojo.android.mwodeola.presentation.account.edit

import android.util.Patterns
import com.jojo.android.mwodeola.data.account.Account
import com.jojo.android.mwodeola.data.account.AccountGroup
import com.jojo.android.mwodeola.model.account.AccountSource

class EditAccountPresenter constructor(
    private val view: EditAccountContract.View,
    private val repository: AccountSource,
    override val initialAccount: Account
) : EditAccountContract.Presenter {

    companion object {
        private val PROTOCOL_REGEX = "^(http|https)://.*".toRegex()
    }

    override fun loadUserIds() {
        repository.getAllUserIds(object : AccountSource.LoadDataCallback<List<String>>() {
            override fun onSucceed(data: List<String>) {
                view.setUserIdsForAutoComplete(data)
            }
        })
    }

    override fun checkDataChanged(): Boolean =
        initialAccount.own_group.web_url.toString() != view.viewBinder.webUrl ||
                initialAccount.detail.user_id != view.viewBinder.userId ||
                initialAccount.detail.user_password != view.viewBinder.userPassword ||
                initialAccount.detail.user_password_pin4 != view.viewBinder.userPasswordPin4 ||
                initialAccount.detail.user_password_pin6 != view.viewBinder.userPasswordPin6 ||
                initialAccount.detail.user_password_pattern != view.viewBinder.userPasswordPattern ||
                initialAccount.detail.memo != view.viewBinder.memo

    override fun isValidWebUrl(url: String): Boolean =
        if (url.isBlank()) true
        else (Patterns.WEB_URL.matcher(url).matches() && PROTOCOL_REGEX.matches(url))

    override fun isPossibleSave(): Boolean =
        checkDataChanged() && isValidWebUrl(view.viewBinder.webUrl) &&
                view.viewBinder.userId?.length != 0 &&
                view.viewBinder.userPassword?.length != 0 &&
                (view.viewBinder.userPasswordPin4 == null || view.viewBinder.userPasswordPin4?.length == 4) &&
                (view.viewBinder.userPasswordPin6 == null || view.viewBinder.userPasswordPin6?.length == 6) &&
                view.viewBinder.userPasswordPattern?.length != 0 &&
                view.viewBinder.memo?.length != 0

    override fun updateFavorite(isFavorite: Boolean) {
        repository.updateFavorite(initialAccount.own_group.id, isFavorite, object : AccountSource.FavoriteCallback() {
            override fun onSucceed(accountGroupId: String, isFavorite: Boolean) {

            }
        })
    }

    override fun save() {
        if (isPossibleSave().not())
            return

        if (initialAccount.isOwnAccount) {
            initialAccount.own_group.web_url = view.viewBinder.webUrl.notBlankOrNull()

            initialAccount.detail.apply {
                user_id = view.viewBinder.userId
                user_password = view.viewBinder.userPassword
                user_password_pin4 = view.viewBinder.userPasswordPin4
                user_password_pin6 = view.viewBinder.userPasswordPin6
                user_password_pattern = view.viewBinder.userPasswordPattern
                memo = view.viewBinder.memo
            }

            repository.updateAccount(initialAccount, object : AccountSource.LoadDataCallback<Account>() {
                override fun onSucceed(data: Account) {
                    view.finishForUpdated(data)
                }
            })
        } else {
            initialAccount.own_group.web_url = view.viewBinder.webUrl.notBlankOrNull()

            repository.updateAccountGroup(initialAccount.own_group, object : AccountSource.LoadDataCallback<AccountGroup>() {
                override fun onSucceed(data: AccountGroup) {
                    val updatedAccount = Account(
                        account_id = initialAccount.account_id,
                        created_at = initialAccount.created_at,
                        own_group = data,
                        sns_group = initialAccount.sns_group,
                        detail = initialAccount.detail
                    )

                    view.finishForUpdated(updatedAccount)
                }
            })
        }
    }

    override fun delete() {
        if (initialAccount.isOwnAccount) {
            repository.deleteDetail(initialAccount.detail.id, object : AccountSource.BaseCallback() {
                override fun onSucceed() {
                    view.finishForDeleted(initialAccount.account_id)
                }
            })
        } else {
            repository.deleteSnsDetail(initialAccount.account_id, object : AccountSource.BaseCallback() {
                override fun onSucceed() {
                    view.finishForDeleted(initialAccount.account_id)
                }
            })
        }
    }

    private fun String.notBlankOrNull(): String? =
        if (this.isNotBlank()) this else null
}