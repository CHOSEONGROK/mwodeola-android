package com.jojo.android.mwodeola.presentation.account.datalist

import android.util.Log
import com.jojo.android.mwodeola.model.account.AccountSource
import com.jojo.android.mwodeola.data.account.AccountGroup

class AccountGroupListPresenter(
    private val view: AccountGroupListContract.View,
    private val repository: AccountSource
) : AccountGroupListContract.Presenter {
    companion object {
        private const val TAG = "AccountGroupListPresenter"
    }

    override fun loadData(isNewData: Boolean, accountGroupID: String) {

    }

    override fun loadAllData() {
        repository.getAllAccountGroups(object : AccountSource.LoadDataCallback<List<AccountGroup>>() {
            override fun onSucceed(data: List<AccountGroup>) {
                Log.i(TAG, "onSucceed(): data.size=${data.size}")
                view.showAllAccountGroups(data)
            }
        })
    }

    override fun deleteData(idsOfAccountGroup: List<String>) {
        // view.removeAccountGroups(idsOfAccountGroup)
        repository.deleteAccountGroup(idsOfAccountGroup, object : AccountSource.BaseCallback() {
            override fun onSucceed() {
                view.removeAccountGroups(idsOfAccountGroup)
            }
        })
    }

    fun loadAccount(accountId: String) {

    }

}