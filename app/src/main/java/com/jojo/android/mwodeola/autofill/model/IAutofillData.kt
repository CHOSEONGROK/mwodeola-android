package com.jojo.android.mwodeola.autofill.model

import android.os.Parcelable

interface IAutofillData {

    val datasetName: String

    val canAutofillService: Boolean
}