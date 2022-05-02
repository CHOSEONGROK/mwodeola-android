package com.jojo.android.mwodeola.presentation.account.datalist

import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.util.Log
import com.jojo.android.mwodeola.data.account.AccountGroup
import com.jojo.android.mwodeola.util.HangulUtils
import com.jojo.android.mwodeola.util.maxOf
import com.jojo.android.mwodeola.util.removeAllIf
import java.lang.StringBuilder

class AccountGroupListRvElementCollection(
    private val packageManager: PackageManager
) {

    companion object {
        const val TAG = "AccountGroupListRvElementCollection"

        const val FAVORITE_GROUP_HEADER_TITLE = "★  즐겨찾기"
        const val MOST_VIEWS_GROUP_HEADER_TITLE = "#  자주 사용하는 계정"
        const val SNS_ACCOUNT_GROUP_HEADER_TITLE = "#  SNS 계정"

        const val HEADER = 0
        const val ITEM = 1
    }

    interface RvElement {
        val viewType: Int
        fun equalsWith(other: RvElement): Boolean
    }

    interface RvHeader : RvElement {
        override val viewType: Int
            get() = HEADER

        val title: String
    }

    interface RvItem : RvElement {
        override val viewType: Int
            get() = ITEM

        var isSelected: Boolean

        val accountGroup: AccountGroup
        val accountGroupID: String

        val installedAppIcon: Drawable?
        val name: String
    }

    class Header(private val _title: String): RvHeader {
        override val title: String
            get() = _title

        override fun equalsWith(other: RvElement): Boolean =
            (other is RvHeader && title == other.title)
    }

    class Item constructor(
        override val name: String,
        override val accountGroup: AccountGroup,
        override val installedAppIcon: Drawable?
    ) : RvItem {
        private var _isSelected: Boolean = false
        override var isSelected: Boolean
            get() = _isSelected
            set(value) { _isSelected = value }

        override val accountGroupID: String
            get() = accountGroup.id

        override fun equalsWith(other: RvElement): Boolean =
            (other is RvItem && accountGroupID == other.accountGroupID)
    }

    private val favoriteItems = arrayListOf<Item>()
    private val mostViewItems = arrayListOf<Item>()
    private val snsAccountItems = arrayListOf<Item>()
    private val map = hashMapOf<Char, ArrayList<Item>>()

    private val _elements = arrayListOf<RvElement>()
    val elements: List<RvElement>
        get() = _elements
    val size: Int
        get() = _elements.size
    val pureItemSize: Int
        get() = _elements.count { it is RvItem } -
                (favoriteItems.size + mostViewItems.size + snsAccountItems.size)
    val isNotEmpty: Boolean
        get() = _elements.isNotEmpty()

    var countOfSelectedItems = 0

    val idsOfSelectedItem: List<String>
        get() = map
            .flatMap { it.value }
            .filter { it.isSelected }
            .map { it.accountGroup.id }

    val accountGroupList: List<AccountGroup>
        get() = map.values.flatMap { itemList ->
            itemList.map { item -> item.accountGroup }
        }

    operator fun get(index: Int): RvElement = elements[index]

    fun setData(data: List<AccountGroup>) {
        Log.i(TAG, "setData()")
//        data.forEachIndexed { index, group ->
//            Log.i(TAG, "[$index]: groupID=${group.id}, name=${group.group_name}, isSNS=${group.isSnsGroup}")
//        }
        favoriteItems.clear()
        mostViewItems.clear()
        snsAccountItems.clear()
        map.clear()

        // 즐겨찾기
        data.filter { it.is_favorite }
            .sortedBy { it.group_name }
            .forEach { favoriteItems.add(createNewItem(it)) }

        // 자주 사용하는 계정
        data.maxOf(5) { it.total_views }
            .forEach {
                if (it.total_views >= 10) {
                    mostViewItems.add(createNewItem(it))
                }
            }

        // SNS 그룹 계정
        data.filter { it.isSnsGroup }
            .sortedBy { it.group_name }
            .forEach { snsAccountItems.add(createNewItem(it)) }

        // 전체 계정 => Map
        data.groupBy { HangulUtils.getFirstCharOfInitialSound(it.group_name) }.entries
            .forEach { entry ->
                val items = arrayListOf<Item>()

                entry.value
                    .sortedBy { it.group_name }
                    .forEach { items.add(createNewItem(it)) }

                map[entry.key] = items
            }

        serialize(forAdded = false, forRemoved = false)
    }

    fun addItem(accountGroup: AccountGroup): List<Int> {
        // 즐겨찾기(isFavorite == true)
        if (accountGroup.is_favorite) {
            favoriteItems.add(createNewItem(accountGroup))
            favoriteItems.sortBy { it.accountGroup.group_name }
        }

        // SNS 그룹 계정
        if (accountGroup.isSnsGroup) {
            snsAccountItems.add(createNewItem(accountGroup))
            snsAccountItems.sortedBy { it.accountGroup.group_name }
        }

        val initialSound = HangulUtils.getFirstCharOfInitialSound(accountGroup.group_name)

        val header: ArrayList<Item>? = map[initialSound]
        if (header == null) {
            map[initialSound] = arrayListOf<Item>().apply { add(createNewItem(accountGroup)) }
        } else {
            header.add(createNewItem(accountGroup))
            header.sortBy { it.accountGroup.group_name }
        }

        return serialize(forAdded = true, forRemoved = false)
    }

    fun addItemInFavoriteGroup(accountGroup: AccountGroup): List<Int> {
//        if (!accountGroup.isFavorite) return emptyList()
//
//        favoriteItems.add(createNewItem(accountGroup))
//        favoriteItems.sortBy { it.accountGroup.name }
//
//        return serialize(forAdded = true, forRemoved = false)
        return emptyList()
    }

    fun removeItem(accountGroupId: String): List<Int> {
        favoriteItems.removeAllIf { it.accountGroupID == accountGroupId }
        mostViewItems.removeAllIf { it.accountGroupID == accountGroupId }
        snsAccountItems.removeAllIf { it.accountGroupID == accountGroupId }
        map.values.forEach {
            it.removeAllIf { item -> item.accountGroupID == accountGroupId }
        }

        return serialize(forAdded = false, forRemoved = true)
    }

    fun removeAccountDetail(accountGroupId: String): List<Int> {
        var isEmptyAccountGroup = false

        map.values.forEach {
            val item = it.firstOrNull { item -> item.accountGroupID == accountGroupId }
            if (item != null) {
                item.accountGroup.detail_count--
                isEmptyAccountGroup = (item.accountGroup.detail_count == 0)
                return@forEach
            }
        }

        if (!isEmptyAccountGroup) {
            favoriteItems.firstOrNull { it.accountGroupID == accountGroupId }
                ?.let { it.accountGroup.detail_count-- }
            mostViewItems.firstOrNull { it.accountGroupID == accountGroupId }
                ?.let { it.accountGroup.detail_count-- }
            snsAccountItems.firstOrNull { it.accountGroupID == accountGroupId }
                ?.let { it.accountGroup.detail_count-- }

            return emptyList()
        }

        favoriteItems.removeAllIf { it.accountGroup.id == accountGroupId }
        mostViewItems.removeAllIf { it.accountGroup.id == accountGroupId }
        snsAccountItems.removeAllIf { it.accountGroup.id == accountGroupId }

        for (pair in map.toList()) {
            val header = pair.first
            val items = pair.second

            items.removeAllIf { it.isSelected }

            if (items.isEmpty()) {
                map.remove(header)
            }
        }

        return serialize(forAdded = false, forRemoved = true)
    }

    fun removeAllSelectedItems(): List<Int> {
        favoriteItems.removeAllIf { it.isSelected }
        mostViewItems.removeAllIf { it.isSelected }
        snsAccountItems.removeAllIf { it.isSelected }

        for (pair in map.toList()) {
            val header = pair.first
            val items = pair.second

            items.removeAllIf { it.isSelected }

            if (items.isEmpty()) {
                map.remove(header)
            }
        }

        return serialize(forAdded = false, forRemoved = true)
    }

    fun selectItem(accountGroupId: String, isSelect: Boolean): List<Int> {
        val results = arrayListOf<Int>()

        _elements.forEachIndexed { index, element ->
            if (element is Item && element.accountGroup.id == accountGroupId) {
                element.isSelected = isSelect
                results.add(index)
            }
        }

        countOfSelectedItems += if (isSelect) 1 else -1
        return results
    }

    fun selectAllItems(isSelect: Boolean): List<Int> {
        val results = arrayListOf<Int>()

        _elements.forEachIndexed { index, element ->
            if (element is Item && element.isSelected != isSelect) {
                element.isSelected = isSelect
                results.add(index)
            }
        }

        countOfSelectedItems =
            if (isSelect) pureItemSize
            else 0

        return results
    }

    fun indexOfFirst(predicate: (RvElement) -> Boolean): Int = elements.indexOfFirst(predicate)
    fun indexOfLast(predicate: (RvElement) -> Boolean): Int = elements.indexOfLast(predicate)

    private fun createNewHeader(title: String): Header =
        Header(title)

    private fun createNewItem(accountGroup: AccountGroup): Item {
        var installedAppIcon: Drawable? = null

        if (accountGroup.icon_type == AccountGroup.ICON_TYPE_INSTALLED_APP_LOGO &&
            accountGroup.app_package_name != null) {
            try {
                installedAppIcon = packageManager.getApplicationIcon(accountGroup.app_package_name!!)
            } catch (e: PackageManager.NameNotFoundException) {
                Log.w(TAG, "createNewItem(): e=$e")
            }
        }
        return Item(accountGroup.group_name, accountGroup, installedAppIcon)
    }

    private fun serialize(forAdded: Boolean, forRemoved: Boolean): List<Int> {
        val oldElements: ArrayList<RvElement>? =
            if (forAdded || forRemoved) arrayListOf<RvElement>().apply { addAll(_elements) }
            else null

        _elements.clear()

        // 즐겨찾기 그룹
        if (favoriteItems.isNotEmpty()) {
            _elements.add(createNewHeader(FAVORITE_GROUP_HEADER_TITLE))
            _elements.addAll(favoriteItems)
        }

        // 자주 사용하는 계정 그룹
        if (mostViewItems.isNotEmpty()) {
            _elements.add(createNewHeader(MOST_VIEWS_GROUP_HEADER_TITLE))
            _elements.addAll(mostViewItems)
        }

        // SNS 계정 그룹
        if (snsAccountItems.isNotEmpty()) {
            _elements.add(createNewHeader(SNS_ACCOUNT_GROUP_HEADER_TITLE))
            _elements.addAll(snsAccountItems)
        }

        map.entries.forEach {
            _elements.add(createNewHeader(it.key.toString()))
            _elements.addAll(it.value)
        }

        return guess(oldElements, _elements, forAdded, forRemoved)
    }

    private fun guess(oldList: ArrayList<RvElement>?, newList: List<RvElement>, forAdded: Boolean, forRemoved: Boolean): List<Int> {
        if (oldList == null) return emptyList()
        if (!forAdded && !forRemoved) return emptyList()
        Log.w(TAG, "guess(forAdded=$forAdded, forRemoved=$forRemoved)")

        val results = arrayListOf<Int>()

        if (forAdded) {
            for (i in newList.indices) {
                val newElement = newList[i]

                if (oldList.isNotEmpty()) {
                    for (j in oldList.indices) {
                        val oldElement = oldList[j]
                        if (newElement.equalsWith(oldElement)) {
                            break
                        } else if (j == oldList.lastIndex) {
                            results.add(i)
                        }
                    }
                } else {
                    results.add(i)
                }
            }
        } else if (forRemoved) {
            for (i in oldList.lastIndex downTo 0) {
                val oldElement = oldList[i]
                Log.d(TAG, LogForGuess.detail("OLD LIST", "Target", i, oldElement))

                if (newList.isNotEmpty()) {
                    for (j in newList.lastIndex downTo 0) {
                        val newElement = newList[j]
                        if (oldElement.equalsWith(newElement)) {
                            Log.i(TAG, LogForGuess.detail("NEW LIST", "Dest", j, newElement))
                            break
                        } else if (j == 0) {
                            Log.e(TAG, LogForGuess.detail("OLD LIST", "찾을 수 없음", i, oldElement))
                            results.add(i)
                        }
                    }
                } else {
                    results.add(i)
                }
            }
        }

        Log.w(TAG, "guess(forAdded=$forAdded, forRemoved=$forRemoved): oldList.size=${oldList.size}, newList.size=${newList.size}")
        Log.i(TAG, LogForGuess.last("Old List!!", oldList))
        Log.i(TAG, LogForGuess.last("New List!!", newList))

        oldList.clear()
        return results
    }

    private object LogForGuess {

        fun detail(listName: String, purpose: String, index: Int, target: RvElement): String {
            return if (target is RvHeader) {
                "guessDetail(): [$listName] [$purpose] Header[$index]=${target.title}"
            } else if (target is RvItem) {
                "guessDetail(): [$listName] [$purpose] Item[$index]=(${target.accountGroupID}, ${target.name})"
            } else {
                ""
            }
        }

        fun last(title: String, list: List<RvElement>): String {
            val builder = StringBuilder(title).appendLine()
            list.forEachIndexed { index, element ->
                if (element is Header) {
                    builder.appendLine("Header[$index]: title=${element.title}]")
                } else if (element is Item) {
                    builder.appendLine("  Item[$index]: groupID=${element.accountGroup.group_name} groupName=${element.name}")
                }
            }
            return builder.toString()
        }
    }
}