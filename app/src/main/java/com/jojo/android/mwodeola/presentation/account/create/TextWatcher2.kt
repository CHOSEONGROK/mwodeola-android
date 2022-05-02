package com.jojo.android.mwodeola.presentation.account.create

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText

abstract class TextWatcher2(
    private val editText: EditText
) : TextWatcher {
    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        onTextChanged(editText, s.toString())
    }
    override fun afterTextChanged(s: Editable?) {}

    abstract fun onTextChanged(editText: EditText, text: CharSequence)
}