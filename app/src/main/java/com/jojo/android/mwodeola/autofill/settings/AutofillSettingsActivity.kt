package com.jojo.android.mwodeola.autofill.settings

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.viewbinding.ViewBinding
import com.jojo.android.mwodeola.R
import com.jojo.android.mwodeola.databinding.ActivityAutofillSettingsBinding
import com.jojo.android.mwodeola.presentation.BaseActivity

class AutofillSettingsActivity : BaseActivity() {
    companion object {
        private const val TAG = "AutofillSettingsActivity"
    }

    override val isScreenLockEnabled: Boolean = true
    override val binding by lazy { ActivityAutofillSettingsBinding.inflate(layoutInflater) }

    private val disabledColor by lazy { ResourcesCompat.getColor(resources, R.color.gray200, null) }
    private val enabledColor by lazy { ResourcesCompat.getColor(resources, R.color.green600, null) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { onBackPressed() }
        binding.cardSwitchAutofill.setOnClickListener {
            if (binding.switchAutofill.isChecked) {
                binding.switchAutofill.isChecked = false
                binding.cardSwitchAutofill.setCardBackgroundColor(disabledColor)
            } else {
                binding.switchAutofill.isChecked = true
                binding.cardSwitchAutofill.setCardBackgroundColor(enabledColor)
            }
        }
    }
}