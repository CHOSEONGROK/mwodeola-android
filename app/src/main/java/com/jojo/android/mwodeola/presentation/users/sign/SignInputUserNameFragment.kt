package com.jojo.android.mwodeola.presentation.users.sign

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.InputType
import android.view.View
import com.jojo.android.mwodeola.databinding.FragmentSignBinding
import com.jojo.android.mwodeola.presentation.users.FilterableMaterialAutoCompleteTextView

@SuppressLint("SetTextI18n")
class SignInputUserNameFragment : SignBaseFragment() {

    override val binding: FragmentSignBinding
        get() = super.binding
    override val parentView: SignContract.ParentView
        get() = super.parentView
    override val presenter: SignContract.Presenter
        get() = super.presenter

    private val userName: String
        get() = binding.edtUserInput.text.toString()

    override fun canNext(): Boolean = userName.isNotBlank()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvLabel1.text = "뭐더라에 오신걸 환영해요 :)"
        binding.tvLabel2.text = "당신의 이름을 입력해주세요 :)"

        binding.containerUserInput.hint = "이름"
        binding.edtUserInput.inputType = InputType.TYPE_TEXT_VARIATION_PERSON_NAME
        binding.edtUserInput.setOnInputCallback(OnNameInputCallback())
    }

    override fun onSelectedPage(isInitial: Boolean) {
        super.onSelectedPage(isInitial)
        parentView.updateNextButton("다음")
    }

    override fun onClickedNextButton() {
        if (canNext()) {
            presenter.setUserName(userName)
            parentView.moveNextPage()
        }
    }

    inner class OnNameInputCallback : FilterableMaterialAutoCompleteTextView.OnInputCallback {
        override fun onTextChanged(isCompleted: Boolean, text: String) {
            parentView.enabledNextButton(isCompleted)
        }
    }

    companion object {
        private const val TAG = "SignInputUserNameFragment"

        fun newInstance(): SignInputUserNameFragment =
            SignInputUserNameFragment().apply { arguments.also { } }
    }
}