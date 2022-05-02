package com.jojo.android.mwodeola.presentation.security.screen.keyPad

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.jojo.android.mwodeola.databinding.FragmentPatternPasswordLightBinding
import com.jojo.android.mwodeola.presentation.security.bottmSheet.PatternPasswordView

class LightPatternPasswordFragment(
    private val watcher: PatternPasswordView.PatternWatcher
) : Fragment(), PatternPasswordView.PatternWatcher {

    private lateinit var binding: FragmentPatternPasswordLightBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View = FragmentPatternPasswordLightBinding
        .inflate(inflater, container, false)
        .also { binding = it }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.patternView.setPatternWatcher(this)
    }

    override fun onPatternUpdated(pattern: String, added: Char) {
        watcher.onPatternUpdated(pattern, added)
    }

    override fun onCompleted(pattern: String) {
        watcher.onCompleted(pattern)
    }

    fun showError() {
        binding.patternView.showError()
    }

    fun reset() {
        binding.patternView.reset()
    }
}