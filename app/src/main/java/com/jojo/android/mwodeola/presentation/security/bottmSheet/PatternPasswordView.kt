package com.jojo.android.mwodeola.presentation.security.bottmSheet

import android.animation.*
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.*
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.FrameLayout
import android.widget.TableLayout
import android.widget.TableRow
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.core.content.res.ResourcesCompat
import androidx.core.text.isDigitsOnly
import com.google.android.material.card.MaterialCardView
import com.jojo.android.mwodeola.R

@SuppressLint("ClickableViewAccessibility")
class PatternPasswordView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : TableLayout(context, attrs) {

    companion object {
        private const val TAG = "PatternPasswordView"

        const val VERY_SLOW = 500L
        const val SLOW = 400L
        const val NORMAL = 300L
        const val FAST = 200L
        const val VERY_FAST = 100L
    }

    interface PatternWatcher {
        fun onPatternUpdated(pattern: String, added: Char)
        fun onCompleted(pattern: String)
    }

    abstract class SimplePatternWatcher : PatternWatcher {
        override fun onPatternUpdated(pattern: String, added: Char) {}
        override fun onCompleted(pattern: String) {}
    }

    var isEditable: Boolean = true
    val pattern: String
        get() = patternBuilder.toString()
    val patternOrNull: String?
        get() = if (pattern.isNotBlank()) pattern else null

    private val strokeDefaultWidth = 4.dpToPixels(context)
    private val strokeDefaultColor = Color.BLACK
    private val strokeDefaultErrorColor = ResourcesCompat.getColor(resources, android.R.color.holo_red_light, null)

    private val dotsDefaultSize = 10.dpToPixels(context)
    private val dotsDefaultScaleFactor = 2.5f
    private val dotsDefaultColor = Color.BLACK
    private val dotsDefaultErrorColor = ResourcesCompat.getColor(resources, android.R.color.holo_red_light, null)
    private val dotsDefaultHighlightColor = Color.BLACK
    private val dotsDefaultTouchSensitivityOffset = 15.dpToPixels(context)

    private val strokeDefaultDisabledColor = ResourcesCompat.getColor(resources, R.color.disabled_color, null)
    private val dotsDefaultDisabledColor = ResourcesCompat.getColor(resources, R.color.disabled_color, null)

    private val errorVibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    private val errorVibrationPattern = longArrayOf(0, 100, 20, 100)

    private var paint: Paint
    private val paintDefault: Paint
    private val paintDisabled: Paint
    private val paintError: Paint

    private val dotViews = arrayListOf<DotView>()
    private val selectedDotViews = mutableListOf<DotView>()
    private val selectedDotViewsForAnimator = mutableListOf<DotView>()
    private var durationPointToPoint = NORMAL

    private val currentTouchPointF = PointF()

    private val patternBuilder = StringBuilder()
    private var watcher: PatternWatcher? = null

    private val animators = mutableListOf<Animator>()

    private var isStartedInput = false
    private var isEndedInput = false
    private var isRunningAnimation = false

