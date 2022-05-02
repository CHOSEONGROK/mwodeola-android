package com.jojo.android.mwodeola.presentation.account.datalist.search

import com.jojo.android.mwodeola.data.account.AccountDetail
import com.jojo.android.mwodeola.data.account.AccountGroup
import com.jojo.android.mwodeola.data.local.SearchHistory

interface SearchAccountContract {

    interface View {
        val isShowingSearchResult: Boolean

        fun setAccountSearchHistory(histories: List<SearchHistory>)
        fun setAccountGroups(groups: List<AccountGroup>)
        fun setAccountDetails(details: List<AccountDetail>)

        fun showSearchHistory()
        fun showSearchResult()
        fun updateVisibleNoneSearchResultLabel(isVisible: Boolean)

        fun performClickSearchHistoryItem(history: SearchHistory)

        fun startAccountDetailActivity(accountGroupId: String)

        fun showDeleteAllHistoriesConfirmDialog()
    }

    interface Presenter {
        fun loadRecentSearchList()
        fun loadAccountGroups()
        fun loadAccountDetails()

        fun createNewSearchHistory(text: String): SearchHistory
        fun deleteSearchHistory(history: SearchHistory)

        fun deleteAllHistories()
    }
}