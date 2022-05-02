package com.jojo.android.mwodeola.autofill.repository

import com.jojo.android.mwodeola.autofill.model.IAutofillCreditCard

interface AutofillCreditCardRepository {

    fun saveCreditCard(cardNumber: String, expirationYear: String,
                       expirationMonth: String, cvc: String?)

    fun getAllCreditCardData(): List<IAutofillCreditCard>

    fun updateCreditCard()
}