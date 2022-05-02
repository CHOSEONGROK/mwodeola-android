package com.jojo.android.mwodeola.autofill

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.service.autofill.Dataset
import android.service.autofill.FillResponse
import android.service.autofill.SaveInfo
import android.util.Log
import android.view.View
import android.view.autofill.AutofillManager
import android.widget.RemoteViews
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toBitmap
import com.jojo.android.mwodeola.R
import com.jojo.android.mwodeola.autofill.auth.AutofillAuthActivity
import com.jojo.android.mwodeola.autofill.model.AutofillDataset
import com.jojo.android.mwodeola.autofill.model.AutofillFieldMetadata
import com.jojo.android.mwodeola.autofill.model.AutofillStructure
import com.jojo.android.mwodeola.autofill.model.AutofillStructure.AutofillDataType
import com.jojo.android.mwodeola.autofill.model.IAutofillAccount

/**
 * This is a class containing helper methods for building Autofill Datasets and Responses.
 */
@RequiresApi(Build.VERSION_CODES.O)
object AutofillHelper {
    private const val TAG = "AutofillHelper"

    fun createFillResponse(context: Context, structure: AutofillStructure): FillResponse? {
        if (structure.metadataList.isEmpty()) return null

        val responseBuilder = FillResponse.Builder()

        Log.i(TAG, "createFillResponse(): datasetList.size=${structure.datasetList.size}")
        if (structure.datasetList.size > 0) {
            if (Build.VERSION.SDK_INT >= 28) {
                responseBuilder.setHeader(RemoteViews(context.packageName, R.layout.autofill_header))
            }

            structure.datasetList.forEach {
//                val intentSender = AutofillAuthActivity.getAuthIntentSenderForDataset(context, it.name)
//
//                responseBuilder.addDataset(
//                    createDatasetBuilder(context, structure.autofillFieldDataType, it, structure.metadataList)
//                        .setAuthentication(intentSender)
//                        .build()
//                )

                val datasetForSender =
                    createDatasetBuilder(context, structure.autofillFieldDataType, it, structure.metadataList)
                        .build()

                val intentSender = AutofillAuthActivity
                    .getAuthIntentSenderForDataset(context, it.name, datasetForSender)

                val dataset =
                    createDatasetBuilder(context, structure.autofillFieldDataType, it, structure.metadataList)
                        .setAuthentication(intentSender)
                        .build()

                responseBuilder.addDataset(dataset)
            }
        }

        if (structure.saveType != 0) {
            responseBuilder.setSaveInfo(
                SaveInfo.Builder(structure.saveType, structure.autofillIds)
                    .build()
            )

            Log.d(TAG, "newResponse(): return responseBuilder.build()")
            return responseBuilder.build()
        } else {
            Log.d(TAG, "newResponse(): return null")
            return null
        }
    }

    fun createDatasetBuilder(context: Context, autofillDataType: AutofillDataType,
                             dataset: AutofillDataset,
                             metadataList: ArrayList<AutofillFieldMetadata>): Dataset.Builder {
        val datasetBuilder = Dataset.Builder()

        for (metadata in metadataList) {
            val autofillData = dataset[metadata.autofillHintFirst]

            val remoteViews = when (autofillDataType) {
                AutofillDataType.ACCOUNT ->
                    createAccountRemoteViews(context, dataset.packageName, dataset.name)
                AutofillDataType.CREDIT_CARD ->
                    createCreditCardRemoteViews(context, dataset.name)
                else -> continue
            }

            datasetBuilder.setValue(
                metadata.autofillId,
                autofillData?.autofillValue,
                remoteViews
            )
        }

        return datasetBuilder
    }

    fun createAccountRemoteViews(context: Context, requestPackageName: String?, text: String): RemoteViews =
        RemoteViews(context.packageName, R.layout.autofill_service_list_account).apply {
            if (requestPackageName == null) return@apply // return Empty RemoteView

            val drawable = context.packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
                .find { it.packageName == requestPackageName }
                ?.loadIcon(context.packageManager)

            setImageViewBitmap(R.id.icon, drawable?.toBitmap())
            setTextViewText(R.id.text, text)
        }

    fun createCreditCardRemoteViews(context: Context, cardNumber: String): RemoteViews =
        RemoteViews(context.packageName, R.layout.autofill_service_list_credit_card).apply {
            setImageViewResource(R.id.icon, R.drawable.credit_card_icon)
            setTextViewText(R.id.text, cardNumber.substring(0, 3))
        }


    fun isValidHint(hint: String): Boolean {
        when (hint) {
            View.AUTOFILL_HINT_CREDIT_CARD_EXPIRATION_DATE,
            View.AUTOFILL_HINT_CREDIT_CARD_EXPIRATION_DAY,
            View.AUTOFILL_HINT_CREDIT_CARD_EXPIRATION_MONTH,
            View.AUTOFILL_HINT_CREDIT_CARD_EXPIRATION_YEAR,
            View.AUTOFILL_HINT_CREDIT_CARD_NUMBER,
            View.AUTOFILL_HINT_CREDIT_CARD_SECURITY_CODE,
            View.AUTOFILL_HINT_EMAIL_ADDRESS,
            View.AUTOFILL_HINT_PHONE,
            View.AUTOFILL_HINT_NAME,
            View.AUTOFILL_HINT_PASSWORD,
            View.AUTOFILL_HINT_POSTAL_ADDRESS,
            View.AUTOFILL_HINT_POSTAL_CODE,
            View.AUTOFILL_HINT_USERNAME ->
                return true
            else ->
                return false
        }
    }

    fun isAutofillSupported(context: Context): Boolean =
        context.getSystemService(AutofillManager::class.java).isAutofillSupported

    fun hasEnabledAutofillServices(context: Context): Boolean =
        context.getSystemService(AutofillManager::class.java).hasEnabledAutofillServices()

    fun requestAutofillServiceSettings(activity: Activity) {
        activity.startActivity(Intent(
            Settings.ACTION_REQUEST_SET_AUTOFILL_SERVICE,
            Uri.parse("package:" + activity.packageName)
        ))
    }

}
