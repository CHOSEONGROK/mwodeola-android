package com.jojo.android.mwodeola.presentation.account.edit

import android.animation.*
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Color
import android.util.Log
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.ViewPropertyAnimator
import android.view.animation.OvershootInterpolator
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.core.view.setMargins
import androidx.core.widget.addTextChangedListener
import androidx.transition.TransitionManager
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputLayout
import com.jojo.android.mwodeola.R
import com.jojo.android.mwodeola.data.account.Account
import com.jojo.android.mwodeola.data.account.AccountDetail
import com.jojo.android.mwodeola.databinding.ActivityEditAccountBinding
import com.jojo.android.mwodeola.presentation.security.bottmSheet.PatternPasswordView
import com.jojo.android.mwodeola.util.*

class EditAccountActivityViewBinder(
    private val activity: EditAccountActivity,
    private val presenter: EditAccountContract.Presenter
) {
    companion object {
        private const val TAG = "EditAccountActivityViewBinder"
    }

    class DetailCardViewHolder(
        val checkBox: CheckBox,
        val container: ConstraintLayout,
        val innerCardView: MaterialCardView,
        val containerRemoveButton: MaterialCardView,
        val icon: ImageView,
        val label: TextView,
        val editTextLayout: TextInputLayout? = null,
        val editText: EditText? = null,
        val patternView: PatternPasswordView? = null,
        val patternResetButton: Button? = null,
        val noChangePredicate: (source: String?, target: CharSequence?) -> Boolean,
        val errorPredicate: (text: CharSequence?) -> Boolean,
        val errorMessage: String
    ) {
        private val defaultColor = ColorStateList.valueOf(ResourcesCompat.getColor(checkBox.resources, R.color.text_view_text_default_color, null))
        private val validColor = ColorStateList.valueOf(ResourcesCompat.getColor(checkBox.resources, R.color.app_theme_color, null))
        private val errorColor = ColorStateList.valueOf(ResourcesCompat.getColor(checkBox.resources, android.R.color.holo_red_light, null))

        fun initView(data: String?, onDataChanged: () -> Unit) {
            val isVisible = (data?.isNotBlank() == true)

            editText?.setText(data)
            patternView?.setPattern(data)

            checkBox.isChecked = isVisible
            container.isVisible = isVisible

            val tintColor = if (isVisible) defaultColor else errorColor
            icon.imageTintList = tintColor
            label.setTextColor(tintColor)

            editTextLayout?.error = if (isVisible) null else errorMessage
            editText?.addTextChangedListener(onTextChanged = { text: CharSequence?, _, _, _ ->
                notifyEditTextChanged(data, text)
                onDataChanged.invoke()
            })

            patternView?.setPatternWatcher(object : PatternPasswordView.SimplePatternWatcher() {
                override fun onCompleted(pattern: String) {
                    notifyPatternInputCompleted(data, pattern)
                    onDataChanged.invoke()
                }
            })

            patternResetButton?.isEnabled = isVisible
            patternResetButton?.setOnClickListener {
                performClickPatternReset()
                onDataChanged.invoke()
            }
        }

        fun disable() {
            checkBox.isEnabled = false
            container.isEnabled = false
            innerCardView.isEnabled = false
            containerRemoveButton.isEnabled = false
            icon.isEnabled = false
            label.isEnabled = false
            editTextLayout?.isEnabled = false
            editText?.isEnabled = false
            patternView?.isEnabled = false
            patternResetButton?.isEnabled = false
        }

        private fun notifyEditTextChanged(source: String?, target: CharSequence?) {
            val color = when {
                noChangePredicate(source, target) -> defaultColor
                errorPredicate(target) -> errorColor
                else -> validColor
            }
            icon.imageTintList = color
            label.setTextColor(color)
            editTextLayout?.error = if (errorPredicate(target)) errorMessage else null
        }

        private fun notifyPatternInputCompleted(source: String?, target: CharSequence?) {
            if (target == null || target.length < 2)
                return

            val color = when {
                noChangePredicate(source, target) -> defaultColor
                errorPredicate(target) -> errorColor
                else -> validColor
            }

            icon.imageTintList = color
            label.setTextColor(color)

            patternResetButton?.isEnabled = true
        }

        private fun performClickPatternReset() {
            patternView?.reset()
            patternResetButton?.isEnabled = false

            icon.imageTintList = errorColor
            label.setTextColor(errorColor)
        }
    }

    val isRunningRemoveMode: Boolean
        get() = cardViewClickListener.isRunningRemoveMode

    val webUrl: String
        get() = binding.edtWebUrl.text.toString()
    val userId: String?
        get() = if (binding.checkBoxUserId.isChecked) binding.userIdEdt.text.toString() else null
    val userPassword: String?
        get() = if (binding.checkBoxUserPassword.isChecked) binding.userPasswordEdt.text.toString() else null
    val userPasswordPin4: String?
        get() = if (binding.checkBoxUserPasswordPin4.isChecked) binding.userPasswordPin4Edt.text.toString() else null
    val userPasswordPin6: String?
        get() = if (binding.checkBoxUserPasswordPin6.isChecked) binding.userPasswordPin6Edt.text.toString() else null
    val userPasswordPattern: String?
        get() = if (binding.checkBoxUserPasswordPattern.isChecked) binding.userPasswordPatternView.pattern else null
    val memo: String?
        get() = if (binding.checkBoxMemo.isChecked) binding.memoEdt.text.toString() else null

    private val binding: ActivityEditAccountBinding = activity.binding
    private val softInputObserver: KeyboardHeightProvider? = activity.softInputObserver

    private val viewHolders = mutableListOf<DetailCardViewHolder>()

    private var windowHeight: Int = 0
    private var navigationBarHeight: Int = 0
    private var currentFocusedEditText: EditText? = null

    private val cardViewClickListener = CardViewClickListener()
    private val containerRemoveButtonClickListener = ContainerRemoveButtonClickListener()
    private val softInputWatcher = SoftInputWatcher()

    private val defaultColor = ColorStateList.valueOf(ResourcesCompat.getColor(activity.resources, R.color.text_view_text_default_color, null))
    private val validColor = ColorStateList.valueOf(ResourcesCompat.getColor(activity.resources, R.color.app_theme_color, null))
    private val errorColor = ColorStateList.valueOf(ResourcesCompat.getColor(activity.resources, android.R.color.holo_red_light, null))

    fun initView(account: Account): Unit = with(binding) {
        accountGroupDivider.isVisible = account.isOwnAccount
        detailHelpLabel.isVisible = account.isOwnAccount

        cardViewForSnsDetail.isVisible = account.isSnsAccount
        snsIcon.isVisible = account.isSnsAccount
        if (account.isSnsAccount) {
            snsIcon.setSnsGroupIcon(account.sns_group!!.sns)
        }

        val detail = account.detail

        windowHeight = Utils.getWindowPixels(activity).heightPixels
        navigationBarHeight = Compat.getSystemBarInset(activity.window).navigationBarHeight

        softInputObserver?.setOnKeyboardListener(softInputWatcher)

        binding.bottomSheet.layoutTransition.setDuration(200L)

        // 웹 사이트 EditText
        edtWebUrl.addTextChangedListener( onTextChanged = { text: CharSequence?, _, _, _ ->
            notifyEditTextChanged(
                icon = ivWebUrl, label = null, layout = edtLayoutWebUrl,
                defaultCondition = text.isNullOrBlank() || text.toString() == presenter.initialAccount.own_group.web_url,
                errorCondition = presenter.isValidWebUrl(text.toString()).not(),
                errorMessage = "웹 사이트 형식이 잘못되었습니다."
            )
        })

        // User Id
        DetailCardViewHolder(
            checkBox = checkBoxUserId,
            container = userIdContainer,
            innerCardView = userIdCardView,
            containerRemoveButton = userIdContainerRemoveButton,
            icon = userIdIcon,
            label = userIdLabel,
            editTextLayout = userIdEdtLayout,
            editText = userIdEdt,
            noChangePredicate = { source, target -> source == target?.toString() },
            errorPredicate = { text -> text.isNullOrBlank() },
            errorMessage = "아이디를 입력해 주세요."
        ).also {
            it.initView(detail.user_id, onDataChanged = {
                btnSave.isEnabled = presenter.isPossibleSave()
            })

            viewHolders.add(it)
        }

        // UserPassword
        DetailCardViewHolder(
            checkBox = checkBoxUserPassword,
            container = userPasswordContainer,
            innerCardView = userPasswordCardView,
            containerRemoveButton = userPasswordContainerRemoveButton,
            icon = userPasswordIcon,
            label = userPasswordLabel,
            editTextLayout = userPasswordEdtLayout,
            editText = userPasswordEdt,
            noChangePredicate = { source, target -> source == target?.toString() },
            errorPredicate = { text -> text.isNullOrBlank() },
            errorMessage = "비밀번호를 입력해 주세요."
        ).also {
            it.initView(detail.user_password, onDataChanged = {
                btnSave.isEnabled = presenter.isPossibleSave()
            })

            viewHolders.add(it)
        }

        // UserPasswordPin4
        DetailCardViewHolder(
            checkBox = checkBoxUserPasswordPin4,
            container = userPasswordPin4Container,
            innerCardView = userPasswordPin4CardView,
            containerRemoveButton = userPasswordPin4ContainerRemoveButton,
            icon = userPasswordPin4Icon,
            label = userPasswordPin4Label,
            editTextLayout = userPasswordPin4EdtLayout,
            editText = userPasswordPin4Edt,
            noChangePredicate = { source, target -> source == target?.toString() },
            errorPredicate = { text -> text.isNullOrBlank() || text.length < 4 },
            errorMessage = "숫자 4자리를 입력해 주세요."
        ).also {
            it.initView(detail.user_password_pin4, onDataChanged = {
                btnSave.isEnabled = presenter.isPossibleSave()
            })

            viewHolders.add(it)
        }

        // UserPasswordPin6
        DetailCardViewHolder(
            checkBox = checkBoxUserPasswordPin6,
            container = userPasswordPin6Container,
            innerCardView = userPasswordPin6CardView,
            containerRemoveButton = userPasswordPin6ContainerRemoveButton,
            icon = userPasswordPin6Icon,
            label = userPasswordPin6Label,
            editTextLayout = userPasswordPin6EdtLayout,
            editText = userPasswordPin6Edt,
            noChangePredicate = { source, target -> source == target?.toString() },
            errorPredicate = { text -> text.isNullOrBlank() || text.length < 6 },
            errorMessage = "숫자 6자리를 입력해 주세요."
        ).also {
            it.initView(detail.user_password_pin6, onDataChanged = {
                btnSave.isEnabled = presenter.isPossibleSave()
            })

            viewHolders.add(it)
        }

        // UserPasswordPattern
        DetailCardViewHolder(
            checkBox = checkBoxUserPasswordPattern,
            container = userPasswordPatternContainer,
            innerCardView = userPasswordPatternCardView,
            containerRemoveButton = userPasswordPatternContainerRemoveButton,
            icon = userPasswordPatternIcon,
            label = userPasswordPatternLabel,
            patternView = userPasswordPatternView,
            patternResetButton = userPasswordPatternResetButton,
            noChangePredicate = { source, target -> source == target?.toString() },
            errorPredicate = { text -> text.isNullOrBlank() },
            errorMessage = ""
        ).also {
            it.initView(detail.user_password_pattern, onDataChanged = {
                btnSave.isEnabled = presenter.isPossibleSave()
            })

            viewHolders.add(it)
        }

        // Memo
        DetailCardViewHolder(
            checkBox = checkBoxMemo,
            container = memoContainer,
            innerCardView = memoCardView,
            containerRemoveButton = memoContainerRemoveButton,
            icon = memoIcon,
            label = memoLabel,
            editTextLayout = memoEdtLayout,
            editText = memoEdt,
            noChangePredicate = { source, target -> source == target?.toString() },
            errorPredicate = { text -> text.isNullOrBlank() },
            errorMessage = "메모를 입력해 주세요."
        ).also {
            it.initView(detail.memo, onDataChanged = {
                btnSave.isEnabled = presenter.isPossibleSave()
            })

            viewHolders.add(it)
        }


        var firstVisibleFlag = true
        viewHolders.forEach {
            if (account.isOwnAccount) {
                it.checkBox.setOnCheckedChangeListener(containerRemoveButtonClickListener)
                it.innerCardView.setOnClickListener(cardViewClickListener)
                it.innerCardView.setOnLongClickListener(cardViewClickListener)
                it.containerRemoveButton.setOnClickListener(containerRemoveButtonClickListener)
                it.editText?.onFocusChangeListener = softInputWatcher
            } else {
                it.disable()

                it.container.scaleX = 0.9f
                it.container.scaleY = 0.9f
                it.innerCardView.layoutParams = (it.innerCardView.layoutParams as ConstraintLayout.LayoutParams).apply {
                    setMargins(0)
                }

                if (it.container.isVisible) {
                    if (firstVisibleFlag) {
                        firstVisibleFlag = false

                        it.icon.layoutParams = (it.icon.layoutParams as ConstraintLayout.LayoutParams).apply {
                            topMargin = 36.dpToPixels(it.icon.context)
                        }
                    } else {
                        it.icon.layoutParams = (it.icon.layoutParams as ConstraintLayout.LayoutParams).apply {
                            topMargin = 6.dpToPixels(it.icon.context)
                        }
                    }
                }

                it.editTextLayout?.layoutParams = (it.editTextLayout?.layoutParams as? ConstraintLayout.LayoutParams)?.apply {
                    bottomMargin = 6.dpToPixels(it.container.context)
                }
                it.editTextLayout?.boxStrokeWidth = 0
                it.editTextLayout?.endIconMode = TextInputLayout.END_ICON_NONE
                it.patternResetButton?.isVisible = false

                it.innerCardView.setCardBackgroundColor(Color.TRANSPARENT)
            }
        }

        btnSave.setOnClickListener {
            presenter.save()
        }
    }

    fun cancelRemoveMode() {
        if (isRunningRemoveMode) cardViewClickListener.setRemoveMode(false)
    }

    private fun notifyEditTextChanged(
        icon: ImageView, label: TextView?, layout: TextInputLayout,
        defaultCondition: Boolean,
        errorCondition: Boolean,
        errorMessage: String,
    ) {
        val color = when {
            defaultCondition -> defaultColor
            errorCondition -> errorColor
            else -> validColor
        }
        icon.imageTintList = color
        label?.setTextColor(color)
        layout.error = if (errorCondition) errorMessage else null

        binding.btnSave.isEnabled = presenter.isPossibleSave()
    }

    inner class CardViewClickListener : View.OnClickListener, View.OnLongClickListener {

        var isRunningRemoveMode = false
            private set

        override fun onClick(view: View?) {}

        override fun onLongClick(view: View?): Boolean {
            setRemoveMode(isRunningRemoveMode.not())

            view?.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            return true
        }

        fun setRemoveMode(isRemoveMode: Boolean) {
            isRunningRemoveMode = isRemoveMode

            with(binding) {
                val containerScale = if (isRemoveMode) 0.9f else 1f

                val removeButtonScaleTo = if (isRemoveMode) 1f else 0f
                val alphaTo = if (isRemoveMode) 1f else 0f

                val animators = mutableListOf<Animator>()

                viewHolders.forEach {
                    animators.add(getContainerAnimator(it.container, containerScale))
                    animators.add(getRemoveButtonAnimator(it.containerRemoveButton, removeButtonScaleTo, alphaTo))
                }

                AnimatorSet().apply {
                    playTogether(animators)
                }.start()
            }

            currentFocusedEditText?.clearFocusAndHideSoftInput()
        }

        private fun getContainerAnimator(container: ConstraintLayout, scaleTo: Float): Animator {
            val scaleX = PropertyValuesHolder.ofFloat(View.SCALE_X, container.scaleX, scaleTo)
            val scaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, container.scaleY, scaleTo)

            return ObjectAnimator.ofPropertyValuesHolder(container, scaleX, scaleY).apply {
                interpolator = OvershootInterpolator()
                duration = 300
            }
        }

        private fun getRemoveButtonAnimator(button: View, scaleTo: Float, alphaTo: Float): Animator {
            val scaleX = PropertyValuesHolder.ofFloat(View.SCALE_X, button.scaleX, scaleTo)
            val scaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, button.scaleY, scaleTo)
            val alpha = PropertyValuesHolder.ofFloat(View.ALPHA, button.alpha, alphaTo)

            return ObjectAnimator.ofPropertyValuesHolder(button, scaleX, scaleY, alpha).apply {
                interpolator = OvershootInterpolator()
                duration = 300
                if (scaleTo == 1f) {
                    doOnStart { button.isVisible = true }
                } else {
                    doOnEnd { button.isVisible = false }
                }
            }
        }
    }

    inner class ContainerRemoveButtonClickListener : View.OnClickListener, CompoundButton.OnCheckedChangeListener {

        // CheckBox's checked changed in BackDrop
        override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
            val container = viewHolders.find { it.checkBox == buttonView }?.container
                ?: return

            TransitionManager.beginDelayedTransition(binding.bottomSheet)
            container.isVisible = isChecked

            binding.btnSave.isEnabled = presenter.isPossibleSave()
        }

        // ContainerRemoveButton's clicked listener
        override fun onClick(view: View?) = with(binding) {
            viewHolders.find { it.containerRemoveButton == view }
                ?.let {
                    TransitionManager.beginDelayedTransition(binding.bottomSheet)
                    it.container.isVisible = false
                    it.checkBox.isChecked = false
                }

            binding.btnSave.isEnabled = presenter.isPossibleSave()
        }
    }

    inner class SoftInputWatcher : View.OnFocusChangeListener, KeyboardHeightProvider.OnKeyboardListener {
        override fun onFocusChange(view: View?, hasFocus: Boolean) {
            currentFocusedEditText = if (hasFocus) view as? EditText else null

            if (softInputObserver?.isShowingKeyboard == true) {
                smoothScrollToFocusedEditText(softInputObserver.lastKeyboardHeight)
            }
        }

        override fun onHeightChanged(keyboardHeight: Int, isShowing: Boolean) {
            Log.d(TAG, "onHeightChanged(): keyboardHeight=$keyboardHeight, isShowing=$isShowing")
            val context = binding.bottomSheetContentsBottomGuideline.context
            if (isShowing) {
                binding.bottomSheetContentsBottomGuideline.setGuidelineEnd(120.dpToPixels(context) + keyboardHeight)

                smoothScrollToFocusedEditText(keyboardHeight)
            } else {
                binding.bottomSheetContentsBottomGuideline.setGuidelineEnd(120.dpToPixels(context))
            }
        }

        private fun smoothScrollToFocusedEditText(keyboardHeight: Int) {
            if (currentFocusedEditText == null)
                return

            val cardView = viewHolders.find { it.editText == currentFocusedEditText }?.innerCardView
                ?: return

            val distanceBetweenCardViewAndNavBar = (windowHeight - cardView.getLocationInWindow().y) - cardView.height - navigationBarHeight
            val scrollBy = keyboardHeight - distanceBetweenCardViewAndNavBar

            if (scrollBy > 0) {
                binding.bottomSheetContentsBottomGuideline.post {
                    binding.scrollView.smoothScrollBy(0, scrollBy)
                }
                //binding.scrollView.scrollBy(0, scrollBy)
            }
        }
    }
}