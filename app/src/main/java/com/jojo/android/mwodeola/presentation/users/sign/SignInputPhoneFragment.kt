package com.jojo.android.mwodeola.presentation.users.sign

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.core.view.marginTop
import com.jojo.android.mwodeola.databinding.FragmentSignBinding
import com.jojo.android.mwodeola.presentation.users.FilterableMaterialAutoCompleteTextView
import com.jojo.android.mwodeola.util.dpToPixels

@SuppressLint("SetTextI18n")
class SignInputPhoneFragment : SignBaseFragment() {

    override val binding: FragmentSignBinding
        get() = super.binding
    override val parentView: SignContract.ParentView
        get() = super.parentView
    override val presenter: SignContract.Presenter
        get() = super.presenter

    private val phoneNumber: String
        get() = binding.edtUserInput.text.toString()
    private val smsCode: String
        get() = binding.edtSmsAuthNumber.text.toString()

    private var sendingFlag = false

    override fun canNext(): Boolean = canNextButton()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.tvLabel1.text = "휴대폰 번호를 입력해주세요 :)"
        binding.tvLabel2.text = "본인 확인을 위해 사용됩니다."

        binding.containerUserInput.hint = "휴대폰 번호"
        binding.edtUserInput.inputType = InputType.TYPE_CLASS_PHONE
        binding.edtUserInput.setOnInputCallback(OnPhoneInputCallback())

        (binding.containerUserInput.layoutParams as ViewGroup.MarginLayoutParams).let {
            it.topMargin = 160.dpToPixels(requireContext())
        }

        binding.edtSmsAuthNumber.setOnInputCallback(OnSmsCodeInputCallback())
        binding.btnSmsAuthRetry.setOnClickListener {
            presenter.resend(phoneNumber)
        }

        binding.tvLabel1.visibility = View.VISIBLE
        binding.tvLabel1.animateFadeInWithAfter {
            binding.tvLabel2.visibility = View.VISIBLE
            binding.tvLabel2.animateFadeIn()
        }

        parentView.smsRetrieveHelper.requestHintPhone()
    }

    override fun onSelectedPage(isInitial: Boolean) {
        super.onSelectedPage(isInitial)
        if (presenter.sendingPhoneNumber.isBlank()) {
            parentView.updateNextButton("인증 문자 보내기")
        } else {
            parentView.updateNextButton("인증하기")
        }
    }

    override fun onClickedNextButton() {
        if (!canNext())
            return

        if (!sendingFlag) { // 문자 전송 전
            sendingFlag = true
            presenter.send(phoneNumber)
            parentView.updateNextButton("인증하기")
            parentView.enabledNextButton(false)
            showSmsAuthInputWidget()
        } else { // 문자 전송 후
            // TODO: add loading progress
            presenter.authenticateSmsCode(smsCode)
            hideSoftKeyboard(binding.edtSmsAuthNumber)
        }
    }

    override fun setPhoneNumber(phoneNumber: String) {
        binding.edtUserInput.setText(phoneNumber)
    }

    override fun showSmsAuthInputWidget() {
        binding.containerSmsAuthNumber.visibility = View.VISIBLE
        binding.containerSmsAuthNumber.alpha = 0f
        binding.btnSmsAuthRetry.visibility = View.VISIBLE
        binding.btnSmsAuthRetry.alpha = 0f

        binding.containerUserInput.animate().translationY(-90.dpToPixels(requireContext()).toFloat())
            .setDuration(300L).setInterpolator(AccelerateInterpolator()).start()
        binding.containerSmsAuthNumber.animate().alpha(1f)
            .setDuration(400L).setInterpolator(AccelerateInterpolator()).start()
        binding.btnSmsAuthRetry.animate().alpha(1f)
            .setDuration(400L).setInterpolator(AccelerateInterpolator()).start()
    }

    override fun requestFocusSmsCode() {
        binding.edtSmsAuthNumber.requestFocus()
    }

    override fun setSmsCode(code: String) {
        binding.edtSmsAuthNumber.setText(code)
        binding.edtSmsAuthNumber.setSelection(code.length)
    }

    override fun clearSmsCode() {
        binding.edtSmsAuthNumber.text?.clear()
    }

    override fun showPhoneNumberError() {
        binding.edtUserInput.error = "잘못된 번호입니다. 다시 확인해주세요."
        binding.edtUserInput.requestFocus()
    }

    override fun showIncorrectSmsCodeError() {
        binding.edtSmsAuthNumber.error = "인증번호가 맞지 않습니다. 다시 확인해주세요."
        binding.edtSmsAuthNumber.requestFocus()
    }

    override fun updateResendButtonText(remainingTimeString: String) {
        binding.btnSmsAuthRetry.text = "인증문자 다시 받기(${remainingTimeString})"
    }

    private fun isValidPhoneNumber(phoneNumber: String): Boolean =
        (phoneNumber.length == 13)

    private fun canNextButton(): Boolean =
        if (!sendingFlag) // 문자 전송 전
            isValidPhoneNumber(phoneNumber)
        else // 문자 전송 후
            !presenter.isTimeOut &&
                    (smsCode.length == 6) &&
                    (phoneNumber == presenter.sendingRawPhoneNumber)

    inner class OnPhoneInputCallback : FilterableMaterialAutoCompleteTextView.OnInputCallback {
        override fun onTextChanged(isCompleted: Boolean, text: String) {
            binding.btnSmsAuthRetry.isEnabled = isCompleted
            parentView.enabledNextButton(canNext())

            if (binding.edtUserInput.isShowingError()) {
                binding.edtUserInput.error = null
            }
        }
    }

    inner class OnSmsCodeInputCallback : FilterableMaterialAutoCompleteTextView.OnInputCallback {
        override fun onTextChanged(isCompleted: Boolean, text: String) {
            Log.i(TAG, "onTextChanged(): isCompleted=$isCompleted, canNext:${canNext()}")

            parentView.enabledNextButton(canNext())

            if (binding.edtSmsAuthNumber.isShowingError()) {
                binding.edtSmsAuthNumber.error = null
            }
        }
    }

    companion object {
        private const val TAG = "SignInputPhoneFragment"

        fun newInstance(): SignInputPhoneFragment =
            SignInputPhoneFragment()
    }
}