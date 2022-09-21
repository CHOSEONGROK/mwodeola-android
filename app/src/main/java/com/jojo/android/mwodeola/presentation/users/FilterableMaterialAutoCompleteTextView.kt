package com.jojo.android.mwodeola.presentation.users

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.text.InputFilter
import android.text.InputType
import android.text.Spanned
import android.util.AttributeSet
import android.util.Log
import android.view.KeyEvent
import android.view.animation.CycleInterpolator
import android.view.animation.TranslateAnimation
import android.widget.ArrayAdapter
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputLayout
import com.jojo.android.mwodeola.R

/**
 * ParentView 인 TextInputLayout 에서 style 지정해줘야함.
 * style="@style/Widget.MaterialComponents.TextInputLayout.FilledBox.Dense.ExposedDropdownMenu"
 * */
class FilterableMaterialAutoCompleteTextView : MaterialAutoCompleteTextView {
    companion object {
        private const val TAG = "FilterableAutoComplete"
    }

    interface OnInputCallback {
        fun onTextChanged(isCompleted: Boolean, text: String)
    }

    private val errorColor = ColorStateList.valueOf(Color.rgb(229, 57, 53))
    private val errorColorText = ColorStateList.valueOf(Color.rgb(211, 47, 47))

    private val shakeErrorAnim = TranslateAnimation(0f, 20f, 0f, 0f).apply {
        duration = 300
        interpolator = CycleInterpolator(2f)
    }

    private val domains = listOf(
        "@naver.com", "@gmail.com", "@daum.net", "@hanmail.net", "@nate.com", "@yahoo.com", "@korea.com",
        "@hotmail.com", "@lycos.co.kr", "@qq.com", "@dreamwiz.com", "@paran.com", "@empal.com", "@empass.com")

    private var mLocal: String = ""

