package com.jojo.android.mwodeola.autofill.model

interface IAutofillAccount : IAutofillData {

    override val datasetName: String
        get() = userIdField ?: appNameField
    override val canAutofillService: Boolean
        get() = packageNameField?.isNotBlank() == true &&
                userPasswordField?.isNotBlank() == true

    val appNameField: String
    val packageNameField: String?

    val userIdField: String?
    val userPasswordField: String?
}