package com.jojo.android.mwodeola.autofill.model

import com.jojo.android.mwodeola.autofill.utils.isNotNullAndNotBlank

interface IAutofillCreditCard : IAutofillData {

    override val datasetName: String
        get() =
            if (manufacturerField.isNotNullAndNotBlank()) "$manufacturerField (${cardNumberField.substring(0..3)})"
            else cardNumberField.substring(0..3)

    override val canAutofillService: Boolean
        get() = true

    val manufacturerField: String?
    val cardNameField: String?
    val cardNumberField: String
    val expirationYearField: String
    val expirationMonthField: String
    val cvcField: String?
}