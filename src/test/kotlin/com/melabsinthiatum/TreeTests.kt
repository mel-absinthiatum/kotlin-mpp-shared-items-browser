package com.melabsinthiatum

import com.melabsinthiatum.imageManager.CustomIcons
import com.melabsinthiatum.model.nodes.TemplateNode
import com.melabsinthiatum.model.nodes.model.NodeModel
import com.melabsinthiatum.tree.diff.TreesDiffManager
import org.jetbrains.kotlin.utils.addToStdlib.assertedCast
import javax.swing.Icon
import javax.swing.tree.DefaultMutableTreeNode
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import org.junit.Test as test

class TreeTests {

    data class TestNodeModel(val title: String): NodeModel {
        override fun getLabelText(): String = title
        override fun getIcon(): Icon? = CustomIcons.Nodes.File
    }

    class TestNode(title: String): TemplateNode<TestNodeModel, TestNode>(
        TestNodeModel(title)
    )

    @test fun `Test root node mutations`() {
        val sourceTreeRoot = TestNode("1").including(
            TestNode("1-1"),
            TestNode("1-2"),
            TestNode("1-3")
        )

        val resultTreeRoot = TestNode("1").including(
            TestNode("1-1"),
            TestNode("1-3"),
            TestNode("1-4")
        )

        val mutationsTree = TreesDiffManager().makeMutationsTree(sourceTreeRoot, resultTreeRoot)

        assertNotNull(mutationsTree, "Mutations tree must be non null")

        val rootModel = mutationsTree.userObject as? TreesDiffManager.DiffNodeModel<*>

        assertNotNull(rootModel, "Root sourceNodeModel must be non null")

        val rootMutations = rootModel.mutations

        assertEquals(rootMutations.size, 2)

        assertEquals(sourceTreeRoot.model, TestNodeModel("1"))
    }

    @test fun `Test node mutations`() {
        val sourceTreeRoot = TestNode("1").including(
            TestNode("1-1"),
            TestNode("1-2").including(
                TestNode("2-1"),
                TestNode("2-2"),
                TestNode("2-3")
            ) as TestNode,
            TestNode("1-3")
        )

        val resultTreeRoot = TestNode("1").including(
            TestNode("1-1"),
            TestNode("1-2").including(
                TestNode("2-1"),
                TestNode("2-3"),
                TestNode("2-4")
            ) as TestNode,
            TestNode("1-3")
        )

        val mutationsTree = TreesDiffManager().makeMutationsTree(sourceTreeRoot, resultTreeRoot)

        assertNotNull(mutationsTree, "Mutations tree must be non null")

        val rootNodeModel = mutationsTree.userObject as? TreesDiffManager.DiffNodeModel<*>

        assertNotNull(rootNodeModel, "Model must be non null")

        assertEquals(rootNodeModel.mutations.size, 0)

        assertEquals(mutationsTree.childCount, 1)

        val childNode = mutationsTree.firstChild

        childNode.assertedCast<DefaultMutableTreeNode> { "" }

        val childNodeModel = (childNode as DefaultMutableTreeNode).userObject as? TreesDiffManager.DiffNodeModel<*>

        assertNotNull(childNodeModel, "Model must be non null")

        val rootMutations = childNodeModel.mutations

        assertEquals(rootMutations.size, 2)
    }
}