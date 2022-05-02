package com.jojo.android.mwodeola.model.account

import android.util.Log
import com.jojo.android.mwodeola.autofill.repository.AutofillAccountRepository
import com.jojo.android.mwodeola.data.account.Account
import com.jojo.android.mwodeola.data.account.AccountDetail
import com.jojo.android.mwodeola.data.account.AccountGroup
import com.jojo.android.mwodeola.data.account.AccountGroupAndDetails


interface AccountSource {
    companion object {
        private const val TAG = "AccountSource"
    }

    abstract class BaseCallback {
        abstract fun onSucceed()
        open fun onFailure() {}
        open fun onUnknownError(errString: String?) {
            Log.w(TAG, "onUnknownError(): $errString")
        }
    }

    abstract class LoadDataCallback<T> {
        abstract fun onSucceed(data: T)
        open fun onFailureDuplicatedGroupName(groupName: String) {}
        open fun onFailure() {}
        open fun onUnknownError(errString: String?) {
            Log.w(TAG, "onUnknownError(): $errString")
        }
    }

    abstract class FavoriteCallback {
        abstract fun onSucceed(accountGroupId: String, isFavorite: Boolean)
        open fun onFailure() {}
        open fun onUnknownError(errString: String?) {
            Log.w(TAG, "onUnknownError(): $errString")
        }
    }

    /**
     * get all account groups
     * */
    fun getAllAccountGroups(callback: LoadDataCallback<List<AccountGroup>>)

    /**
     * update account group:
     * AccountGroup 에 sns_detail 하나만 존재하는 경우,
     * detail 수정이 불가하므로 group 만 수정하기 위한 api
     * */
    fun updateAccountGroup(accountGroup: AccountGroup,
                           callback: LoadDataCallback<AccountGroup>)

    /**
     * delete account group
     * */
    fun deleteAccountGroup(accountGroupIds: List<String>,
                           callback: BaseCallback)

    /**
     * get all sns account groups
     * */
    fun getAllSnsAccountGroups(callback: LoadDataCallback<List<AccountGroup>>)

    /**
     * update favorite of AccountGroup
     * */
    fun updateFavorite(accountGroupId: String, isFavorite: Boolean,
                       callback: FavoriteCallback)

    /**
     * get Account(Group, Detail, (sns_group))
     * */
    fun getAccount(accountId: String,
                   callback: LoadDataCallback<Account>)

    /**
     * create new Account(Group, Detail(=no sns_detail))
     * */
    fun createNewAccount(account: Account,
                         callback: LoadDataCallback<Account>)

    /**
     * update Account(Group, Detail(=no sns_detail))
     * */
    fun updateAccount(account: Account,
                      callback: LoadDataCallback<Account>)

    /**
     * get all AccountDetails in AccountGroup
     * note: This api does increase detail's views count
     * */
    fun getAllAccountDetailsInGroup(accountGroupId: String,
                                    callback: LoadDataCallback<AccountGroupAndDetails>)

    /**
     * get all Simple AccountDetails in AccountGroup
     * note: This api does not increase detail's views count
     * */
    fun getAllSimpleAccountDetailsInGroup(accountGroupId: String,
                                          callback: LoadDataCallback<AccountGroupAndDetails>)

    /**
     * create new AccountGroup with sns_detail
     * 새 AccountGroup 을 생성하면서,
     * 자가 계정이 아닌 sns_account_group 의 detail 을 연동하는 api
     * */
    fun createNewAccountGroupWithSnsDetail(accountGroup: AccountGroup, snsDetailId: String,
                                      callback: LoadDataCallback<Account>)

    /**
     * add sns_detail to AccountGroup
     * 기존 AccountGroup 에 sns_detail 추가(연동)
     * */
    fun addSnsDetailToGroup(accountGroupId: String, snsDetailId: String,
                            callback: LoadDataCallback<Account>)

    /**
     * delete sns_detail
     * AccountGroup 에 연결된 sns_detail 제거.
     * ## AccountGroup 에 detail 갯수가 0일 경우 AccountGroup 도 자동 삭제됨.
     * */
    fun deleteSnsDetail(accountId: String, callback: BaseCallback)

    /**
     * add new AccountDetail
     * 기존 AccountGroup 에 새 AccountDetail 추가.
     * */
    fun addNewDetail(accountDetail: AccountDetail, callback: LoadDataCallback<Account>)

    /**
     * delete AccountDetail
     * ## AccountGroup 에 detail 갯수가 0일 경우 AccountGroup 도 자동 삭제됨.
     * */
    fun deleteDetail(accountDetailId: String, callback: BaseCallback)

    /**
     * get all UserIds
     * */
    fun getAllUserIds(callback: LoadDataCallback<List<String>>)

    /**
     * search AccountGroup: keyword=group_name
     * */
    fun searchAccountGroup(keyword: String, callback: LoadDataCallback<List<AccountGroup>>)

    /**
     * search AccountDetail: keyword=user_id
     * */
    fun searchAccountDetail(keyword: String, callback: LoadDataCallback<List<Account>>)

    /**
     * get Accounts for Autofill Service By App PackageName
     * */
    fun getAccountsForAutofillServiceBy(appPackageName: String, callback: LoadDataCallback<List<Account>>)

    /**
     * get Accounts for Autofill Service By App PackageName
     * */
    fun saveAccountForAutofillService(
        packageName: String, appName: String,
        userID: String?, userPassword: String,
        callback: AutofillAccountRepository.SaveCallback
    )
}