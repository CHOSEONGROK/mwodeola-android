package com.jojo.android.mwodeola.presentation.common

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.graphics.drawable.*
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import androidx.annotation.ColorInt
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.graphics.drawable.toBitmapOrNull
import androidx.core.graphics.get
import kotlin.math.*

@SuppressLint("ViewConstructor")
class OvalShapeImageView constructor(
    context: Context, n: Float = 2.7f, ovalShapeEnabled: Boolean = true, autoStrokeEnabled: Boolean = true,
) : AppCompatImageView(context, null, 0) {

    companion object {
        private const val TAG = "OvalShapeImageView"
        private const val BORDER_WIDTH = 1f
        private val STROKE_COLOR_LIGHT = Color.rgb(204, 204, 204)
        private val STROKE_COLOR_DARK = Color.rgb(136, 136, 136)
    }

    private val n: Float
    private val isOvalShapeEnabled: Boolean
    private val isAutoStrokeEnabled: Boolean

    private var parentColor: Int = Color.WHITE

    private var isDrawingStroke = false
    private var isSizeDecision = false
    private var isLazySetDrawable = false

    private val ovalPath = Path()
    private val pointsInOvalPath = Array(20) { PointF() }
    private val strokePaint = Paint().also {
        it.style = Paint.Style.STROKE
        it.color = STROKE_COLOR_LIGHT
        it.strokeWidth = BORDER_WIDTH
    }

    private var cashedOriginDrawable: Drawable? = null

    init {
        // scaleType = ScaleType.CENTER_CROP
        this.n = n.coerceAtLeast(0.1f).coerceAtMost(10f)
        this.isOvalShapeEnabled = ovalShapeEnabled
        this.isAutoStrokeEnabled = if (ovalShapeEnabled) autoStrokeEnabled else false
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        parentColor = initParentColor()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        isSizeDecision = true

        resetOvalPath()

        if (isLazySetDrawable) {
            isLazySetDrawable = false
            super.setImageDrawable(transformDrawable(cashedOriginDrawable))
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (isAutoStrokeEnabled && isDrawingStroke) {
            canvas.drawPath(ovalPath, strokePaint)
        }
    }

    override fun setImageDrawable(drawable: Drawable?) {
        if (drawable == null || drawable == cashedOriginDrawable)
            return

        // 테두리 모양 변형하기 전 Drawable
        cashedOriginDrawable = drawable

        if (isOvalShapeEnabled) {
            if (width == 0 || height == 0) {
                isLazySetDrawable = true
                return
            }

            super.setImageDrawable(transformDrawable(drawable))
        } else {
            super.setImageDrawable(drawable)
        }
    }

    fun setParentColor(@ColorInt color: Int) {
        parentColor = color

//        strokePaint.color = if (ColorUtil.isSimilar(color, STROKE_COLOR_LIGHT)) {
//            STROKE_COLOR_DARK
//        } else {
//            STROKE_COLOR_LIGHT
//        }

        if (isAutoStrokeEnabled) {
            (drawable as? BitmapDrawable)?.let { executeAutoStroke(it.bitmap) }
        }
    }

    private fun transformDrawable(drawable: Drawable?): Drawable? {
        if (drawable == null || ovalPath.isEmpty)
            return null

        val bitmap = drawable.toBitmapOrNull(width, height)
            ?: return null

        val transformed = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        val canvas = Canvas(transformed)
        val paint = Paint()

        canvas.drawPath(ovalPath, paint)

        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)

        val rectSrc = Rect(0, 0, bitmap.width, bitmap.height)
        val rectDst = Rect(0, 0, width, height)

        canvas.drawBitmap(bitmap, rectSrc, rectDst, paint)

        if (isAutoStrokeEnabled) {
            executeAutoStroke(transformed)
        }

        return BitmapDrawable(resources, transformed)
    }

    private fun executeAutoStroke(bitmap: Bitmap) {
        val prev = isDrawingStroke

        isDrawingStroke = pointsInOvalPath.any {
            var color = bitmap[it.x.toInt(), it.y.toInt()]
            if (color == 0) color = -1
            val similar = ColorUtil.isSimilar(color, parentColor)
            similar
        }

        if (prev != isDrawingStroke)
            invalidate()
    }

    private fun resetOvalPath() {
        // |x/a|^n + |y/b|^n = 1
        if (width == 0 || height == 0)
            return

        val n = this.n
        val rn = 1/n
        var x = 0f
        var y = 0f
        val a = width / 2f
        val b = height / 2f
        val a_n = a.pow(n)
        val b_n = b.pow(n)

        ovalPath.reset()
        ovalPath.moveTo(-a, 0f)
        x = -a
        while (x <= a) {
            y = ((1 - x.absoluteValue.pow(n) / a_n) * b_n).pow(rn)
            ovalPath.lineTo(x++, y)
        }

        x = a
        while (x >= -a) {
            y = -((1 - x.absoluteValue.pow(n) / a_n) * b_n).pow(rn)
            ovalPath.lineTo(x--, y)
        }

        ovalPath.close()

        val matrix = Matrix()
        matrix.setScale((a - BORDER_WIDTH) / a, (b - BORDER_WIDTH) / b)
        matrix.postTranslate(a, b)

        ovalPath.transform(matrix)

        copyPath()
    }

    private fun copyPath() {
        val matrix = Matrix()
        val a = width / 2f
        val b = height / 2f
        val path = Path()
        matrix.postTranslate(-a, -b)
        ovalPath.transform(matrix, path)
        matrix.setScale((a - BORDER_WIDTH - 3) / a, (b - BORDER_WIDTH - 3) / b)
        matrix.postTranslate(a, b)
        path.transform(matrix)

        val pm = PathMeasure(path, false)
        val length = pm.length
        var distance = 0f
        val speed = length / 20
        var counter = 0
        val coordinates = FloatArray(2)
        val tan = FloatArray(2)

        while ((distance < length) && (counter < 20)) {
            pm.getPosTan(distance, coordinates, tan)
            pointsInOvalPath[counter].x = coordinates[0]
            pointsInOvalPath[counter].y = coordinates[1]
            counter++
            distance += speed
        }
    }

    private fun initParentColor(): Int {
        var parent = parent
        while (parent != null) {
            if (parent is View && parent.background is ColorDrawable) {
                return (parent.background as ColorDrawable).color
            }
            parent = parent.parent
        }
        return Color.WHITE
    }

    private val Int.dpToPixels: Float
        get() = this * (resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)

    private fun Int.hexString(): String {
        return "#${Integer.toHexString(this)}"
    }
}