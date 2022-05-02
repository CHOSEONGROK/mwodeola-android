package com.jojo.android.mwodeola.data.account

import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator
import android.text.SpannableString
import android.text.SpannableStringBuilder
import androidx.recyclerview.widget.DiffUtil
import com.jojo.android.mwodeola.presentation.account.datalist.search.FuzzyMatcher
import java.io.Serializable

data class AccountGroup(
    val id: String,
    val sns: Int = NO_SNS,
    var group_name: String = "",
    var app_package_name: String? = null,
    var web_url: String? = null,
    var icon_type: Int = ICON_TYPE_TEXT,
    var icon_image_url: String? = null,
    var is_favorite: Boolean = false,
    val created_at: String,
    var detail_count: Int = 0,
    val total_views: Int = 0,
): Comparable<AccountGroup>, Parcelable, Serializable {
    val isOwnGroup: Boolean
        get() = sns == NO_SNS
    val isSnsGroup: Boolean
        get() = sns != NO_SNS

    constructor(parcel: Parcel): this(
        parcel.readString()!!,
        parcel.readInt(),
        parcel.readString()!!,
        parcel.readString(),
        parcel.readString(),
        parcel.readInt(),
        parcel.readString(),
        (parcel.readInt() == 1),
        parcel.readString()!!,
        parcel.readInt(),
        parcel.readInt()
    )

    override fun compareTo(other: AccountGroup): Int = when {
        // (1) isOwnGroup > isSnsGroup
        // (2) if both are the same case, compare by group_name
        this.isOwnGroup && other.isOwnGroup || this.isSnsGroup && other.isSnsGroup ->
            this.group_name.compareTo(other.group_name)
        this.isOwnGroup -> -1
        this.isSnsGroup -> 1
        else -> 0 // 불가능
    }

    override fun describeContents(): Int = 0

    override fun writeToParcel(parcel: Parcel?, tag: Int) {
        parcel?.run {
            writeString(id)
            writeInt(sns)
            writeString(group_name)
            writeString(app_package_name)
            writeString(web_url)
            writeInt(icon_type)
            writeString(icon_image_url)
            if (is_favorite) writeInt(1)
            else writeInt(0)
            writeString(created_at)
            writeInt(detail_count)
            writeInt(total_views)
        }
    }

    fun duplicate(other: AccountGroup) {
        group_name = other.group_name
        app_package_name = other.app_package_name
        web_url = other.web_url
        icon_type = other.icon_type
        icon_image_url = other.icon_image_url
        is_favorite = other.is_favorite
        detail_count = other.detail_count
    }

    fun clone(): AccountGroup = AccountGroup(
        id, sns, group_name, app_package_name, web_url, icon_type, icon_image_url, is_favorite, created_at, detail_count, total_views
    )

    companion object CREATOR : Creator<AccountGroup> {
        const val NO_SNS = 0
        const val SNS_CODE_NAVER = 1
        const val SNS_CODE_KAKAO = 2
        const val SNS_CODE_LINE = 3
        const val SNS_CODE_GOOGLE = 4
        const val SNS_CODE_FACEBOOK = 5
        const val SNS_CODE_TWITTER = 6

        const val ICON_TYPE_TEXT = 0
        const val ICON_TYPE_IMAGE = 1
        const val ICON_TYPE_INSTALLED_APP_LOGO = 2
        const val ICON_TYPE_SNS = 3

        override fun createFromParcel(parcel: Parcel): AccountGroup {
            return AccountGroup(parcel)
        }

        override fun newArray(size: Int): Array<AccountGroup?> {
            return arrayOfNulls(size)
        }

        fun empty(): AccountGroup {
            return AccountGroup("", NO_SNS, "", null, null,
                ICON_TYPE_TEXT, null, false, "", 0, 0)
        }

        fun newInstanceForSns(snsCode: Int, group_name: String, web_url: String?, is_favorite: Boolean): AccountGroup =
            AccountGroup("", snsCode, group_name, null, web_url, ICON_TYPE_SNS, null, is_favorite, "", 0, 0)

        fun forAutofill(group_name: String, app_package_name: String?): AccountGroup =
            AccountGroup(
                "", NO_SNS, group_name, app_package_name, null,
                ICON_TYPE_INSTALLED_APP_LOGO, null, false, "", 0, 0
            )

        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<AccountGroup>() {
            override fun areItemsTheSame(oldItem: AccountGroup, newItem: AccountGroup): Boolean =
                oldItem === newItem

            override fun areContentsTheSame(oldItem: AccountGroup, newItem: AccountGroup): Boolean =
                oldItem == newItem
        }
    }
}