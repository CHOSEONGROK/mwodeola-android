package com.jojo.android.mwodeola.presentation.account.datalist.dialog

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Point
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.jojo.android.mwodeola.data.account.Account
import com.jojo.android.mwodeola.data.account.AccountGroupAndDetails
import com.jojo.android.mwodeola.databinding.DialogAccountSelectionBinding
import com.jojo.android.mwodeola.model.account.AccountRepository
import com.jojo.android.mwodeola.model.account.AccountSource

class SelectAccountInGroupDialog private constructor(
    private val accountGroupID: String,
    private val listener: ((item: Account) -> Unit)?
) : DialogFragment() {

    private val binding by lazy { DialogAccountSelectionBinding.inflate(layoutInflater) }
    private val adapter by lazy { SelectAccountInGroupRvAdapter(this) }
    private val repository by lazy { AccountRepository(requireContext()) }


    val packageManager: PackageManager
        get() = requireContext().packageManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        resize(0.85f)
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?, savedInstanceState: Bundle?): View = binding.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerView.adapter = adapter

        repository.getAllSimpleAccountDetailsInGroup(accountGroupID, object : AccountSource.LoadDataCallback<AccountGroupAndDetails>() {
            override fun onSucceed(data: AccountGroupAndDetails) {
                adapter.setData(data.accounts)
            }
        })
    }

    fun finishForResult(account: Account) {
        listener?.invoke(account)
        dismiss()
    }

    private fun resize(width: Float) {
        val windowManager = requireContext().getSystemService(Context.WINDOW_SERVICE) as WindowManager
        if (Build.VERSION.SDK_INT < 30) {
            val display = windowManager.defaultDisplay
            val size = Point()
            display.getSize(size)
            val params = dialog?.window?.attributes
            val x = (size.x * width).toInt()
            params?.width = x
            dialog?.window?.attributes = params
        } else {
            val rect = windowManager.currentWindowMetrics.bounds
            val params = dialog?.window?.attributes
            val x = (rect.width() * width).toInt()
            params?.width = x
            dialog?.window?.attributes = params
        }
    }

    class Builder(private val fragmentManager: FragmentManager) {

        private var accountGroupID: String? = null
        private var listener: ((item: Account) -> Unit)? = null

        fun setAccountGroupID(id: String) = apply {
            this.accountGroupID = id
        }

        fun setOnItemSelectListener(listener: (item: Account) -> Unit) = apply {
            this.listener = listener
        }

        fun show() {
            if (accountGroupID != null) {
                SelectAccountInGroupDialog(accountGroupID!!, listener)
                    .show(fragmentManager, null)
            }
        }
    }
}