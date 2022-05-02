package com.jojo.android.mwodeola.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.PixelFormat
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.view.*
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.Toast
import com.jojo.android.mwodeola.databinding.FloatingCaptureViewBinding

class FloatingCaptureService : Service() {
    companion object { const val TAG = "FloatingCaptureService" }

    private val floatingCaptureTopView by lazy { FloatingCaptureTopView(baseContext) }

    private val mBinder = FloatingCaptureBinder(this)
    private var windowManager: WindowManager? = null
    private var view: View? = null


    override fun onCreate() {
        Log.d(TAG, "onCreate()")
    }

    override fun onBind(intent: Intent?): IBinder {
        Log.d(TAG, "onBind()")
        return mBinder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.d(TAG, "onUnbind()")
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy()")
//        windowManager?.removeView(view)
        view = null
//        windowManager = null
    }

    fun showFloatingCaptureView(view: View): Boolean {
        floatingCaptureTopView
            .init()
//            .setCaptureView(view)
            .show()

        return true
    }

    private fun getBitmapFromView(view: View?): Bitmap? {
        if (view == null || view.width == 0 || view.height == 0)
            return null

        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }

    class FloatingCaptureBinder(val service: FloatingCaptureService) : Binder()
}