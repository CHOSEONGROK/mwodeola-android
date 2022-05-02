package com.jojo.android.mwodeola.autofill.model

class AutofillDataset(
    val name: String,
    val dataList: ArrayList<AutofillData>
) {
    var packageName: String? = null

    operator fun get(hint: String): AutofillData? =
        dataList.find { it.autofillHint == hint }
}