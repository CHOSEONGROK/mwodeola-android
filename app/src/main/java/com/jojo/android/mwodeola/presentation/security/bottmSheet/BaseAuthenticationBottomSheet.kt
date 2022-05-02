package com.jojo.android.mwodeola.presentation.security.bottmSheet

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Resources
import android.os.*
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.CycleInterpolator
import android.widget.Toast
import androidx.appcompat.widget.AppCompatCheckedTextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.FragmentActivity
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.jojo.android.mwodeola.R
import com.jojo.android.mwodeola.databinding.BottomSheetAuthenticationBinding
import com.jojo.android.mwodeola.presentation.common.BottomUpDialog
import com.jojo.android.mwodeola.presentation.security.AuthType
import com.jojo.android.mwodeola.presentation.security.Authenticators
import com.jojo.android.mwodeola.util.dpToPixels

@SuppressLint("SetTextI18n")
abstract class BaseAuthenticationBottomSheet constructor(
    private val activity: FragmentActivity,
    private val listener: Authenticators.AuthenticationCallback?,
    private val forAutofillService: Boolean
) : BottomSheetDialog(activity), AuthenticationBottomSheetContract.View {

    companion object {
        private const val TAG = "BaseAuthenticationBottomSheet"
    }

    interface OnSecureKeyPadListener {
        fun onKeyClicked(char: Char)
        fun onBackPressed()
    }

    abstract val authType: AuthType
    abstract val presenter: AuthenticationBottomSheetContract.Presenter

    protected val binding by lazy { BottomSheetAuthenticationBinding.inflate(layoutInflater) }
    protected val resources: Resources
        get() = activity.resources



    protected val toggleViews = mutableListOf<AppCompatCheckedTextView>()
    protected val passwordBuilder = StringBuilder()

    protected var isAuthenticationSucceed = false
    protected var isBiometricConfirmed = false
    protected var isExceedAuthLimit = false

    protected val subTitleDefaultColor = resources.getColor(R.color.text_view_text_default_color, null)
    protected val subTitleErrorColor = resources.getColor(R.color.red400, null)
    protected val toggleColorLight = resources.getColor(R.color.app_theme_color, null)
    protected val toggleColorDark = resources.getColor(R.color.gray300, null)
    protected val toggleColorError = resources.getColor(R.color.red400, null)
    protected val biometricConfirmedOkTintColor = ColorStateList.valueOf(resources.getColor(R.color.app_theme_color, null))
    protected val biometricConfirmedNoTintColor = ColorStateList.valueOf(resources.getColor(R.color.gray400, null))
    protected val errorVibrationPattern = longArrayOf(0, 100, 20, 100)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        presenter.loadAuthFailureCount()
        presenter.checkBiometricEnroll()

        initDialogHeight()
        initView()
    }

    override fun dismiss() {
        super.dismiss()
        when {
            isAuthenticationSucceed ->
                listener?.onSucceed()
            isExceedAuthLimit ->
                listener?.onExceedAuthLimit(presenter.authFailureLimit)
            else ->
                listener?.onFailure()
        }
    }

    override fun dismissForSucceed() {
        if (isBiometricConfirmed) {
            presenter.changeAuthTypeToBiometric()
        }

        isAuthenticationSucceed = true
        dismiss()
    }

    override fun dismissForExceedAuthLimit() {
        isExceedAuthLimit = true
        dismiss()
    }

    override fun initView() {}

    override fun showBiometricConfirmButton(): Unit = with(binding) {
        btnBiometricAuthConfirmedIcon.imageTintList = biometricConfirmedNoTintColor
        btnBiometricAuthConfirmedLabel.setTextColor(biometricConfirmedNoTintColor)
        btnBiometricAuthConfirmed.setOnClickListener {
            isBiometricConfirmed = !isBiometricConfirmed

            val tintColor =
                if (isBiometricConfirmed) biometricConfirmedOkTintColor
                else biometricConfirmedNoTintColor

            btnBiometricAuthConfirmedIcon.imageTintList = tintColor
            btnBiometricAuthConfirmedLabel.setTextColor(tintColor)
        }
    }

    override fun appendToggleAt(index: Int) {
        toggleViews.getOrNull(index)?.let {
            it.isEnabled = true

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

    override fun removeToggleAt(index: Int) {
        toggleViews.getOrNull(index)?.isEnabled = false
    }

    override fun clearToggle() {
        toggleViews.forEach {
            it.isChecked = false
            it.isEnabled = false
        }
    }

    override fun showAuthFailureText() {
        binding.tvSubtitle.text = "비밀번호가 맞지 않습니다. 다시 입력해주세요."
        binding.tvSubtitle.setTextColor(subTitleErrorColor)
    }

    override fun showAuthFailureCountText(count: Int, limit: Int) {
        binding.tvAuthFailed.text = "인증 오류 횟수($count/$limit)"
        binding.tvAuthFailed.visibility = View.VISIBLE
    }

    override fun showAuthFailure() {
        binding.toggleContainer.animate().setInterpolator(CycleInterpolator(3f))
            .setDuration(400)
            .translationX(20f)
            .withStartAction {
                toggleViews.forEach { it.isChecked = true }
                vibrateAuthFailure()
            }
            .withEndAction {
                clearToggle()
                passwordBuilder.clear()
                binding.viewPager2.currentItem = 0
            }
            .start()
    }

    override fun vibrateAuthFailure() {
        val vibrator = activity.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            vibrator.vibrate(errorVibrationPattern, -1)
        } else {
            vibrator.vibrate(VibrationEffect.createWaveform(errorVibrationPattern, -1))
        }
    }

    override fun showBiometricEnrollChangedDialog() {
        if (forAutofillService) {
            showToast("휴대폰의 생체 인증 정보가 변경되어 앱에 등록되었던 지문 인증은 사라집니다")
        } else {
            BottomUpDialog.Builder(activity.supportFragmentManager)
                .title("생체 인증 정보 변경됨")
                .subtitle("휴대폰의 생체 인증 정보가 변경되어 앱에 등록되었던 지문 인증은 사라집니다.")
                .confirmedButton()
                .show()
        }
    }

    override fun showExceedAuthLimitDialog() {
        BottomUpDialog.Builder(activity.supportFragmentManager)
            .title("비밀번호 인증 횟수 제한을 초과하여 계정이 잠금 처리됩니다")
            .subtitle("인증 오류 횟수: ${presenter.authFailureLimit}번")
            .confirmedButton {
                dismissForExceedAuthLimit()
            }
            .show()
    }

    override fun showLockedUserDialog() {
        BottomUpDialog.Builder(activity.supportFragmentManager)
            .title("계정 잠금")
            .subtitle("비밀번호 인증 초과 등의 보안 사유로 계정이 잠겨 있습니다.")
            .confirmedButton {
                dismissForExceedAuthLimit()
            }
            .show()
    }

    override fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    private fun initDialogHeight() {
        val windowHeight = (getWindowDisplayMetrics().heightPixels * 0.3f).toInt()
        if (windowHeight > 0) {
            if (presenter.hasNewBiometricEnrolled) {
                val extraHeight = 50.dpToPixels(activity) + 12.dpToPixels(activity)
                binding.keyPadGuideline.setGuidelineEnd(windowHeight + extraHeight)
                binding.btnBiometricAuthConfirmed.visibility = View.VISIBLE
            } else {
                val extraHeight = 12.dpToPixels(activity)
                binding.keyPadGuideline.setGuidelineEnd(windowHeight + extraHeight)
                binding.btnBiometricAuthConfirmed.visibility = View.GONE
            }
        }

        // behavior.peekHeight = getWindowHeight(0.3f)
        behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {}
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    behavior.state = BottomSheetBehavior.STATE_COLLAPSED
                }
            }
        })
        // behavior.disableShapeAnimations()

        if (forAutofillService) {
            resizingLayoutForAutofillAuthentication()
        }
    }

    private fun resizingLayoutForAutofillAuthentication() {
        val windowWidth = getWindowDisplayMetrics().widthPixels
        if (windowWidth > 0) {
            binding.root.layoutParams = binding.root.layoutParams.apply {
                width = windowWidth
            }
        }

        window?.navigationBarColor = ContextCompat.getColor(activity, android.R.color.white)

        if (authType != AuthType.BIOMETRIC) {
            Handler(Looper.getMainLooper()).postDelayed({
                binding.root.background = ResourcesCompat
                    .getDrawable(resources, R.drawable.bg_bottom_sheet, null)
                binding.root.requestLayout()
            }, 200)
        }
    }

    private fun getWindowDisplayMetrics(): DisplayMetrics {
        val displayMetrics = DisplayMetrics()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            activity.window.windowManager?.defaultDisplay?.getRealMetrics(displayMetrics)
        } else {
            activity.display?.getRealMetrics(displayMetrics)
        }
        return displayMetrics
    }
}