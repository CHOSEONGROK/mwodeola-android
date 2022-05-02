package com.jojo.android.mwodeola.autofill.utils

import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import java.util.*

@RequiresApi(Build.VERSION_CODES.O)
object AutofillCommonUtil {

    private const val TAG = "AutofillCommonUtil"

    const val EXTRA_DATASET_NAME = "dataset_name"
    const val EXTRA_FOR_RESPONSE = "for_response"

    private fun bundleToString(builder: StringBuilder, data: Bundle) {
        val keySet = data.keySet()
        builder.append("[Bundle with ").append(keySet.size).append(" keys:")
        for (key in keySet) {
            builder.append(' ').append(key).append('=')
            val value = data.get(key)
            if (value is Bundle) {
                bundleToString(builder, value)
            } else {
                val string = if (value is Array<*>) Arrays.toString(value) else value
                builder.append(string)
            }
        }
        builder.append(']')
    }

    fun bundleToString(data: Bundle?): String {
        if (data == null) {
            return "N/A"
        }
        val builder = StringBuilder()
        bundleToString(builder, data)
        return builder.toString()
    }
}