    private var callback: OnInputCallback? = null

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        setFiltersByInputType(inputType)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        findTextInputLayoutAncestor()?.let {
            it.isErrorEnabled = true
            it.boxStrokeErrorColor = errorColor
            it.setErrorTextColor(errorColorText)
            it.setErrorIconTintList(errorColor)
        }
    }

    fun setOnInputCallback(callback: OnInputCallback) {
        this.callback = callback
    }

    fun removeOnInputCallback() {
        this.callback = null
    }

    override fun setInputType(type: Int) {
        super.setInputType(type)
        setFiltersByInputType(type)
    }

    override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
        if (event?.action != KeyEvent.ACTION_DOWN)
            return super.dispatchKeyEvent(event)

        if (inputType == InputType.TYPE_CLASS_PHONE && event.keyCode == KeyEvent.KEYCODE_DEL) {
            // removed
            val length = text?.length
            if (length == 5 || length == 10) {
                text?.delete(length - 2, length)
                return true
            }
        }

        return super.dispatchKeyEvent(event)
    }

    override fun onTextChanged(text: CharSequence?, start: Int, lengthBefore: Int, lengthAfter: Int) {
        callback?.onTextChanged(isCompleteFormat(text), text.toString())

        if (text.isNullOrBlank())
            return

        val isEmailInputType = (inputType == InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS ||
                inputType == InputType.TYPE_TEXT_VARIATION_EMAIL_SUBJECT ||
                inputType == InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS)

        if (isEmailInputType) {
            val parts = text.split('@')

            if (parts.size == 2 && parts[0].isNotBlank()) {
                val local = parts[0]
                if (mLocal != local) {
                    mLocal = local
                    val addresses = domains.map { local + it }
                    val adapter = ArrayAdapter(context, R.layout.email_popup_window_list_item, addresses)
                    setAdapter(adapter)
                }
            } else {
                mLocal = ""
                setAdapter(null)
                if (isPopupShowing) {
                    dismissDropDown()
                }
            }
        }
    }

    override fun setError(error: CharSequence?) {
        val ancestor = findTextInputLayoutAncestor()
        if (ancestor != null) {
            ancestor.error = error
            ancestor.startAnimation(shakeErrorAnim)
        } else {
            super.setError(error)
        }
    }

    override fun setError(error: CharSequence?, icon: Drawable?) {
        val ancestor = findTextInputLayoutAncestor()
        if (ancestor != null) {
            ancestor.errorIconDrawable = icon
            ancestor.error = error
            ancestor.startAnimation(shakeErrorAnim)
        } else {
            super.setError(error, icon)
        }
    }

    fun isShowingError(): Boolean {
        val ancestor = findTextInputLayoutAncestor()
        if (ancestor != null) {
            return (ancestor.error != null)
        } else {
            return (this.error != null)
        }
    }

    private fun isCompleteFormat(text: CharSequence?): Boolean =
        if (text.isNullOrBlank()) false
        else when (inputType) {
            InputType.TYPE_TEXT_VARIATION_PERSON_NAME ->
                text.isNotBlank()

            InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS,
            InputType.TYPE_TEXT_VARIATION_EMAIL_SUBJECT,
            InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS ->
                android.util.Patterns.EMAIL_ADDRESS.matcher(text).matches()

            InputType.TYPE_CLASS_PHONE ->
                text.length == 13

            InputType.TYPE_CLASS_NUMBER,
            InputType.TYPE_NUMBER_FLAG_DECIMAL,
            InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL ->
                text.length == 6

            else -> text.isNotBlank()
        }

    private fun setFiltersByInputType(inputType: Int) {
        // 필터 순서 중요!!
        when (inputType) {
            InputType.TYPE_TEXT_VARIATION_PERSON_NAME -> {}

            InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS,
            InputType.TYPE_TEXT_VARIATION_EMAIL_SUBJECT,
            InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS ->
                filters = arrayOf(InputFilter.LengthFilter(100))

            InputType.TYPE_CLASS_PHONE ->
                filters = arrayOf(PhoneInputFilter(), InputFilter.LengthFilter(13))

            InputType.TYPE_CLASS_NUMBER,
            InputType.TYPE_NUMBER_FLAG_DECIMAL,
            InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL ->
                filters = arrayOf(InputFilter.LengthFilter(6))
        }
    }

    private fun findTextInputLayoutAncestor(): TextInputLayout? {
        var parent = parent
        while (parent != null) {
            if (parent is TextInputLayout) {
                return parent
            }
            parent = parent.parent
        }
        return null
    }

    /**
     * 사용자가 어떤 입력을 하든 010 0000 0000 포맷을 자동으로 유지해주는 필터.
     * */
    class PhoneInputFilter : InputFilter {

        override fun filter(source: CharSequence?, start: Int, end: Int,
                            dest: Spanned?, dstart: Int, dend: Int): CharSequence {

            if (source.isNullOrBlank()) {
                // removed
                return ""
            } else {
                // added
                val dest2 = dest.toString().replace(" ", "")
                val source2 =
                    if (source.take(3) == "+82") // +82-10-0000-0000 을 위한 처리
                        "0" + source.drop(3).digits(10)
                    else source.digits(11)

                val sb = StringBuilder()
                    .append(dest2)
                    .append(source2)

                if (sb.length > 7) {
                    sb.insert(7, " ")
                    sb.insert(3, " ")
                } else if (sb.length == 7) {
                    sb.append(" ")
                    sb.insert(3, " ")
                } else if (sb.length > 3) {
                    sb.insert(3, " ")
                } else if (sb.length == 3) {
                    sb.append(" ")
                }

                return sb.drop(dstart)
            }
        }

        // 시퀀스에서 decimal number(0~9) 만 가져옴.
        private fun CharSequence.digits(maxLength: Int = -1): CharSequence {
            val sb = StringBuilder()
            for (c in this) {
                if (c.isDigit()) {
                    sb.append(c)
                    if (sb.length == maxLength) {
                        return sb.toString()
                    }
                }
            }
            return sb.toString()
        }
    }
}