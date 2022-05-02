package com.jojo.android.mwodeola.presentation.account.detail

import android.os.Parcelable
import androidx.fragment.app.FragmentActivity
import com.jojo.android.mwodeola.data.account.Account
import com.jojo.android.mwodeola.data.account.AccountDetail
import com.jojo.android.mwodeola.data.account.AccountGroupAndDetails
import com.jojo.android.mwodeola.presentation.security.Authenticators

interface AccountDetailContract {

    interface View {
        val currentPosition: Int
        val isDeleteMode: Boolean

        fun showAccounts(accounts: AccountGroupAndDetails, position: Int)

        fun addNewAccountDetail(accountsAfterAdded: AccountGroupAndDetails, position: Int)
        fun deleteAccountDetail(accountsAfterDeleted: AccountGroupAndDetails, position: Int)

        fun updateDeleteButton(performHaptic: Boolean)

        fun showAddNormalDetailBottomSheet()
        fun showAddSnsDetailBottomSheet()

        fun showDeleteAccountDetailConfirmDialog(account: Account)
        fun showDeleteAccountGroupConfirmDialog()

        fun startCreateNewAccountActivity()
        fun startCreateNewAccountActivityWithSnsDetail(snsAccount: Account)
        fun startCreateNewAccountActivityForSnsAccount(snsCode: Int)

        fun startEditAccountActivity(account: Account)

        fun showAuthenticationExceedDialog(limit: Int)

        fun finishForDeletedAccountGroup(accountGroupId: String?)
        fun finishForAuthenticationExceeded()
    }

    interface Presenter {
        val accounts: AccountGroupAndDetails

        fun loadAccounts(accountGroupId: String, accountId: String?)
        fun updateAccounts(accounts: AccountGroupAndDetails)
        fun updateAccount(account: Account)
        fun updateFavorite(isFavorite: Boolean)

        fun addNewDetail(newAccount: Account)
        fun addNewSnsDetail(snsDetailId: String)

        fun deleteAccount(account: Account)
        fun deletedAccount(deletedAccountId: String)
        fun deleteAccountGroup()

        fun authenticate(activity: FragmentActivity, callback: Authenticators.AuthenticationCallback)
    }
}