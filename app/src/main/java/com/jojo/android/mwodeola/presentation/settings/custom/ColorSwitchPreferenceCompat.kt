package com.jojo.android.mwodeola.presentation.settings.custom

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.core.content.res.ResourcesCompat
import androidx.preference.PreferenceViewHolder
import androidx.preference.SwitchPreferenceCompat
import com.jojo.android.mwodeola.R

class ColorSwitchPreferenceCompat : SwitchPreferenceCompat {

    private var titleOn: String? = null
    private var titleOff: String? = null

    @ColorInt private var titleColor: Int = Color.BLACK
    @ColorInt private var titleOnColor: Int? = null
    @ColorInt private var titleOffColor: Int? = null

    @ColorInt private var summaryColor: Int = getColor(R.color.app_theme_color)
    @ColorInt private var summaryOnColor: Int? = null
    @ColorInt private var summaryOffColor: Int? = null

    private var titleTextView: TextView? = null
    private var summaryTextView: TextView? = null

    private val currentTitle: String
        get() = if (isChecked) titleOn ?: title.toString()
        else titleOff ?: title.toString()

    private val currentTitleColor: Int
        get() = if (isChecked) titleOnColor ?: titleColor
        else titleOffColor ?: titleColor

    private val currentSummaryColor: Int
        get() = if (isChecked) summaryOnColor ?: summaryColor
        else summaryOffColor ?: summaryColor

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
            titleOn = it.getString(R.styleable.ColorPreference_titleOn)
            titleOff = it.getString(R.styleable.ColorPreference_titleOff)

            titleColor = it.getColor(R.styleable.ColorPreference_titleColor, Color.BLACK)
            titleOnColor = it.getColor(R.styleable.ColorPreference_titleOnColor, -1)
            if (titleOnColor == -1) titleOnColor = null
            titleOffColor = it.getColor(R.styleable.ColorPreference_titleOffColor, -1)
            if (titleOffColor == -1) titleOffColor = null

            summaryColor = it.getColor(R.styleable.ColorPreference_summaryColor, getColor(R.color.gray500))
            summaryOnColor = it.getColor(R.styleable.ColorPreference_summaryOnColor, -1)
            if (summaryOnColor == -1) summaryOnColor = null
            summaryOffColor = it.getColor(R.styleable.ColorPreference_summaryOffColor, -1)
            if (summaryOffColor == -1) summaryOffColor = null
            it.recycle()
        }

        title = currentTitle
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        titleTextView = holder.findViewById(android.R.id.title) as? TextView
        titleTextView?.text = currentTitle
        titleTextView?.setTextColor(currentTitleColor)
        summaryTextView = holder.findViewById(android.R.id.summary) as? TextView
        summaryTextView?.setTextColor(currentSummaryColor)
    }

    override fun setTitle(title: CharSequence?) {
        super.setTitle(title)
        titleTextView?.setTextColor(currentTitleColor)
    }

    override fun setSummary(summary: CharSequence?) {
        super.setSummary(summary)
        summaryTextView?.setTextColor(currentSummaryColor)
    }

    override fun setSummaryOn(summary: CharSequence?) {
        super.setSummaryOn(summary)
        summaryTextView?.setTextColor(currentSummaryColor)
    }

    override fun setSummaryOff(summary: CharSequence?) {
        super.setSummaryOff(summary)
        summaryTextView?.setTextColor(currentSummaryColor)
    }

    private fun getColor(@ColorRes colorResId: Int): Int =
        ResourcesCompat.getColor(context.resources, colorResId, null)
}