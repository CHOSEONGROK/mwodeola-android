package com.jojo.android.mwodeola.presentation.security.bottmSheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.jojo.android.mwodeola.databinding.FragmentPatternPasswordBinding

class PatternPasswordFragment(
    private val watcher: PatternPasswordView.PatternWatcher
) : Fragment(), PatternPasswordView.PatternWatcher {

    private lateinit var binding: FragmentPatternPasswordBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View = FragmentPatternPasswordBinding
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