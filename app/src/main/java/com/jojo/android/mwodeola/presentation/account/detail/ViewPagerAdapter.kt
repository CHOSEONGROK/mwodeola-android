package com.jojo.android.mwodeola.presentation.account.detail

import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.jojo.android.mwodeola.data.account.Account
import com.jojo.android.mwodeola.data.account.AccountGroupAndDetails
import com.jojo.android.mwodeola.presentation.security.Authenticators

class ViewPagerAdapter(
    private val view: AccountDetailContract.View,
    private val presenter: AccountDetailContract.Presenter
) : ListAdapter<Account?, RecyclerView.ViewHolder>(Account.NULLABLE_DIFF_CALLBACK) {

    companion object {
        private const val TAG = "ViewPagerAdapter"

        private const val DETAIL_PAGE = 0
        private const val DETAIL_ADD_PAGE = 1

        private const val PAYLOAD_SHOW_DELETE_BUTTON = "show_delete_button"
        private const val PAYLOAD_HIDE_DELETE_BUTTON = "hide_delete_button"
    }

    val lastIndex: Int
        get() = itemCount - 1

    private val requireActivity: AccountDetailActivity
        get() = view as AccountDetailActivity

    override fun getItemCount(): Int = currentList.size

    override fun getItemViewType(position: Int): Int = when {
        position < currentList.lastIndex -> DETAIL_PAGE
        position == currentList.lastIndex -> DETAIL_ADD_PAGE
        else -> throw IndexOutOfBoundsException("itemCount=$itemCount, position=$position")
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder = when (viewType) {
        DETAIL_PAGE -> DetailPageViewHolder.newInstance(parent)
        DETAIL_ADD_PAGE -> DetailPageAddViewHolder.newInstance(parent)
        else -> throw IndexOutOfBoundsException("viewType=$viewType")
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        when {
            payloads.isEmpty() ->
                super.onBindViewHolder(holder, position, payloads)
            payloads[0] == PAYLOAD_SHOW_DELETE_BUTTON ->
                (holder as? DetailPageViewHolder)?.updateDeleteButton(true)
            payloads[0] == PAYLOAD_HIDE_DELETE_BUTTON ->
                (holder as? DetailPageViewHolder)?.updateDeleteButton(false)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int): Unit = when (holder) {
        is DetailPageViewHolder -> {
            val account = currentList[position]!!

            holder.bind(account)
            holder.updateDeleteButton(view.isDeleteMode)

            val toggleListener = object : DetailPageViewHolder.OnPasswordToggleListener {
                override fun onToggleClicked(target: View, isPasswordVisible: Boolean) {
                    if (isPasswordVisible) {
                        holder.updatePasswordVisible(target, false)
                    } else {
                        presenter.authenticate(requireActivity, object : Authenticators.AuthenticationCallback() {
                            override fun onSucceed() {
                                holder.updatePasswordVisible(target, true)
                            }
                            override fun onFailure() {}
                            override fun onExceedAuthLimit(limit: Int) {
                                view.showAuthenticationExceedDialog(limit)
                            }
                        })
                    }
                }
            }

            if (account.detail.user_password?.isNotBlank() == true) {
                holder.setOnNormalPasswordToggleListener(toggleListener)
            }
            if (account.detail.user_password_pin4?.isNotBlank() == true) {
                holder.setOnPin4PasswordToggleListener(toggleListener)
            }
            if (account.detail.user_password_pin6?.isNotBlank() == true) {
                holder.setOnPin6PasswordToggleListener(toggleListener)
            }
            if (account.detail.user_password_pattern?.isNotBlank() == true) {
                holder.setOnPatternPasswordToggleListener(toggleListener)
            }

            holder.btnDelete.setOnClickListener {
                view.showDeleteAccountDetailConfirmDialog(account)
            }
        }
        is DetailPageAddViewHolder -> {

            holder.btnAddNormalDetail.setOnClickListener {
                view.showAddNormalDetailBottomSheet()
            }

            if (presenter.accounts.own_group.isSnsGroup) {
                holder.btnAddSnsDetail.isVisible = false
                holder.divider.isVisible = false
            } else {
                holder.btnAddSnsDetail.setOnClickListener {
                    view.showAddSnsDetailBottomSheet()
                }
            }
        }
        else -> throw IllegalArgumentException("position=$position, holder=$holder")
    }

    fun submit(data: AccountGroupAndDetails, commitCallback: Runnable? = null) {
        // last index is null: addition page
        val newList = ArrayList<Account?>(data.accounts)
            .also { it.add(it.size, null) }

        submitList(newList, commitCallback)
    }

    fun removeAt(position: Int) {
        val newList = ArrayList(currentList)
            .also { it.removeAt(position) }

        submitList(newList)
    }

    fun updateDeleteButton(isVisible: Boolean) {
        if (isVisible) {
            notifyItemRangeChanged(0, currentList.size, PAYLOAD_SHOW_DELETE_BUTTON)
        } else {
            notifyItemRangeChanged(0, currentList.size, PAYLOAD_HIDE_DELETE_BUTTON)
        }
    }
}