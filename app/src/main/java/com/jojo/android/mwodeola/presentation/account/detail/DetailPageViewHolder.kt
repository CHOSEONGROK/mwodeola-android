package com.jojo.android.mwodeola.presentation.account.detail

import android.content.res.Resources
import android.text.method.PasswordTransformationMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.TransitionManager
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.jojo.android.mwodeola.R
import com.jojo.android.mwodeola.data.account.Account
import com.jojo.android.mwodeola.data.account.AccountDetail
import com.jojo.android.mwodeola.databinding.ActivityAccountDetailPageBinding

class DetailPageViewHolder(
    private val binding: ActivityAccountDetailPageBinding
) : RecyclerView.ViewHolder(binding.root) {

    interface OnPasswordToggleListener {
        fun onToggleClicked(target: View, isPasswordVisible: Boolean)
    }

    val btnDelete: ImageView = binding.btnDelete

    private val resources: Resources
        get() = binding.root.context.resources

    private val strokeColor = ResourcesCompat.getColor(resources, R.color.material_light_green_200, null)
    private val strokeColorRed = ResourcesCompat.getColor(resources, R.color.red200, null)

    fun bind(account: Account): Unit = with(binding) {
        if (account.isSnsAccount) {
            icon.setSnsGroupIcon(account.sns_group!!.sns)
        } else {
            if (account.own_group.isSnsGroup) {
                icon.setSnsGroupIcon(account.own_group.sns)
            } else {
                icon.setNormalGroupIcon(account.own_group)
            }
        }

        val detail = account.detail

        tvUserId.text = detail.user_id
        tvUserPassword.setText(detail.user_password)
        tvUserPasswordPin4.setText(detail.user_password_pin4)
        tvUserPasswordPin6.setText(detail.user_password_pin6)
        userPasswordPatternView.isEditable = false
        userPasswordPatternView.setPattern(detail.user_password_pattern)
        tvMemo.text = detail.memo

        userIdContainer.isVisible = (detail.user_id?.isNotBlank() == true)
        userPasswordContainer.isVisible = (detail.user_password?.isNotBlank() == true)
        userPasswordPin4Container.isVisible = (detail.user_password_pin4?.isNotBlank() == true)
        userPasswordPin6Container.isVisible = (detail.user_password_pin6?.isNotBlank() == true)
        userPasswordPatternContainer.isVisible = (detail.user_password_pattern?.isNotBlank() == true)
        memoContainer.isVisible = (detail.memo?.isNotBlank() == true)

        btnExpand.isVisible = updateContainersVisible(detail, 3) > 3
        btnExpandLabel.text = "항목 더 보기"
        btnExpandIcon.rotation = 0f

        // Divider Visible
        when {
            userIdContainer.visibility == View.VISIBLE -> null
            userPasswordContainer.visibility == View.VISIBLE -> userPasswordDivider
            userPasswordPin4Container.visibility == View.VISIBLE -> userPasswordPin4Divider
            userPasswordPin6Container.visibility == View.VISIBLE -> userPasswordPin6Divider
            userPasswordPatternContainer.visibility == View.VISIBLE -> userPasswordPatternDivider
            memoContainer.visibility == View.VISIBLE -> memoDivider
            else -> null
        }?.also { topDivider ->
            topDivider.visibility = View.GONE
        }

        if (detail.user_password_pattern?.isNotBlank() == true) {
            userPasswordPatternPlayButton.setOnClickListener {
                userPasswordPatternView.play()
            }
        }

        if (btnExpand.isVisible) {
            btnExpand.setOnClickListener {
                if (btnExpandLabel.text == "항목 더 보기") {
                    btnExpandLabel.text = "항목 접기"
                    btnExpandIcon.animate().setInterpolator(AccelerateDecelerateInterpolator())
                        .setDuration(150L)
                        .rotation(180f)
                        .start()

                    updateContainersVisible(detail, 6)
                } else {
                    btnExpandLabel.text = "항목 더 보기"
                    btnExpandIcon.animate().setInterpolator(AccelerateDecelerateInterpolator())
                        .setDuration(150L)
                        .rotation(0f)
                        .start()

                    updateContainersVisible(detail, 3)
                }
            }
        }
    }

    private fun updateVisibleIf(container: View, condition1: Boolean, condition2: Boolean): Int {
        container.isVisible = condition1 && condition2
        return if (condition1) 1 else 0
    }

    fun setOnNormalPasswordToggleListener(listener: OnPasswordToggleListener) {
        setOnPasswordToggleListener(binding.tvLayoutUserPassword, binding.tvUserPassword, listener)
    }

    fun setOnPin4PasswordToggleListener(listener: OnPasswordToggleListener) {
        setOnPasswordToggleListener(binding.tvLayoutUserPasswordPin4, binding.tvUserPasswordPin4, listener)
    }

    fun setOnPin6PasswordToggleListener(listener: OnPasswordToggleListener) {
        setOnPasswordToggleListener(binding.tvLayoutUserPasswordPin6, binding.tvUserPasswordPin6, listener)
    }

    fun setOnPatternPasswordToggleListener(listener: OnPasswordToggleListener) {
        binding.userPasswordPatternExpandButton.setOnClickListener {
            val isPasswordVisible = (binding.userPasswordPatternPlayButton.visibility == View.VISIBLE)

            listener.onToggleClicked(binding.userPasswordPatternPlayButton, isPasswordVisible)
        }
    }

    fun updatePasswordVisible(target: View, isVisible: Boolean) {
        when (target) {
            is TextInputEditText ->
                target.transformationMethod = PasswordTransformationMethod.getInstance()
                    .takeIf { isVisible.not() }
            binding.userPasswordPatternPlayButton -> {
                binding.userPasswordPatternPlayButton.isVisible = isVisible

                val rotation =
                    if (isVisible) 180f
                    else 0f

                binding.userPasswordPatternExpandButton.animate().setInterpolator(AccelerateDecelerateInterpolator())
                    .setDuration(150L)
                    .rotation(rotation)
                    .start()
            }
        }
    }

    fun updateDeleteButton(isVisible: Boolean) {
        btnDelete.isVisible = isVisible
        binding.cardView.strokeColor =
            if (isVisible) strokeColorRed
            else strokeColor
    }

    private fun updateContainersVisible(detail: AccountDetail, maxVisible: Int): Int {
        //TransitionManager.beginDelayedTransition(binding.contentContainer)

        var itemCount = 0

        itemCount += updateVisibleIf(
            container = binding.userIdContainer,
            condition1 = detail.user_id?.isNotBlank() == true,
            condition2 = itemCount < maxVisible
        )
        itemCount += updateVisibleIf(
            container = binding.userPasswordContainer,
            condition1 = detail.user_password?.isNotBlank() == true,
            condition2 = itemCount < maxVisible
        )
        itemCount += updateVisibleIf(
            container = binding.memoContainer,
            condition1 = detail.memo?.isNotBlank() == true,
            condition2 = itemCount < maxVisible
        )
        itemCount += updateVisibleIf(
            container = binding.userPasswordPin4Container,
            condition1 = detail.user_password_pin4?.isNotBlank() == true,
            condition2 = itemCount < maxVisible
        )
        itemCount += updateVisibleIf(
            container = binding.userPasswordPin6Container,
            condition1 = detail.user_password_pin6?.isNotBlank() == true,
            condition2 = itemCount < maxVisible
        )
        itemCount += updateVisibleIf(
            container = binding.userPasswordPatternContainer,
            condition1 = detail.user_password_pattern?.isNotBlank() == true,
            condition2 = itemCount < maxVisible
        )

        return itemCount
    }

    private fun setOnPasswordToggleListener(
        textInputLayout: TextInputLayout,
        textInputEditText: TextInputEditText,
        listener: OnPasswordToggleListener
    ) {
        textInputLayout.setEndIconOnClickListener {
            val isPasswordVisible = textInputEditText.transformationMethod !is PasswordTransformationMethod

            listener.onToggleClicked(textInputEditText, isPasswordVisible)
        }
    }



//    inner class PasswordToggleListener : View.OnClickListener {
//        override fun onClick(view: View?) {
//            val oldSelection = tvUserPassword.selectionEnd
//            val isPasswordVisible = tvUserPassword.transformationMethod !is PasswordTransformationMethod
//
//            if (isPasswordVisible) {
//                updatePasswordVisible(false)
//            } else {
//                presenter.authenticate(requireActivity(), object : Authenticators.AuthenticationCallback() {
//                    override fun onSucceed() {
//                        updatePasswordVisible(true)
//
//                        if (oldSelection >= 0) {
//                            tvUserPassword.setSelection(oldSelection)
//                        }
//                    }
//                    override fun onFailure() {}
//                    override fun onExceedAuthLimit(limit: Int) {}
//                })
//            }
//        }
//    }

    companion object {
        private const val TAG = "DetailPageViewHolder"

        fun newInstance(parent: ViewGroup): DetailPageViewHolder =
            DetailPageViewHolder(
                ActivityAccountDetailPageBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
    }
}