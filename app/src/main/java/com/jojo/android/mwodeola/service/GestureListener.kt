package com.jojo.android.mwodeola.service

import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent

class GestureListener : GestureDetector.SimpleOnGestureListener() {
    companion object { const val TAG = "GestureListener" }

    private var mActivePointerId: Int = 0

    override fun onDown(e: MotionEvent?): Boolean {
        Log.w(TAG, "onDown()")
        return super.onDown(e)
    }

    override fun onSingleTapUp(e: MotionEvent?): Boolean {
        Log.w(TAG, "onSingleTapUp()")
        return super.onSingleTapUp(e)
    }

    override fun onScroll(
        e1: MotionEvent?,
        e2: MotionEvent?,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
//        Log.i(TAG, "onScroll(), e1=$e1")
//        Log.i(TAG, "onScroll(), e2=$e2")
//        Log.i(TAG, "onScroll(), distanceX=$distanceX")
//        Log.i(TAG, "onScroll(), distanceY=$distanceY")

        // Get the pointer ID
//        mActivePointerId = event.getPointerId(0)
        return super.onScroll(e1, e2, distanceX, distanceY)
    }

    override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
        Log.w(TAG, "onSingleTapConfirmed()")
        return super.onSingleTapConfirmed(e)
    }

    override fun onDoubleTap(e: MotionEvent?): Boolean {
        Log.w(TAG, "onDoubleTap()")
        return super.onDoubleTap(e)
    }
}