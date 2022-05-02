package com.jojo.android.mwodeola.presentation.users.sign

import android.animation.Animator
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import com.jojo.android.mwodeola.databinding.FragmentSignBinding
import com.jojo.android.mwodeola.presentation.BaseFragment

abstract class SignBaseFragment : BaseFragment(), SignContract.BaseChildView {
    companion object {
        private const val TAG = "SignBaseFragment"
    }

    protected open val binding by lazy { FragmentSignBinding.inflate(layoutInflater) }
    protected open val parentView: SignContract.ParentView
        get() = requireActivity() as SignContract.ParentView
    protected open val presenter: SignContract.Presenter
        get() = parentView.presenter

    abstract fun canNext(): Boolean

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = binding.root

    override fun onSelectedPage(isInitial: Boolean) {
        Log.d(TAG, "onPageSelected(): isFirst=$isInitial")
        if (isInitial) {
            binding.tvLabel1.visibility = View.VISIBLE
            binding.tvLabel1.animateFadeInWithAfter {
                binding.tvLabel2.visibility = View.VISIBLE
                binding.tvLabel2.animateFadeIn()
            }
        }
        parentView.enabledNextButton(canNext())
    }

    override fun onClickedNextButton() {}

    // SignUpInputPhoneFragment
    override fun setPhoneNumber(phoneNumber: String) {}
    override fun showSmsAuthInputWidget() {}
    override fun requestFocusSmsCode() {}
    override fun setSmsCode(code: String) {}
    override fun clearSmsCode() {}
    override fun showPhoneNumberError() {}
    override fun showIncorrectSmsCodeError() {}
    override fun updateResendButtonText(remainingTimeString: String) {}

    // SignUpInputEmailFragment
    override fun setEmail(email: String) {}
    override fun showEmailExistError() {}

    protected fun showSoftKeyboard(view: EditText) {
        if (view.requestFocus()) {
            (requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                .showSoftInput(view, InputMethodManager.SHOW_FORCED)
        }
    }

    protected fun hideSoftKeyboard(view: EditText) {
        (requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
            .hideSoftInputFromWindow(view.windowToken, 0)
    }

    protected fun View.animateFadeIn() {
        animateFadeInWithAfter(null)
    }

    protected fun View.animateFadeInWithAfter(action: (() -> Unit)?) {
        visibility = View.VISIBLE
        val alpha = PropertyValuesHolder.ofFloat(View.ALPHA, 0f, 0.2f, 1f)
        val translate = PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, -100f, 0f)

        ObjectAnimator.ofPropertyValuesHolder(this, alpha, translate).apply {
            duration = 500
            interpolator = AccelerateDecelerateInterpolator()
            addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator?) {}
                override fun onAnimationEnd(animation: Animator?) {
                    action?.invoke()
                }
                override fun onAnimationCancel(animation: Animator?) {}
                override fun onAnimationRepeat(animation: Animator?) {}
            })
        }.start()
    }
}