package com.jojo.android.mwodeola.autofill.auth

import android.app.PendingIntent
import android.app.assist.AssistStructure
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.os.Build
import android.os.Bundle
import android.service.autofill.Dataset
import android.util.DisplayMetrics
import android.util.Log
import android.view.ViewTreeObserver
import android.view.autofill.AutofillManager
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.jojo.android.mwodeola.R
import com.jojo.android.mwodeola.autofill.AutofillHelper
import com.jojo.android.mwodeola.autofill.StructureParser
import com.jojo.android.mwodeola.autofill.model.AutofillStructure
import com.jojo.android.mwodeola.autofill.model.IAutofillAccount
import com.jojo.android.mwodeola.autofill.model.IAutofillData
import com.jojo.android.mwodeola.autofill.repository.AutofillAccountRepository
import com.jojo.android.mwodeola.databinding.ActivityAutofillAuthBinding

import com.jojo.android.mwodeola.model.account.AccountRepository
import com.jojo.android.mwodeola.model.token.TokenSharedPref
import com.jojo.android.mwodeola.presentation.common.BottomUpDialog
import com.jojo.android.mwodeola.presentation.security.Authenticators
import com.jojo.android.mwodeola.presentation.security.SecurityManager

import java.util.concurrent.Executor

@RequiresApi(Build.VERSION_CODES.O)
class AutofillAuthActivity : AppCompatActivity() {

    private val binding by lazy { ActivityAutofillAuthBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.root.setOnClickListener { onAuthenticationFailure() }

        Authenticators.BottomSheetBuilder(this@AutofillAuthActivity)
            .callback(AuthenticationCallback())
            .autofillService()
            .execute()
    }

    override fun onBackPressed() {
        finish(false)
    }

    private fun onAuthenticationSucceedOld() {
        // Get Autofill AssistStructure.
        val structure = intent.getParcelableExtra<AssistStructure>(AutofillManager.EXTRA_ASSIST_STRUCTURE)!!
        val packageName = structure.activityComponent.packageName

        // Parse Autofill AssistStructure and Get data from repository.
        val metadataList = StructureParser.parseForFill(structure)

        val data = emptyList<IAutofillData>()

        // Add metadata and data to parsed structure.
        val parsedStructure = AutofillStructure()
            .addAllMetadata(metadataList)
            .addAllData(data)

        when (parsedStructure.autofillFieldDataType) {
            AutofillStructure.AutofillDataType.ACCOUNT -> {
                val datasetName = intent.getStringExtra(EXTRA_DATASET_NAME)!!
                val datasetBuilder: Dataset.Builder

                val dataset = parsedStructure.datasetList.find { it.name == datasetName }
                datasetBuilder = AutofillHelper.createDatasetBuilder(
                    this@AutofillAuthActivity, parsedStructure.autofillFieldDataType, dataset!!, parsedStructure.metadataList)

                val replyIntent = Intent().apply {
                    putExtra(AutofillManager.EXTRA_AUTHENTICATION_RESULT, datasetBuilder.build())
                }
                finish(true, replyIntent)
            }
            AutofillStructure.AutofillDataType.CREDIT_CARD -> {

            }
            AutofillStructure.AutofillDataType.NONE -> finish(false)
        }
    }

    private fun onAuthenticationSucceed() {
        // PendingIntent 의 FLAG 에 따라 데이터가 null 일 수 있음
        // PendingIntent.FLAG_MUTABLE, PendingIntent.FLAG_IMMUTABLE
        val dataset = intent.getParcelableExtra<Dataset>(EXTRA_DATASET)
        val datasetName = intent.getStringExtra(EXTRA_DATASET_NAME)
        val structure = intent.getParcelableExtra<AssistStructure>(AutofillManager.EXTRA_ASSIST_STRUCTURE)
        val packageName = structure?.activityComponent?.packageName

//        Log.i(TAG, "onAuthenticationSucceed(): dataset=$dataset")
//        Log.i(TAG, "onAuthenticationSucceed(): datasetName=$datasetName")
//        Log.i(TAG, "onAuthenticationSucceed(): structure=$structure")
//        Log.i(TAG, "onAuthenticationSucceed(): packageName=$packageName")

        val replyIntent = Intent()
        replyIntent.putExtra(AutofillManager.EXTRA_AUTHENTICATION_RESULT, dataset)

        finish(true, replyIntent)
    }

