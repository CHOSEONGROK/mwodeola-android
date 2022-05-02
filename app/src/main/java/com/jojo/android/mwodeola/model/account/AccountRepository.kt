package com.jojo.android.mwodeola.model.account

import android.content.Context
import com.jojo.android.mwodeola.autofill.model.IAutofillAccount
import com.jojo.android.mwodeola.autofill.repository.AutofillAccountRepository
import com.jojo.android.mwodeola.data.account.Account
import com.jojo.android.mwodeola.data.account.AccountDetail
import com.jojo.android.mwodeola.data.account.AccountGroup
import com.jojo.android.mwodeola.data.account.AccountGroupAndDetails


class AccountRepository(context: Context) : AccountSource, AutofillAccountRepository {
    companion object {
        private const val TAG = "AccountRepository"
    }

    private val dataSource = AccountDataSource(context)

    override fun getAllAccountGroups(callback: AccountSource.LoadDataCallback<List<AccountGroup>>) {
        dataSource.getAllAccountGroups(callback)
    }

    override fun updateAccountGroup(accountGroup: AccountGroup, callback: AccountSource.LoadDataCallback<AccountGroup>) {
        dataSource.updateAccountGroup(accountGroup, callback)
    }

    override fun deleteAccountGroup(accountGroupIds: List<String>, callback: AccountSource.BaseCallback) {
        dataSource.deleteAccountGroup(accountGroupIds, callback)
    }

    override fun getAllSnsAccountGroups(callback: AccountSource.LoadDataCallback<List<AccountGroup>>) {
        dataSource.getAllSnsAccountGroups(callback)
    }

    override fun updateFavorite(accountGroupId: String, isFavorite: Boolean, callback: AccountSource.FavoriteCallback) {
        dataSource.updateFavorite(accountGroupId, isFavorite, callback)
    }

    override fun getAccount(accountId: String, callback: AccountSource.LoadDataCallback<Account>) {
        dataSource.getAccount(accountId, callback)
    }

    override fun createNewAccount(account: Account, callback: AccountSource.LoadDataCallback<Account>) {
        dataSource.createNewAccount(account, callback)
    }

    override fun updateAccount(account: Account, callback: AccountSource.LoadDataCallback<Account>) {
        dataSource.updateAccount(account, callback)
    }

    override fun getAllAccountDetailsInGroup(
        accountGroupId: String,
        callback: AccountSource.LoadDataCallback<AccountGroupAndDetails>
    ) {
        dataSource.getAllAccountDetailsInGroup(accountGroupId, callback)
    }

    override fun getAllSimpleAccountDetailsInGroup(
        accountGroupId: String,
        callback: AccountSource.LoadDataCallback<AccountGroupAndDetails>
    ) {
        dataSource.getAllSimpleAccountDetailsInGroup(accountGroupId, callback)
    }

    override fun createNewAccountGroupWithSnsDetail(
        accountGroup: AccountGroup,
        snsDetailId: String,
        callback: AccountSource.LoadDataCallback<Account>
    ) {
        dataSource.createNewAccountGroupWithSnsDetail(accountGroup, snsDetailId, callback)
    }

    override fun addSnsDetailToGroup(
        accountGroupId: String,
        snsDetailId: String,
        callback: AccountSource.LoadDataCallback<Account>
    ) {
        dataSource.addSnsDetailToGroup(accountGroupId, snsDetailId, callback)
    }

    override fun deleteSnsDetail(accountId: String, callback: AccountSource.BaseCallback) {
        dataSource.deleteSnsDetail(accountId, callback)
    }

    override fun addNewDetail(accountDetail: AccountDetail, callback: AccountSource.LoadDataCallback<Account>) {
        dataSource.addNewDetail(accountDetail, callback)
    }

    override fun deleteDetail(accountDetailId: String, callback: AccountSource.BaseCallback) {
        dataSource.deleteDetail(accountDetailId, callback)
    }

    override fun getAllUserIds(callback: AccountSource.LoadDataCallback<List<String>>) {
        dataSource.getAllUserIds(callback)
    }

    override fun searchAccountGroup(keyword: String, callback: AccountSource.LoadDataCallback<List<AccountGroup>>) {
        dataSource.searchAccountGroup(keyword, callback)
    }

    override fun searchAccountDetail(keyword: String, callback: AccountSource.LoadDataCallback<List<Account>>) {
        dataSource.searchAccountDetail(keyword, callback)
    }

    override fun getAccountsForAutofillServiceBy(
        appPackageName: String,
        callback: AccountSource.LoadDataCallback<List<Account>>
    ) {
        dataSource.getAccountsForAutofillServiceBy(appPackageName, callback)
    }

    override fun saveAccountForAutofillService(
        packageName: String,
        appName: String,
        userID: String?,
        userPassword: String,
        callback: AutofillAccountRepository.SaveCallback
    ) {
        dataSource.saveAccountForAutofillService(packageName, appName, userID, userPassword, callback)
    }

    /**
     * [AutofillAccountRepository]'s override functions.
     * */
    override fun saveAccount(
        packageName: String,
        appName: String,
        userID: String?,
        userPassword: String,
        callback: AutofillAccountRepository.SaveCallback
    ) {
        dataSource.saveAccountForAutofillService(packageName, appName, userID, userPassword, callback)
    }

    override fun getAccountData(packageName: String, callback: AutofillAccountRepository.LoadCallback) {
        dataSource.getAccountsForAutofillServiceBy(packageName, object : AccountSource.LoadDataCallback<List<Account>>() {
            override fun onSucceed(data: List<Account>) {
                callback.onSucceed(data)
            }

            override fun onFailure() {
                callback.onFailure()
            }

            override fun onUnknownError(errString: String?) {
                callback.onUnknownError(errString)
            }
        })
    }

    override fun updateAccount(callback: AutofillAccountRepository.LoadCallback) {}
}