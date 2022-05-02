package com.jojo.android.mwodeola.model.local

import com.jojo.android.mwodeola.data.local.SearchHistory

interface SearchHistorySource {

    fun getAllSearchHistories(): List<SearchHistory>

    fun getAccountSearchHistories(): List<SearchHistory>

    fun createNewAccountSearchHistory(value: String): SearchHistory

    fun delete(data: SearchHistory)

    fun deleteAllAccountSearchHistories()

    fun deleteAllSearchHistories()
}