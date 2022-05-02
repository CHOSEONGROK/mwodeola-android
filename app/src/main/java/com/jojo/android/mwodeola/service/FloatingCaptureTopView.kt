package com.jojo.android.mwodeola.service

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.PixelFormat
import android.os.Build
import android.util.Log
import android.view.*
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.Toast
import androidx.core.view.MotionEventCompat
import com.jojo.android.mwodeola.databinding.FloatingCaptureViewBinding

class FloatingCaptureTopView(
    private val context: Context
) {
    companion object { const val TAG = "FloatingCaptureTopView" }

    private var windowManager: WindowManager? = null

    private val binding by lazy {
        FloatingCaptureViewBinding.inflate(
            context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater)
    }

    private val params by lazy {
        val windowType = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
            WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
        else WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY

        WindowManager.LayoutParams(
            WRAP_CONTENT, WRAP_CONTENT, windowType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
            PixelFormat.TRANSLUCENT
        ).apply { gravity = Gravity.TOP }
    }


    fun init() = apply {
        // WindowManager Settings.
        windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

        val gestureDetector = GestureDetector(context, GestureListener())

        // FloatingCaptureViewBinding Settings.
//        binding.floatingCaptureLayout.setOnTouchListener(TouchListener())

//        binding.ivFloatingCapture.setOnClickListener {
//            Toast.makeText(context, "ivFloatingCapture", Toast.LENGTH_SHORT).show()
//        }

//        binding.button.setOnClickListener {
//            Toast.makeText(context, "button", Toast.LENGTH_SHORT).show()
//        }

        binding.ivFloatingCapture.isFocusable = true
        binding.ivFloatingCapture.isFocusableInTouchMode = true
        binding.ivFloatingCapture.setOnFocusChangeListener { v, hasFocus ->
            Log.e(TAG, "setOnFocusChangeListener, $hasFocus")
        }

        binding.btnClose.setOnClickListener {
            windowManager?.removeView(binding.root)
        }
        binding.btnExpander.setOnClickListener {
            params.also {
                Log.w(FloatingCaptureService.TAG, "(전) x=${params.x}, x=${params.y}, width=${params.width}, height=${params.height}")
//                it.width += 10.dpToPixels(context)
//                it.height += 10.dpToPixels(context)
                Log.e(FloatingCaptureService.TAG, "(후) x=${params.x}, x=${params.y}, width=${params.width}, height=${params.height}")
            }
            windowManager?.updateViewLayout(binding.root, params)
        }
        binding.btnReducer.setOnClickListener {
            params.also {
//                it.width -= 10.dpToPixels(context)
//                it.height -= 10.dpToPixels(context)
            }
            windowManager?.updateViewLayout(binding.root, params)
        }
        binding.btnTransparency.setOnClickListener {
            Toast.makeText(context, "btnTransparency", Toast.LENGTH_SHORT).show()
        }

        binding.floatingCaptureLayout.setOnFloatingViewTouchListener(OnFloatingViewGestureListener())
    }


    fun setCaptureView(view: View) = apply {
        binding.ivFloatingCapture.setImageBitmap(getBitmapFromView(view))
    }

    fun show() {
        if (windowManager != null) {
            windowManager?.addView(binding.floatingCaptureLayout, params)
//            activity.moveTaskToBack(true)
        }
    }

    private fun getBitmapFromView(view: View?): Bitmap? {
        if (view == null || view.width == 0 || view.height == 0)
            return null

        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }

    private fun actionToString(action: Int): String {
        return when (action) {
            MotionEvent.ACTION_DOWN -> "Down"
            MotionEvent.ACTION_MOVE -> "Move"
            MotionEvent.ACTION_POINTER_DOWN -> "Pointer Down"
            MotionEvent.ACTION_UP -> "Up"
            MotionEvent.ACTION_POINTER_UP -> "Pointer Up"
            MotionEvent.ACTION_OUTSIDE -> "Outside"
            MotionEvent.ACTION_CANCEL -> "Cancel"
            else -> ""
        }
    }

    inner class OnFloatingViewGestureListener : FloatingCaptureLayout.OnGestureListener {
        override fun onMove(dX: Int, dY: Int) {
            params.x += dX
            params.y += dY

            windowManager?.updateViewLayout(binding.root, params)
        }

        override fun onPinchZoom(dX: Int, dY: Int) {
            Log.i(FloatingCaptureService.TAG, "onPinchZoom(), dX=$dX, dY=$dY")
            params.width += dX
            params.height += dY

            windowManager?.updateViewLayout(binding.root, params)
        }
    }
}