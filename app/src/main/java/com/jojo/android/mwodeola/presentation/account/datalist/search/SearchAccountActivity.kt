package com.jojo.android.mwodeola.presentation.account.datalist.search

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.EditorInfo
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import com.jojo.android.mwodeola.data.account.AccountDetail
import com.jojo.android.mwodeola.data.account.AccountGroup
import com.jojo.android.mwodeola.data.local.SearchHistory
import com.jojo.android.mwodeola.databinding.ActivitySearchAccountBinding
import com.jojo.android.mwodeola.model.account.AccountRepository
import com.jojo.android.mwodeola.model.local.SearchHistoryRepository
import com.jojo.android.mwodeola.presentation.BaseActivity
import com.jojo.android.mwodeola.presentation.account.datalist.search.adapter.SearchHistoryAccountListAdapter
import com.jojo.android.mwodeola.presentation.account.datalist.search.adapter.SearchResultAccountGroupListAdapter
import com.jojo.android.mwodeola.presentation.account.detail.AccountDetailActivity
import com.jojo.android.mwodeola.presentation.common.BottomUpDialog
import com.jojo.android.mwodeola.util.Utils

class SearchAccountActivity : BaseActivity(), SearchAccountContract.View {

    override val isScreenLockEnabled: Boolean = true
    override val binding by lazy { ActivitySearchAccountBinding.inflate(layoutInflater) }

    override val isShowingSearchResult: Boolean
        get() = binding.searchResultRecyclerView.isVisible

    private val presenter by lazy {
        SearchAccountPresenter(
            this,
            SearchHistoryRepository(this),
            AccountRepository(this)
        )
    }

    private val historyAdapter by lazy { SearchHistoryAccountListAdapter(this, presenter) }
    private val accountGroupAdapter by lazy { SearchResultAccountGroupListAdapter(this) }

    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == RESULT_OK) {
            setResult(RESULT_OK, it.data)
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        with (binding) {
            root.layoutTransition.setDuration(100L)
            searchResultContainer.layoutTransition.setDuration(100L)

            btnBack.setOnClickListener { onBackPressed() }
            btnSearch.setOnClickListener { performClickSearchButton() }

            Utils.showSoftInput(edtSearch)
            edtSearch.addTextChangedListener(MyTextWatcher())
            edtSearch.setOnEditorActionListener { v, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    performClickSearchButton()
                }
                false
            }

            searchHistoryRecyclerView.adapter = historyAdapter
            searchResultRecyclerView.adapter = accountGroupAdapter

            btnDeleteAllSearchHistory.setOnClickListener {
                showDeleteAllHistoriesConfirmDialog()
            }
        }

        presenter.loadRecentSearchList()
        presenter.loadAccountGroups()
        presenter.loadAccountDetails()
    }

    override fun onBackPressed() {
        if (binding.edtSearch.text.toString().isNotBlank()) {
            binding.edtSearch.text?.clear()
        } else {
            super.onBackPressed()
        }
    }

    override fun setAccountSearchHistory(histories: List<SearchHistory>) {
        historyAdapter.submitList(histories)

        if (histories.isEmpty()) {
            showSearchResult()
        }
    }

    override fun setAccountGroups(groups: List<AccountGroup>) {
        accountGroupAdapter.setAccountGroups(this, groups)
    }

    override fun setAccountDetails(details: List<AccountDetail>) {

    }

    override fun showSearchHistory() = with(binding) {
        searchResultContainer.isVisible = false
        searchHistoryContainer.isVisible = true
    }

    override fun showSearchResult() = with(binding) {
        searchHistoryContainer.isVisible = false
        searchResultContainer.isVisible = true
        updateVisibleNoneSearchResultLabel(accountGroupAdapter.currentList.isEmpty())
    }

    override fun updateVisibleNoneSearchResultLabel(isVisible: Boolean) = with(binding) {
        noneSearchResultLabel.isVisible = isVisible
        searchResultRecyclerView.isVisible = isVisible.not()
    }

    override fun performClickSearchHistoryItem(history: SearchHistory) {
        binding.edtSearch.setText(history.value)
        binding.edtSearch.setSelection(history.value.length)
        showSearchResult()

        Utils.hideSoftInput(binding.edtSearch)
    }

    override fun startAccountDetailActivity(accountGroupId: String) {
        performClickSearchButton()

        val constants = AccountDetailActivity
        launcher.launch(Intent(this, AccountDetailActivity::class.java).apply {
            putExtra(constants.EXTRA_REQUEST, constants.LOAD)
            putExtra(constants.EXTRA_ACCOUNT_GROUP_ID, accountGroupId)
            putExtra(constants.EXTRA_ACCOUNT_ID, 0)
        })
    }

    override fun showDeleteAllHistoriesConfirmDialog() {
        BottomUpDialog.Builder(supportFragmentManager)
            .title("최근 검색 기록을 모두 삭제하시겠습니까?")
            .positiveButton {
                presenter.deleteAllHistories()
                historyAdapter.removeAllHistories()
                showSearchResult()
            }
            .show()
    }

    private fun performClickSearchButton() {
        val searchKeyword = binding.edtSearch.text.toString()
        if (searchKeyword.isNotBlank()) {
            val oldHistory = historyAdapter.currentList
                .find { it.value == searchKeyword }

            val newHistory = presenter.createNewSearchHistory(searchKeyword)

            if (oldHistory != null) {
                presenter.deleteSearchHistory(oldHistory)
            }

            historyAdapter.removeAndAdd(oldHistory, newHistory)
        }
        Utils.hideSoftInput(binding.edtSearch)
    }

    inner class MyTextWatcher : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(s: Editable?) {
            if (s.isNullOrBlank()) {
                showSearchHistory()
                accountGroupAdapter.filter("")
            } else {
                if (binding.searchResultRecyclerView.isVisible.not()) {
                    showSearchResult()
                }
                accountGroupAdapter.filter(s.toString())
            }
        }
    }
}