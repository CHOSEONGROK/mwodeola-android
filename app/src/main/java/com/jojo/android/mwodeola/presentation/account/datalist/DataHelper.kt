package com.jojo.android.mwodeola.presentation.account.datalist

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.util.Log
import com.jojo.android.mwodeola.data.account.AccountGroup
import com.jojo.android.mwodeola.data.account.AccountGroup.CREATOR.ICON_TYPE_INSTALLED_APP_LOGO
import com.jojo.android.mwodeola.presentation.account.datalist.RvElement.Companion.HEADER_NORMAL
import com.jojo.android.mwodeola.presentation.account.datalist.RvElement.Companion.HEADER_SPECIAL
import com.jojo.android.mwodeola.util.HangulUtils

object DataHelper {
    private const val TAG = "DataHelper"

    const val HEADER_FAVORITE = "★  즐겨찾기"
    const val HEADER_SNS_GROUP = "#  SNS 계정"
    const val HEADER_MOST_VIEWS = "#  자주 사용하는 계정"

    const val MOST_VIEW_ITEM_COUNT = 5

    private val normalHeaderComparator = Comparator<String> { s1, s2 ->
        when {
            s1 == "#" && s2 == "#" -> 0
            s1 == "#" -> 1
            s2 == "#" -> -1
            else -> (s1 as Comparable<String>).compareTo(s2)
        }
    }

    private val normalHeaderMapComparator = Comparator<Map.Entry<String, Any>> { e1, e2 ->
        normalHeaderComparator.compare(e1.key, e2.key)
    }

    fun create(packageManager: PackageManager, data: List<AccountGroup>): List<RvElement> {
        val newElements = arrayListOf<RvElement>()

        val sortedData = data.sortedBy { it.group_name }

        sortedData.filterOrNull { it.is_favorite }
            ?.map { newItem(packageManager, it.copy()) }
            ?.let {
                val header = newHeader(HEADER_SPECIAL, HEADER_FAVORITE)
                newElements.add(header)
                newElements.addAll(it)
            }

        sortedData.filterOrNull { it.isSnsGroup }
            ?.map { newItem(packageManager, it.copy()) }
            ?.let {
                val header = newHeader(HEADER_SPECIAL, HEADER_SNS_GROUP)
                newElements.add(header)
                newElements.addAll(it)
            }

        sortedData.filterNot { it.isSnsGroup }
            .sortedByDescending { it.total_views }
            .take(MOST_VIEW_ITEM_COUNT)
            .mapOrNull { newItem(packageManager, it.copy()) }
            ?.let {
                val header = newHeader(HEADER_SPECIAL, HEADER_MOST_VIEWS)
                newElements.add(header)
                newElements.addAll(it)
            }

        sortedData.filter { it.group_name.isNotBlank() }
            .groupBy { HangulUtils.getFirstCharOfInitialSound2(it.group_name) }.entries
            .sortedWith(normalHeaderMapComparator)
            .forEach { entry ->
                val header = newHeader(HEADER_NORMAL, entry.key)
                newElements.add(header)
                newElements.addAll(
                    entry.value
                        .sortedBy { it.group_name }
                        .map { newItem(packageManager, it) }
                )
            }

        return newElements
    }

    fun old(packageManager: PackageManager, elements: List<RvElement>): Composer {
        return Composer(packageManager, elements)
    }

    class Composer(val packageManager: PackageManager, oldList: List<RvElement>) {
        private val newList = oldList.toMutableList()

