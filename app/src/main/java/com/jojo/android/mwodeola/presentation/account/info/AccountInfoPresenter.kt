package com.jojo.android.mwodeola.presentation.account.info

import com.jojo.android.mwodeola.data.account.Account


class AccountInfoPresenter(
    private val view: AccountInfoContract.View,
//    private val repository: IAccountRepository,
    private var _account: Account
) : AccountInfoContract.Presenter {

//    override var account: Account
//        get() = _account
//        set(value) { _account = value }
//
//    override fun updateFavorite() {
//        _account.ownGroup.let {
//            it.isFavorite = !it.isFavorite
////            repository.updateFavorite(it.accountGroupID, it.isFavorite)
//            view.updateFavoriteIcon(it.isFavorite)
//        }
//    }
}