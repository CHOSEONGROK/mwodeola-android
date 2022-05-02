package com.jojo.android.mwodeola.presentation.account.datalist

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.*
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.CheckBox
import androidx.activity.result.ActivityResult
import androidx.constraintlayout.utils.widget.ImageFilterView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.animation.ArgbEvaluatorCompat
import com.jojo.android.mwodeola.R
import com.jojo.android.mwodeola.data.account.Account
import com.jojo.android.mwodeola.data.account.AccountGroup
import com.jojo.android.mwodeola.databinding.ActivityAccountGroupListBinding
import com.jojo.android.mwodeola.model.account.AccountRepository
import com.jojo.android.mwodeola.presentation.BaseFragment
import com.jojo.android.mwodeola.presentation.account.datalist.dialog.SelectAccountInGroupDialog
import com.jojo.android.mwodeola.presentation.account.datalist.dialog.SelectSnsGroupAndDetailBottomSheet
import com.jojo.android.mwodeola.presentation.account.datalist.dialog.SelectSnsGroupBottomSheet
import com.jojo.android.mwodeola.presentation.account.datalist.search.SearchAccountActivity
import com.jojo.android.mwodeola.presentation.account.detail.AccountDetailActivity
import com.jojo.android.mwodeola.presentation.common.BottomUpDialog
import com.jojo.android.mwodeola.presentation.common.ChildFab
import com.jojo.android.mwodeola.presentation.common.IncubatableFloatingActionButton
import com.jojo.android.mwodeola.presentation.drawer.interactive.DrawerContent
import com.jojo.android.mwodeola.presentation.drawer.interactive.DrawerOwner
import com.jojo.android.mwodeola.util.Log2
import com.jojo.android.mwodeola.util.dpToPixels
import java.util.*
import kotlin.math.max
import kotlin.math.min

class AccountGroupListFragment : BaseFragment(), DrawerContent, AccountGroupListContract.View {
    companion object { const val TAG = "AccountGroupListFragment" }

    private lateinit var drawerOwner: DrawerOwner

    private lateinit var binding: ActivityAccountGroupListBinding

    private val presenter: AccountGroupListContract.Presenter by lazy { AccountGroupListPresenter(this, AccountRepository(requireContext())) }
    private val listAdapter by lazy { AccountGroupListAdapter(this, presenter) }

    private var tracker: SelectionTracker<Long>? = null
    private val rvSmoothScrollHelper = RecyclerViewSmoothScrollHelper()

    override val colorThemeTransitionListener = ColorThemeTransitionListener()
    override val sharedWidgetsListener = SharedWidgetsListener()

    override val selectionTracker: SelectionTracker<Long>
        get() = tracker!!
    override val checkBoxSelectAll: CheckBox
        get() = drawerOwner.checkBoxSelectAll

//    private val btnDeleteBottomBehavior: OptionalHideBottomViewOnScrollBehavior<View>
//        get() = (binding.btnDeleteBottom.layoutParams as CoordinatorLayout.LayoutParams).behavior as OptionalHideBottomViewOnScrollBehavior

    private val supportFragmentManager: FragmentManager
        get() = requireActivity().supportFragmentManager

    private var isSelectionModeActivated = false
    private var isBackDropOpened = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        drawerOwner = requireActivity() as DrawerOwner
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = ActivityAccountGroupListBinding.inflate(inflater)
        .also { binding = it }
        .root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d(TAG, "onViewCreated(): rootView.layoutParams=${binding.root.layoutParams}")

        binding.root.layoutParams = (binding.root.layoutParams as ConstraintLayout.LayoutParams).apply {
            Log.d(TAG, "onViewCreated(): rootView.layoutParams.width=${width}")
            Log.d(TAG, "onViewCreated(): rootView.layoutParams.height=${height}")
            width = MATCH_PARENT
            height = MATCH_PARENT
        }

        initView()

