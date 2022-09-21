package com.jojo.android.mwodeola.util

open class BaseNode<T> constructor(
    val value: T,
    val isRoot: Boolean = false
) {

    companion object {
        const val TAG = "BaseNode"
        const val TOP = 0
        const val MIDDLE = 1
        const val BOTTOM = 2
        const val TOP_AND_BOTTOM = 3
    }

    operator fun get(index: Int): BaseNode<T>? = traverse(index)

    val rootNode: BaseNode<T>?
        get() = if (isRoot) this
        else if (parent!!.isRoot) parent!!
        else parent!!.rootNode

    var parent: BaseNode<T>? = null

    protected val _children = arrayListOf<BaseNode<T>>()
    val children: List<BaseNode<T>>
        get() = _children

    val size: Int
        get() = 1 + _children.sumOf { it.size }

    val isLeaf: Boolean
        get() = _children.isEmpty()

    val depth: Int
        get() = if (isRoot) 0
        else parent!!.depth + 1

    val indexFromRootNode: Int
        get() = rootNode?.indexOf(this, 0) ?: -1

    val indexWithInChildren: Int
        get() = if (isRoot) 0
        else parent!!.children.indexOf(this)

    val positionInChildren: Int
        get() = if (isRoot) TOP_AND_BOTTOM
        else getPositionInChildren()

    fun addChild(child: BaseNode<T>): BaseNode<T> = child.also {
        it.parent = this
        _children.add(it)
    }

    fun addChild(index: Int, child: BaseNode<T>): BaseNode<T> = child.also {
        it.parent = this
        _children.add(index, it)
    }

    fun removeChild(child: BaseNode<T>) { _children.remove(child) }
    fun clear() { _children.clear() }

    fun toList(): List<BaseNode<T>> = arrayListOf<BaseNode<T>>().also {
        it.add(this)
        if (!isLeaf) _children.forEach { child -> it.addAll(child.toList()) }
    }

    fun <R : Comparable<R>> sortBy(selector: (BaseNode<T>) -> R?) {
        _children.sortBy(selector)
        _children.forEach { it.sortBy(selector) }
    }

    private fun traverse(destination: Int, count: Int = 0): BaseNode<T>? {
        if (destination == count) {
            return this
        } else {
            var result: BaseNode<T>? = null
            var temp = count + 1
            for (child in children) {
                result = child.traverse(destination, temp)

                if (result == null) {
                    temp += child.size
                } else {
                    return result
                }
            }
            return null
        }
    }

    private fun indexOf(target: BaseNode<T>, count: Int): Int {
        if (target == this) {
            return count
        } else {
            var result = -1
            var temp = count + 1
            for (child in children) {
                if (result == -1) {
                    result = child.indexOf(target, temp)
                    temp += child.size
                } else {
                    return result
                }
            }
            return result
        }
    }

    @JvmName("getPositionInChildren1")
    private fun getPositionInChildren(): Int {
        //TODO:
        return 0
    }

    override fun toString(): String {
        val builder = StringBuilder()

        if (isRoot) {
            builder.append(".\n★★ Tree ★★\n")
        }

        builder.append("[depth=$depth]  ")

        for (i in 0 until depth) {
            builder.append("    ")
        }

        builder.appendLine(value.toString())

        if (!isLeaf) {
            children.forEach {
                builder.append(it.toString())
            }
        }

        return builder.toString()
    }
}