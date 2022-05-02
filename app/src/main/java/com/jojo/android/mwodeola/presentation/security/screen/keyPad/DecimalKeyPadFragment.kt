package com.jojo.android.mwodeola.presentation.security.screen.keyPad

import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.core.view.forEachIndexed
import androidx.fragment.app.Fragment
import com.jojo.android.mwodeola.databinding.FragmentDecimalKeyPad2Binding
import com.jojo.android.mwodeola.presentation.security.screen.BaseAuthenticationFragment

class DecimalKeyPadFragment constructor(
    private val listener: BaseAuthenticationFragment.OnSecureKeyPadListener,
    private val randomDecimal: List<Int> = MutableList(10) { it }.apply { shuffle() }
) : Fragment(), View.OnClickListener {

    private lateinit var binding: FragmentDecimalKeyPad2Binding
    private val decimalKeyMap = hashMapOf<TextView, Int>()
    private lateinit var btnBack: FrameLayout

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?, savedInstanceState: Bundle?
    ): View = FragmentDecimalKeyPad2Binding
        .inflate(inflater, container, false)
        .also { binding = it }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.keyPadContainer.forEachIndexed { i, raw ->
            if (raw is TableRow) {
                raw.forEachIndexed { j, view ->
                    when (view) {
                        is TextView -> {
                            val value = randomDecimal.getOrNull(i * 3 + j) ?: 0
                            view.text = value.toString()
                            decimalKeyMap[view] = value
                        }
                        is FrameLayout -> {
                            btnBack = view
                        }
                    }
                    view.setOnClickListener(this)
                }
            }
        }

    }

    override fun onClick(view: View) {
        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)

        when (view) {
            is TextView -> {
                val decimal = decimalKeyMap[view]
                    ?: return
                listener.onKeyClicked(decimal.digitToChar())
            }
            is FrameLayout -> {
                listener.onBackPressed()
            }
        }
    }
}