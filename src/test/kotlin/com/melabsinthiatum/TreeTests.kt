/*
 * Copyright (c) 2019 mel-absinthiatum
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package com.melabsinthiatum

import com.melabsinthiatum.model.nodes.TemplateNode
import com.melabsinthiatum.model.nodes.model.NodeModel
import com.melabsinthiatum.services.imageManager.CustomIcons
import com.melabsinthiatum.sharedElementsTree.tree.diff.TreesDiffManager
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