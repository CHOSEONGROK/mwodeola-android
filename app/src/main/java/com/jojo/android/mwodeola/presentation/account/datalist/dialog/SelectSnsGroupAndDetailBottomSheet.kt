package com.jojo.android.mwodeola.presentation.account.datalist.dialog

import android.app.Activity
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.card.MaterialCardView
import com.jojo.android.mwodeola.data.account.Account
import com.jojo.android.mwodeola.data.account.AccountGroup
import com.jojo.android.mwodeola.data.account.AccountGroup.CREATOR.SNS_CODE_NAVER
import com.jojo.android.mwodeola.data.account.AccountGroup.CREATOR.SNS_CODE_KAKAO
import com.jojo.android.mwodeola.data.account.AccountGroup.CREATOR.SNS_CODE_LINE
import com.jojo.android.mwodeola.data.account.AccountGroup.CREATOR.SNS_CODE_GOOGLE
import com.jojo.android.mwodeola.data.account.AccountGroup.CREATOR.SNS_CODE_FACEBOOK
import com.jojo.android.mwodeola.data.account.AccountGroup.CREATOR.SNS_CODE_TWITTER
import com.jojo.android.mwodeola.data.account.AccountGroupAndDetails
import com.jojo.android.mwodeola.databinding.BottomSheetSelectSnsBinding
import com.jojo.android.mwodeola.databinding.BottomSheetSelectSnsRvItemBinding
import com.jojo.android.mwodeola.model.account.AccountRepository
import com.jojo.android.mwodeola.model.account.AccountSource
import com.jojo.android.mwodeola.presentation.common.BaseBottomSheetDialog

class SelectSnsGroupAndDetailBottomSheet(activity: Activity) : BaseBottomSheetDialog(activity), View.OnClickListener {

    private val binding by lazy { BottomSheetSelectSnsBinding.inflate(layoutInflater) }
    private val repository: AccountSource = AccountRepository(context)
    private val listAdapter = SnsDetailListAdapter(this)

    private val snsMap = hashMapOf<MaterialCardView, AccountGroup>()
    private val exclusionList = mutableListOf<Account>()

    private var isShowingDetailList = false

    private var listener: ((account: Account) -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { onBackPressed() }

        binding.naverCard.visibility = View.GONE
        binding.kakaoCard.visibility = View.GONE
        binding.lineCard.visibility = View.GONE
        binding.googleCard.visibility = View.GONE
        binding.facebookCard.visibility = View.GONE
        binding.twitterCard.visibility = View.GONE

        binding.cardContainer.visibility = View.INVISIBLE
        binding.contentLoadingProgressBar.show()

        binding.recyclerView.adapter = listAdapter

        repository.getAllSnsAccountGroups(SnsGroupLoadCallback())
    }

    override fun onBackPressed() {
        if (isShowingDetailList) {
            isShowingDetailList = false

            listAdapter.clear()
            binding.recyclerView.visibility = View.INVISIBLE

            binding.title.text = "SNS 그룹 계정 선택하기"
            binding.btnBack.visibility = View.GONE
            binding.cardContainer.visibility = View.VISIBLE
        } else {
            super.onBackPressed()
        }
    }

    override fun onClick(cardView: View?) {
        val snsGroupId = snsMap[cardView]?.id!!

        repository.getAllAccountDetailsInGroup(snsGroupId, SnsDetailLoadCallback())
    }

    fun setListener(listener: (account: Account) -> Unit) = apply {
        this.listener = listener
    }

    fun setExclusionList(accounts: List<Account>) = apply {
        exclusionList.addAll(accounts)
    }

    fun dismissForItemSelected(account: Account) {
        listener?.invoke(account)
        dismiss()
    }

    private fun bindMap(snsGroup: AccountGroup) {
        val cardView = when (snsGroup.sns) {
            SNS_CODE_NAVER -> binding.naverCard
            SNS_CODE_KAKAO -> binding.kakaoCard
            SNS_CODE_LINE -> binding.lineCard
            SNS_CODE_GOOGLE -> binding.googleCard
            SNS_CODE_FACEBOOK -> binding.facebookCard
            SNS_CODE_TWITTER -> binding.twitterCard
            else -> null
        }

        cardView?.let {
            it.visibility = View.VISIBLE
            it.setOnClickListener(this)
            snsMap[it] = snsGroup
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    inner class SnsGroupLoadCallback : AccountSource.LoadDataCallback<List<AccountGroup>>() {
        override fun onSucceed(data: List<AccountGroup>) {
            if (snsMap.isNotEmpty()) {
                snsMap.clear()
            }

            data.forEach { bindMap(it) }

            binding.contentLoadingProgressBar.hide()
            binding.cardContainer.visibility = View.VISIBLE
        }

        override fun onUnknownError(errString: String?) {
            super.onUnknownError(errString)

            showToast("서버와 연결이 원활하지 않습니다.")
            binding.contentLoadingProgressBar.hide()
            binding.cardContainer.visibility = View.VISIBLE
        }
    }

    inner class SnsDetailLoadCallback : AccountSource.LoadDataCallback<AccountGroupAndDetails>() {
        override fun onSucceed(data: AccountGroupAndDetails) {
            binding.title.text = "${data.own_group.group_name} 계정 선택하기"

            isShowingDetailList = true

            binding.btnBack.visibility = View.VISIBLE
            binding.cardContainer.visibility = View.INVISIBLE
            binding.recyclerView.visibility = View.VISIBLE

            listAdapter.submitList(data.accounts)
        }

        override fun onUnknownError(errString: String?) {
            super.onUnknownError(errString)

            showToast("서버와 연결이 원활하지 않습니다.")
        }
    }

    class SnsDetailListAdapter(
        val view: SelectSnsGroupAndDetailBottomSheet
    ) : ListAdapter<Account, SnsDetailListAdapter.ViewHolder>(Account.DIFF_CALLBACK) {

        override fun getItemCount(): Int = currentList.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
            ViewHolder.newInstance(parent)

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val account = currentList[position]

            with (holder) {
                val isEnabled = view.exclusionList.none { it.detail.id == account.detail.id }
                bind(account, isEnabled)

                itemView.isEnabled = isEnabled
                if (isEnabled) {
                    itemView.setOnClickListener {
                        view.dismissForItemSelected(account)
                    }
                }
            }
        }

        fun clear() {
            submitList(mutableListOf())
        }

        class ViewHolder(private val binding: BottomSheetSelectSnsRvItemBinding) : RecyclerView.ViewHolder(binding.root) {
            fun bind(account: Account, isEnabled: Boolean) {
                binding.icon.setSnsGroupIcon(account.own_group.sns)
                binding.icon.setGrayScale(!isEnabled)
                binding.tvUserId.text = account.detail.user_id
                binding.tvUserId.alpha =
                    if (isEnabled) 1f
                    else 0.3f
            }

            companion object {
                fun newInstance(parent: ViewGroup) =
                    ViewHolder(
                        BottomSheetSelectSnsRvItemBinding.inflate(
                            LayoutInflater.from(parent.context), parent, false
                        )
                    )
            }
        }
    }
}