    private fun onAuthenticationFailure() {
        finish(false)
    }

    private fun finish(isAuthSucceed: Boolean, replyIntent: Intent? = null) {
        if (isAuthSucceed) {
            setResult(RESULT_OK, replyIntent)
        } else {
            setResult(RESULT_CANCELED)
        }
        super.finish()
    }

    inner class AuthenticationCallback : Authenticators.AuthenticationCallback() {
        override fun onSucceed() {
            onAuthenticationSucceed()
        }

        override fun onFailure() {
            onAuthenticationFailure()
        }

        override fun onExceedAuthLimit(limit: Int) {
            Toast.makeText(
                this@AutofillAuthActivity,
                "인증 횟수 초과로 뭐더라 계정이 잠금 처리되었습니다.",
                Toast.LENGTH_SHORT
            ).show()

            SecurityManager.SharedPref(this@AutofillAuthActivity)
                .clearAll()
            TokenSharedPref.removeToken(this@AutofillAuthActivity)
            onAuthenticationFailure()
        }
    }

    private fun getWindowHeight(ratio: Float): Int {
        val displayMetrics = DisplayMetrics()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            window.windowManager?.defaultDisplay?.getRealMetrics(displayMetrics)
        } else {
            display?.getRealMetrics(displayMetrics)
        }
        return (displayMetrics.heightPixels * ratio).toInt()
    }

    private fun getWindowWidth(ratio: Float): Int {
        val displayMetrics = DisplayMetrics()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            window.windowManager?.defaultDisplay?.getRealMetrics(displayMetrics)
        } else {
            display?.getRealMetrics(displayMetrics)
        }
        return (displayMetrics.widthPixels * ratio).toInt()
    }


    companion object {
        private const val TAG = "AfAuthAuthActivity"
        private const val EXTRA_DATASET_NAME = "extra_dataset_name"
        private const val EXTRA_DATASET = "extra_dataset"

        // Unique autofillId for dataset intents.
        private var datasetPendingIntentId = 0

        internal fun getAuthIntentSenderForDataset(
            context: Context, datasetName: String, dataset: Dataset,
        ): IntentSender {
            Log.w(TAG, "getAuthIntentSenderForDataset(): $dataset")
            val intent = Intent(context, AutofillAuthActivity::class.java).apply {
                putExtra(EXTRA_DATASET_NAME, datasetName)
                putExtra(EXTRA_DATASET, dataset)
            }
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.getActivity(context, ++datasetPendingIntentId, intent,
                    PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_CANCEL_CURRENT).intentSender
            } else {
                PendingIntent.getActivity(context, ++datasetPendingIntentId, intent,
                    PendingIntent.FLAG_CANCEL_CURRENT).intentSender
            }
        }

        internal fun getAuthIntentSenderForDataset(context: Context, datasetName: String): IntentSender {
            val intent = Intent(context, AutofillAuthActivity::class.java)
            intent.putExtra(EXTRA_DATASET_NAME, datasetName)
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.getActivity(context, ++datasetPendingIntentId, intent,
                    PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_CANCEL_CURRENT).intentSender
            } else {
                PendingIntent.getActivity(context, ++datasetPendingIntentId, intent,
                    PendingIntent.FLAG_CANCEL_CURRENT).intentSender
            }
        }

        internal fun canBiometricAuthenticationFromDevice(context: Context): Boolean =
            BiometricManager.from(context).canAuthenticate(BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS
    }
}