package com.jojo.android.mwodeola.presentation.common

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.util.AttributeSet
import android.util.Log
import android.view.*
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.animation.AlphaAnimation
import android.view.animation.OvershootInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.ContentFrameLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.jojo.android.mwodeola.util.dpToPixels
import jp.wasabeef.blurry.Blurry

class IncubatableFloatingActionButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FloatingActionButton(context, attrs, defStyleAttr) {

    companion object {
        private const val TAG = "IncubatableFloatingActionButton"

        private const val BLUR_RADIUS = 25
        private const val BLUR_SAMPLING = 5
        private const val DURATION = 250L // animation duration

        private const val ACTION_NONE = -1
        private const val ACTION_DOWN = MotionEvent.ACTION_DOWN
        private const val ACTION_MOVE = MotionEvent.ACTION_MOVE
        private const val ACTION_HOVER_MOVE = MotionEvent.ACTION_HOVER_MOVE
        private const val ACTION_UP = MotionEvent.ACTION_UP

        private const val ON_CLICK_OFFSET = 100
    }

    data class Child(val fab: FloatingActionButton, val label: TextView)

    interface OnFabListener {
        fun onClickedParentFab(isActivate: Boolean)
        fun onClickedChildFab(position: Int, label: String?)
    }

    val isActivate: Boolean
        get() = _isActivate

    private var _isActivate: Boolean = false
    private var isAnimating: Boolean = false

    private val rootLayout: ViewGroup?
        get() = findRootLayout()

    private val parentViewGroup: ViewGroup
        get() = parent as ViewGroup

    private val blurContainer = ConstraintLayout(context)
    private val blurView = ImageView(context)

    private val parentFabMask: View
    val parentFabMaskId: Int
        get() = parentFabMask.id

    private val children = arrayListOf<Child>()

    private val gapOfChildFab = 14.dpToPixels(context) // Child Fab 간의 간격
    private val gapOfChildFabOffset = 14.dpToPixels(context) // Owner Fab 과 첫째 Child Fab 과의 간격 오프셋

    private var tempX: Float = -1f
    private var tempY: Float = -1f
    private var tempWidth: Int = -1
    private var tempHeight: Int = -1

    private val fadeIn = AlphaAnimation(0f, 1f).apply { duration = DURATION }
    private val fadeOut = AlphaAnimation(1f, 0f).apply { duration = DURATION }

    private var motionEventState = ACTION_NONE

    private var userListener: OnFabListener? = null
    private val childrenFabListener = OnChildrenFabClickedListener()

    init {
        blurContainer.apply {
            tag = "blur_container"
            layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT)
            elevation = 5.dpToPixels(context).toFloat()
            fitsSystemWindows = true
            setBackgroundColor(Color.WHITE)
            setOnClickListener {
                if (_isActivate) {
                    hideBlurAndChildrenFab()
                }
            }
        }

        blurView.apply {
            layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT)
            scaleType = ScaleType.CENTER_CROP
        }

        parentFabMask = View(context)
        parentFabMask.id = parentFabMask.hashCode()

        blurContainer.addView(blurView)
        blurContainer.addView(parentFabMask)

        isClickable = true // 터치 이벤트 이슈
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

