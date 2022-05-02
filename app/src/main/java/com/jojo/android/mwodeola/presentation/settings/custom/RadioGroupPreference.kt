package com.jojo.android.mwodeola.presentation.settings.custom

import android.content.Context
import android.util.AttributeSet
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceViewHolder
import androidx.preference.children
import com.jojo.android.mwodeola.util.Log2

class RadioGroupPreference : PreferenceCategory, RadioButtonPreference.OnRadioButtonProvider {

    abstract class ToggleWatcher {
        open fun onToggleClicked(toggle: RadioButtonPreference): Boolean { return false }
        open fun onToggleChanged(toggle: RadioButtonPreference, isChecked: Boolean) {}
    }

    val childrenToggles: Sequence<RadioButtonPreference>
        get() = children.filterIsInstance<RadioButtonPreference>()

    val checkedToggle: RadioButtonPreference?
        get() = childrenToggles.find { it.isChecked }

    private var toggleWatcher: ToggleWatcher? = null

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int, defStyleRes: Int) : super(context, attrs, defStyle, defStyleRes)
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context) : super(context, null)

    override fun onAttached() {
        super.onAttached()
        childrenToggles.forEach {
            it.setOnRadioButtonProvider(this)
        }
    }

    override fun onDetached() {
        super.onDetached()
        toggleWatcher = null
        childrenToggles.forEach {
            it.removeOnRadioButtonProvider()
        }
    }

    override fun onRadioButtonClicked(preference: RadioButtonPreference): Boolean {
        var isConsumedEvent = toggleWatcher?.onToggleClicked(preference) ?: false

        // 이미 checked 된 토글이 재선택된 경우 -> 이벤트 강제 소비
        if (preference == checkedToggle) {
            isConsumedEvent = true
        }

        // 이벤트 소비x -> 기존 checkedToggle 선택 해제
        if (isConsumedEvent.not()) {
            checkedToggle?.isChecked = false
        }

        return isConsumedEvent
    }

    override fun onRadioButtonCheckedChanged(preference: RadioButtonPreference, isChecked: Boolean) {
        toggleWatcher?.onToggleChanged(preference, isChecked)
    }

    fun setToggleWatcher(watcher: ToggleWatcher) {
        toggleWatcher = watcher
    }

    fun checkToggle(key: String) {
        val oldCheckedToggle = checkedToggle
        val newCheckedToggle = childrenToggles.find { it.key == key }
            ?: return

        if (oldCheckedToggle == newCheckedToggle)
            return

        oldCheckedToggle?.isChecked = false
        newCheckedToggle.isChecked = true
    }
}