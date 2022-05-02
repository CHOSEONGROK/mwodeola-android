package com.jojo.android.mwodeola.presentation.account.datalist.dialog

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.jojo.android.mwodeola.R
import com.jojo.android.mwodeola.data.account.Account
import com.jojo.android.mwodeola.data.account.AccountGroup
import com.jojo.android.mwodeola.databinding.DialogAccountSelectionRvHeaderBinding
import com.jojo.android.mwodeola.databinding.DialogAccountSelectionRvItemBinding
import com.jojo.android.mwodeola.presentation.common.SquircleIcon
import com.jojo.android.mwodeola.util.doForEach
import kotlin.IllegalArgumentException

class SelectAccountInGroupRvAdapter(
    private val dialog: SelectAccountInGroupDialog
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    enum class ViewType { HEADER, ITEM }

    private val items = arrayListOf<Any>()

    override fun getItemViewType(position: Int): Int = when (items[position]) {
        is RvHeader -> ViewType.HEADER.ordinal
        is RvItem -> ViewType.ITEM.ordinal
        else -> throw IllegalArgumentException("getItemViewType(): position=$position, item=${items[position]}")
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder = when (viewType) {
        ViewType.HEADER.ordinal -> HeaderViewHolder(
            DialogAccountSelectionRvHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        ViewType.ITEM.ordinal -> ItemViewHolder(
            DialogAccountSelectionRvItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        else -> throw IllegalArgumentException("onCreateViewHolder(): viewType=$viewType")
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int): Unit {
        if (holder is HeaderViewHolder) {
            val header = items[position] as RvHeader

            holder.tvTitle.text = header.title


        } else if (holder is ItemViewHolder) {
            val item = items[position] as RvItem

            if (item.account.isSnsAccount) {
                holder.ivIcon.setSnsGroupIcon(item.account.sns_group!!.sns)
            } else {
                if (item.iconDrawable != null) {
                    holder.ivIcon.setIconImageDrawable(item.iconDrawable)
                } else if (item.iconText != null) {
                    holder.ivIcon.setIconText(item.iconText)
                }
            }

            holder.tvUserID.text = item.userID

            holder.snsMark.visibility =
                if (item.account.isSnsAccount) View.VISIBLE
                else View.GONE

            holder.itemView.setOnClickListener {
                dialog.finishForResult(item.account)
            }
        }
    }

    override fun getItemCount(): Int = items.size

    fun setData(accounts: List<Account>) {
        val accountMap = accounts.groupBy { it.isSnsAccount }

        val ownAccounts = accountMap[false]
        val linkedSNSAccounts = accountMap[true]

        ownAccounts // 자사 계정
            ?.sortedBy { it.own_group.group_name }
            ?.doForEach(::createRvHeader) {
                items.add(createRvItem(it))
            }

        linkedSNSAccounts // SNS 연동 계정
            ?.groupBy { it.sns_group?.group_name }?.entries // SNS 계정 이름으로 매핑
            ?.forEach { entry ->
                entry.value
                    .sortedBy { it.sns_group?.group_name }
                    .doForEach(::createRvHeader) { account ->
                        items.add(createRvItem(account))
                    }
            }

        notifyItemRangeInserted(0, items.size)
    }

    private fun createRvHeader(account: Account): Unit {
        if (account.isSnsAccount) {
            items.add(
                RvHeader("${account.sns_group?.group_name} 연동 계정", true)
            )
        } else {
            items.add(
                RvHeader("${account.own_group.group_name} 계정", false)
            )
        }
    }

    private fun createRvItem(account: Account): RvItem =
        if (account.isSnsAccount) { // SNS 연동 계정
            val iconText: String = account.sns_group!!.group_name
            var iconDrawable: Drawable? = null

            val accountName = account.sns_group.group_name
            val userID = account.detail.user_id

            RvItem(iconText, iconDrawable, null, accountName, userID, account)
        } else { // 자사 계정
            var iconText: String? = null
            var iconDrawable: Drawable? = null

            iconText = account.own_group.group_name

            when (account.own_group.icon_type) {
                AccountGroup.ICON_TYPE_INSTALLED_APP_LOGO ->
                    iconDrawable = getInstalledAppIcon(account.own_group.app_package_name)
                AccountGroup.ICON_TYPE_SNS ->
                    iconDrawable = getSnsIcon(account.own_group.sns)
            }

            if (account.own_group.icon_type == AccountGroup.ICON_TYPE_INSTALLED_APP_LOGO) {
                iconDrawable = getInstalledAppIcon(account.own_group.app_package_name)
            }

            val accountName = account.own_group.group_name
            val userID = account.detail.user_id

            RvItem(iconText, iconDrawable, null, accountName, userID, account)
        }

    private fun getInstalledAppIcon(packageName: String?): Drawable? {
        if (packageName == null)
            return null

        return try {
            dialog.packageManager.getApplicationIcon(packageName)
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
    }

    private fun getSnsIcon(snsCode: Int): Drawable? {
        val resId = when (snsCode) {
            1 -> R.drawable.sns_naver_icon
            2 -> R.drawable.sns_kakao_icon
            3 -> R.drawable.sns_line_icon
            4 -> R.drawable.sns_google_icon_white_320
            5 -> R.drawable.sns_facebook_icon
            6 -> R.drawable.sns_twitter_icon
            else -> return null
        }
        return ResourcesCompat.getDrawable(dialog.resources, resId, null)
    }

    data class RvHeader(val title: String, val isLinkedSNSAccount: Boolean)
    data class RvItem(
        val iconText: String?,
        val iconDrawable: Drawable?,
        val iconUrl: String?,
        val accountName: String,
        val userID: String?,
        val account: Account
    ) {
//        val iconType: AccountGroup.IconType
//            get() =
//                if (account.isLinkedSNSAccount) account.linkedSNSGroup!!.iconType
//                else account.ownGroup.iconType
    }

    class HeaderViewHolder(
        binding: DialogAccountSelectionRvHeaderBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        val tvTitle: TextView = binding.tvHeaderTitle

    }

    class ItemViewHolder(
        binding: DialogAccountSelectionRvItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        val ivIcon: SquircleIcon = binding.ivIcon
        val tvUserID: TextView = binding.tvUserId
        val snsMark: FrameLayout = binding.containerSnsMark
    }
}