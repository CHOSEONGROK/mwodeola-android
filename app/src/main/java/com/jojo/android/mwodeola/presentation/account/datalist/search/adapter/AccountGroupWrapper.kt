package com.jojo.android.mwodeola.presentation.account.datalist.search.adapter

import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.DiffUtil
import com.jojo.android.mwodeola.R
import com.jojo.android.mwodeola.data.account.AccountGroup
import com.jojo.android.mwodeola.presentation.account.datalist.search.FuzzyMatcher
import com.jojo.android.mwodeola.presentation.common.SquircleIcon

class AccountGroupWrapper(
    val group: AccountGroup
) : FuzzyMatcher.Matchable {

    var iconDrawable: Drawable? = null
    var groupNameSpannable = SpannableString(group.group_name)

    private var _fuzzyPoint: Long = 0

    override val comparator: String
        get() = group.group_name

    override var fuzzyPoint: Long
        get() = _fuzzyPoint
        set(value) { _fuzzyPoint = value }

    override fun getSpannable(): SpannableString = groupNameSpannable

    fun initIcon(context: Context) {
        if (group.isSnsGroup) {
            val resId = when (group.sns) {
                1 -> R.drawable.sns_naver_icon
                2 -> R.drawable.sns_kakao_icon
                3 -> R.drawable.sns_line_icon
                4 -> R.drawable.sns_google_icon_white_320
                5 -> R.drawable.sns_facebook_icon
                6 -> R.drawable.sns_twitter_icon
                else -> SquircleIcon.NO_ID
            }
            iconDrawable = ResourcesCompat.getDrawable(context.resources, resId, null)
        } else {
            if (group.icon_type == AccountGroup.ICON_TYPE_INSTALLED_APP_LOGO) {
                iconDrawable = getInstalledAppIcon(context, group.app_package_name)
            }
        }
    }

    fun applyIcon(icon: SquircleIcon) {
        if (iconDrawable != null) {
            icon.setIconImageDrawable(iconDrawable)
        } else {
            icon.setIconText(group.group_name)
        }
    }

    fun clone() = AccountGroupWrapper(group).also {
        it.iconDrawable = iconDrawable
        it.groupNameSpannable = groupNameSpannable
        it.fuzzyPoint = fuzzyPoint
    }

    private fun getInstalledAppIcon(context: Context, packageName: String?): Drawable? {
        if (packageName == null)
            return null

        return try {
            context.packageManager.getApplicationIcon(packageName)
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
    }

    companion object {
        const val PAYLOAD_CHANGED = "changed"

        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<AccountGroupWrapper>() {
            override fun areItemsTheSame(oldItem: AccountGroupWrapper, newItem: AccountGroupWrapper): Boolean =
                oldItem.group.id == newItem.group.id

            override fun areContentsTheSame(oldItem: AccountGroupWrapper, newItem: AccountGroupWrapper): Boolean =
                oldItem.group == newItem.group && oldItem.fuzzyPoint == newItem.fuzzyPoint

            override fun getChangePayload(oldItem: AccountGroupWrapper, newItem: AccountGroupWrapper): Any? =
                if (oldItem.fuzzyPoint != newItem.fuzzyPoint) PAYLOAD_CHANGED
                else null
        }
    }
}