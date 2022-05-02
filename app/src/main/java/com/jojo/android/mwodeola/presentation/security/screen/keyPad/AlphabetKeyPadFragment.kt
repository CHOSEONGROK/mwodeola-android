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
import com.jojo.android.mwodeola.databinding.FragmentAlphabetKeyPad2Binding
import com.jojo.android.mwodeola.presentation.security.screen.BaseAuthenticationFragment

class AlphabetKeyPadFragment(
    private val listener: BaseAuthenticationFragment.OnSecureKeyPadListener
) : Fragment(), View.OnClickListener {

    private lateinit var binding: FragmentAlphabetKeyPad2Binding
    private val alphabets = MutableList(26) { Char(65 + it) }
    private val alphabetKeyMap = hashMapOf<TextView, Char>()
    private lateinit var btnBack: FrameLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View = FragmentAlphabetKeyPad2Binding
        .inflate(inflater, container, false)
        .also { binding = it }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        alphabets.shuffle()
        binding.keyPadContainer.forEachIndexed { i, row ->
            if (row is TableRow) {
                row.forEachIndexed { j, view ->
                    when (view) {
                        is TextView -> {
                            val value = alphabets.getOrNull(i * 7 + j) ?: '?'
                            view.text = value.toString()
                            alphabetKeyMap[view] = value
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
                val value = alphabetKeyMap[view]
                    ?: return
                listener.onKeyClicked(value)
            }
            is FrameLayout -> {
                listener.onBackPressed()
            }
        }
    }
}