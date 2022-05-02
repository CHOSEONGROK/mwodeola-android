package com.jojo.android.mwodeola.presentation.users

import android.content.Context
import android.text.InputFilter
import android.text.InputType
import android.text.Spanned
import android.util.AttributeSet
import android.util.Log
import android.view.KeyEvent
import com.google.android.material.textfield.TextInputEditText

class FilterableInputEditText : TextInputEditText {
    companion object { private const val TAG = "FilterableInputEditText" }

    interface OnInputCallback {
        fun onTextChanged(isCompleted: Boolean, text: String)
    }

    private var callback: OnInputCallback? = null

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        setFiltersByInputType(inputType)
    }

    override fun setInputType(type: Int) {
        super.setInputType(type)
        setFiltersByInputType(type)
    }

    fun setOnInputCallback(callback: OnInputCallback) {
        this.callback = callback
    }

    fun removeOnInputCallback() {
        this.callback = null
    }

    override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
        if (event?.action != KeyEvent.ACTION_DOWN)
            return super.dispatchKeyEvent(event)

        if (event.keyCode == KeyEvent.KEYCODE_DEL && inputType == InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS) {
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
    }

    private fun isCompleteFormat(text: CharSequence?): Boolean =
        if (text.isNullOrBlank()) false
        else when (inputType) {
            InputType.TYPE_TEXT_VARIATION_PERSON_NAME ->
                text.isNotBlank()
            InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS ->
                android.util.Patterns.EMAIL_ADDRESS.matcher(text).matches()
            InputType.TYPE_CLASS_PHONE ->
                text.length == 13
            InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL ->
                text.length == 6
            else -> text.isNotBlank()
        }

    private fun setFiltersByInputType(inputType: Int) {
        // 필터 순서 중요!!
        Log.i(TAG, "setFiltersByInputType(): inputType=$inputType")
        when (inputType) {
            InputType.TYPE_TEXT_VARIATION_PERSON_NAME -> {}
            InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS ->
                filters = arrayOf(InputFilter.LengthFilter(100))
            InputType.TYPE_CLASS_PHONE ->
                filters = arrayOf(PhoneInputFilter(), InputFilter.LengthFilter(13))
            InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL ->
                filters = arrayOf(InputFilter.LengthFilter(6))
        }
    }

    /**
     * 사용자가 어떤 입력을 하든 010 0000 0000 포맷을 자동으로 유지해주는 필터.
     * */
    class PhoneInputFilter : InputFilter {

        override fun filter(source: CharSequence?, start: Int, end: Int,
                            dest: Spanned?, dstart: Int, dend: Int): CharSequence {
//            Log.i(TAG, "filter(): source=$source, start=$start, end=$end, dest=$dest, dstart=$dstart, dend=$dend")

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