//        Log.d(TAG, "onAttachedToWindow(): elevation=$elevation")
//        elevation = 20f
//        compatElevation = 20f
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (x != tempX || y != tempY || width != tempWidth || height != tempHeight) {
            tempX = x
            tempY = y
            tempWidth = width
            tempHeight = height

            parentFabMask.layoutParams = ConstraintLayout.LayoutParams(width, height).also {
                it.topToTop = PARENT_ID
                it.startToStart = PARENT_ID
                it.marginStart = x.toInt()
                it.topMargin = y.toInt()
            }
        }
    }

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> onActionDown(event)
            MotionEvent.ACTION_MOVE -> onActionMove(event)
            MotionEvent.ACTION_UP ->  onActionUp(event)
        }
        return super.dispatchTouchEvent(event)
    }

    fun setOnFabListener(listener: OnFabListener) {
        this.userListener = listener
    }

    fun addChild(fab: FloatingActionButton, label: TextView) {
        fab.setOnClickListener(childrenFabListener)
        val child = Child(fab, label)
        children.add(child)
        blurContainer.addView(child.fab)
        blurContainer.addView(child.label)
    }

    fun removeChildAt(index: Int) {
        val child = children.removeAt(index)
        blurContainer.removeView(child.fab)
        blurContainer.removeView(child.label)
    }

    fun removeChildWith(label: String) {
        val index = children.indexOfFirst { it.label.text == label }
        removeChildAt(index)
    }

    fun open() {
        if (isActivate.not() && isAnimating.not()) {
            showBlurAndChildrenFab()
        }
    }

    fun close() {
        if (isActivate && isAnimating.not()) {
            hideBlurAndChildrenFab()
        }
    }

    private fun onActionDown(event: MotionEvent) {
        motionEventState = ACTION_DOWN
        this.animate().setInterpolator(OvershootInterpolator())
            .setDuration(DURATION)
            .scaleX(0.8f)
            .scaleY(0.8f)
            .start()
    }

    private fun onActionMove(event: MotionEvent) {
        val isWidth = (-ON_CLICK_OFFSET <= event.x && event.x <= width + ON_CLICK_OFFSET)
        val isHeight = (-ON_CLICK_OFFSET <= event.y && event.y <= height + ON_CLICK_OFFSET)
        motionEventState =
            if (isWidth && isHeight) ACTION_MOVE
            else ACTION_HOVER_MOVE
    }

    private fun onActionUp(event: MotionEvent) {
        if (motionEventState == ACTION_DOWN || motionEventState == ACTION_MOVE) { // 클릭 판정 O
            if (!isAnimating) { // 애니메이션 중에는 작동 금지
                userListener?.onClickedParentFab(!_isActivate)

                if (!_isActivate) { // Blur 모드 활성화
                    showBlurAndChildrenFab()
                } else { // Blur 모드 비활성화
                    hideBlurAndChildrenFab()
                }
            }
        } else if (motionEventState == ACTION_HOVER_MOVE) { // 클릭 판정 X
            //Log.w(TAG, "onActionUp(): motionEventState=$motionEventState")
            this.animate().setInterpolator(OvershootInterpolator())
                .setDuration(DURATION)
                .scaleX(1f)
                .scaleY(1f)
                .start()
        }

        motionEventState = ACTION_NONE
    }

    private fun showBlurAndChildrenFab() {
        isAnimating = true
        _isActivate = true

        // capture blur bitmap
        this.visibility = View.INVISIBLE
        val bitmap = Blurry.with(context)
            .radius(BLUR_RADIUS)
            .sampling(BLUR_SAMPLING)
            .capture(rootLayout)
//            .capture(parentViewGroup)
            .get()
        this.visibility = View.VISIBLE

        // add BlurContainer
        blurView.setImageDrawable(BitmapDrawable(resources, bitmap))
//        (rootView as ViewGroup).addView(blurContainer)
//        rootLayout?.addView(blurContainer)
        parentViewGroup.addView(blurContainer)

        bringToFront()


        Log.d(TAG, "showBlurAndChildrenFab(): rootView=$rootView")
        Log.d(TAG, "showBlurAndChildrenFab(): rootLayout=$rootLayout")
        Log.d(TAG, "showBlurAndChildrenFab(): (rootView == rootLayout)=${rootView == rootLayout}")

        blurContainer.startAnimation(fadeIn)

        // rotate this(owner fab)
        this.animate().setInterpolator(OvershootInterpolator())
            .setDuration(DURATION)
            .scaleX(1f)
            .scaleY(1f)
            .rotation(135f)
            .start()

        // animate children fab and label (show)
        children.forEachIndexed { index, child ->
            val translationY = gapOfChildFabOffset + (gapOfChildFab + child.fab.customSize) * (index + 1).toFloat()
            child.fab.animate().setInterpolator(OvershootInterpolator())
                .setDuration(DURATION)
                .translationY(-translationY)
                .alpha(1f)
                .start()
            child.label.animate().setInterpolator(OvershootInterpolator())
                .setDuration(DURATION)
                .translationY(-translationY)
                .alpha(1f)
                .withEndAction {
                    isAnimating = false
                    bringToFront()
                }
        }
    }

    private fun hideBlurAndChildrenFab(endAction: Runnable? = null) {
        isAnimating = true
        _isActivate = false

        // rotate this(owner fab)
        this.animate().setInterpolator(OvershootInterpolator())
            .setDuration(DURATION)
            .rotation(0f)
            .scaleX(1f)
            .scaleY(1f)
            .start()

        blurContainer.startAnimation(fadeOut)

        // animate children fab and label (hide)
        children.forEachIndexed { index, child ->
            child.fab.animate().setInterpolator(OvershootInterpolator())
                .setDuration(DURATION)
                .translationY(0f)
                .alpha(0f)
                .start()
            val animator = child.label.animate().setInterpolator(OvershootInterpolator())
                .setDuration(DURATION)
                .translationY(0f)
                .alpha(0f)
            if (index < children.lastIndex) {
                animator.start()
            } else {
                animator.withEndAction {
                    // remove BlurContainer
                    blurView.setImageDrawable(null)
//                    rootLayout?.removeView(blurContainer)
                    parentViewGroup.removeView(blurContainer)

                    isAnimating = false
                    endAction?.run()
                }
                animator.start()
            }
        }
    }

    private fun findRootLayout(): ViewGroup? {
        var parent: ViewParent = parent
        try {
            while (parent is ViewGroup) {
                if (parent.parent is ContentFrameLayout) {
                    return parent
                }
                parent = parent.parent
            }
        } catch (e: java.lang.RuntimeException) {
            return null
        }
        return null
    }

    private fun FloatingActionButton.setMarginStart(marginStart: Int) {
        this.layoutParams = (this.layoutParams as? ViewGroup.MarginLayoutParams)?.also {
            it.marginStart = marginStart
        }
    }

    private fun FloatingActionButton.setMarginTop(marginTop: Int) {
        this.layoutParams = (this.layoutParams as? ViewGroup.MarginLayoutParams)?.also {
            it.topMargin = marginTop
        }
    }

    inner class OnChildrenFabClickedListener : OnClickListener {
        override fun onClick(view: View?) {
            hideBlurAndChildrenFab {
                children.forEachIndexed { index, child ->
                    if (child.fab == view) {
                        userListener?.onClickedChildFab(index, child.label.text.toString())
                    }
                }
            }
        }
    }
}