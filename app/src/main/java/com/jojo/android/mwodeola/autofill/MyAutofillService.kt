package com.jojo.android.mwodeola.autofill

import android.os.Build
import android.os.CancellationSignal
import android.service.autofill.*
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.jojo.android.mwodeola.autofill.auth.AutofillAuthActivity
import com.jojo.android.mwodeola.autofill.model.AutofillStructure
import com.jojo.android.mwodeola.autofill.model.AutofillStructure.AutofillDataType
import com.jojo.android.mwodeola.autofill.utils.AutofillCommonUtil
import com.jojo.android.mwodeola.autofill.utils.PackageVerifier

@RequiresApi(Build.VERSION_CODES.O)
class MyAutofillService : AutofillService() {

    companion object {
        const val TAG = "MyAutofillService"
    }

    private var accountRemoteService: AutofillRemoteService? = null

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate()")
        if (accountRemoteService == null)
            accountRemoteService = AutofillRemoteService(applicationContext)

    }

    override fun onFillRequest(
        request: FillRequest,
        cancellationSignal: CancellationSignal,
        callback: FillCallback
    ) {
        Log.d(TAG, "★★ onFillRequest(): Start ★★")

        // Get the structure from the request
        val assistStructure = request.fillContexts[request.fillContexts.size - 1].structure
        val packageName = assistStructure.activityComponent.packageName

        // Check Valid Package
        if (!PackageVerifier.isValidPackage(applicationContext, packageName)) {
            Log.e(TAG, "onFillRequest(): Invalid package signature, packageNameField=$packageName")
            return
        }

        cancellationSignal.setOnCancelListener { Log.w(TAG, "Cancel autofill not implemented in this sample.") }

        // Parse AutoFill data in Activity
        val metadataList = StructureParser.parseForFill(assistStructure)
        Log.d(TAG, "onFillRequest(), $metadataList")

        val parsedStructure = AutofillStructure()
            .addAllMetadata(metadataList)

        // Get data from web server.
        when (parsedStructure.autofillFieldDataType) {
            AutofillDataType.ACCOUNT ->
                accountRemoteService?.requestLoadAccount(packageName, parsedStructure, callback)
            AutofillDataType.CREDIT_CARD ->
                accountRemoteService?.requestLoadCreditCard(parsedStructure, callback)
            AutofillDataType.NONE ->
                callback.onSuccess(null)
        }

        Log.d(TAG, "★★ onFillRequest(): End ★★")
    }

    override fun onSaveRequest(request: SaveRequest, callback: SaveCallback) {
        Log.d(TAG, "★★ onSaveRequest(): Start ★★")
        // Get the structure from the request
        val structure = request.fillContexts[request.fillContexts.size - 1].structure
        val packageName = structure.activityComponent.packageName

        // Check Valid Package
        if (!PackageVerifier.isValidPackage(applicationContext, packageName)) {
            Toast.makeText(applicationContext, "Invalid package signature",
                Toast.LENGTH_SHORT).show()
            Log.e(TAG, "onSaveRequest(): Invalid package signature, packageNameField=$packageName")
            return
        }

        Log.d(TAG, "onSaveRequest(): data=" + AutofillCommonUtil.bundleToString(request.clientState))

        // Parse AutoFill data in Activity for save
        val metadataList = StructureParser.parseForSave(structure)

        val parsedStructure = AutofillStructure()
            .addAllMetadata(metadataList)

        // Save Autofill data to Repository
        when (parsedStructure.autofillFieldDataType) {
            AutofillDataType.ACCOUNT ->
                accountRemoteService?.requestSaveAccount(packageName, parsedStructure, callback)
            AutofillDataType.CREDIT_CARD ->
                accountRemoteService?.requestSaveCreditCard(parsedStructure, callback)
            AutofillDataType.NONE -> {}
        }

        Log.d(TAG, "★★ onSaveRequest(): End ★★")
    }

    override fun onConnected() {
        super.onConnected()
        Log.d(TAG, "onConnected()")
    }

    override fun onDisconnected() {
        super.onDisconnected()
        Log.d(TAG, "onDisconnected()")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy()")
        accountRemoteService = null
    }
}
