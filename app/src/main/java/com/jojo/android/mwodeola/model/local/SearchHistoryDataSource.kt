package com.jojo.android.mwodeola.model.local

import android.content.Context
import com.jojo.android.mwodeola.data.local.SearchHistory
import com.jojo.android.mwodeola.data.local.SearchHistoryDB

class SearchHistoryDataSource(context: Context) : SearchHistorySource {

    private val searchHistoryDao = SearchHistoryDB.getInstance(context).searchHistoryDao()

    override fun getAllSearchHistories(): List<SearchHistory> {
        return searchHistoryDao.getAll()
    }

    override fun getAccountSearchHistories(): List<SearchHistory> {
        return searchHistoryDao.getAllBy(SearchHistory.ACCOUNT)
    }

    override fun createNewAccountSearchHistory(value: String): SearchHistory {
        val rowId = searchHistoryDao.insert(SearchHistory.newInstanceForAccount(value = value))
        return SearchHistory.newInstanceForAccount(rowId, value)
    }

    override fun delete(data: SearchHistory) {
        searchHistoryDao.delete(data)
    }

    override fun deleteAllAccountSearchHistories() {
        searchHistoryDao.deleteAll(SearchHistory.ACCOUNT)
    }

    override fun deleteAllSearchHistories() {
        searchHistoryDao.deleteAll()
    }
}