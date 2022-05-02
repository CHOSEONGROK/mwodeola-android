package com.jojo.android.mwodeola.autofill.model

import android.content.Context
import android.os.Build
import android.util.Log
import android.view.View
import android.view.autofill.AutofillId
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.O)
class AutofillStructure {
    companion object { const val TAG = "AutofillStructure" }

    enum class AutofillDataType { NONE, ACCOUNT, CREDIT_CARD }

    data class ParsedAccount(val packageName: String, val appName: String, val userID: String?, val userPassword: String)
    data class ParsedCreditCard(val cardNumber: String, val expirationYear: String, val expirationMonth: String, val cvc: String?)

    val metadataList = arrayListOf<AutofillFieldMetadata>()
    val datasetList = arrayListOf<AutofillDataset>()

    val autofillIds: Array<AutofillId>
        get() = metadataList.map { it.autofillId }.toTypedArray()

    var saveType: Int = 0
        private set

    var autofillFieldDataType: AutofillDataType = AutofillDataType.NONE
        private set

    fun addAllMetadata(metadataList: ArrayList<AutofillFieldMetadata>) = apply {
        this.metadataList.clear()

        metadataList.forEach {
            this.metadataList.add(it)

            saveType = saveType or it.saveType
            Log.i(TAG, "addAllMetadata(): saveType=$saveType")

        }

        autofillFieldDataType = setAutofillFieldDataType(metadataList)
        Log.i(TAG, "addAllMetadata(): autofillFieldDataType=$autofillFieldDataType")
    }

    fun addAllData(data: List<IAutofillData>?) = apply {
        if (metadataList.isEmpty())
            return this

        data?.filter { it.canAutofillService } // 패키지명이 없는 데이터는 Autofill Service 미지원.
            ?.forEach {
                val dataset = createAutofillDataset(it)
                if (dataset != null) {
                    datasetList.add(dataset)
                }
            }
    }

    fun parseForSaveUserAccount(context: Context, packageName: String): ParsedAccount? {
        val appName = context.packageManager
            .getApplicationInfo(packageName, 0)
            .loadLabel(context.packageManager)
            .toString()

        var userID: String? = null
        var userPassword = ""

        metadataList.forEach {
            if (it.autofillValue == null || !it.autofillValue.isText) {
                return null
            }

            if (it.autofillHintFirst == View.AUTOFILL_HINT_USERNAME) {
                userID = it.autofillValue.textValue.toString()
            } else if (it.autofillHintFirst == View.AUTOFILL_HINT_PASSWORD) {
                userPassword = it.autofillValue.textValue.toString()
            }
        }

        if (userID?.isBlank() == true) {
            userID = null
        }

        return if (userPassword.isNotBlank()) {
            ParsedAccount(packageName, appName, userID, userPassword)
        } else {
            null
        }
    }

    fun parseForSaveCreditCard(): ParsedCreditCard? {
        var cardNumber = ""
        var expirationYear = ""
        var expirationMonth = ""
        var cvc: String? = null

        metadataList.forEach {
            if (it.autofillValue == null || !it.autofillValue.isText) {
                return null
            }

            when (it.autofillHintFirst) {
                View.AUTOFILL_HINT_CREDIT_CARD_NUMBER ->
                    cardNumber = it.autofillValue.textValue.toString()
                View.AUTOFILL_HINT_CREDIT_CARD_EXPIRATION_YEAR ->
                    expirationYear = it.autofillValue.textValue.toString()
                View.AUTOFILL_HINT_CREDIT_CARD_EXPIRATION_MONTH ->
                    expirationMonth = it.autofillValue.textValue.toString()
                View.AUTOFILL_HINT_CREDIT_CARD_SECURITY_CODE ->
                    cvc = it.autofillValue.textValue.toString()
            }
        }

        return if (cardNumber.isNotBlank() && expirationYear.isNotBlank() && expirationMonth.isNotBlank()) {
            ParsedCreditCard(cardNumber, expirationYear, expirationMonth, cvc)
        } else {
            null
        }
    }

    private fun createAutofillDataset(data: IAutofillData): AutofillDataset? {
        val autofillDataList = arrayListOf<AutofillData>()

        when (data) {
            is IAutofillAccount -> {
                autofillDataList.add(
                    AutofillData(View.AUTOFILL_HINT_USERNAME, data.userIdField))
                autofillDataList.add(
                    AutofillData(View.AUTOFILL_HINT_PASSWORD, data.userPasswordField))

                return AutofillDataset(data.datasetName, autofillDataList).apply {
                    packageName = data.packageNameField
                }
            }
            is IAutofillCreditCard -> {
                autofillDataList.add(
                    AutofillData(View.AUTOFILL_HINT_CREDIT_CARD_NUMBER, data.cardNumberField))
                autofillDataList.add(
                    AutofillData(View.AUTOFILL_HINT_CREDIT_CARD_EXPIRATION_YEAR, data.expirationYearField))
                autofillDataList.add(
                    AutofillData(View.AUTOFILL_HINT_CREDIT_CARD_EXPIRATION_MONTH, data.expirationMonthField))

                val cvcFieldExist = metadataList.any { it.autofillHintFirst == View.AUTOFILL_HINT_CREDIT_CARD_SECURITY_CODE }
                if (data.cvcField != null && cvcFieldExist ) {
                    autofillDataList.add(
                        AutofillData(View.AUTOFILL_HINT_CREDIT_CARD_SECURITY_CODE, data.cvcField!!))
                }

                return AutofillDataset(data.datasetName, autofillDataList)
            }
            else -> return null
        }
    }


    private fun setAutofillFieldDataType(metadataList: ArrayList<AutofillFieldMetadata>): AutofillDataType {
        var result = AutofillDataType.NONE

        for (i in metadataList.indices) {
            val metadata = metadataList[i]

            if (i == 0) {
                result = getAutofillDataType(metadata)
            } else {
                if (result == getAutofillDataType(metadata)) continue
                else result = AutofillDataType.NONE
            }
        }
        return result
    }

    private fun getAutofillDataType(metadata: AutofillFieldMetadata): AutofillDataType =
        when (metadata.autofillHintFirst) {
            View.AUTOFILL_HINT_USERNAME,
            View.AUTOFILL_HINT_PASSWORD ->
                AutofillDataType.ACCOUNT
            View.AUTOFILL_HINT_CREDIT_CARD_EXPIRATION_DATE,
            View.AUTOFILL_HINT_CREDIT_CARD_EXPIRATION_DAY,
            View.AUTOFILL_HINT_CREDIT_CARD_EXPIRATION_MONTH,
            View.AUTOFILL_HINT_CREDIT_CARD_EXPIRATION_YEAR,
            View.AUTOFILL_HINT_CREDIT_CARD_NUMBER,
            View.AUTOFILL_HINT_CREDIT_CARD_SECURITY_CODE ->
                AutofillDataType.CREDIT_CARD
            else -> AutofillDataType.NONE
        }

}