        fun add(accountGroup: AccountGroup) = apply {
            Log.d(TAG, "add(): $accountGroup")

            // Special Header: Favorite
            if (accountGroup.is_favorite) {
                var headerIndex = newList.indexOfFirst { it.header?.title == HEADER_FAVORITE }
                if (headerIndex == -1) {
                    // Favorite Header 존재 x
                    headerIndex = 0
                    val newHeader = newHeader(HEADER_SPECIAL, HEADER_FAVORITE)
                    newList.add(headerIndex, newHeader)
                    newList.add(headerIndex + 1, newItem(packageManager, accountGroup))
                } else {
                    // Favorite Header 존재 O
                    newList.add(headerIndex + 1, newItem(packageManager, accountGroup))
                    val nextHeaderIndex = newList.indexOfFirstOrNull(headerIndex + 1) { it.isHeader }!!
                    newList.sortBy(headerIndex + 1 until nextHeaderIndex) { it.item?.group_name }
                }
            }

            // Special Header: SNS Group
            if (accountGroup.isSnsGroup) {
                var headerIndex = newList.indexOfFirst { it.header?.title == HEADER_SNS_GROUP }
                if (headerIndex == -1) {
                    // SNS Group Header 존재 x
                    val mostViewHeaderIndex = newList.indexOfFirst { it.header?.title == HEADER_MOST_VIEWS }
                    headerIndex =
                        if (mostViewHeaderIndex != -1) mostViewHeaderIndex
                        else newList.indexOfFirst { it.header?.type == HEADER_NORMAL }

                    if (headerIndex == -1)
                        headerIndex = 0

                    val newHeader = newHeader(HEADER_SPECIAL, HEADER_SNS_GROUP)
                    newList.add(headerIndex, newHeader)
                    newList.add(headerIndex + 1, newItem(packageManager, accountGroup))
                } else {
                    // SNS Group Header 존재 o
                    newList.add(headerIndex + 1, newItem(packageManager, accountGroup))
                    val nextHeaderIndex = newList.indexOfFirstOrNull(headerIndex + 1) { it.isHeader }!!
                    newList.sortBy(headerIndex + 1 until nextHeaderIndex) { it.item?.group_name }
                }
            }

            // Normal Header
            val newHeaderTitle = HangulUtils.getFirstCharOfInitialSound2(accountGroup.group_name)
            val headerIndex = newList.indexOfFirst { it.header?.title == newHeaderTitle }
            if (headerIndex == -1) {
                // Normal Header 존재 x

                val newHeaderIndex = newList.indexOfFirstOrNull {
                    it.isHeader && it.header!!.type == HEADER_NORMAL && normalHeaderComparator.compare(newHeaderTitle, it.header.title) < 0
                }
                    ?: newList.size // newHeaderTitle 보다 작은 헤더 존재 x
                val newHeader = newHeader(HEADER_NORMAL, newHeaderTitle)
                newList.add(newHeaderIndex, newHeader)
                newList.add(newHeaderIndex + 1, newItem(packageManager, accountGroup))
            } else {
                // Normal Header 존재 o
                newList.add(headerIndex + 1, newItem(packageManager, accountGroup))

                val nextHeaderIndex = newList.indexOfFirstOrNull(headerIndex + 1) { it.isHeader }
                    ?: newList.size // next header 존재 x
                newList.sortBy(headerIndex + 1 until nextHeaderIndex) { it.item?.group_name }
            }
        }

        fun removeBy(accountGroupId: String) = apply {
            for (index in newList.lastIndex downTo 0) {
                val element = newList[index]
                if (element.item?.id == accountGroupId) {
                    newList.removeAt(index)
                }
            }

            for (index in newList.lastIndex downTo 0) {
                val element = newList[index]
                val elementRear = newList.getOrNull(index + 1)

                if (element.isHeader && (elementRear == null || !elementRear.isItem)) {
                    newList.removeAt(index)
                }
            }
        }

        fun removeAllAt(indices: Iterable<Long>) = apply {
            indices.sortedDescending().forEach {
                newList.removeAt(it.toInt())
            }

            for (index in newList.lastIndex downTo 0) {
                val element = newList[index]
                val elementRear = newList.getOrNull(index + 1)

                if (element.isHeader && (elementRear == null || !elementRear.isItem)) {
                    newList.removeAt(index)
                }
            }
        }

        fun update(accountGroup: AccountGroup) = apply {
            for (index in newList.indices) {
                val element = newList[index]
                if (element.item?.id == accountGroup.id) {
                    newList.removeAt(index)
                    newList.add(index, newItem(packageManager, accountGroup))
                }
            }
        }

