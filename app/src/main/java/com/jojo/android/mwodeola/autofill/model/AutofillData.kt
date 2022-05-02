package com.jojo.android.mwodeola.autofill.model

import android.os.Build
import android.view.autofill.AutofillValue
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.O)
data class AutofillData(
    val autofillHint: String,
    val value: String?
) {

    val autofillValue: AutofillValue?
        get() = AutofillValue.forText(value)

}