package com.jojo.android.mwodeola.presentation.settings.custom

import android.content.Context
import android.util.AttributeSet
import android.widget.RadioButton
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.core.content.res.ResourcesCompat
import androidx.preference.*
import com.jojo.android.mwodeola.R
import com.jojo.android.mwodeola.util.Log2

class RadioButtonPreference : Preference {

    /**
     * [RadioGroupPreference] 과의 내부 상호작용을 위한 interface
     * */
    interface OnRadioButtonProvider {
        fun onRadioButtonClicked(preference: RadioButtonPreference): Boolean
        fun onRadioButtonCheckedChanged(preference: RadioButtonPreference, isChecked: Boolean)
    }

    var summaryOnColor: Int = ResourcesCompat.getColor(context.resources, R.color.app_theme_color, null)
    var summaryOffColor: Int = ResourcesCompat.getColor(context.resources, R.color.gray500, null)

    var isChecked: Boolean
        get() = _isChecked
        set(value) { performChecked(value) }

    private var _isChecked = false

    private var radioButton: RadioButton? = null
    private var summaryTextView: TextView? = null

    private var _summaryOn: String? = null
    private var _summaryOff: String? = null

    private var _summaryColor: Int = ResourcesCompat.getColor(context.resources, R.color.gray500, null)

    private var provider: OnRadioButtonProvider? = null

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
            _summaryOn = it.getString(R.styleable.ColorPreference_summaryOn)
            _summaryOff = it.getString(R.styleable.ColorPreference_summaryOff)

            it.recycle()
        }

        widgetLayoutResource = R.layout.prefs_end_widget_radio_button
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        radioButton = holder.findViewById(R.id.radio_button) as? RadioButton
        radioButton?.isClickable = false
        radioButton?.isChecked = _isChecked

        summaryTextView = holder.findViewById(android.R.id.summary) as? TextView
        summaryTextView?.setTextColor(_summaryColor)
    }

    override fun setSummary(summary: CharSequence?) {
        super.setSummary(summary)
    }

    fun setOnRadioButtonProvider(provider: OnRadioButtonProvider) {
        if (provider is RadioGroupPreference) {
            this.provider = provider
        }
    }

    fun removeOnRadioButtonProvider() {
        provider = null
    }

    fun setSummaryColor(@ColorInt color: Int) {
        _summaryColor = color
        summaryTextView?.setTextColor(color)
    }

    override fun onClick() {
        super.onClick()
        if (provider?.onRadioButtonClicked(this) == false) {
            performChecked(_isChecked.not())
        }
    }

    fun setSummaryOn(summaryOn: String?) {
        _summaryOn = summaryOn
        summaryTextView?.let {
            it.text = summaryOn
            it.setTextColor(summaryOnColor)
        }
    }

    fun setSummaryOff(summaryOff: String?) {
        _summaryOff = summaryOff
        summaryTextView?.let {
            it.text = summaryOff
            it.setTextColor(summaryOffColor)
        }
    }

    private fun performChecked(checked: Boolean) {
        _isChecked = checked
        radioButton?.isChecked = checked
        provider?.onRadioButtonCheckedChanged(this, checked)
    }
}