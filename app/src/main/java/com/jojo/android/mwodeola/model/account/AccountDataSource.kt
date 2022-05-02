package com.jojo.android.mwodeola.model.account

import android.content.Context
import android.util.Log
import com.jojo.android.mwodeola.autofill.repository.AutofillAccountRepository
import com.jojo.android.mwodeola.data.account.Account
import com.jojo.android.mwodeola.data.account.AccountDetail
import com.jojo.android.mwodeola.data.account.AccountGroup
import com.jojo.android.mwodeola.data.account.AccountGroupAndDetails
import com.jojo.android.mwodeola.model.ErrorResponse
import com.jojo.android.mwodeola.model.ErrorResponse.Companion.ERROR_CODE_DUPLICATED_FIELD
import com.jojo.android.mwodeola.model.ErrorResponse.Companion.ERROR_CODE_TOKEN_NOT_VALID
import com.jojo.android.mwodeola.model.RetrofitService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AccountDataSource(private val context: Context) : AccountSource {
    companion object { private const val TAG = "AccountDataSource" }

    private val service: AccountService
        get() = RetrofitService.getAccountService(context)

    override fun getAllAccountGroups(callback: AccountSource.LoadDataCallback<List<AccountGroup>>) {
        service.getAllAccountGroup().enqueue(object : Callback<List<AccountGroup>> {
            override fun onResponse(call: Call<List<AccountGroup>>, response: Response<List<AccountGroup>>) {

                val responseBody = response.body()
                if (response.isSuccessful && responseBody != null) {
                    callback.onSucceed(responseBody)
                } else {
                    val errorResponse = ErrorResponse.createBy(response)

                    callback.onUnknownError(errorResponse.toString())
                }
            }

            override fun onFailure(call: Call<List<AccountGroup>>, t: Throwable) {
                callback.onUnknownError(t.toString())
            }
        })
    }

    override fun updateAccountGroup(accountGroup: AccountGroup, callback: AccountSource.LoadDataCallback<AccountGroup>) {
        service.updateAccountGroup(accountGroup).enqueue(object : Callback<AccountGroup> {
            override fun onResponse(call: Call<AccountGroup>, response: Response<AccountGroup>) {

                val responseBody = response.body()
                if (response.isSuccessful && responseBody != null) {
                    callback.onSucceed(responseBody)
                } else {
                    val errorResponse = ErrorResponse.createBy(response)

                    if (errorResponse.code == ERROR_CODE_TOKEN_NOT_VALID) {
                        callback.onFailureDuplicatedGroupName(accountGroup.group_name)
                    } else {
                        callback.onUnknownError(errorResponse.toString())
                    }
                }
            }

            override fun onFailure(call: Call<AccountGroup>, t: Throwable) {
                callback.onUnknownError(t.toString())
            }
        })
    }

    override fun deleteAccountGroup(accountGroupIds: List<String>, callback: AccountSource.BaseCallback) {
        val body = hashMapOf<String, List<String>>()
        body["account_group_ids"] = accountGroupIds

        service.deleteAccountGroups(body).enqueue(object : Callback<Any> {
            override fun onResponse(call: Call<Any>, response: Response<Any>) {
                if (response.isSuccessful) {
                    callback.onSucceed()
                } else {
                    val errorResponse = ErrorResponse.createBy(response)
                    callback.onUnknownError(errorResponse.toString())
                }
            }

            override fun onFailure(call: Call<Any>, t: Throwable) {
                callback.onUnknownError(t.toString())
            }
        })
    }

    override fun getAllSnsAccountGroups(callback: AccountSource.LoadDataCallback<List<AccountGroup>>) {
        service.getSnsAccountGroups().enqueue(object : Callback<List<AccountGroup>> {
            override fun onResponse(
                call: Call<List<AccountGroup>>,
                response: Response<List<AccountGroup>>
            ) {
                val responseBody = response.body()

                if (response.isSuccessful && responseBody != null) {
                    callback.onSucceed(responseBody)
                } else {
                    val errorResponse = ErrorResponse.createBy(response)
                    callback.onUnknownError(errorResponse.toString())
                }
            }

            override fun onFailure(call: Call<List<AccountGroup>>, t: Throwable) {
                callback.onUnknownError(t.toString())
            }
        })
    }

    override fun updateFavorite(
        accountGroupId: String,
        isFavorite: Boolean,
        callback: AccountSource.FavoriteCallback
    ) {
        service.updateFavorite(accountGroupId, isFavorite).enqueue(object : Callback<Any> {
            override fun onResponse(call: Call<Any>, response: Response<Any>) {
                if (response.isSuccessful) {
                    callback.onSucceed(accountGroupId, isFavorite)
                } else {
                    val errorResponse = ErrorResponse.createBy(response)
                    callback.onUnknownError(errorResponse.toString())
                }
            }

            override fun onFailure(call: Call<Any>, t: Throwable) {
                callback.onUnknownError(t.toString())
            }
        })
    }

    override fun getAccount(accountId: String, callback: AccountSource.LoadDataCallback<Account>) {
        service.getAccount(accountId).enqueue(object : Callback<Account> {
            override fun onResponse(call: Call<Account>, response: Response<Account>) {
                val responseBody = response.body()

                if (response.isSuccessful && responseBody != null) {
                    callback.onSucceed(responseBody)
                } else {
                    val errorResponse = ErrorResponse.createBy(response)
                    callback.onUnknownError(errorResponse.toString())
                }
            }

            override fun onFailure(call: Call<Account>, t: Throwable) {
                callback.onUnknownError(t.toString())
            }
        })
    }

    override fun createNewAccount(
        account: Account,
        callback: AccountSource.LoadDataCallback<Account>
    ) {
        service.createNewAccount(account).enqueue(object : Callback<Account> {
            override fun onResponse(call: Call<Account>, response: Response<Account>) {
                val responseBody = response.body()

                if (response.isSuccessful && responseBody != null) {
                    callback.onSucceed(responseBody)
                } else {
                    val errorResponse = ErrorResponse.createBy(response)
                    if (errorResponse.code == ERROR_CODE_DUPLICATED_FIELD) {
                        callback.onFailureDuplicatedGroupName(account.own_group.group_name)
                    } else {
                        callback.onUnknownError(errorResponse.toString())
                    }
                }
            }

            override fun onFailure(call: Call<Account>, t: Throwable) {
                callback.onUnknownError(t.toString())
            }
        })
    }

    override fun updateAccount(
        account: Account,
        callback: AccountSource.LoadDataCallback<Account>
    ) {
        service.updateAccount(account).enqueue(object : Callback<Account> {
            override fun onResponse(call: Call<Account>, response: Response<Account>) {
                val responseBody = response.body()

                if (response.isSuccessful && responseBody != null) {
                    callback.onSucceed(responseBody)
                } else {
                    val errorResponse = ErrorResponse.createBy(response)
                    if (errorResponse.code == ERROR_CODE_DUPLICATED_FIELD) {
                        callback.onFailureDuplicatedGroupName(account.own_group.group_name)
                    } else {
                        callback.onUnknownError(errorResponse.toString())
                    }
                }
            }

            override fun onFailure(call: Call<Account>, t: Throwable) {
                callback.onUnknownError(t.toString())
            }
        })
    }

    override fun getAllAccountDetailsInGroup(
        accountGroupId: String,
        callback: AccountSource.LoadDataCallback<AccountGroupAndDetails>
    ) {
        service.getAllAccountDetailsInGroup(accountGroupId).enqueue(object : Callback<List<Account>> {
            override fun onResponse(
                call: Call<List<Account>>,
                response: Response<List<Account>>
            ) {
                val responseBody = response.body()

                if (response.isSuccessful && responseBody != null) {
                    val ownGroup = responseBody.firstOrNull()?.own_group
                        ?: AccountGroup.empty()

                    val sortedData = AccountGroupAndDetails(ownGroup, responseBody.toMutableList()).also {
                        it.accounts.sort()
                    }

                    callback.onSucceed(sortedData)
                } else {
                    val errorResponse = ErrorResponse.createBy(response)
                    callback.onUnknownError(errorResponse.toString())
                }
            }

            override fun onFailure(call: Call<List<Account>>, t: Throwable) {
                callback.onUnknownError(t.toString())
            }
        })
    }

    override fun getAllSimpleAccountDetailsInGroup(
        accountGroupId: String,
        callback: AccountSource.LoadDataCallback<AccountGroupAndDetails>
    ) {
        service.getAllSimpleAccountDetailsInGroup(accountGroupId).enqueue(object : Callback<List<Account>> {
            override fun onResponse(call: Call<List<Account>>, response: Response<List<Account>>) {
                val responseBody = response.body()

                if (response.isSuccessful && responseBody != null) {
                    val ownGroup = responseBody.firstOrNull()?.own_group
                        ?: AccountGroup.empty()


                    val sortedData = AccountGroupAndDetails(ownGroup, responseBody.toMutableList()).also {
                        it.accounts.forEachIndexed { index, account ->
                            Log.i(TAG, "[$index]: $account")
                        }
                        it.accounts.sort()
                        it.accounts.forEachIndexed { index, account ->
                            Log.i(TAG, "[$index]: $account")
                        }
                    }

                    callback.onSucceed(sortedData)
                } else {
                    val errorResponse = ErrorResponse.createBy(response)
                    callback.onUnknownError(errorResponse.toString())
                }
            }

            override fun onFailure(call: Call<List<Account>>, t: Throwable) {
                callback.onUnknownError(t.toString())
            }
        })
    }

    override fun createNewAccountGroupWithSnsDetail(
        accountGroup: AccountGroup,
        snsDetailId: String,
        callback: AccountSource.LoadDataCallback<Account>
    ) {
        val body = hashMapOf<String, Any>()
        body["own_group"] = accountGroup
        body["sns_detail_id"] = snsDetailId
        service.createNewAccountGroupWithSnsDetail(body).enqueue(object : Callback<Account> {
            override fun onResponse(call: Call<Account>, response: Response<Account>) {
                val responseBody = response.body()

                if (response.isSuccessful && responseBody != null) {
                    callback.onSucceed(responseBody)
                } else {
                    val errorResponse = ErrorResponse.createBy(response)
                    if (errorResponse.code == ERROR_CODE_DUPLICATED_FIELD) {
                        callback.onFailureDuplicatedGroupName(accountGroup.group_name)
                    } else {
                        callback.onUnknownError(errorResponse.toString())
                    }
                }
            }

            override fun onFailure(call: Call<Account>, t: Throwable) {
                callback.onUnknownError(t.toString())
            }
        })
    }

    override fun addSnsDetailToGroup(
        accountGroupId: String,
        snsDetailId: String,
        callback: AccountSource.LoadDataCallback<Account>
    ) {
        service.addSnsDetailToGroup(accountGroupId, snsDetailId).enqueue(object : Callback<Account> {
            override fun onResponse(call: Call<Account>, response: Response<Account>) {
                val responseBody = response.body()

                if (response.isSuccessful && responseBody != null) {
                    callback.onSucceed(responseBody)
                } else {
                    val errorResponse = ErrorResponse.createBy(response)
                    callback.onUnknownError(errorResponse.toString())
                }
            }

            override fun onFailure(call: Call<Account>, t: Throwable) {
                callback.onUnknownError(t.toString())
            }
        })
    }

    override fun deleteSnsDetail(accountId: String, callback: AccountSource.BaseCallback) {
        val body = hashMapOf(Pair("account_id", accountId))

        service.deleteSnsDetail(body).enqueue(object : Callback<Any> {
            override fun onResponse(call: Call<Any>, response: Response<Any>) {
                if (response.isSuccessful) {
                    callback.onSucceed()
                } else {
                    val errorResponse = ErrorResponse.createBy(response)
                    callback.onUnknownError(errorResponse.toString())
                }
            }

            override fun onFailure(call: Call<Any>, t: Throwable) {
                callback.onUnknownError(t.toString())
            }
        })
    }

    override fun addNewDetail(
        accountDetail: AccountDetail,
        callback: AccountSource.LoadDataCallback<Account>
    ) {
        service.addNewDetail(accountDetail).enqueue(object : Callback<Account> {
            override fun onResponse(call: Call<Account>, response: Response<Account>) {
                val responseBody = response.body()

                if (response.isSuccessful && responseBody != null) {
                    callback.onSucceed(responseBody)
                } else {
                    val errorResponse = ErrorResponse.createBy(response)
                    callback.onUnknownError(errorResponse.toString())
                }
            }

            override fun onFailure(call: Call<Account>, t: Throwable) {
                callback.onUnknownError(t.toString())
            }
        })
    }

    override fun deleteDetail(accountDetailId: String, callback: AccountSource.BaseCallback) {
        val body = hashMapOf(Pair("account_detail_id", accountDetailId))

        service.deleteDetail(body).enqueue(object : Callback<Any> {
            override fun onResponse(call: Call<Any>, response: Response<Any>) {
                if (response.isSuccessful) {
                    callback.onSucceed()
                } else {
                    val errorResponse = ErrorResponse.createBy(response)
                    callback.onUnknownError(errorResponse.toString())
                }
            }

            override fun onFailure(call: Call<Any>, t: Throwable) {
                callback.onUnknownError(t.toString())
            }
        })
    }

    override fun getAllUserIds(callback: AccountSource.LoadDataCallback<List<String>>) {
        service.getAllUserIds().enqueue(object : Callback<List<String>> {
            override fun onResponse(call: Call<List<String>>, response: Response<List<String>>) {
                val responseBody = response.body()
                if (response.isSuccessful && responseBody != null) {
                    callback.onSucceed(responseBody)
                } else {
                    val errorResponse = ErrorResponse.createBy(response)
                    callback.onUnknownError(errorResponse.toString())
                }
            }

            override fun onFailure(call: Call<List<String>>, t: Throwable) {
                callback.onUnknownError(t.toString())
            }
        })
    }

    override fun searchAccountGroup(
        keyword: String,
        callback: AccountSource.LoadDataCallback<List<AccountGroup>>
    ) {
        service.searchAccountGroup(keyword).enqueue(object : Callback<List<AccountGroup>> {
            override fun onResponse(
                call: Call<List<AccountGroup>>,
                response: Response<List<AccountGroup>>
            ) {
                val responseBody = response.body()
                if (response.isSuccessful && responseBody != null) {
                    callback.onSucceed(responseBody)
                } else {
                    val errorResponse = ErrorResponse.createBy(response)
                    callback.onUnknownError(errorResponse.toString())
                }
            }

            override fun onFailure(call: Call<List<AccountGroup>>, t: Throwable) {
                callback.onUnknownError(t.toString())
            }
        })
    }

    override fun searchAccountDetail(
        keyword: String,
        callback: AccountSource.LoadDataCallback<List<Account>>
    ) {
        service.searchAccountDetail(keyword).enqueue(object : Callback<List<Account>> {
            override fun onResponse(call: Call<List<Account>>, response: Response<List<Account>>) {
                val responseBody = response.body()

                if (response.isSuccessful && responseBody != null) {
                    callback.onSucceed(responseBody)
                } else {
                    val errorResponse = ErrorResponse.createBy(response)
                    callback.onUnknownError(errorResponse.toString())
                }
            }

            override fun onFailure(call: Call<List<Account>>, t: Throwable) {
                callback.onUnknownError(t.toString())
            }
        })
    }

    override fun getAccountsForAutofillServiceBy(
        appPackageName: String,
        callback: AccountSource.LoadDataCallback<List<Account>>
    ) {
        service.getAccountsForAutofillServiceBy(appPackageName).enqueue(object : Callback<List<Account>> {
            override fun onResponse(call: Call<List<Account>>, response: Response<List<Account>>) {
                val responseBody = response.body()

                if (response.isSuccessful && responseBody != null) {
                    callback.onSucceed(responseBody)
                } else {
                    val errorResponse = ErrorResponse.createBy(response)
                    callback.onUnknownError(errorResponse.toString())
                }
            }

            override fun onFailure(call: Call<List<Account>>, t: Throwable) {
                callback.onUnknownError(t.toString())
            }
        })
    }

    override fun saveAccountForAutofillService(
        packageName: String,
        appName: String,
        userID: String?,
        userPassword: String,
        callback: AutofillAccountRepository.SaveCallback
    ) {
        service.saveAccountForAutofillService(
            packageName, appName, userID, userPassword
        ).enqueue(object : Callback<HashMap<String, String>> {
            override fun onResponse(call: Call<HashMap<String, String>>, response: Response<HashMap<String, String>>) {
                val responseBody = response.body()
                if (response.isSuccessful && responseBody != null) {
                    callback.onSucceed(responseBody["code"] ?: "")
                } else {
                    val errorResponse = ErrorResponse.createBy(response)
                    callback.onUnknownError(errorResponse.toString())
                }
            }

            override fun onFailure(call: Call<HashMap<String, String>>, t: Throwable) {
                callback.onUnknownError(t.toString())
            }
        })
    }

    //    private fun <T> refreshTokenAndReRequestCall(pendingCall: Call<T>, pendingCallback: Callback<T>) {
//        val refreshToken = TokenSharedPref.REFRESH_TOKEN ?: return
//
//        RetrofitService.signUpAPI.refreshToken(refreshToken).enqueue(object : Callback<TokenPair> {
//            override fun onResponse(call: Call<TokenPair>, response: Response<TokenPair>) {
//                Log.i(TAG, "checkTokenExpirationResponseAndReRequest.onResponse(): response=$response")
//
//                val responseBody = response.body()
//                if (response.isSuccessful && responseBody != null) {
//                    TokenSharedPref.setAccessToken(context, responseBody.access)
//
//                    pendingCall.clone().enqueue(pendingCallback)
//                } else {
//                    val responseError = ErrorResponse.createBy(response)
//                    Log.w(TAG, "checkTokenExpirationResponseAndReRequest.onResponse(): $responseError")
//                }
//            }
//
//            override fun onFailure(call: Call<TokenPair>, t: Throwable) {
//                Log.w(TAG, "checkTokenExpirationResponseAndReRequest.onResponse(): $t")
//            }
//        })
//    }
}