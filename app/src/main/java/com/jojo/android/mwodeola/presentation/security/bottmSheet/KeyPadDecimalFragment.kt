package com.jojo.android.mwodeola.presentation.security.bottmSheet

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
import com.jojo.android.mwodeola.databinding.FragmentKeyPadDecimalBinding

class KeyPadDecimalFragment(
    private val listener: BaseAuthenticationBottomSheet.OnSecureKeyPadListener
) : Fragment(), View.OnClickListener {

    private lateinit var binding: FragmentKeyPadDecimalBinding
    private val decimal = MutableList(10) { it }
    private val decimalKeyMap = hashMapOf<TextView, Int>()
    private lateinit var btnBack: FrameLayout

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?, savedInstanceState: Bundle?
    ): View = FragmentKeyPadDecimalBinding
        .inflate(inflater, container, false)
        .also { binding = it }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        decimal.shuffle()
        binding.keyPadContainer.forEachIndexed { i, raw ->
            if (raw is TableRow) {
                raw.forEachIndexed { j, view ->
                    when (view) {
                        is TextView -> {
                            val value = decimal.getOrNull(i * 3 + j) ?: 0
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