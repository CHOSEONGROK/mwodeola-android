package com.jojo.android.mwodeola.presentation.account.datalist.dialog

import android.app.Activity
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.core.content.res.ResourcesCompat
import com.jojo.android.mwodeola.R
import com.jojo.android.mwodeola.data.account.AccountGroup
import com.jojo.android.mwodeola.databinding.BottomSheetSelectSnsBinding
import com.jojo.android.mwodeola.presentation.common.BaseBottomSheetDialog


class SelectSnsGroupBottomSheet(
    activity: Activity,
    private val existingList: List<Int>
) : BaseBottomSheetDialog(activity), View.OnClickListener {

    private val binding by lazy { BottomSheetSelectSnsBinding.inflate(layoutInflater) }
    private var listener: ((snsCode: Int) -> Unit)? = null

    private val grayScaleColor = ResourcesCompat.getColor(activity.resources, R.color.disabled_color, null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        existingList.forEach { snsCode: Int ->
            val button = when (snsCode) {
                AccountGroup.SNS_CODE_NAVER -> binding.naverCard
                AccountGroup.SNS_CODE_KAKAO -> {
                    binding.kakaoCardIcon.imageTintList = ColorStateList.valueOf(Color.WHITE)
                    binding.kakaoCardLabel.setTextColor(Color.WHITE)
                    binding.kakaoCard
                }
                AccountGroup.SNS_CODE_LINE -> binding.lineCard
                AccountGroup.SNS_CODE_GOOGLE -> {
                    binding.googleCardIcon.imageTintList = ColorStateList.valueOf(grayScaleColor)
                    binding.googleCardLabel.setTextColor(grayScaleColor)
                    binding.googleCard
                }
                AccountGroup.SNS_CODE_FACEBOOK -> binding.facebookCard
                AccountGroup.SNS_CODE_TWITTER -> binding.twitterCard
                else -> null
            }

            button?.isEnabled = false
        }

        binding.naverCard.setOnClickListener(this)
        binding.kakaoCard.setOnClickListener(this)
        binding.lineCard.setOnClickListener(this)
        binding.googleCard.setOnClickListener(this)
        binding.facebookCard.setOnClickListener(this)
        binding.twitterCard.setOnClickListener(this)
    }

    fun showWithSelectedListener(listener: (snsCode: Int) -> Unit) {
        this.listener = listener
        super.show()
    }

    override fun onClick(view: View?) {
        val snsCode = when (view) {
            binding.naverCard -> AccountGroup.SNS_CODE_NAVER
            binding.kakaoCard -> AccountGroup.SNS_CODE_KAKAO
            binding.lineCard -> AccountGroup.SNS_CODE_LINE
            binding.googleCard -> AccountGroup.SNS_CODE_GOOGLE
            binding.facebookCard -> AccountGroup.SNS_CODE_FACEBOOK
            binding.twitterCard -> AccountGroup.SNS_CODE_TWITTER
            else -> throw IllegalArgumentException()
        }

        dismiss()
        listener?.invoke(snsCode)
    }
}