package com.jojo.android.mwodeola.autofill.utils

import android.app.assist.AssistStructure
import android.content.Context
import android.os.Build
import android.util.DisplayMetrics
import androidx.annotation.RequiresApi
import java.lang.StringBuilder

const val TAG = "ExtensionFunctions.kt"

fun Int.dpToPixels(context: Context): Int =
    (this * (context.resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)).toInt()

fun Int.pixelsToDp(context: Context): Int =
    (this / (context.resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)).toInt()

fun Float.dpToPixels(context: Context): Int =
    (this * (context.resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)).toInt()

fun Float.pixelsToDp(context: Context): Int =
    (this / (context.resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)).toInt()

@RequiresApi(Build.VERSION_CODES.O)
fun AssistStructure.ViewNode.toStringForLog(): String {
    val logBuilder = StringBuilder()
    val scheme =
        if (Build.VERSION.SDK_INT >= 28) webScheme
        else "null"

    logBuilder.append(
        "ViewNode: {autofillId=$autofillId, autofillFieldDataType=$autofillType, autofillValue=$autofillValue, " +
                "text=$text, autofillHint=$hint, isFocused=$isFocused, inputType=$inputType, webDomain=$webDomain}. webScheme=$scheme"
    )
    if (autofillHints.isNullOrEmpty()) {
        logBuilder.append(", autofillHints=null")
    } else {
        logBuilder.append(", autofillHints(${autofillHints!!.size})=(")
        autofillHints!!.forEach { logBuilder.append("$it ") }
        logBuilder.append(")")
    }

    if (autofillOptions.isNullOrEmpty()) {
        logBuilder.append(", autofillOptions=null")
    } else {
        logBuilder.append(", autofillOptions(${autofillOptions!!.size}=(")
        autofillOptions!!.forEach { logBuilder.append("$it ") }
        logBuilder.append(")")
    }

    return logBuilder.toString()
}

fun Array<*>?.isNotNullAndNotEmpty(): Boolean =
    this != null && this.isNotEmpty()

fun String?.isNotNullAndNotBlank(): Boolean =
    this != null && this.isNotBlank()