    init {
        val dotsSize: Float
        val dotsScaleFactor: Float
        val dotsColor: Int
        val dotsErrorColor: Int
        val dotsHighlightColor: Int
        val dotsTouchSensitivityOffset: Float

        context.obtainStyledAttributes(attrs, R.styleable.PatternPasswordView).let {
            isEditable = it.getBoolean(R.styleable.PatternPasswordView_editable, true)
            dotsSize = it.getDimension(R.styleable.PatternPasswordView_dotsSize, dotsDefaultSize.toFloat())
            dotsScaleFactor = it.getFloat(R.styleable.PatternPasswordView_dotsScaleFactor, dotsDefaultScaleFactor)
            dotsColor = it.getColor(R.styleable.PatternPasswordView_dotsColor, dotsDefaultColor)
            dotsErrorColor = it.getColor(R.styleable.PatternPasswordView_dotsErrorColor, dotsDefaultErrorColor)
            dotsHighlightColor = it.getColor(R.styleable.PatternPasswordView_dotsHighlightColor, dotsDefaultHighlightColor)
            dotsTouchSensitivityOffset =
                it.getDimension(R.styleable.PatternPasswordView_dotsTouchSensitivityOffset, dotsDefaultTouchSensitivityOffset.toFloat())

            val strokeWidth = it.getDimension(R.styleable.PatternPasswordView_strokeWidth, strokeDefaultWidth.toFloat())
            val strokeColor = it.getColor(R.styleable.PatternPasswordView_strokeColor, strokeDefaultColor)
            val strokeErrorColor = it.getColor(R.styleable.PatternPasswordView_strokeErrorColor, strokeDefaultErrorColor)

            paintDefault = Paint().also { p ->
                p.strokeWidth = strokeWidth
                p.strokeCap = Paint.Cap.ROUND
                p.color = strokeColor
            }
            paintDisabled = Paint().also { p ->
                p.strokeWidth = strokeWidth
                p.strokeCap = Paint.Cap.ROUND
                p.color = strokeDefaultDisabledColor
            }
            paintError = Paint().also { p ->
                p.strokeWidth = strokeWidth
                p.strokeCap = Paint.Cap.ROUND
                p.color = strokeErrorColor
            }

            paint = if (isEnabled) paintDefault else paintDisabled

            it.recycle()
        }

        isClickable = true // Touch event 이슈
        isStretchAllColumns = true

        for (row in 0 until 3) {
            val tableRow = TableRow(context, attrs).also {
                it.layoutParams = LayoutParams(0, 0).also { params ->
                    params.weight = 1f
                }
            }

            for (column in 0 until 3) {
                val dotContainer = FrameLayout(context).apply {
                    layoutParams = TableRow.LayoutParams(0, MATCH_PARENT)
                }

                val dotView = DotView(
                    context = context,
                    row = row,
                    column = column,
                    order = row * 3 + column,
                    size = dotsSize.toInt(),
                    scaleFactor = dotsScaleFactor,
                    defaultColor = dotsColor,
                    disabledColor = dotsDefaultDisabledColor,
                    errorColor = dotsErrorColor,
                    highlightColor = dotsHighlightColor,
                    touchSensitivityOffset = dotsTouchSensitivityOffset.toInt()
                )

                dotContainer.addView(dotView)
                tableRow.addView(dotContainer)
                dotViews.add(dotView)
            }

            addView(tableRow)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        //Log.d(TAG, "PatternView.onMeasure(): widthMeasureSpec=$widthMeasureSpec, heightMeasureSpec=$heightMeasureSpec")
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        //Log.d(TAG, "PatternView.onLayout(): visibility=$visibility, $changed, $l, $t, $r, $b")
        super.onLayout(changed, l, t, r, b)

//        children.forEachIndexed { i, tableRow -> // TableRow
//            Log.i(TAG, "($i)$tableRow")
//            if (tableRow is TableRow) {
//                tableRow.children.forEachIndexed { j, frameLayout -> // DotContainer(FrameLayout)
//                    Log.i(TAG, "($j)$frameLayout")
//                    if (frameLayout is FrameLayout) {
//                        frameLayout.children.forEachIndexed { k, dotView -> // DotView
//                            Log.i(TAG, "($k)$dotView")
//                        }
//                    }
//                }
//            }
//        }
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        isEditable = enabled

        paint = paintDisabled
        dotViews.forEach { it.isEnabled = false }
        invalidate()
    }

    fun requestLayoutDotView() {
//        val isNotMeasured = dotViews.any { it.width == 0 || it.height == 0 }
//        if (isNotMeasured) {
//            dotViews[0].forceLayout()
//            //forceLayout()
//        }

//        children.forEach {
//            it.requestLayout()
//        }
//        dotViews.forEach {
//            it.requestLayout()
//            it.invalidate()
//        }
    }

    fun setPatternWatcher(watcher: PatternWatcher) {
        this.watcher = watcher
    }

    fun removeWatcher() {
        this.watcher = null
    }

    fun setPattern(pattern: String?) {
        if (pattern == null ||
            pattern.length !in 2..9 ||
            pattern.isDigitsOnly().not() ||
            pattern.length != pattern.toList().distinct().size ||
            pattern.contains('0')
        ) {
            return
        }

        reset()
        isStartedInput = false
        isEndedInput = true
        this.patternBuilder.append(pattern)

        pattern.map { it.digitToInt() }
            .forEach { digit ->
                val dotView = dotViews[digit - 1]
                dotView.select(false)
                selectedDotViews.add(dotView)
            }

        watcher?.onCompleted(pattern)
        invalidate()
    }

    fun setDurationPointToPoint(duration: Long) {
        durationPointToPoint = duration
    }

    fun play(speed: Long = NORMAL) {
        if (selectedDotViews.size < 2)
            return

        if (isRunningAnimation) {
            stop()
        }

        isRunningAnimation = true
        animators.clear()
        selectedDotViewsForAnimator.clear()
        dotViews.forEach { it.reset() }
        invalidate()

        durationPointToPoint = speed

        for (index in selectedDotViews.indices) {
            if (index == selectedDotViews.lastIndex)
                break

            val dotView = selectedDotViews[index]
            val dotViewNext = selectedDotViews[index + 1]

            val fromX = dotView.centerX
            val toX = dotViewNext.centerX

            val fromY = dotView.centerY
            val toY = dotViewNext.centerY

            val holderX = PropertyValuesHolder.ofFloat("x", fromX, toX)
            val holderY = PropertyValuesHolder.ofFloat("y", fromY, toY)
            val animator = ValueAnimator.ofPropertyValuesHolder(holderX, holderY).apply {
                interpolator = AccelerateDecelerateInterpolator()
                duration = durationPointToPoint

                doOnStart {
                    selectedDotViewsForAnimator.add(dotView)
                    dotView.select(true)
                }
                addUpdateListener {
                    currentTouchPointF.x = it.getAnimatedValue("x") as Float
                    currentTouchPointF.y = it.getAnimatedValue("y") as Float
                    invalidate()
                }
            }

            animators.add(animator)
        }

        animators.forEachIndexed { index, animator ->
            if (index < animators.lastIndex) {
                animator.doOnEnd {
                    animators[index + 1].start()
                }
            } else {
                animator.doOnEnd {
                    val lastSelectedDotView = selectedDotViews.last()
                    lastSelectedDotView.select(true)
                    selectedDotViewsForAnimator.add(lastSelectedDotView)
                    isRunningAnimation = false
                }
            }
        }

        animators[0].start()
    }

    fun stop() {
        if (isRunningAnimation) {
            isRunningAnimation = false
            animators.forEach { animator ->
                if (animator.isRunning) {
                    animator.pause()
                }
            }
        }
    }

    fun showError() {
        dotViews.filter { it.selectedFlag }
            .forEach { it.showError() }

        paint = paintError
        invalidate()
    }

    fun vibrateError() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            errorVibrator.vibrate(errorVibrationPattern, -1)
        } else {
            errorVibrator.vibrate(VibrationEffect.createWaveform(errorVibrationPattern, -1))
        }
    }

    fun reset() {
        if (isRunningAnimation) {
            stop()
        }

        dotViews.forEach { it.reset() }

        isStartedInput = false
        isEndedInput = false
        isRunningAnimation = false

        paint = if (isEnabled) paintDefault else paintDisabled
        currentTouchPointF.x = 0f
        currentTouchPointF.y = 0f

        patternBuilder.clear()
        animators.clear()
        selectedDotViews.clear()
        selectedDotViewsForAnimator.clear()
        invalidate()
    }

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        //Log.i(TAG, "dispatchTouchEvent(): $event")
        if (event == null || isEndedInput || isEditable.not())
            return super.dispatchTouchEvent(event)

        when (event.action) {
            MotionEvent.ACTION_DOWN,
            MotionEvent.ACTION_MOVE -> {
                if (event.action == MotionEvent.ACTION_DOWN) {
                    isEndedInput = false
                }

                for (index in dotViews.indices) {
                    val dotView = dotViews[index]
                    val point = dotView.comeWithinTouchPoint(event.x, event.y)
                    if (point != null) {
                        val addedDotValue = dotView.value.digitToChar()

                        patternBuilder.append(addedDotValue)
                        watcher?.onPatternUpdated(patternBuilder.toString(), addedDotValue)
                        performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)

                        selectedDotViews.add(dotView)

                        if (selectedDotViews.size == 1) {
                            isStartedInput = true
                        }
                        break
                    }
                }

                currentTouchPointF.x = event.x
                currentTouchPointF.y = event.y
                invalidate()
            }
            MotionEvent.ACTION_UP -> {
                if (patternBuilder.isEmpty()) {
                    reset()
                    return true
                }

                if (patternBuilder.length == 1) {
                    reset()
                } else {
                    isStartedInput = false
                    isEndedInput = true
                    invalidate()
                }

                watcher?.onCompleted(patternBuilder.toString())
            }
        }

        if (isStartedInput) {
            parent.requestDisallowInterceptTouchEvent(true)
        }
        return true
    }

    override fun dispatchDraw(canvas: Canvas) {
        // call block() here if you want to draw behind children
        super.dispatchDraw(canvas)
        // call block() here if you want to draw over children

        if (!isRunningAnimation) {
            for (index in selectedDotViews.indices) {
                val point = selectedDotViews[index].centerPoint
                // Log.i(TAG, "dispatchDraw(): DotView($index)=${point.x}, ${point.y}")
                val pointNext =
                    if (index < selectedDotViews.lastIndex) {
                        selectedDotViews[index + 1].centerPoint
                    } else {
                        if (isStartedInput) currentTouchPointF
                        else return
                    }

                canvas.drawLine(
                    point.x, point.y,
                    pointNext.x, pointNext.y, paint)
            }
        } else {
            for (index in selectedDotViewsForAnimator.indices) {
                val point = selectedDotViewsForAnimator[index].centerPoint
                val pointNext =
                    if (index < selectedDotViewsForAnimator.lastIndex) {
                        selectedDotViewsForAnimator[index + 1].centerPoint
                    } else {
                        currentTouchPointF
                    }

                canvas.drawLine(
                    point.x, point.y,
                    pointNext.x, pointNext.y, paint)
            }
        }
    }

    private fun Int.dpToPixels(context: Context): Int =
        (this * (context.resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)).toInt()

    private fun accelerateHardware(window: Window) {
        window.setFlags(
            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
        )
    }

    class DotView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0,
        val row: Int = 0,
        val column: Int = 0,
        val order: Int = 0,
        val size: Int = 0,
        val scaleFactor: Float = 0f,
        val defaultColor: Int = 0,
        val disabledColor: Int = 0,
        val errorColor: Int = 0,
        val highlightColor: Int = 0,
        val touchSensitivityOffset: Int = 0
    ) : MaterialCardView(context, attrs, defStyleAttr) {

        var selectedFlag = false
            private set

        val value = order + 1

        val centerPoint = PointF()

        val centerX: Float
            get() = centerPoint.x
        val centerY: Float
            get() = centerPoint.y

        private lateinit var rangeX: ClosedFloatingPointRange<Float>
        private lateinit var rangeY: ClosedFloatingPointRange<Float>

        private val animScaleX = PropertyValuesHolder.ofFloat(SCALE_X, 1f, scaleFactor)
        private val animScaleY = PropertyValuesHolder.ofFloat(SCALE_Y, 1f, scaleFactor)
        private val scaleAnimation = ObjectAnimator.ofPropertyValuesHolder(this, animScaleX, animScaleY).apply {
            interpolator = AccelerateDecelerateInterpolator()
            duration = 130
            repeatCount = 1
            repeatMode = ValueAnimator.REVERSE
//            addListener(object : AnimatorListenerAdapter() {
//                override fun onAnimationStart(animation: Animator) {}
//                override fun onAnimationEnd(animation: Animator) {}
//            })
        }
        private val animScaleX2 = PropertyValuesHolder.ofFloat(SCALE_X, 1f, 1.5f)
        private val animScaleY2 = PropertyValuesHolder.ofFloat(SCALE_Y, 1f, 1.5f)
        private val scaleAnimation2 = ObjectAnimator.ofPropertyValuesHolder(this, animScaleX2, animScaleY2).apply {
            interpolator = AccelerateDecelerateInterpolator()
            duration = 130
            repeatCount = 1
            repeatMode = ValueAnimator.REVERSE
//            addListener(object : AnimatorListenerAdapter() {
//                override fun onAnimationStart(animation: Animator) {}
//                override fun onAnimationEnd(animation: Animator) {}
//            })
        }

        init {
            layoutParams = LayoutParams(size, size).apply {
                gravity = Gravity.CENTER
            }

            radius = size / 2f
            cardElevation = 1.dpToPixels(context).toFloat()
            setCardBackgroundColor(
                if (isEnabled) defaultColor else disabledColor
            )
        }

        override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
            //Log.d(TAG, "DotView.onMeasure(): widthMeasureSpec=$widthMeasureSpec, heightMeasureSpec=$heightMeasureSpec")
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }

        override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
            //Log.d(TAG, "DotView.onLayout(): $changed, $left, $top, $right, $bottom")
            super.onLayout(changed, left, top, right, bottom)

            val frameLayout = parent as? FrameLayout ?: return
            val tableRow = parent.parent as? TableRow ?: return

            val xFromTableLayout = x + frameLayout.x
            val yFromTableLayout = y + tableRow.y

            // Log.d(TAG, "onLayout($order): xFromTableLayout=$xFromTableLayout, yFromTableLayout=$yFromTableLayout")

            rangeX = (xFromTableLayout - touchSensitivityOffset)..(xFromTableLayout + width + touchSensitivityOffset)
            rangeY = (yFromTableLayout - touchSensitivityOffset)..(yFromTableLayout + height + touchSensitivityOffset)

            centerPoint.x = xFromTableLayout + width / 2f
            centerPoint.y = yFromTableLayout + height / 2f

            // Log.w(TAG, "DotView.onLayout(): rangeX=$rangeX")
            // Log.w(TAG, "DotView.onLayout(): rangeY=$rangeY")
            // Log.w(TAG, "DotView.onLayout(): centerPoint=$centerPoint")
        }

        override fun setEnabled(enabled: Boolean) {
            super.setEnabled(enabled)

            setCardBackgroundColor(
                if (isEnabled) defaultColor else disabledColor
            )
        }

        fun select(isAnimate: Boolean) {
            selectedFlag = true
            setCardBackgroundColor(highlightColor)
            if (isAnimate) {
                //scaleAnimation.start()
                scaleAnimation2.start()
            }
        }

        fun showError() {
            setCardBackgroundColor(errorColor)
        }

        fun reset() {
            selectedFlag = false

            setCardBackgroundColor(
                if (isEnabled) defaultColor else disabledColor
            )
        }

        fun comeWithinTouchPoint(targetX: Float, targetY: Float): PointF? {
            if (selectedFlag)
                return null

            if (targetX in rangeX && targetY in rangeY) {
                selectedFlag = true
                setCardBackgroundColor(highlightColor)
                scaleAnimation.start()
                return PointF(centerPoint.x, centerPoint.y)
            }
            return null
        }

        private fun Int.dpToPixels(context: Context): Int =
            (this * (context.resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)).toInt()

        override fun toString(): String {
            return "DotView($value): visible=$visibility, width=$width, height=$height, center=($centerX, $centerY)"
        }
    }
}