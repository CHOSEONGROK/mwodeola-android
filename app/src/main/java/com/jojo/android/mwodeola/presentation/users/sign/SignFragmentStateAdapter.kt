package com.jojo.android.mwodeola.presentation.users.sign

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class SignFragmentStateAdapter(
    fragmentActivity: FragmentActivity
) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int = SignActivity.FRAGMENT_ITEM_COUNT

    override fun createFragment(position: Int): Fragment =
        when (position) {
            SignActivity.FRAGMENT_POSITION_PHONE -> SignInputPhoneFragment.newInstance()
            SignActivity.FRAGMENT_POSITION_USER_NAME -> SignInputUserNameFragment.newInstance()
            SignActivity.FRAGMENT_POSITION_EMAIL -> SignInputEmailFragment.newInstance()
            else -> throw IndexOutOfBoundsException("position=$position")
        }
}