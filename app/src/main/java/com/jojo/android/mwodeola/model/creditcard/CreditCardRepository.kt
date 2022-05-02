package com.jojo.android.mwodeola.model.creditcard

import android.content.Context
import com.jojo.android.mwodeola.autofill.model.IAutofillCreditCard
import com.jojo.android.mwodeola.autofill.repository.AutofillCreditCardRepository

class CreditCardRepository(context: Context) : AutofillCreditCardRepository {

    override fun saveCreditCard(
        cardNumber: String,
        expirationYear: String,
        expirationMonth: String,
        cvc: String?
    ) {

    }

    override fun getAllCreditCardData(): List<IAutofillCreditCard> {
        return emptyList()
    }

    override fun updateCreditCard() {

    }
}