        presenter.loadAllData()
    }

    override fun onActivityResult(result: ActivityResult) {
        if (result.resultCode != RESULT_OK)
            return

        val response = result.data?.getIntExtra(AccountDetailActivity.EXTRA_RESPONSE, -1)
        when (response) {
            AccountDetailActivity.CREATED -> {
                val newAccountGroup = result.data?.getSerializableExtra(AccountDetailActivity.EXTRA_ACCOUNT_GROUP) as? AccountGroup
                if (newAccountGroup != null) {
                    listAdapter.addItem(newAccountGroup)
                }
            }
            AccountDetailActivity.UPDATED -> {
                val updatedAccountGroup = result.data?.getSerializableExtra(AccountDetailActivity.EXTRA_ACCOUNT_GROUP) as? AccountGroup
                if (updatedAccountGroup != null) {
                    listAdapter.updateItem(updatedAccountGroup)
                }
            }
            AccountDetailActivity.DELETED -> {
                val accountGroupId = result.data?.getStringExtra(AccountDetailActivity.EXTRA_DELETED_ACCOUNT_GROUP_ID)
                if (accountGroupId != null) {
                    listAdapter.deleteItem(accountGroupId)
                }
            }
        }
    }

    override fun onBackPressed(): Boolean = when {
        listAdapter.isSelectionActivated -> {
            listAdapter.cancelSelectionMode()
            cancelSelectionMode()
            true
        }
        else -> false
    }

    override fun initView(): Unit = with (binding) {
        drawerOwner.swipeRefreshLayout.setOnRefreshListener {
            presenter.loadAllData()
        }

        drawerOwner.title = "모든 계정"
        drawerOwner.subtitle = "0개"

        recyclerView.let {
            it.visibility = View.GONE
            it.adapter = listAdapter
            it.addOnScrollListener(rvSmoothScrollHelper)
            it.itemAnimator = RecyclerViewItemAnimator()

            tracker = SelectionTracker.Builder(
                "selection_id",
                it,
                AccountGroupListAdapter.KeyProvider(),
                AccountGroupListAdapter.DetailsLookUp(it),
                StorageStrategy.createLongStorage()
            )
                .withSelectionPredicate(AccountGroupListAdapter.OnlyItemSelectionPredicate(listAdapter))
//                .withBandPredicate()
                .build()

            listAdapter.initSelectionTracker(tracker!!)
        }

        shimmer.visibility = View.VISIBLE
        shimmer.startShimmer()

        checkBoxSelectAll.setOnCheckedChangeListener { buttonView, isChecked ->
            if (buttonView.isPressed) {
                buttonView.post { listAdapter.selectAll(isChecked) }
            }
        }

        btnCancel.setOnClickListener {
            listAdapter.cancelSelectionMode()
            cancelSelectionMode()
        }

        btnDelete.setOnClickListener {
            showDeleteAccountGroupWarning(listAdapter.selectedAccountGroupIds.toList())
        }

        drawerOwner.incubatableFab.let {
            it.setOnFabListener(OnClickedFabListener())

            ChildFab.parent(it)
                .icon(R.drawable.add_account)
                .label("일반 그룹 + 일반 계정 생성")
                .build()
            ChildFab.parent(it)
                .icon(R.drawable.add_group)
                .label("일반 그룹 + SNS 계정 생성")
                .build()
            ChildFab.parent(it)
                .icon(R.drawable.social_network)
                .label("SNS 그룹 + SNS 계정 생성")
                .build()
        }
    }

    override fun showAllAccountGroups(accountGroups: List<AccountGroup>) {
        drawerOwner.subtitle = "${accountGroups.size}개"
        if (drawerOwner.swipeRefreshLayout.isRefreshing) {
            listAdapter.setData(accountGroups) {
                drawerOwner.swipeRefreshLayout.isRefreshing = false
            }
        } else {
            listAdapter.setData(accountGroups)
        }
    }

    override fun addNewAccountGroup(accountGroup: AccountGroup) {
//        listAdapter.addItem(accountGroup).let {
//            binding.recyclerView.smoothScrollToPosition(it)
//        }
    }

    override fun updateAccountGroup(accountGroup: AccountGroup) {
//        listAdapter.updateItem(accountGroup).let {
//            binding.recyclerView.smoothScrollToPosition(it)
//        }
    }

    override fun removeAccountGroups(ids: List<String>) {
        Log.e(TAG, "removeAccountGroups(): ids.size=${ids.size}")
        ids.forEachIndexed { i, id -> Log.i(TAG, "removeAccountGroups(): groupID[$i]=$id") }

        listAdapter.deleteSelectionItems()
        listAdapter.cancelSelectionMode()
        cancelSelectionMode()
    }

    override fun stopShimmerAndShowRecyclerView() = with(binding) {
        shimmer.stopShimmer()
        shimmer.isVisible = false
        recyclerView.isVisible = true
    }

    override fun startSelectionMode() = with(binding) {
        bottomController.isVisible = true
        bottomGuideline.setGuidelineEnd(72.dpToPixels(requireContext()))

        drawerOwner.startSelectionMode()
    }

    override fun cancelSelectionMode() = with(binding) {
        bottomController.isVisible = false
        bottomGuideline.setGuidelineEnd(12.dpToPixels(requireContext()))

        drawerOwner.title = "모든 계정"
        drawerOwner.cancelSelectionMode()
    }

    override fun updateTitleInSelectionMode(count: Int) {
        drawerOwner.title = "${count}개 선택 됨"
    }

    override fun smoothScrollWithEndAction(position: Int, endAction: Runnable?) {
        rvSmoothScrollHelper.smoothScrollWithEndAction(position, endAction)
    }

    override fun showDeleteAccountGroupWarning(idsOfAccountGroup: List<String>) {
        BottomUpDialog.Builder(supportFragmentManager)
            .title("정말 ${idsOfAccountGroup.size}개의 계정을 모두 삭제하시겠습니까?")
            .positiveButton { presenter.deleteData(idsOfAccountGroup) }
            .show()
    }

    override fun showSelectAccountInGroupDialog(accountGroupID: String) {
        SelectAccountInGroupDialog.Builder(supportFragmentManager)
            .setAccountGroupID(accountGroupID)
            .setOnItemSelectListener { startAccountDetailActivity(it) }
            .show()
    }

    override fun startCreateNewAccountActivity() {
        Log.d(TAG, "startCreateNewAccountActivity()")
        val constants = AccountDetailActivity

        drawerOwner.launchActivityForResult(AccountDetailActivity::class.java, Intent().apply {
            putExtra(constants.EXTRA_REQUEST, constants.CREATE)
        })
    }

    override fun startCreateNewAccountActivityWithSnsDetail(snsAccount: Account) {
        val constants = AccountDetailActivity

        drawerOwner.launchActivityForResult(AccountDetailActivity::class.java, Intent().apply {
            putExtra(constants.EXTRA_REQUEST, constants.CREATE)
            putExtra(constants.EXTRA_SNS_ACCOUNT, snsAccount)
        })
    }

    override fun startCreateNewAccountActivityForSnsAccount(snsCode: Int) {
        val constants = AccountDetailActivity

        drawerOwner.launchActivityForResult(AccountDetailActivity::class.java, Intent().apply {
            putExtra(constants.EXTRA_REQUEST, constants.CREATE)
            putExtra(constants.EXTRA_SNS_CODE, snsCode)
        })
    }

    override fun startAccountDetailActivity(account: Account) {
        val constants = AccountDetailActivity

        drawerOwner.launchActivityForResult(AccountDetailActivity::class.java, Intent().apply {
            putExtra(constants.EXTRA_REQUEST, constants.LOAD)
            putExtra(constants.EXTRA_ACCOUNT_GROUP_ID, account.own_group.id)
            putExtra(constants.EXTRA_ACCOUNT_ID, account.account_id)
        })
    }

    override fun startAccountDetailActivity(accountGroup: AccountGroup) {
        val constants = AccountDetailActivity

        drawerOwner.launchActivityForResult(AccountDetailActivity::class.java, Intent().apply {
            putExtra(constants.EXTRA_REQUEST, constants.LOAD)
            putExtra(constants.EXTRA_ACCOUNT_GROUP_ID, accountGroup.id)
        })
    }

    inner class SharedWidgetsListener : DrawerOwner.OnSharedWidgetsListener {
        override fun onDrawerOpened(drawerView: View) {}
        override fun onDrawerClosed(drawerView: View) {}

        override fun onDeleteClicked(view: View) {
            if (listAdapter.isSelectionActivated) {
                cancelSelectionMode()
                listAdapter.cancelSelectionMode()
            } else {
                startSelectionMode()
                updateTitleInSelectionMode(0)
                listAdapter.startSelectionMode()
            }
            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        }

        override fun onSearchClicked(view: View) {
            drawerOwner.launchActivityForResult(SearchAccountActivity::class.java, null)
        }

        override fun onFilterClicked(view: ImageFilterView) {
            if (isBackDropOpened) {

            } else {

            }
        }
    }

    inner class OnClickedFabListener : IncubatableFloatingActionButton.OnFabListener {
        override fun onClickedParentFab(isActivate: Boolean) {

        }

        override fun onClickedChildFab(position: Int, label: String?) {
            when (position) {
                0 -> startCreateNewAccountActivity()
                1 -> SelectSnsGroupAndDetailBottomSheet(requireActivity())
                    .setListener { snsAccount ->
                        startCreateNewAccountActivityWithSnsDetail(snsAccount)
                    }
                    .show()
                2 -> SelectSnsGroupBottomSheet(requireActivity(), listAdapter.snsCodeList)
                    .showWithSelectedListener { snsCode ->
                        startCreateNewAccountActivityForSnsAccount(snsCode)
                    }
            }
        }
    }

    inner class RecyclerViewSmoothScrollHelper : RecyclerView.OnScrollListener() {
        private val recyclerView: RecyclerView
            get() = binding.recyclerView
        private val layoutManager: LinearLayoutManager
            get() = binding.recyclerView.layoutManager as LinearLayoutManager

        private var endAction: Runnable? = null

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
//            Log.d(TAG, "onScrollStateChanged(): $newState")
            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                if (endAction != null) {
                    endAction?.run()
                    endAction = null
                }
            }
        }

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
        }

        fun smoothScrollWithEndAction(position: Int, endAction: Runnable? = null) {
            val firstVisiblePosition = layoutManager.findFirstCompletelyVisibleItemPosition()
            val lastVisiblePosition = layoutManager.findLastCompletelyVisibleItemPosition()
            val itemCount = recyclerView.adapter!!.itemCount

//            Log.i(TAG, "smoothScrollWithEndAction(): target=$position, first=$firstVisiblePosition, last=$lastVisiblePosition")

            when (position) {
                in 0 until firstVisiblePosition -> {
                    this.endAction = endAction
                    recyclerView.smoothScrollToPosition(max(position - 5, 0))
                }
                in firstVisiblePosition..lastVisiblePosition -> {
                    endAction?.run()
                }
                in lastVisiblePosition..Int.MAX_VALUE -> {
                    this.endAction = endAction
                    recyclerView.smoothScrollToPosition(min(position + 5, itemCount))
                }
            }

        }
    }

    inner class ColorThemeTransitionListener : DrawerOwner.OnColorThemeTransitionListener {

        private val argbEvaluator = ArgbEvaluatorCompat.getInstance()

        override fun onTransitionStarted(isDarkTheme: Boolean, backgroundColor: Int) {}
        override fun onTransitionCompleted(isDarkTheme: Boolean, backgroundColor: Int) {
            listAdapter.isDarkTheme = isDarkTheme

            val layoutManager = binding.recyclerView.layoutManager as LinearLayoutManager

            val firstPosition = layoutManager.findFirstVisibleItemPosition()
            val lastPosition = layoutManager.findLastVisibleItemPosition()

            for (i in firstPosition..lastPosition) {
                val viewHolder = binding.recyclerView.findViewHolderForLayoutPosition(i) as? AccountGroupListAdapter.DayAndNightThemeViewHolder
                    ?: continue

                viewHolder.setTheme(isDarkTheme)
            }
        }
        override fun onTransitionChanged(progress: Float, backgroundColor: Int) {
            val layoutManager = binding.recyclerView.layoutManager as LinearLayoutManager

            val firstPosition = layoutManager.findFirstVisibleItemPosition()
            val lastPosition = layoutManager.findLastVisibleItemPosition()

            val textColor = argbEvaluator.evaluate(progress, Color.WHITE, Color.BLACK)

            for (i in firstPosition..lastPosition) {
                val viewHolder = binding.recyclerView.findViewHolderForLayoutPosition(i) as? AccountGroupListAdapter.DayAndNightThemeViewHolder
                    ?: continue

                viewHolder.onTransitionChange(progress, textColor)
            }

//            if (binding.bottomController.isVisible) {
//                binding.bottomControllerBackground.setCardBackgroundColor(backgroundColor)
//                binding.btnCancel.setTextColor(textColor)
//                binding.btnDelete.setTextColor(textColor)
//            }
        }

        override fun onThemeChanged(isDarkTheme: Boolean) {
            listAdapter.isDarkTheme = isDarkTheme
        }
    }
}