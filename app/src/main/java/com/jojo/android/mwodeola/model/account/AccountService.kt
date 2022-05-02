package com.jojo.android.mwodeola.model.account

import com.jojo.android.mwodeola.data.account.Account
import com.jojo.android.mwodeola.data.account.AccountDetail
import com.jojo.android.mwodeola.data.account.AccountGroup
import com.jojo.android.mwodeola.data.account.AccountGroupAndDetails
import retrofit2.Call
import retrofit2.http.*



interface AccountService {
    companion object {
        private const val PATH_ACCOUNT_GROUP = "account/group"
        private const val PATH_ACCOUNT_GROUP_SNS = "account/group/sns"
        private const val PATH_ACCOUNT_GROUP_FAVORITE = "account/group/favorite"
        private const val PATH_ACCOUNT_GROUP_DETAIL = "account/group/detail"
        private const val PATH_ACCOUNT_GROUP_DETAIL_ALL = "account/group/detail/all"
        private const val PATH_ACCOUNT_GROUP_DETAIL_ALL_SIMPLE = "account/group/detail/all/simple"
        private const val PATH_ACCOUNT_GROUP_SNS_DETAIL = "account/group/sns_detail"
        private const val PATH_ACCOUNT_DETAIL = "account/detail"
        private const val PATH_SEARCH_GROUP = "account/search/group"
        private const val PATH_SEARCH_DETAIL = "account/search/detail"
        private const val PATH_ACCOUNT_USER_ID_ALL = "account/user_id/all"
        private const val PATH_ACCOUNT_FOR_AUTOFILL_SERVICE = "account/for_autofill_service"
    }

    @GET(PATH_ACCOUNT_GROUP)
    fun getAllAccountGroup(): Call<List<AccountGroup>>

    @PUT(PATH_ACCOUNT_GROUP)
    fun updateAccountGroup(@Body body: AccountGroup): Call<AccountGroup>

    @HTTP(method = "DELETE", path = PATH_ACCOUNT_GROUP, hasBody = true)
    fun deleteAccountGroups(@Body body: HashMap<String, List<String>>): Call<Any>

    @GET(PATH_ACCOUNT_GROUP_SNS)
    fun getSnsAccountGroups(): Call<List<AccountGroup>>

    @FormUrlEncoded
    @PUT(PATH_ACCOUNT_GROUP_FAVORITE)
    fun updateFavorite(
        @Field("account_group_id") account_group_id: String,
        @Field("is_favorite") is_favorite: Boolean
    ): Call<Any>


    @GET(PATH_ACCOUNT_GROUP_DETAIL)
    fun getAccount(@Query("account_id") account_id: String): Call<Account>

    @POST(PATH_ACCOUNT_GROUP_DETAIL)
    fun createNewAccount(@Body body: Account): Call<Account>

    @PUT(PATH_ACCOUNT_GROUP_DETAIL)
    fun updateAccount(@Body body: Account): Call<Account>


    @GET(PATH_ACCOUNT_GROUP_DETAIL_ALL)
    fun getAllAccountDetailsInGroup(@Query("group_id") group_id: String): Call<List<Account>>

    @GET(PATH_ACCOUNT_GROUP_DETAIL_ALL_SIMPLE)
    fun getAllSimpleAccountDetailsInGroup(@Query("group_id") group_id: String): Call<List<Account>>

    @POST(PATH_ACCOUNT_GROUP_SNS_DETAIL)
    fun createNewAccountGroupWithSnsDetail(@Body body: HashMap<String, Any>): Call<Account>

    @FormUrlEncoded
    @PUT(PATH_ACCOUNT_GROUP_SNS_DETAIL)
    fun addSnsDetailToGroup(
        @Field("account_group_id") account_group_id: String,
        @Field("sns_detail_id") sns_detail_id: String
    ): Call<Account>

    @HTTP(method = "DELETE", path = PATH_ACCOUNT_GROUP_SNS_DETAIL, hasBody = true)
    fun deleteSnsDetail(@Body body: HashMap<String, String>): Call<Any>

    @POST(PATH_ACCOUNT_DETAIL)
    fun addNewDetail(@Body body: AccountDetail): Call<Account>

    @HTTP(method = "DELETE", path = PATH_ACCOUNT_DETAIL, hasBody = true)
    fun deleteDetail(@Body body: HashMap<String, String>): Call<Any>

    @GET(PATH_ACCOUNT_USER_ID_ALL)
    fun getAllUserIds(): Call<List<String>>

    @GET(PATH_SEARCH_GROUP)
    fun searchAccountGroup(@Query("group_name") keyword: String): Call<List<AccountGroup>>

    @GET(PATH_SEARCH_DETAIL)
    fun searchAccountDetail(@Query("user_id") keyword: String): Call<List<Account>>

    @GET(PATH_ACCOUNT_FOR_AUTOFILL_SERVICE)
    fun getAccountsForAutofillServiceBy(@Query("app_package_name") app_package_name: String): Call<List<Account>>

    @FormUrlEncoded
    @POST(PATH_ACCOUNT_FOR_AUTOFILL_SERVICE)
    fun saveAccountForAutofillService(
        @Field("app_package_name") app_package_name: String,
        @Field("group_name") group_name: String,
        @Field("user_id") user_id: String?,
        @Field("user_password") user_password: String
    ): Call<HashMap<String, String>>
}