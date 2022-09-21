package com.jojo.android.mwodeola.service

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.children
import androidx.core.view.setPadding

class FloatingCaptureLayout : ConstraintLayout {

    companion object {
        const val TAG = "FloatingCaptureLayout"

        const val HELP_BUTTON_HALF_WIDTH = 40f

        const val SQRT_2 = 1.414
    }

    interface OnGestureListener {
        fun onMove(dX: Int, dY: Int)
        fun onPinchZoom(dX: Int, dY: Int)
    }

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)

    private var _isFocused = false
    private var isResizingActivate = false

    private lateinit var child1: View
    private lateinit var child2: View

    private val rect = RectF()

    private val scaleGestureDetector by lazy { ScaleGestureDetector(context, OnScaleGestureListener()) }

    private var floatingViewGestureListener: OnGestureListener? = null

    private val paint1: Paint = Paint().apply {
        strokeWidth = 40f
        strokeCap = Paint.Cap.ROUND
        color = Color.DKGRAY
    }
    private val paint2: Paint = Paint().apply {
        color = Color.WHITE
    }

    private var prevTouchPoint = PointF()

    init {
        setPadding(40)
        setWillNotDraw(false) // ViewGroup 의 onDraw() 호출을 풀어주는 함수
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        Log.d(TAG, "onAttachedToWindow(), childCount=$childCount")
        child1 = children.first()
        child2 = children.elementAt(1)

        rect.run {
            top = child1.top - 20f
            bottom = child1.top + 20f
            left = (child1.left + child1.right) / 2f - HELP_BUTTON_HALF_WIDTH
            right = (child1.left + child1.right) / 2f + HELP_BUTTON_HALF_WIDTH
        }
    }


    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
//        Log.i(TAG, "onLayout()")
        layoutParams.let {
            it.width = width
            it.height = height
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
//        Log.i(TAG, "onDraw()")
    }

    override fun dispatchDraw(canvas: Canvas?) {
        super.dispatchDraw(canvas)
//        Log.i(TAG, "dispatchDraw($canvas)")
        if (canvas == null) return
        if (!_isFocused) return
        if (child2.visibility == VISIBLE) return

        val centerX = (child1.left + child1.right) / 2f
        val centerY = child1.top.toFloat()

        canvas.drawLine(
            centerX - HELP_BUTTON_HALF_WIDTH, centerY,
            centerX + HELP_BUTTON_HALF_WIDTH, centerY, paint1)

        canvas.drawCircle(centerX - 20f, centerY, 5f, paint2)
        canvas.drawCircle(centerX, centerY, 5f, paint2)
        canvas.drawCircle(centerX + 20f, centerY, 5f, paint2)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event == null) return false
//        Log.i(TAG, "onTouchEvent(), $event")
        scaleGestureDetector.onTouchEvent(event)

        rect.run {
            top = child1.top - 20f
            bottom = child1.top + 20f
            left = (child1.left + child1.right) / 2f - HELP_BUTTON_HALF_WIDTH
            right = (child1.left + child1.right) / 2f + HELP_BUTTON_HALF_WIDTH
        }

        if (!isResizingActivate) {
            when (event.action) {
                // ACTION_DOWN -> 처음 위치를 기억
                MotionEvent.ACTION_DOWN -> {
                    _isFocused = true

                    prevTouchPoint.x = event.rawX
                    prevTouchPoint.y = event.rawY

                    invalidate()
                }
                // ACTION_MOVE -> 이동 거리 계산
                MotionEvent.ACTION_MOVE -> {
                    floatingViewGestureListener?.onMove(
                        (event.rawX - prevTouchPoint.x).toInt(),
                        (event.rawY - prevTouchPoint.y).toInt()
                    )

                    prevTouchPoint.x = event.rawX
                    prevTouchPoint.y = event.rawY

                    invalidate()
                }
                MotionEvent.ACTION_UP -> {
                    if (rect.contains(event.x, event.y)) {
                        child2.visibility = VISIBLE
                        invalidate()
                    }
                }
                MotionEvent.ACTION_OUTSIDE -> {
                    _isFocused = false
                    child2.visibility = GONE
                    invalidate()
                }
            }
        } else if (event.action == MotionEvent.ACTION_UP) { // Resizing 일 때
            isResizingActivate = false
        }

        return false
    }



    fun setOnFloatingViewTouchListener(listener: OnGestureListener) {
        this.floatingViewGestureListener = listener
    }

    inner class OnScaleGestureListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        private var prevSpan = 0f
        private var prevSpanX = 0f
        private var prevSpanY = 0f

        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
            isResizingActivate = true

            prevSpanX = detector.previousSpanX
            prevSpanY = detector.previousSpanY

            return super.onScaleBegin(detector)
        }

        override fun onScale(detector: ScaleGestureDetector): Boolean {
//            Log.i(TAG, "onScale(), curSpan=${detector?.currentSpan}, preSpan=${detector?.previousSpan}")

            floatingViewGestureListener?.onPinchZoom(
                (detector.currentSpanX - prevSpanX).toInt(),
                (detector.currentSpanY - prevSpanY).toInt()
            )

            prevSpanX = detector.currentSpanX
            prevSpanY = detector.currentSpanY

            return super.onScale(detector)
        }

        override fun onScaleEnd(detector: ScaleGestureDetector) {
//            Log.w(TAG, "onScale() End")
//            isResizingActivate = false
            super.onScaleEnd(detector)
        }
    }

}