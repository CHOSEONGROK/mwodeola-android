package com.jojo.android.mwodeola.presentation.account.create

import android.content.pm.PackageManager
import com.jojo.android.mwodeola.data.account.Account
import com.jojo.android.mwodeola.data.account.AccountGroup
import com.jojo.android.mwodeola.data.account.AccountGroupAndDetails
import com.jojo.android.mwodeola.data.common.SnsInfo

interface CreateNewAccountContract {

    interface View {
        val mAppInfo: AppInfo?
        val webUrl: String
        val userID: String
        val password: String
        val passwordPin4: String
        val passwordPin6: String
        val passwordPattern: String
        val memo: String

        fun initView()

        fun showSnsGroup(snsInfo: SnsInfo)

        fun setSnsDetailInfo(snsAccount: Account)

        fun setExistingGroupsForBottomSheet(groups: List<AccountGroup>)
        fun setInstalledUserAppsForBottomSheet(apps: List<AppInfo>)
        fun setUserIdsForAutoComplete(userIds: List<String>)

        fun enabledGroupNameLayout()
        fun showHelpLabels()
        fun hideHelpLabels()
        fun showMainIcon()
        fun showInputWidgets()
        fun showCreateButton(enabled: Boolean)

        fun showFinishConfirmDialog()

        fun finishForSuccess(account: AccountGroupAndDetails)

        fun showToast(message: String?)
    }

    interface Presenter {
        val isCompletedLoadingAccountGroups: Boolean
        val isCompletedInstalledAppLoadingJob: Boolean

        val existingGroups: List<AccountGroup>
        val installedUserApps: List<AppInfo>

        fun loadAllAccountGroups(packageManager: PackageManager)
        fun loadUserIds()
        fun loadSnsAccountGroup(snsCode: Int)
        fun createNormalGroupAndNormalDetail()
        fun createNormalGroupAndSnsDetail(snsDetailId: String)
        fun createSnsGroupAndSnsDetail()

        fun isValidWebUrl(url: String): Boolean
        fun checkInputFields(): Boolean

        fun onDestroy()
    }
}