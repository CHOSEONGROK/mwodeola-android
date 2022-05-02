package com.jojo.android.mwodeola.presentation.account.create

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.util.Log
import android.util.Patterns
import com.jojo.android.mwodeola.data.account.Account
import com.jojo.android.mwodeola.data.account.AccountDetail
import com.jojo.android.mwodeola.data.account.AccountGroup
import com.jojo.android.mwodeola.data.account.AccountGroupAndDetails
import com.jojo.android.mwodeola.data.common.SnsInfo
import com.jojo.android.mwodeola.model.account.AccountSource
import com.jojo.android.mwodeola.model.common.CommonRepository
import com.jojo.android.mwodeola.model.common.CommonSource
import com.jojo.android.mwodeola.util.notBlankOrNull
import kotlinx.coroutines.*
import java.lang.Exception

class CreateNewAccountPresenter(
    private val view: CreateNewAccountContract.View,
    private val repository: AccountSource,
    private val commonRepository: CommonRepository
) : CreateNewAccountContract.Presenter {

    companion object {
        private const val TAG = "CreateNewAccountPresenter"
        //private val PROTOCOL_REGEX = "^(http|https|rtsp|ftp)://.*".toRegex()
        private val PROTOCOL_REGEX = "^(http|https)://.*".toRegex()
    }

    override var isCompletedLoadingAccountGroups: Boolean = false
    override var isCompletedInstalledAppLoadingJob: Boolean = false

    override val existingGroups = mutableListOf<AccountGroup>()
    override val installedUserApps = mutableListOf<AppInfo>()

    private var snsGroupInfo: SnsInfo? = null

    private var appLoadingJob: Job? = null

    override fun loadAllAccountGroups(packageManager: PackageManager) {
        repository.getAllAccountGroups(object : AccountSource.LoadDataCallback<List<AccountGroup>>() {
            override fun onSucceed(data: List<AccountGroup>) {
                isCompletedLoadingAccountGroups = true

                existingGroups.clear()
                existingGroups.addAll(data)

                view.enabledGroupNameLayout()
                view.setExistingGroupsForBottomSheet(data)

                loadInstalledApplications(packageManager, data)
            }
        })
    }

    override fun loadUserIds() {
        repository.getAllUserIds(object : AccountSource.LoadDataCallback<List<String>>() {
            override fun onSucceed(data: List<String>) {
                view.setUserIdsForAutoComplete(data)
            }
        })
    }

    override fun loadSnsAccountGroup(snsCode: Int) {
        commonRepository.getSnsInfo(object : CommonSource.BaseCallback<List<SnsInfo>>() {
            override fun onSucceed(data: List<SnsInfo>) {
                snsGroupInfo = data.find { it.id == snsCode }

                view.showSnsGroup(snsGroupInfo!!)
            }

            override fun onFailure(errString: String?) {
                view.showToast(errString)
            }
        })
    }

    override fun createNormalGroupAndNormalDetail() {
        if (checkInputFields().not())
            return

        val validWebUrl =
            if (isValidWebUrl(view.webUrl)) view.webUrl
            else null

        val newGroup = AccountGroup.empty().apply {
            group_name = view.mAppInfo!!.label
            val packageName = view.mAppInfo?.packageName
            if (packageName != null) {
                app_package_name = packageName
                icon_type = AccountGroup.ICON_TYPE_INSTALLED_APP_LOGO
            }
            web_url = validWebUrl
        }
        val newDetail = AccountDetail.empty().apply {
            user_id = view.userID.notBlankOrNull()
            user_password = view.password.notBlankOrNull()
            user_password_pin4 = if (view.passwordPin4.length == 4) view.passwordPin4 else null
            user_password_pin6 = if (view.passwordPin6.length == 6) view.passwordPin6 else null
            user_password_pattern = view.passwordPattern.notBlankOrNull()
            memo = view.memo.notBlankOrNull()
        }
        val newAccount = Account(
            account_id = "",
            created_at = "",
            own_group = newGroup,
            sns_group = null,
            detail = newDetail
        )

        repository.createNewAccount(newAccount, object : AccountSource.LoadDataCallback<Account>() {
            override fun onSucceed(data: Account) {
                val newGroupAndDetails = AccountGroupAndDetails(
                    own_group = data.own_group,
                    accounts = mutableListOf(data)
                )

                view.finishForSuccess(newGroupAndDetails)
            }
        })
    }

    override fun createNormalGroupAndSnsDetail(snsDetailId: String) {
        if (view.mAppInfo?.label.isNullOrBlank())
            return

        val validWebUrl =
            if (isValidWebUrl(view.webUrl)) view.webUrl
            else null

        val newGroup = AccountGroup.empty().apply {
            group_name = view.mAppInfo!!.label
            val packageName = view.mAppInfo?.packageName
            if (packageName != null) {
                app_package_name = packageName
                icon_type = AccountGroup.ICON_TYPE_INSTALLED_APP_LOGO
            }
            web_url = validWebUrl
        }

        repository.createNewAccountGroupWithSnsDetail(newGroup, snsDetailId, object : AccountSource.LoadDataCallback<Account>() {
            override fun onSucceed(data: Account) {
                val newGroupAndDetails = AccountGroupAndDetails(
                    own_group = data.own_group,
                    accounts = mutableListOf(data)
                )

                view.finishForSuccess(newGroupAndDetails)
            }
        })
    }

    override fun createSnsGroupAndSnsDetail() {
        if (checkInputFields().not())
            return

        val validWebUrl =
            if (isValidWebUrl(view.webUrl)) view.webUrl
            else null

        val newGroup = AccountGroup.newInstanceForSns(
            snsCode = snsGroupInfo!!.id,
            group_name = snsGroupInfo!!.name,
            web_url = validWebUrl,
            is_favorite = false
        )
        val newDetail = AccountDetail.empty().apply {
            user_id = view.userID.notBlankOrNull()
            user_password = view.password.notBlankOrNull()
            user_password_pin4 = if (view.passwordPin4.length == 4) view.passwordPin4 else null
            user_password_pin6 = if (view.passwordPin6.length == 6) view.passwordPin6 else null
            user_password_pattern = view.passwordPattern.notBlankOrNull()
            memo = view.memo.notBlankOrNull()
        }
        val newAccount = Account(
            account_id = "",
            created_at = "",
            own_group = newGroup,
            sns_group = null,
            detail = newDetail
        )

        repository.createNewAccount(newAccount, object : AccountSource.LoadDataCallback<Account>() {
            override fun onSucceed(data: Account) {
                val newGroupAndDetails = AccountGroupAndDetails(
                    own_group = data.own_group,
                    accounts = mutableListOf(data)
                )

                view.finishForSuccess(newGroupAndDetails)
            }
        })
    }

    override fun isValidWebUrl(url: String): Boolean =
        (Patterns.WEB_URL.matcher(url).matches() && PROTOCOL_REGEX.matches(url))

    override fun checkInputFields(): Boolean =
        view.mAppInfo?.label?.isNotBlank() == true &&
                (view.userID.isNotBlank() ||
                        view.password.isNotBlank() ||
                        view.passwordPin4.length == 4 ||
                        view.passwordPin6.length == 6 ||
                        view.passwordPattern.isNotBlank() ||
                        view.memo.isNotBlank())

    override fun onDestroy() {
        if (appLoadingJob?.isActive == true) {
            appLoadingJob?.cancel()
        }
    }

    private fun loadInstalledApplications(
        packageManager: PackageManager,
        existingList: List<AccountGroup>
    ) {
        val userApps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
            .filter { isUserApp(it) }

        if (installedUserApps.size == userApps.size)
            return

        if (appLoadingJob != null) {
            appLoadingJob?.cancel()
            appLoadingJob = null
        }

        appLoadingJob = CoroutineScope(Dispatchers.Default).launch {
            Log.w(TAG, "loadAppsJob.Start !!")
            isCompletedInstalledAppLoadingJob = false

            userApps.forEachIndexed { index, app ->
                try {
                    val icon = app.loadIcon(packageManager)
                    val appName = app.loadLabel(packageManager).toString()
                    val packageName = app.packageName
                    val existingAccountGroup = existingList.find {
                        it.group_name == appName ||
                                it.app_package_name == packageName
                    }

                    val appInfo = AppInfo(icon, appName, packageName, existingAccountGroup)
                    installedUserApps.add(appInfo)
                } catch (e: Exception) {
                    Log.w(TAG, "loadInstalledApplications(): e=$e")
                }
            }

            installedUserApps.sortBy { it.label }

            withContext(Dispatchers.Main) {
                view.setInstalledUserAppsForBottomSheet(installedUserApps)
            }

            isCompletedInstalledAppLoadingJob = true
            Log.w(TAG, "loadAppsJob.End !!")
        }
    }

    private fun isUserApp(app: ApplicationInfo): Boolean =
        (app.flags and ApplicationInfo.FLAG_SYSTEM == 0) || (app.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP > 0)

    private fun testUserApp(index: Int, appName: String, app: ApplicationInfo) {
        val log = StringBuilder("App[$index]: appName=$appName, flags=${app.flags}")
            .appendLine()
            .appendLine("FLAG_SYSTEM=${app.flags and ApplicationInfo.FLAG_SYSTEM}")
            .appendLine("FLAG_UPDATED_SYSTEM_APP=${app.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP}")
            .toString()

        Log.i(TAG, log)
    }
}