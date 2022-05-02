package com.jojo.android.mwodeola.presentation.security.screen

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.CycleInterpolator
import androidx.fragment.app.Fragment
import com.google.android.material.card.MaterialCardView
import com.jojo.android.mwodeola.R
import com.jojo.android.mwodeola.databinding.FragmentAuthenticationBinding
import com.jojo.android.mwodeola.presentation.security.SecurityManager

@SuppressLint("SetTextI18n")
abstract class BaseAuthenticationFragment(
    private val presenter: AuthenticationContract.Presenter
) : Fragment(), BaseAuthenticationActivity.BackPressedInterceptor {

    companion object {
        private const val TAG = "BaseAuthenticationFragment"
    }

    interface OnSecureKeyPadListener {
        fun onKeyClicked(char: Char)
        fun onBackPressed()
    }

    protected lateinit var binding: FragmentAuthenticationBinding

    protected val baseActivity: BaseAuthenticationActivity
        get() = requireActivity() as BaseAuthenticationActivity

    protected val purpose: Int
        get() = baseActivity.purpose

    protected lateinit var sharedPref: SecurityManager.SharedPref

    protected val toggleViews = mutableListOf<MaterialCardView>()
    protected val passwordBuilder = StringBuilder()

    protected val subTitleDefaultColor by lazy { resources.getColor(R.color.gray400, null) }
    protected val subTitleErrorColor by lazy { resources.getColor(R.color.red400, null) }
    protected val toggleColor by lazy { resources.getColor(R.color.white, null) }
    protected val toggleColorError by lazy { resources.getColor(R.color.red400, null) }
    protected val errorVibrationPattern = longArrayOf(0, 100, 20, 100)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = FragmentAuthenticationBinding.inflate(inflater, container, false)
        .also { binding = it }
        .root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPref = SecurityManager.SharedPref(requireContext())

        initView()
    }

    override fun onInterceptBackPressed(): Boolean = when {
        passwordBuilder.isNotBlank() -> {
            clearPassword()
            true
        }
        canBackPage() -> {
            moveBackPage()
            true
        }
        else -> false
    }

    abstract fun canBackPage(): Boolean

    protected open fun initView() {}

    protected fun moveBackPage() {
        baseActivity.moveBackPage()
    }

    protected fun moveNextPage() {
        baseActivity.moveNextPage()
    }

    protected fun finishForSucceed() {
        baseActivity.finishForSucceed()
    }

    protected fun appendToggleAt(index: Int) {
        toggleViews.getOrNull(index)?.let {
            it.alpha = 1f

            val scaleX = PropertyValuesHolder.ofFloat(View.SCALE_X, 1f, 1.2f)
            val scaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f, 1.2f)

            val scaleAnimator = ObjectAnimator.ofPropertyValuesHolder(it, scaleX, scaleY).apply {
                interpolator = AccelerateDecelerateInterpolator()
                duration = 100
                repeatMode = ValueAnimator.REVERSE
                repeatCount = 1
            }
            scaleAnimator.start()
        }
    }

    protected fun removeToggleAt(index: Int) {
        toggleViews.getOrNull(index)?.alpha = 0.2f
        toggleViews.getOrNull(index)?.setCardBackgroundColor(toggleColor)
    }

    protected fun clearPassword() {
        passwordBuilder.clear()
        toggleViews.forEach {
            it.alpha = 0.2f
            it.setCardBackgroundColor(toggleColor)
        }
    }

    protected fun showIncorrectError() {
        showIncorrectErrorWithEndAction(null)
    }

    protected fun showIncorrectErrorWithEndAction(endAction: Runnable?) {
        binding.viewPager2.currentItem = 0
        binding.toggleContainer.animate().setInterpolator(CycleInterpolator(3f))
            .setDuration(400)
            .translationX(20f)
            .withStartAction {
                vibrateIncorrectError()
                toggleViews.forEach {
                    it.alpha = 1f
                    it.setCardBackgroundColor(toggleColorError)
                }
            }
            .withEndAction {
                clearPassword()
                endAction?.run()
            }
            .start()
    }

    protected fun showIncorrectErrorText(count: Int, limit: Int) {
        binding.tvSubtitle.text = "비밀번호가 맞지 않습니다. 다시 입력해주세요."
        binding.tvSubtitle.setTextColor(subTitleErrorColor)
        binding.tvAuthFailed.text = "인증 오류 횟수($count/$limit)"
        binding.tvAuthFailed.visibility = View.VISIBLE
    }

    protected fun vibrateIncorrectError() {
        val vibrator = requireContext().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            vibrator.vibrate(errorVibrationPattern, -1)
        } else {
            vibrator.vibrate(VibrationEffect.createWaveform(errorVibrationPattern, -1))
        }
    }
}