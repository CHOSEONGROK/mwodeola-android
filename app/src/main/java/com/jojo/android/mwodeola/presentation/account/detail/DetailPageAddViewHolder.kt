package com.jojo.android.mwodeola.presentation.account.detail

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.divider.MaterialDivider
import com.jojo.android.mwodeola.databinding.ActivityAccountDetailPageAddBinding

class DetailPageAddViewHolder(binding: ActivityAccountDetailPageAddBinding) : RecyclerView.ViewHolder(binding.root) {

    val btnAddNormalDetail: LinearLayoutCompat = binding.btnAddNormalDetail
    val divider: MaterialDivider = binding.divider
    val btnAddSnsDetail: LinearLayoutCompat = binding.btnAddSnsDetail

    companion object {
        fun newInstance(parent: ViewGroup) =
            DetailPageAddViewHolder(
                ActivityAccountDetailPageAddBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
    }
}