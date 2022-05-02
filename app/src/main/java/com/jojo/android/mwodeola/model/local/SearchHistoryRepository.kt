package com.jojo.android.mwodeola.model.local

import android.content.Context
import com.jojo.android.mwodeola.data.local.SearchHistory

class SearchHistoryRepository(context: Context) : SearchHistorySource {

    private val dataSource: SearchHistorySource = SearchHistoryDataSource(context)

    override fun getAllSearchHistories(): List<SearchHistory> {
        return dataSource.getAllSearchHistories()
    }

    override fun getAccountSearchHistories(): List<SearchHistory> {
        return dataSource.getAccountSearchHistories()
    }

    override fun createNewAccountSearchHistory(value: String): SearchHistory {
        return dataSource.createNewAccountSearchHistory(value)
    }

    override fun delete(data: SearchHistory) {
        dataSource.delete(data)
    }

    override fun deleteAllAccountSearchHistories() {
        dataSource.deleteAllAccountSearchHistories()
    }

    override fun deleteAllSearchHistories() {
        dataSource.deleteAllSearchHistories()
    }
}