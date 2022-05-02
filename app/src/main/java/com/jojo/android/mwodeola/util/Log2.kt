package com.jojo.android.mwodeola.util

import android.util.Log
import java.lang.StringBuilder

object Log2 {
    private const val TAG = "abcd"

    fun d() = Log.d(TAG, buildLogMsg())
    fun d(msg: String?) = Log.d(TAG, buildLogMsg(msg))
    fun d(msg: Double) = Log.d(TAG, buildLogMsg(msg.toString()))
    fun d(msg: Int) = Log.d(TAG, buildLogMsg(msg.toString()))

    fun i() = Log.i(TAG, buildLogMsg())
    fun i(msg: String?) = Log.i(TAG, buildLogMsg(msg))
    fun i(msg: Double) = Log.i(TAG, buildLogMsg(msg.toString()))
    fun i(msg: Int) = Log.i(TAG, buildLogMsg(msg.toString()))

    fun w() = Log.w(TAG, buildLogMsg())
    fun w(msg: String?) = Log.w(TAG, buildLogMsg(msg))
    fun w(msg: Double) = Log.w(TAG, buildLogMsg(msg.toString()))
    fun w(msg: Int) = Log.w(TAG, buildLogMsg(msg.toString()))

    fun e() = Log.e(TAG, buildLogMsg())
    fun e(msg: String?) = Log.e(TAG, buildLogMsg(msg))
    fun e(msg: Double) = Log.e(TAG, buildLogMsg(msg.toString()))
    fun e(msg: Int) = Log.e(TAG, buildLogMsg(msg.toString()))

    private fun buildLogMsg(message: String? = ""): String {
        val ste = Thread.currentThread().stackTrace[4]
        val sb = StringBuilder()
        sb.append("[")
        sb.append(ste.fileName.replace(".java", "").replace(".kt", ""))
        sb.append("::")
        sb.append(ste.methodName)
        sb.append("] ")
        sb.append(message)
        return sb.toString()
    }

    private fun buildShortClassTag(cls: Any?, out: StringBuilder) {
        if (cls == null) {
            out.append("null")
        } else {
            var simpleName = cls.javaClass.simpleName
            if (simpleName.isBlank()) {
                simpleName = cls.javaClass.name
                val end = simpleName.lastIndexOf('.')
                if (end > 0) {
                    simpleName = simpleName.substring(end + 1)
                }
            }
            out.append(simpleName)
            out.append('{')
            out.append(Integer.toHexString(System.identityHashCode(cls)))
        }
    }
}