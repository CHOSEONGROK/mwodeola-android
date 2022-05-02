package com.jojo.android.mwodeola.autofill

import android.content.Context
import android.os.Build
import android.service.autofill.FillCallback
import android.service.autofill.SaveCallback
import android.util.Log
import androidx.annotation.RequiresApi
import com.jojo.android.mwodeola.autofill.model.AutofillStructure
import com.jojo.android.mwodeola.autofill.model.IAutofillAccount
import com.jojo.android.mwodeola.autofill.repository.AutofillAccountRepository
import com.jojo.android.mwodeola.autofill.repository.AutofillCreditCardRepository
import com.jojo.android.mwodeola.model.account.AccountRepository
import com.jojo.android.mwodeola.model.creditcard.CreditCardRepository

@RequiresApi(Build.VERSION_CODES.O)
class AutofillRemoteService(
    private val context: Context
) {
    private val accountRepository: AutofillAccountRepository = AccountRepository(context)
    private val creditCardRepository: AutofillCreditCardRepository = CreditCardRepository(context)

    fun requestLoadAccount(packageName: String, structure: AutofillStructure, callback: FillCallback) {
        accountRepository.getAccountData(packageName, object :
            AutofillAccountRepository.LoadCallback {
            override fun onSucceed(accounts: List<IAutofillAccount>) {
                structure.addAllData(accounts)

                val response = AutofillHelper.createFillResponse(context, structure)

                callback.onSuccess(response)
            }

            override fun onFailure() {
                Log.w(TAG, "onFailure()")
                callback.onSuccess(null)
            }

            override fun onUnknownError(errString: String?) {
                Log.w(TAG, "onUnknownError(): $errString")
                callback.onSuccess(null)
                // callback.onFailure(errString)
            }
        })
    }

    fun requestSaveAccount(packageName: String, structure: AutofillStructure, callback: SaveCallback) {
        val (appPackageName, appName, userID, userPassword) = structure.parseForSaveUserAccount(context, packageName)
            ?: return

        accountRepository.saveAccount(
            appPackageName, appName, userID, userPassword,
            object : AutofillAccountRepository.SaveCallback {
                override fun onSucceed(code: String) {
                    Log.i(MyAutofillService.TAG, "onSaveRequest.onSucceed(): code=$code")
                    callback.onSuccess()
                }

                override fun onFailure() {
                    Log.w(MyAutofillService.TAG, "onSaveRequest.onFailure()")
                }

                override fun onUnknownError(errString: String?) {
                    Log.w(MyAutofillService.TAG, "onUnknownError.onFailure(): $errString")
                    // callback.onFailure()
                }
            }
        )
    }

    fun requestLoadCreditCard(structure: AutofillStructure, callback: FillCallback) {

        // creditCardRepository.getAllCreditCardData()
    }

    fun requestSaveCreditCard(structure: AutofillStructure, callback: SaveCallback) {
        val (cardNumber, expirationYear, expirationMonth, cvc) = structure.parseForSaveCreditCard()
            ?: return

    }

    companion object {
        private const val TAG = "AutofillRemoteService"
    }
}