package com.jojo.android.mwodeola.presentation.settings.custom

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.core.content.res.ResourcesCompat
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import com.jojo.android.mwodeola.R

class ColorPreference : Preference {

    @ColorInt
    private var titleColor = Color.BLACK
    @ColorInt
    private var summaryColor = getColor(R.color.gray500)

    private var titleTextView: TextView? = null
    private var summaryTextView: TextView? = null

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        init(context, attrs, defStyleAttr, defStyleRes)
    }
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context, attrs, defStyleAttr)
    }
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs)
    }
    constructor(context: Context) : super(context) {
        init(context)
    }

    private fun init(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0, defStyleRes: Int = 0) {
        context.obtainStyledAttributes(attrs, R.styleable.ColorPreference, defStyleAttr, defStyleRes).let {
            titleColor = it.getColor(R.styleable.ColorPreference_titleColor, Color.BLACK)
            summaryColor = it.getColor(R.styleable.ColorPreference_summaryColor, getColor(R.color.gray500))

            it.recycle()
        }
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        titleTextView = holder.findViewById(android.R.id.title) as? TextView
        titleTextView?.setTextColor(titleColor)

        summaryTextView = holder.findViewById(android.R.id.summary) as? TextView
        summaryTextView?.setTextColor(summaryColor)
    }

    fun setTitleColor(@ColorInt color: Int) {
        titleColor = color
        titleTextView?.setTextColor(color)
    }

    fun setTitleColorResource(@ColorRes colorResId: Int) {
        setTitleColor(getColor(colorResId))
    }

    fun setSummaryColor(@ColorInt color: Int) {
        summaryColor = color
        summaryTextView?.setTextColor(color)
    }

    fun setSummaryColorResource(@ColorRes colorResId: Int) {
        setSummaryColor(getColor(colorResId))
    }

    private fun getColor(@ColorRes colorResId: Int): Int =
        ResourcesCompat.getColor(context.resources, colorResId, null)
}