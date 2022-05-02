package com.jojo.android.mwodeola.util

open class Node<T> constructor(
    val value: T,
    val isRoot: Boolean = false
) {

    val rootNode: Node<T>?
        get() = if (isRoot) this
        else if (parent!!.isRoot) parent!!
        else parent!!.rootNode

    var parent: Node<T>? = null

    private val _children = arrayListOf<Node<T>>()
    val children: List<Node<T>>
        get() = _children

    val size: Int
        get() = 1 + _children.sumOf { it.size }

    val isLeaf: Boolean
        get() = _children.isEmpty()

    val depth: Int
        get() = if (isRoot) 0
        else parent!!.depth + 1


    fun addChild(child: Node<T>) {
        child.parent = this
        _children.add(child)
    }

    fun addChild(index: Int, child: Node<T>) {
        child.parent = this
        _children.add(index, child)
    }

    fun removeChild(child: Node<T>) {
        _children.remove(child)
    }

    fun clear() {
        _children.clear()
    }

    fun <R : Comparable<R>> sortBy(selector: (Node<T>) -> R?) {
        _children.sortBy(selector)
        _children.forEach { it.sortBy(selector) }
    }

    override fun toString(): String = StringBuilder().also {
        if (isRoot) it.append(".\n★★ Tree ★★\n")
        it.append("[depth=$depth]  ")
        for (i in 0 until depth) it.append("    ")
        it.appendLine(value.toString())

        if (!isLeaf) {
            children.forEach { child ->
                it.append(child.toString())
            }
        }
    }.toString()
}