package com.jojo.android.mwodeola.presentation.users.sign

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.InputType
import android.view.View
import com.jojo.android.mwodeola.databinding.FragmentSignBinding
import com.jojo.android.mwodeola.presentation.users.FilterableMaterialAutoCompleteTextView

@SuppressLint("SetTextI18n")
class SignInputEmailFragment : SignBaseFragment() {

    override val binding: FragmentSignBinding
        get() = super.binding
    override val parentView: SignContract.ParentView
        get() = super.parentView
    override val presenter: SignContract.Presenter
        get() = super.presenter

    private val email: String
        get() = binding.edtUserInput.text.toString()

    override fun canNext(): Boolean = isValidEmail(email)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.tvLabel1.text = "이메일 주소를 입력해주세요 :)"
        binding.tvLabel2.text = "고객님의 소중한 개인정보를 \n지켜드리기 위해 사용됩니다."

        binding.containerUserInput.hint = "이메일"
        binding.edtUserInput.inputType = InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS
        binding.edtUserInput.setOnInputCallback(OnEmailInputCallback())
    }

    override fun onSelectedPage(isInitial: Boolean) {
        super.onSelectedPage(isInitial)
        if (isInitial) {
            parentView.smsRetrieveHelper.requestHintEmail()
        }
        parentView.updateNextButton("다음")
    }

    override fun onClickedNextButton() {
        if (canNext()) {
            presenter.signUpVerifyEmail(email)
            hideSoftKeyboard(binding.edtUserInput)
        }
    }

    override fun setEmail(email: String) {
        binding.edtUserInput.setText(email)
    }

    override fun showEmailExistError() {
        binding.edtUserInput.error = "가입된 이메일이 존재합니다"
    }

    private fun isValidEmail(email: String): Boolean =
        android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()

    inner class OnEmailInputCallback : FilterableMaterialAutoCompleteTextView.OnInputCallback {
        override fun onTextChanged(isCompleted: Boolean, text: String) {
            parentView.enabledNextButton(isCompleted)

            if (binding.containerUserInput.error != null) {
                binding.containerUserInput.error = null
            }
        }
    }

    companion object {
        private const val TAG = "SignInputEmailFragment"

        fun newInstance(): SignInputEmailFragment =
            SignInputEmailFragment()
    }
}