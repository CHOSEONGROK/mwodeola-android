package com.jojo.android.mwodeola.presentation.account.datalist

import android.widget.CheckBox
import androidx.recyclerview.selection.SelectionTracker
import com.jojo.android.mwodeola.data.account.Account
import com.jojo.android.mwodeola.data.account.AccountGroup

interface AccountGroupListContract {
    
    interface View {
        val selectionTracker: SelectionTracker<Long>
        val checkBoxSelectAll: CheckBox

        fun initView()

        fun showAllAccountGroups(accountGroups: List<AccountGroup>)
        fun addNewAccountGroup(accountGroup: AccountGroup)
        fun updateAccountGroup(accountGroup: AccountGroup)
        fun removeAccountGroups(ids: List<String>)

        fun stopShimmerAndShowRecyclerView()

        fun startSelectionMode()
        fun cancelSelectionMode()

        fun updateTitleInSelectionMode(count: Int)

        fun smoothScrollWithEndAction(position: Int, endAction: Runnable? = null)

        fun showDeleteAccountGroupWarning(idsOfAccountGroup: List<String>)
        fun showSelectAccountInGroupDialog(accountGroupID: String)

        fun startCreateNewAccountActivity()
        fun startCreateNewAccountActivityWithSnsDetail(snsAccount: Account)
        fun startCreateNewAccountActivityForSnsAccount(snsCode: Int)

        fun startAccountDetailActivity(account: Account)
        fun startAccountDetailActivity(accountGroup: AccountGroup)
    }
    
    interface Presenter {
        fun loadData(isNewData: Boolean, accountGroupID: String)
        fun loadAllData()
        fun deleteData(idsOfAccountGroup: List<String>)
    }
}