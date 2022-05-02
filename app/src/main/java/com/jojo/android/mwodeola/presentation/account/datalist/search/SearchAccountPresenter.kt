package com.jojo.android.mwodeola.presentation.account.datalist.search

import com.jojo.android.mwodeola.data.account.AccountGroup
import com.jojo.android.mwodeola.data.local.SearchHistory
import com.jojo.android.mwodeola.model.account.AccountSource
import com.jojo.android.mwodeola.model.local.SearchHistorySource

class SearchAccountPresenter(
    private val view: SearchAccountContract.View,
    private val historyRepository: SearchHistorySource,
    private val accountRepository: AccountSource
) : SearchAccountContract.Presenter {

    override fun loadRecentSearchList() {
        view.setAccountSearchHistory(
            historyRepository.getAccountSearchHistories()
                .sortedByDescending { it.dateTime }
        )
    }

    override fun loadAccountGroups() {
        accountRepository.getAllAccountGroups(object : AccountSource.LoadDataCallback<List<AccountGroup>>() {
            override fun onSucceed(data: List<AccountGroup>) {
                view.setAccountGroups(data.sortedBy { it.group_name })
            }
        })
    }

    override fun loadAccountDetails() {

    }

    override fun createNewSearchHistory(text: String): SearchHistory {
        return historyRepository.createNewAccountSearchHistory(text)
    }

    override fun deleteSearchHistory(history: SearchHistory) {
        historyRepository.delete(history)
    }

    override fun deleteAllHistories() {
        historyRepository.deleteAllAccountSearchHistories()
    }
}