        fun get(): List<RvElement> {
            refreshMostViews()
            return newList
        }

        private fun refreshMostViews() {
            if (newList.isEmpty())
                return

            val mostViewElements = mutableListOf<RvElement>()

            var mostViewHeaderIndex = newList.indexOfFirst { it.header?.title == HEADER_MOST_VIEWS }
            if (mostViewHeaderIndex == -1) {
                // Most View Header 존재 x

                val firstNormalHeaderIndex =
                    newList.indexOfFirst { it.isHeader && it.header!!.type == HEADER_NORMAL }
                mostViewHeaderIndex = firstNormalHeaderIndex

                val newMostViewHeader = newHeader(HEADER_SPECIAL, HEADER_MOST_VIEWS)
                newList.add(mostViewHeaderIndex, newMostViewHeader)
            } else {
                // Most View Header 존재 o

                val mostViewItemIndices = mutableListOf<Int>()
                for (index in mostViewHeaderIndex + 1 until newList.size) {
                    if (newList[index].isHeader)
                        break
                    mostViewItemIndices.add(index)
                }

                mostViewItemIndices.sortedDescending().forEach {
                    mostViewElements.add(0, newList.removeAt(it))
                }
            }

            val originItems = mutableListOf<AccountGroup>()

            for (index in mostViewHeaderIndex + 1 until newList.size) {
                val element = newList[index]
                if (element.isItem)
                    originItems.add(element.item!!)
            }

            val newMostViewItems = originItems.filterNot { it.isSnsGroup }
                .sortedByDescending { it.total_views }
                .take(MOST_VIEW_ITEM_COUNT)

            for (item in newMostViewItems) {
                val isNotExists = mostViewElements.none { it.item == item }
                if (isNotExists)
                    mostViewElements.add(newItem(packageManager, item))
            }

            mostViewElements.sortByDescending { it.item?.total_views }

            if (mostViewElements.isEmpty()) {
                newList.removeAt(mostViewHeaderIndex)
            } else {
                newList.addAll(mostViewHeaderIndex + 1, mostViewElements.take(MOST_VIEW_ITEM_COUNT))
            }
        }
    }

    private fun newHeader(type: Int, title: String): RvElement =
        RvElement(header = RvElement.Header(type, title))

    private fun newItem(packageManager: PackageManager, accountGroup: AccountGroup): RvElement =
        RvElement(item = accountGroup).apply {
            if (accountGroup.icon_type == ICON_TYPE_INSTALLED_APP_LOGO) {
                icon = getInstalledAppIcon(packageManager, accountGroup.app_package_name)
            }
        }

    private fun getInstalledAppIcon(packageManager: PackageManager, packageName: String?): Drawable? {
        if (packageName == null)
            return null

        return try {
            packageManager.getApplicationIcon(packageName)
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
    }

    private fun <T> Iterable<T>.filterOrNull(predicate: (T) -> Boolean): Iterable<T>? {
        val result = this.filter(predicate)
        return if (result.isNotEmpty()) result
        else null
    }

    private fun <T, R> Iterable<T>.mapOrNull(transform: (T) -> R): List<R>? {
        val result = this.map(transform)
        return if (result.isNotEmpty()) result
        else null
    }

    private fun <T> List<T>.indexOfFirstOrNull(start: Int = 0, predicate: (T) -> Boolean): Int? {
        if (start > lastIndex)
            return null
        for (index in start until size) {
            if (predicate(this[index]))
                return index
        }
        return null
    }

    private fun <T, R : Comparable<R>> MutableList<T>.sortBy(range: IntRange, selector: (T) -> R?) {
        if (range.first < 0 || range.last > lastIndex)
            return

        val tempList = mutableListOf<T>()
        for (index in range.last downTo range.first) {
            tempList.add(this.removeAt(index))
        }
        tempList.sortBy(selector)
        this.addAll(range.first, tempList)
    }
}