package com.jojo.android.mwodeola.presentation.account.datalist

import android.animation.ValueAnimator
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.constraintlayout.widget.Guideline
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.ItemKeyProvider
import androidx.recyclerview.selection.MutableSelection
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.divider.MaterialDivider
import com.google.android.material.shape.CornerFamily
import com.jojo.android.mwodeola.R
import com.jojo.android.mwodeola.data.account.AccountGroup
import com.jojo.android.mwodeola.databinding.ActivityAccountGroupListRvHeaderBinding
import com.jojo.android.mwodeola.databinding.ActivityAccountGroupListRvItemBinding
import com.jojo.android.mwodeola.presentation.common.IconView
import com.jojo.android.mwodeola.util.dpToPixels

class AccountGroupListAdapter(
    private val view: AccountGroupListContract.View,
    private val presenter: AccountGroupListContract.Presenter
) : ListAdapter<RvElement, RecyclerView.ViewHolder>(RvElement.DIFF_CALLBACK) {
    companion object {
        const val TAG = "AccountGroupListAdapter"

        private const val HEADER = 0
        private const val ITEM = 1

        private const val PAYLOAD_SELECTION_STARTED = "Selection-Started"
        private const val PAYLOAD_SELECTION_CHANGED = SelectionTracker.SELECTION_CHANGED_MARKER
        private const val PAYLOAD_SELECTION_FINISHED = "Selection-Finished"
        private const val PAYLOAD_UPDATE_CONTAINER_SHAPE = "Container-Shape"
    }

    var isSelectionActivated = false
        private set

    var isDarkTheme: Boolean = true

    private val selectionTracker: SelectionTracker<Long>
        get() = view.selectionTracker

    val accountGroupList: List<AccountGroup>
        get() = currentList.filter { it.isItem }
            .map { it.item!! }
            .distinctBy { it.id }

    val isExistsSnsGroup: Boolean
        get() = currentList.any { it.isItem && it.item!!.isSnsGroup }

    val snsAccountGroupList: List<AccountGroup>
        get() = currentList.filter { it.isItem && it.item!!.isSnsGroup }
            .map { it.item!! }
            .distinctBy { it.id }

    val allAccountGroupIds: List<String>
        get() = currentList.filter { it.isItem }
            .map { it.item!!.id }
            .distinct()

    val snsCodeList: List<Int>
        get() = currentList.filter { it.isItem && it.item!!.isSnsGroup }
            .map { it.item!!.sns }
            .distinct()

    val allItemIds = mutableListOf<Long>()
    val selectedAccountGroupIds = hashSetOf<String>()

    private var onlyItemCount: Int = 0
    private var realItemCount: Int = 0

    private val packageManager: PackageManager
        get() = (view as AccountGroupListFragment).requireActivity().packageManager

    override fun onCurrentListChanged(
        previousList: MutableList<RvElement>, currentList: MutableList<RvElement>) {
        Log.d(TAG, "onCurrentListChanged(): previous.size=${previousList.size}, current.size=${currentList.size}")
        onlyItemCount = currentList.count { it.isItem }
        realItemCount = currentList.filter { it.isItem }.distinctBy { it.item?.id }.size
        allItemIds.clear()
        allItemIds.addAll(
            currentList.mapIndexed { index, _ -> index.toLong() }
                .filter { getItemViewType(it.toInt()) == ITEM }
        )
    }

    override fun getItemCount(): Int = currentList.size

    override fun getItemViewType(position: Int): Int =
        if (currentList[position].isHeader) HEADER else ITEM

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder = when (viewType) {
        HEADER -> HeaderViewHolder.newInstance(parent)
        ITEM -> ItemViewHolder.newInstance(parent, selectionTracker)
        else -> throw IllegalArgumentException("viewType=$viewType")
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads)
        } else {
            if (holder is ItemViewHolder) {
                when (payloads[0]) {
                    PAYLOAD_SELECTION_STARTED -> {
                        holder.showCheckBox(true)
                    }
                    PAYLOAD_SELECTION_CHANGED -> {
                        holder.changeSelection()
                        holder.itemView.post { findSameItems(position) } // 같은 항목 찾기.

                        val accountGroupId = currentList[position].item!!.id

                        val isSelected = selectionTracker.isSelected(position.toLong())
                        if (isSelected) {
                            selectedAccountGroupIds.add(accountGroupId)
                        } else {
                            selectedAccountGroupIds.remove(accountGroupId)
                        }

                        view.updateTitleInSelectionMode(selectedAccountGroupIds.size)
                    }
                    PAYLOAD_SELECTION_FINISHED -> {
                        val front = currentList[position - 1]
                        val rear = currentList.getOrNull(position + 1)
                        holder.changeSelection()
                        holder.updateContainerShape(front, rear)
                        holder.hideCheckBox(true)
                    }
                    PAYLOAD_UPDATE_CONTAINER_SHAPE -> {
                        val front = currentList[position - 1]
                        val rear = currentList.getOrNull(position + 1)
                        holder.updateContainerShape(front, rear)
                    }
                }
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int): Unit = when (holder) {
        is HeaderViewHolder -> {
            val header = currentList[position].header!!

            holder.bind(header)
            holder.setTheme(isDarkTheme)
        }

        is ItemViewHolder -> {
            val element = currentList[position]
            val item = element.item!!
            val front = currentList[position - 1]
            val rear = currentList.getOrNull(position + 1)

            holder.bind(element)
            holder.setTheme(isDarkTheme)
            holder.updateContainerShape(front, rear)

            if (isSelectionActivated) {
                holder.showCheckBox(false)
                holder.changeSelection()
            } else {
                holder.hideCheckBox(false)
                holder.changeSelection()
            }

            holder.cardView.setOnClickListener {
                Log.w(TAG, "itemView.onClicked(): position=$position, adapterPosition=${holder.adapterPosition}")
                Log.w(TAG, "itemView.onClicked(): $item")
                if (isSelectionActivated) {
                    selectionTracker.select(holder.adapterPosition.toLong())
                } else {
                    if (item.detail_count > 1) {
                        view.showSelectAccountInGroupDialog(item.id)
                    } else {
                        view.startAccountDetailActivity(item)
                    }
                }
            }
        }

        else -> throw IllegalArgumentException("holder=$holder, position=$position")
    }

    @JvmName("setSelectionTracker1")
    fun initSelectionTracker(tracker: SelectionTracker<Long>) {
        tracker.addObserver(object : SelectionTracker.SelectionObserver<Long>() {
            override fun onItemStateChanged(key: Long, selected: Boolean) {
                Log.d(TAG, "onItemStateChanged(): ${selectionTracker.selection}")

                val selectionSize = selectionTracker.selection.size()

                if (isSelectionActivated) {
                    view.checkBoxSelectAll.isChecked = (selectionSize == onlyItemCount)
//                    view.changeDeleteButtonOf(selectionSize)
                } else {
                    if (!selectionTracker.selection.isEmpty) {
                        // selection tracker started
                        startSelectionMode()
                        view.startSelectionMode()
                        view.updateTitleInSelectionMode(1)
                    }
                }
            }
        })
    }

    fun setData(data: List<AccountGroup>, commitCallBack: Runnable? = null) {
        val newList = DataHelper.create(packageManager, data)
        view.stopShimmerAndShowRecyclerView()
        submitList(newList, commitCallBack)
    }

    fun addItem(accountGroup: AccountGroup) {
        val newList = DataHelper.old(packageManager, currentList)
            .add(accountGroup)
            .get()
        val newItemIndex = newList.indexOfLast { it.item == accountGroup }

        view.smoothScrollWithEndAction(newItemIndex) {
            submitList(newList) {
                notifyItemRangeChanged(0, currentList.size, PAYLOAD_UPDATE_CONTAINER_SHAPE)
            }
        }
    }

    fun updateItem(accountGroup: AccountGroup) {
        Log.d(TAG, "updateItem(): $accountGroup")

//        val updatedIndices = mutableListOf<Int>()
//        for (index in currentList.indices) {
//            val element = currentList[index]
//            if (element.item?.id == accountGroup.id) {
//                element.item = accountGroup
//                updatedIndices.add(index)
//            }
//        }
//
//        view.smoothScrollWithEndAction(updatedIndices.last()) {
//            submitList(currentList) {
//                updatedIndices.sortedDescending().forEach {
//                    Log.i(TAG, "updateItem(): notifyItemChanged($it)")
//                    notifyItemChanged(it)
//                }
//            }
//        }
    }

    fun selectAllOrNone(): Boolean {
        if (isSelectionActivated) {
            val selected = (selectionTracker.selection.size() < onlyItemCount)
            if (selected) {
                selectedAccountGroupIds.addAll(allAccountGroupIds)
            } else {
                selectedAccountGroupIds.clear()
            }
            selectionTracker.setItemsSelected(allItemIds, selected)
            return selected
        }
        return false
    }

    fun selectAll(select: Boolean) {
        if (isSelectionActivated) {
            if (select) {
                selectedAccountGroupIds.addAll(allAccountGroupIds)
            } else {
                selectedAccountGroupIds.clear()
            }

            selectionTracker.setItemsSelected(allItemIds, select)
        }
    }

    fun selectAll() {
        if (isSelectionActivated) {
            selectedAccountGroupIds.addAll(allAccountGroupIds)
            selectionTracker.setItemsSelected(allItemIds, true)
        }
    }

    fun unselectAll() {
        if (isSelectionActivated) {
            selectedAccountGroupIds.clear()
            selectionTracker.setItemsSelected(allItemIds, false)
        }
    }

    fun deleteItem(accountGroupId: String) {
        val newList = DataHelper.old(packageManager, currentList)
            .removeBy(accountGroupId)
            .get()

        submitList(newList) {
            notifyItemRangeChanged(0, currentList.size, PAYLOAD_UPDATE_CONTAINER_SHAPE)
        }
    }

    fun deleteSelectionItems() {
        val selection = MutableSelection<Long>()
        selectionTracker.copySelection(selection)
        selectionTracker.clearSelection()
        Log.d(TAG, "deleteSelectionItems(): ${selection.sortedDescending()}")

        val newList = DataHelper.old(packageManager, currentList)
            .removeAllAt(selection)
            .get()

        submitList(newList) {
            cancelSelectionMode()
        }
    }

    fun startSelectionMode() {
        isSelectionActivated = true
        notifyItemRangeChanged(0, currentList.size, PAYLOAD_SELECTION_STARTED)
    }

    fun cancelSelectionMode() {
        isSelectionActivated = false
        selectedAccountGroupIds.clear()
        selectionTracker.clearSelection()
        notifyItemRangeChanged(0, currentList.size, PAYLOAD_SELECTION_FINISHED)
    }

    /** 동일한 AccountGroup 찾아서 Selection State 변경 */
    private fun findSameItems(position: Int) {
        val selectedItem = currentList[position]
        val isSelected = selectionTracker.isSelected(position.toLong())

        currentList.forEachIndexed { index, element ->
            if (isSelected) {
                if (!selectionTracker.selection.contains(index.toLong()) &&
                    element.isItem && element.item!!.id == selectedItem.item!!.id) {
                    selectionTracker.select(index.toLong())
                }
            } else {
                if (selectionTracker.selection.contains(index.toLong()) &&
                    element.isItem && element.item!!.id == selectedItem.item!!.id) {
                    selectionTracker.deselect(index.toLong())
                }
            }
        }
    }

    interface DayAndNightThemeViewHolder {
        fun setTheme(isDarkTheme: Boolean)
        fun onTransitionChange(progress: Float, textColor: Int)
    }

    interface DetailsLookUpProvider {
        fun getItemDetails(): ItemDetailsLookup.ItemDetails<Long>
    }

    class HeaderViewHolder(
        binding: ActivityAccountGroupListRvHeaderBinding
    ) : RecyclerView.ViewHolder(binding.root), DetailsLookUpProvider, DayAndNightThemeViewHolder {
        private val details = ItemDetails()

        private val tvTitle: TextView = binding.tvHeaderTitle

        fun bind(header: RvElement.Header) {
            tvTitle.text = header.title
        }

        override fun setTheme(isDarkTheme: Boolean) {
            tvTitle.setTextColor(if (isDarkTheme) Color.WHITE else Color.BLACK)
        }

        override fun onTransitionChange(progress: Float, textColor: Int) {
            tvTitle.setTextColor(textColor)
        }

        override fun getItemDetails(): ItemDetailsLookup.ItemDetails<Long> = details

        inner class ItemDetails : ItemDetailsLookup.ItemDetails<Long>() {
            override fun getPosition(): Int = adapterPosition
            override fun getSelectionKey(): Long = adapterPosition.toLong()
        }

        companion object {
            fun newInstance(parent: ViewGroup): HeaderViewHolder =
                HeaderViewHolder(
                    ActivityAccountGroupListRvHeaderBinding.inflate(
                        LayoutInflater.from(parent.context), parent, false))
        }
    }

    class ItemViewHolder(
        binding: ActivityAccountGroupListRvItemBinding,
        private val selectionTracker: SelectionTracker<Long>
    ) : RecyclerView.ViewHolder(binding.root), DetailsLookUpProvider, DayAndNightThemeViewHolder {
        private val context = binding.root.context
        private val radius = 26.dpToPixels(context).toFloat()
        private val selectedBackColor = getColor(context, R.color.red100)

        private val checkBoxDarkThemeColor = ColorStateList.valueOf(getColor(context, R.color.white))
        private val checkBoxLightThemeColor = ColorStateList.valueOf(getColor(context, R.color.day_theme_color))
        private val guideBeginWhenCheckBoxHidden = 84.dpToPixels(context)
        private val guideBeginWhenCheckBoxShown = 138.dpToPixels(context)
        private val checkBoxAnimatorShown = createCheckBoxVisibleAnimator(guideBeginWhenCheckBoxHidden, guideBeginWhenCheckBoxShown)
        private val checkBoxAnimatorHidden = createCheckBoxVisibleAnimator(guideBeginWhenCheckBoxShown, guideBeginWhenCheckBoxHidden)

        private var isDarkTheme = true

        private val details = ItemDetails()

        val cardView: MaterialCardView = binding.cardView
        private val checkBox: MaterialCheckBox = binding.checkBox
        private val guideLine: Guideline = binding.startGuideline
        private val icon: IconView = binding.icon
        private val snsMark: TextView = binding.snsMark
        private val tvAccountName: TextView = binding.tvAccountName
        private val divider: MaterialDivider = binding.divider

        private val cardViewMinAlpha = cardView.alpha

        override fun setTheme(isDarkTheme: Boolean) {
            this.isDarkTheme = isDarkTheme

            cardView.alpha = if (isDarkTheme) cardViewMinAlpha else 1f
            tvAccountName.setTextColor(if (isDarkTheme) Color.WHITE else Color.BLACK)
            checkBox.buttonTintList = if (isDarkTheme) checkBoxDarkThemeColor else checkBoxLightThemeColor
        }

        override fun onTransitionChange(progress: Float, textColor: Int) {
            cardView.alpha = cardViewMinAlpha.coerceAtLeast(progress)
            tvAccountName.setTextColor(textColor)
            when {
                progress < 0.5 && isDarkTheme.not() -> {
                    isDarkTheme = true
                    checkBox.buttonTintList = checkBoxDarkThemeColor
                }
                progress >= 0.5 && isDarkTheme -> {
                    isDarkTheme = false
                    checkBox.buttonTintList = checkBoxLightThemeColor
                }
            }
        }

        fun bind(element: RvElement) {
            val accountGroup = element.item!!

            when (accountGroup.icon_type) {
                AccountGroup.ICON_TYPE_TEXT ->
                    icon.setText(accountGroup.group_name)
                AccountGroup.ICON_TYPE_IMAGE ->
                    icon.setText(accountGroup.group_name)
                AccountGroup.ICON_TYPE_INSTALLED_APP_LOGO -> {
                    if (element.icon != null) {
                        icon.setIconImage(element.icon)
                    } else {
                        icon.setText(accountGroup.group_name)
                    }
                }
                AccountGroup.ICON_TYPE_SNS ->
                    icon.setSnsGroupIcon(accountGroup.sns)
            }

            tvAccountName.text = accountGroup.group_name

            snsMark.visibility =
                if (accountGroup.isSnsGroup) View.VISIBLE
                else View.GONE
        }

        fun showCheckBox(isAnimate: Boolean) {
            if (isAnimate) {
                checkBoxAnimatorShown.start()
            } else {
                checkBox.isVisible = true
                guideLine.setGuidelineBegin(guideBeginWhenCheckBoxShown)
            }
        }

        fun hideCheckBox(isAnimate: Boolean) {
            if (isAnimate) {
                checkBoxAnimatorHidden.start()
            } else {
                checkBox.isVisible = false
                guideLine.setGuidelineBegin(guideBeginWhenCheckBoxHidden)
            }
        }

        fun changeSelection() {
            val isSelected = selectionTracker.isSelected(adapterPosition.toLong()) ?: false

            checkBox.isChecked = isSelected
            itemView.isActivated = isSelected

            val backgroundColor =
                if (isSelected) selectedBackColor
                else Color.WHITE
            cardView.setCardBackgroundColor(backgroundColor)
        }

        fun updateContainerShape(front: RvElement, rear: RvElement?) {
            if (rear == null) {
                if (front.isHeader) {
                    updateCardCornerShape(radius, radius, radius, radius)
                } else {
                    updateCardCornerShape(0f, 0f, radius, radius)
                }
            } else {
                if (front.isHeader && rear.isHeader) {
                    updateCardCornerShape(radius, radius, radius, radius)
                } else if (front.isHeader && rear.isItem) {
                    updateCardCornerShape(radius, radius, 0f, 0f)
                } else if (front.isItem && rear.isItem) {
                    updateCardCornerShape(0f, 0f, 0f, 0f)
                } else if (front.isItem && rear.isHeader) {
                    updateCardCornerShape(0f, 0f, radius, radius)
                }
            }
            updateDividerVisible(front, rear)
        }

        private fun updateCardCornerShape(topLeft: Float, topRight: Float, bottomLeft: Float, bottomRight: Float) {
            cardView.shapeAppearanceModel = cardView.shapeAppearanceModel.toBuilder()
                .setTopLeftCorner(CornerFamily.ROUNDED, topLeft)
                .setTopRightCorner(CornerFamily.ROUNDED, topRight)
                .setBottomLeftCorner(CornerFamily.ROUNDED, bottomLeft)
                .setBottomRightCorner(CornerFamily.ROUNDED, bottomRight)
                .build()
        }

        private fun updateDividerVisible(front: RvElement, rear: RvElement?) {
            divider.visibility =
                if (rear?.isItem == true) View.VISIBLE
                else View.GONE
        }

        private fun getColor(context: Context, @ColorRes resId: Int): Int =
            ResourcesCompat.getColor(context.resources, resId, null)

        private fun createCheckBoxVisibleAnimator(from: Int, to: Int): ValueAnimator =
            ValueAnimator.ofInt(from, to).apply {
                interpolator = DecelerateInterpolator()
                duration = 300
                addUpdateListener { guideLine.setGuidelineBegin(it.animatedValue as Int) }
                if (from < to) doOnStart { checkBox.isVisible = true }
                else doOnEnd { checkBox.isVisible = false }
            }

        override fun getItemDetails(): ItemDetailsLookup.ItemDetails<Long> = details

        inner class ItemDetails : ItemDetailsLookup.ItemDetails<Long>() {
            override fun getPosition(): Int = adapterPosition
            override fun getSelectionKey(): Long = adapterPosition.toLong()
        }

        companion object {
            fun newInstance(parent: ViewGroup, selectionTracker: SelectionTracker<Long>): ItemViewHolder =
                ItemViewHolder(
                    ActivityAccountGroupListRvItemBinding.inflate(
                        LayoutInflater.from(parent.context), parent, false),
                    selectionTracker
                )
        }
    }

    /**
     * SelectionTracker Process
     * (1) DetailsLookUp.getItemDetails() ->
     * (2) HeaderViewHolder/ItemViewHolder.getItemDetails() ->
     * (3) KeyProvider.getKey() ->
     * (4) KeyProvider.getPosition() ->
     * (5) OnlyItemSelectionPredicate.canSetStateForKey() ->
     * (5) OnlyItemSelectionPredicate.canSetStateAtPosition()
     * */
    class OnlyItemSelectionPredicate(private val adapter: AccountGroupListAdapter) : SelectionTracker.SelectionPredicate<Long>() {
        override fun canSetStateForKey(key: Long, nextState: Boolean): Boolean =
            adapter.getItemViewType(key.toInt()) == ITEM
        override fun canSetStateAtPosition(position: Int, nextState: Boolean): Boolean =
            adapter.getItemViewType(position) == ITEM
        override fun canSelectMultiple(): Boolean = true
    }

    class KeyProvider : ItemKeyProvider<Long>(SCOPE_MAPPED) {
        override fun getKey(position: Int): Long = position.toLong()
        override fun getPosition(key: Long): Int = key.toInt()
    }

    class DetailsLookUp(private val recyclerView: RecyclerView) : ItemDetailsLookup<Long>() {

        override fun getItemDetails(event: MotionEvent): ItemDetails<Long>? {
            recyclerView.findChildViewUnder(event.x, event.y)?.let {
                val viewHolder = recyclerView.getChildViewHolder(it)
                return if (viewHolder is DetailsLookUpProvider) viewHolder.getItemDetails()
                else null
            }
            return null
        }
    }
}