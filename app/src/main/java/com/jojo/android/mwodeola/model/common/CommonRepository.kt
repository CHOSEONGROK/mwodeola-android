package com.jojo.android.mwodeola.model.common

import android.content.Context
import com.jojo.android.mwodeola.data.common.SnsInfo

class CommonRepository(context: Context) : CommonSource {

    private val dataSource: CommonDataSource = CommonDataSource(context)

    override fun getSnsInfo(callback: CommonSource.BaseCallback<List<SnsInfo>>) {
        dataSource.getSnsInfo(callback)
    }

    override fun getAllDataCount(callback: CommonSource.DataCountCallback) {
        dataSource.getAllDataCount(callback)
    }
}