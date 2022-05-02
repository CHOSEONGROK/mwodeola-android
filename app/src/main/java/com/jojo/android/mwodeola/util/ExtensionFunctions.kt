package com.jojo.android.mwodeola.util

import android.content.Context
import android.graphics.Color
import android.graphics.Point
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.text.Editable
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.children
import androidx.recyclerview.widget.RecyclerView
import java.lang.IllegalStateException
import java.util.*
import kotlin.collections.ArrayList

fun Int.dpToPixels(context: Context): Int =
    (this * (context.resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)).toInt()

fun Int.pixelsToDp(context: Context): Int =
    (this / (context.resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)).toInt()

inline fun <T> ArrayList<T>.removeAllIf(predicate: (T) -> Boolean) {
    val indices = arrayListOf<Int>()

    this.forEachIndexed { index, t -> if (predicate(t)) indices.add(index) }

    for (i in indices.lastIndex downTo 0) {
        this.removeAt(indices[i])
    }
}

inline fun <T> Iterable<T>.doForEach(doingAtFirst: (T) -> Any, action: (T) -> Unit): Unit {
    this.forEachIndexed { index, element ->
        if (index == 0) {
            doingAtFirst(element)
        }
        action(element)
    }
}

fun <T> List<T>.toSubList(startIndex: Int, count: Int): List<T> =
    arrayListOf<T>()
        .also {
            val endIndex = (startIndex + count - 1).coerceAtMost(lastIndex) // 둘 중 최솟값.
            for (i in startIndex..endIndex) {
                it.add(this[i])
            }
        }
        .toList()

inline fun <T, R : Comparable<R>> Iterable<T>.maxOf(count: Int, crossinline selector: (T) -> R): List<T> =
    this.sortedByDescending(selector)
        .toSubList(0, count)

fun ViewGroup.applyGrayFilterToAllChildren(isApply: Boolean) {
//    val transparent = Color.parseColor("#00FFFFFF")
    val transParentFilter = PorterDuffColorFilter(Color.parseColor("#00FFFFFF"), PorterDuff.Mode.MULTIPLY)
    val grayColorFilter = PorterDuffColorFilter(Color.parseColor("#757575"), PorterDuff.Mode.MULTIPLY)

    children.forEach { child ->
//        child.background.colorFilter = if (isApply) grayColorFilter else transParentFilter
        when (child) {
            is ImageView ->
                child.colorFilter = if (isApply) grayColorFilter else transParentFilter
            is TextView -> {
                child.background.colorFilter = if (isApply) grayColorFilter else transParentFilter
                child.setTextColor(Color.GRAY)
            }
        }
    }
}

fun <VH : RecyclerView.ViewHolder?> RecyclerView.Adapter<VH>.notifyItemsInserted(positions: List<Int>) {
    positions.sortedByDescending { it }
        .forEach { notifyItemInserted(it) }
}

fun <VH : RecyclerView.ViewHolder?> RecyclerView.Adapter<VH>.notifyItemsRemoved(positions: List<Int>) {
    positions.sortedByDescending { it }
        .forEach { notifyItemRemoved(it) }
}

inline fun <T> List<T>.alsoForEach(action: (T) -> Unit): List<T> {
    for (element in this) action(element)
    return this
}

fun String?.indexOfStartEnd(other: CharSequence?): Array<Int>? {
    if (this.isNullOrBlank() || other.isNullOrBlank()) {
        return null
    }

    var startIndex = -1
    var endIndex = -1

    var i = 0

    while (i < this.length) {
        if (i + other.length > this.length) {
            break
        }

        if (this[i].equals(other.first(), true)) {
            startIndex = i

            for (j in other.indices) {
                if (!this[i++].equals(other[j], true)) {
                    startIndex = -1
                    break
                } else if (j == other.lastIndex) {
                    endIndex = i
                }
            }
        }

        if (endIndex != -1) {
            break
        } else {
            i++
        }
    }


    return if (startIndex != -1 && endIndex != -1) {
        arrayOf(startIndex, endIndex)
    } else {
        null
    }
}

fun CharSequence.trimAll(): CharSequence =
    filterNot { it.isWhitespace() }

fun Editable?.toStringOrNull(): String? =
    if (this?.isNotBlank() == true) this.toString()
    else null

fun String?.notBlankOrNull(): String? =
    if (this?.isNotBlank() == true) this.toString()
    else null

fun EditText.requestFocusAndShowSoftInput() {
    this.requestFocus()
    (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
        .showSoftInput(this, InputMethodManager.SHOW_FORCED)
}

fun View.clearFocusAndHideSoftInput() {
    (this.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
        .hideSoftInputFromWindow(this.windowToken, 0)
    this.clearFocus() // 상위 뷰에 focusable 이 true 인 View 가 있어야 함.
}

fun View.getLocationInWindow(): Point {
    val intArr = IntArray(2) { 0 }
    this.getLocationInWindow(intArr)
    return Point(intArr[0], intArr[1])
}