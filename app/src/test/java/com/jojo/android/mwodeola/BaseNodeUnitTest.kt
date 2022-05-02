package com.jojo.android.mwodeola

import com.jojo.android.mwodeola.util.BaseNode
import org.junit.Before
import org.junit.Test

import org.junit.Assert.*

class BaseNodeUnitTest {

    private lateinit var rootNode: BaseNode<Int>
    private lateinit var nodeList: List<BaseNode<Int>>

    @Before
    fun setup() {
        rootNode = BaseNode(0, true)
        val node0 = BaseNode(1)
        val node0_0 = BaseNode(2)
        val node0_0_0 = BaseNode(3)
        val node0_0_1 = BaseNode(4)
        val node0_1 = BaseNode(5)
        val node0_1_0 = BaseNode(6)
        val node0_1_1 = BaseNode(7)
        val node0_1_1_0 = BaseNode(8)
        val node0_1_2 = BaseNode(9)
        val node0_2 = BaseNode(10)
        val node0_2_0 = BaseNode(11)
        val node0_2_1 = BaseNode(12)
        val node0_2_2 = BaseNode(13)
        node0_2.addChild(node0_2_0)
        node0_2.addChild(node0_2_1)
        node0_2.addChild(node0_2_2)
        node0_1.addChild(node0_1_0)
        node0_1.addChild(node0_1_1)
        node0_1_1.addChild(node0_1_1_0)
        node0_1.addChild(node0_1_2)
        node0_0.addChild(node0_0_0)
        node0_0.addChild(node0_0_1)
        node0.addChild(node0_0)
        node0.addChild(node0_1)
        node0.addChild(node0_2)
        rootNode.addChild(node0)

        val node1 = BaseNode(14)
        val node1_0 = BaseNode(15)
        val node1_0_0 = BaseNode(16)
        val node1_0_1 = BaseNode(17)
        val node1_1 = BaseNode(18)
        val node1_1_0 = BaseNode(19)
        val node1_1_1 = BaseNode(20)
        val node1_1_2 = BaseNode(21)
        val node1_2 = BaseNode(22)
        val node1_2_0 = BaseNode(23)
        val node1_2_1 = BaseNode(24)
        val node1_2_2 = BaseNode(25)

        node1_2.addChild(node1_2_0)
        node1_2.addChild(node1_2_1)
        node1_2.addChild(node1_2_2)
        node1_1.addChild(node1_1_0)
        node1_1.addChild(node1_1_1)
        node1_1.addChild(node1_1_2)
        node1_0.addChild(node1_0_0)
        node1_0.addChild(node1_0_1)
        node1.addChild(node1_0)
        node1.addChild(node1_1)
        node1.addChild(node1_2)
        rootNode.addChild(node1)

        val node2 = BaseNode(26)
        val node2_0 = BaseNode(27)
        val node2_0_0 = BaseNode(28)
        val node2_0_1 = BaseNode(29)

        node2_0.addChild(node2_0_0)
        node2_0.addChild(node2_0_1)
        node2.addChild(node2_0)
        rootNode.addChild(node2)

        val node3 = BaseNode(300, true)

        nodeList = rootNode.toList()
    }

    @Test
    fun rootNodeSize_NodeListSize_Equals() {
        assertEquals(rootNode.size, nodeList.size)
    }

    @Test
    fun rootNodeValueOfEveryNodes_ReturnsZero() {
        nodeList.forEach { assertEquals(0, it.rootNode?.value) }
    }

    @Test
    fun test_Property_IndexFromRootNode() {
        nodeList.forEachIndexed { i, node -> assertEquals(i, node.indexFromRootNode) }
    }
}