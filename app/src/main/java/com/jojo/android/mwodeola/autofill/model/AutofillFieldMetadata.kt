package com.jojo.android.mwodeola.autofill.model

import android.app.assist.AssistStructure
import android.os.Build
import android.service.autofill.SaveInfo
import android.util.Log
import android.view.View
import android.view.autofill.AutofillId
import android.view.autofill.AutofillValue
import androidx.annotation.RequiresApi
import com.jojo.android.mwodeola.autofill.AutofillHelper
import com.jojo.android.mwodeola.autofill.StructureParser.containsAll
import com.jojo.android.mwodeola.autofill.utils.isNotNullAndNotEmpty
import com.jojo.android.mwodeola.autofill.utils.toStringForLog

import java.lang.StringBuilder

@RequiresApi(Build.VERSION_CODES.O)
class AutofillFieldMetadata private constructor(
    viewNode: AssistStructure.ViewNode,
    inferredAutofillHints: Array<String>
) {
    val autofillHints: Array<String> = inferredAutofillHints
    val autofillHintFirst: String
        get() = autofillHints.first()

    val autofillId: AutofillId = viewNode.autofillId!!
    val autofillType: Int = viewNode.autofillType
    val autofillValue: AutofillValue? = viewNode.autofillValue
    val autofillOptions: Array<CharSequence>? = viewNode.autofillOptions
    val isFocused: Boolean = viewNode.isFocused

    val text: CharSequence? = viewNode.text
    val hint: String? = viewNode.hint

    val webDomain: String? = viewNode.webDomain
    val webScheme: String? =
        if (Build.VERSION.SDK_INT >= 28) viewNode.webScheme
        else null


    val saveType: Int

    init {
        saveType = setSaveTypeFromHint()
    }

    private fun setSaveTypeFromHint(): Int = when (autofillHintFirst) {
        View.AUTOFILL_HINT_CREDIT_CARD_EXPIRATION_DATE,
        View.AUTOFILL_HINT_CREDIT_CARD_EXPIRATION_DAY,
        View.AUTOFILL_HINT_CREDIT_CARD_EXPIRATION_MONTH,
        View.AUTOFILL_HINT_CREDIT_CARD_EXPIRATION_YEAR,
        View.AUTOFILL_HINT_CREDIT_CARD_NUMBER,
        View.AUTOFILL_HINT_CREDIT_CARD_SECURITY_CODE ->
            SaveInfo.SAVE_DATA_TYPE_CREDIT_CARD
        View.AUTOFILL_HINT_EMAIL_ADDRESS ->
            SaveInfo.SAVE_DATA_TYPE_EMAIL_ADDRESS
        View.AUTOFILL_HINT_PHONE, View.AUTOFILL_HINT_NAME ->
            SaveInfo.SAVE_DATA_TYPE_GENERIC
        View.AUTOFILL_HINT_PASSWORD ->
            SaveInfo.SAVE_DATA_TYPE_PASSWORD
        View.AUTOFILL_HINT_POSTAL_ADDRESS,
        View.AUTOFILL_HINT_POSTAL_CODE ->
            SaveInfo.SAVE_DATA_TYPE_ADDRESS
        View.AUTOFILL_HINT_USERNAME ->
            SaveInfo.SAVE_DATA_TYPE_USERNAME
        else -> setSaveTypeFromWhat()
    }

    private fun setSaveTypeFromWhat(): Int {
        return 0
    }

    override fun toString(): String =
        StringBuilder("\n★★★ $TAG: Start ★★★\n")
            .append("autofillId=$autofillId\n")
            .append("autofillFieldDataType=$autofillType\n")
            .append("autofillValue=$autofillValue\n")
            .append("autofillHints=\n")
            .append(autofillHints.forEachIndexedReturnString { i, hint -> "autofillHint[$i]=$hint\n" })
            .append("autofillOptions=")
            .append(autofillOptions.forEachIndexedReturnString { i, option -> "autofillOption[$i]=$option\n" })
            .append("isFocused=$isFocused\n")
            .append("text=$text\n")
            .append("autofillHint=$hint\n")
            .append("webDomain=$webDomain\n")
            .append("webScheme=$webScheme\n")
            .append("saveType=$saveType\n")
            .append("☆☆☆ $TAG: End ☆☆☆")
            .toString()

    companion object {
        private const val TAG = "AutofillFieldMetadata"

        fun getInstance(viewNode: AssistStructure.ViewNode): AutofillFieldMetadata? {
            Log.i(TAG, "getInstance():\n${viewNode.toStringForLog()}")

            if (viewNode.autofillType == View.AUTOFILL_TYPE_NONE &&
                viewNode.autofillHints == null) {
                return null
            }

            if (viewNode.autofillHints.isNotNullAndNotEmpty()) { // autofillHints == NotNull and NotEmpty
                // If the client app provides autofill hints, you can obtain them using:
                // viewNode.getAutofillHints()
                val autofillHints = viewNode.autofillHints!!.filter(AutofillHelper::isValidHint).toTypedArray()
                if (autofillHints.isNotNullAndNotEmpty()) {
                    return AutofillFieldMetadata(viewNode, autofillHints)
                }
            } else {
                // Or use your own heuristics to describe the contents of a view
                // using methods such as getText() or getAutofillHint().
                val autofillHints = guessAutofillHintsFromViewDescription(viewNode)
                if (autofillHints.isNotEmpty()) {
                    return AutofillFieldMetadata(viewNode, autofillHints)
                }
            }

            return null
        }

        /**
         * View 에 autofillHints == null 인데, autofillFieldDataType 만 존재하는 경우.
         * View 의 autofillHint 값을 통해 autofillHints 값을 유추함.
         * */
        private fun guessAutofillHintsFromViewDescription(viewNode: AssistStructure.ViewNode): Array<String> {
            viewNode.hint?.let {
                if (viewNode.autofillType != View.AUTOFILL_TYPE_NONE) {
                    if (it.containsAll("아이디", "id", "이메일", "email", "e-mail")) {
                        return arrayOf(View.AUTOFILL_HINT_USERNAME)
                    } else if (it.containsAll("비밀번호", "패스워드", "password")) {
                        return arrayOf(View.AUTOFILL_HINT_PASSWORD)
                    } else if (it.containsAll("신용 카드", "체크 카드", "Credit Card", "Check Card")) {
                        return arrayOf(View.AUTOFILL_HINT_CREDIT_CARD_NUMBER)
                    }
//                    else if (it.containsAll("년", "year")) {
//                        return arrayOf(View.AUTOFILL_HINT_CREDIT_CARD_EXPIRATION_YEAR)
//                    } else if (it.containsAll("월", "month")) {
//                        return arrayOf(View.AUTOFILL_HINT_CREDIT_CARD_EXPIRATION_MONTH)
//                    }
                }
            }
            return arrayOf()
        }

        fun <T> Array<out T>?.forEachIndexedReturnString(action: (index: Int, T) -> String): String {
            if (this == null) return "null\n"
            var result = ""
            var index = 0
            for (item in this) result += action(index++, item)
            return result
        }
    }
}