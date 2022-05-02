package com.jojo.android.mwodeola.presentation.account.create

import android.graphics.drawable.Drawable
import com.jojo.android.mwodeola.data.account.AccountGroup

data class AppInfo constructor(
    val icon: Drawable?,
    val label: String,
    val packageName: String?,
    val existingAccountGroup: AccountGroup? = null
) {
    var startIndex: Int = -1
    var endIndex: Int = -1

    val isExists: Boolean
        get() = when {
            label == existingAccountGroup?.group_name ->
                true
            packageName == existingAccountGroup?.app_package_name ->
                true
            else -> false
        }
}