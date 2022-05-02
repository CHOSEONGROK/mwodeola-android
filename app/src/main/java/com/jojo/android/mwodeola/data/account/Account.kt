package com.jojo.android.mwodeola.data.account

import androidx.recyclerview.widget.DiffUtil
import com.jojo.android.mwodeola.autofill.model.IAutofillAccount
import java.io.Serializable

data class Account(
    val account_id: String,
    val created_at: String,
    val own_group: AccountGroup,
    val sns_group: AccountGroup?,
    val detail: AccountDetail
): Comparable<Account>, Serializable, IAutofillAccount {
    val isOwnAccount: Boolean
        get() = sns_group == null
    val isSnsAccount: Boolean
        get() = sns_group != null


    override val appNameField: String
        get() = own_group.group_name
    override val packageNameField: String?
        get() = own_group.app_package_name
    override val userIdField: String?
        get() = detail.user_id
    override val userPasswordField: String?
        get() = detail.user_password

    override fun compareTo(other: Account): Int {
        val myDetailOwner =
            if (isOwnAccount) own_group
            else sns_group!!

        val otherDetailOwner =
            if (other.isOwnAccount) other.own_group
            else other.sns_group!!

        val compareGroup = myDetailOwner.compareTo(otherDetailOwner)

        // (1) compare by group
        // (2) if both are the same case, compare by detail
        return if (compareGroup != 0) compareGroup
        else detail.compareTo(other.detail)
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Account?>() {
            override fun areItemsTheSame(oldItem: Account, newItem: Account): Boolean =
                oldItem === newItem
            override fun areContentsTheSame(oldItem: Account, newItem: Account): Boolean =
                oldItem == newItem
        }

        val NULLABLE_DIFF_CALLBACK = object : DiffUtil.ItemCallback<Account?>() {
            override fun areItemsTheSame(oldItem: Account, newItem: Account): Boolean =
                oldItem === newItem
            override fun areContentsTheSame(oldItem: Account, newItem: Account): Boolean =
                oldItem == newItem
        }

        fun empty(): Account =
            Account("", "", AccountGroup.empty(), null, AccountDetail.empty())
    }

}