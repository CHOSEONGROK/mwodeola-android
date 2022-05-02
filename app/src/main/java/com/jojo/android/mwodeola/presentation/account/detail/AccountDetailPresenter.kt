package com.jojo.android.mwodeola.presentation.account.detail

import android.util.Log
import androidx.fragment.app.FragmentActivity
import com.jojo.android.mwodeola.data.account.Account
import com.jojo.android.mwodeola.data.account.AccountGroupAndDetails
import com.jojo.android.mwodeola.model.account.AccountSource
import com.jojo.android.mwodeola.presentation.security.Authenticators

class AccountDetailPresenter(
    private val view: AccountDetailContract.View,
    private val repository: AccountSource
): AccountDetailContract.Presenter {
    companion object {
        private const val TAG = "AccountDetailPresenter"
    }

    override val accounts: AccountGroupAndDetails
        get() = _accounts

    private var _accounts = AccountGroupAndDetails.empty()

    private var hasAuthentication: Boolean = false

    override fun loadAccounts(accountGroupId: String, accountId: String?) {
        repository.getAllAccountDetailsInGroup(accountGroupId, object : AccountSource.LoadDataCallback<AccountGroupAndDetails>() {
            override fun onSucceed(data: AccountGroupAndDetails) {
                _accounts = data

                var position = accounts.accounts.indexOfFirst { it.account_id == accountId }
                if (position == -1) {
                    position = 0
                }

                view.showAccounts(accounts, position)
            }
        })
    }

    override fun updateAccounts(accounts: AccountGroupAndDetails) {
        _accounts = accounts
        view.showAccounts(accounts, 0)
    }

    override fun updateAccount(account: Account) {
        val index = _accounts.accounts.indexOfFirst { it.account_id == account.account_id }

        _accounts.own_group.duplicate(account.own_group)
        _accounts.accounts.forEach { it.own_group.duplicate(account.own_group) }

        _accounts.accounts.removeAt(index)
        _accounts.accounts.add(index, account)

        view.showAccounts(accounts, index)
    }

    override fun updateFavorite(isFavorite: Boolean) {
        accounts.updateFavorite(isFavorite)

        repository.updateFavorite(accounts.own_group.id, isFavorite, object : AccountSource.FavoriteCallback() {
            override fun onSucceed(accountGroupId: String, isFavorite: Boolean) {
                Log.d(TAG, "onSucceed(): $accountGroupId, $isFavorite")
            }
        })
    }

    override fun addNewDetail(newAccount: Account) {
        accounts.accounts.add(newAccount)
        accounts.accounts.sort()

        val position = accounts.accounts.indexOf(newAccount)
        view.addNewAccountDetail(accounts, position)
    }

    override fun addNewSnsDetail(snsDetailId: String) {
        repository.addSnsDetailToGroup(accounts.own_group.id, snsDetailId, object : AccountSource.LoadDataCallback<Account>() {
            override fun onSucceed(data: Account) {
                accounts.accounts.add(data)
                accounts.accounts.sort()

                val position = accounts.accounts.indexOf(data)
                view.addNewAccountDetail(accounts, position)
            }
        })
    }

    override fun deleteAccount(account: Account) {
        val position = accounts.accounts.indexOf(account)

        if (account.isOwnAccount) {
            repository.deleteDetail(account.detail.id, object : AccountSource.BaseCallback() {
                override fun onSucceed() {
                    if (accounts.size == 1) {
                        view.finishForDeletedAccountGroup(accounts.own_group.id)
                    } else {
                        accounts.accounts.removeAt(position)
                        view.deleteAccountDetail(accounts, position)
                    }
                }
            })
        } else {
            repository.deleteSnsDetail(account.account_id, object : AccountSource.BaseCallback() {
                override fun onSucceed() {
                    if (accounts.size == 1) {
                        view.finishForDeletedAccountGroup(accounts.own_group.id)
                    } else {
                        accounts.accounts.removeAt(position)
                        view.deleteAccountDetail(accounts, position)
                    }
                }
            })
        }
    }

    override fun deletedAccount(deletedAccountId: String) {
        val deletedPosition = accounts.accounts.indexOfFirst { it.account_id == deletedAccountId }

        if (deletedPosition != -1) {
            accounts.accounts.removeAt(deletedPosition)
            view.deleteAccountDetail(accounts, deletedPosition)
        }
    }

    override fun deleteAccountGroup() {
        repository.deleteAccountGroup(listOf(accounts.own_group.id), object : AccountSource.BaseCallback() {
            override fun onSucceed() {
                view.finishForDeletedAccountGroup(accounts.own_group.id)
            }
        })
    }

    override fun authenticate(
        activity: FragmentActivity,
        callback: Authenticators.AuthenticationCallback
    ) {
        if (hasAuthentication) {
            callback.onSucceed()
        } else {
            Authenticators.BottomSheetBuilder(activity)
                .callback(object : Authenticators.AuthenticationCallback() {
                    override fun onSucceed() {
                        hasAuthentication = true
                        callback.onSucceed()
                    }

                    override fun onFailure() {
                        callback.onFailure()
                    }

                    override fun onExceedAuthLimit(limit: Int) {
                        callback.onExceedAuthLimit(limit)
                        view.showAuthenticationExceedDialog(limit)
                    }
                })
                .execute()
        }
    }
}