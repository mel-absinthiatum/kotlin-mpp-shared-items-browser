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

import com.melabsinthiatum.sharedElementsBrowser.tree.diff.TreeDiffManager
import org.jetbrains.kotlin.utils.addToStdlib.assertedCast
import javax.swing.tree.DefaultMutableTreeNode
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import org.junit.Test as test

class TreeTests {

    private val diffManager = TreeDiffManager

    @test
    fun `Test default root node mutations`() {
        val sourceTreeRoot = DefaultMutableTreeNode("1").including(
            DefaultMutableTreeNode("1-1"),
            DefaultMutableTreeNode("1-2"),
            DefaultMutableTreeNode("1-3")
        )

        val resultTreeRoot = DefaultMutableTreeNode("1").including(
            DefaultMutableTreeNode("1-1"),
            DefaultMutableTreeNode("1-3"),
            DefaultMutableTreeNode("1-4")
        )

        val mutationsTree = diffManager.makeMutationsTree(sourceTreeRoot, resultTreeRoot)

        assertNotNull(mutationsTree, "Mutations tree must be non null")

        val rootModel = mutationsTree.userObject as? TreeDiffManager.DiffNodeModel<*>

        assertNotNull(rootModel, "Root sourceNodeModel must be non null")

        val rootMutations = rootModel.mutations

        assertEquals(rootMutations.size, 2)

        assertEquals(sourceTreeRoot.userObject, "1")
    }

    @test
    fun `Test default node mutations`() {
        val sourceTreeRoot = DefaultMutableTreeNode("1").including(
            DefaultMutableTreeNode("1-1"),
            DefaultMutableTreeNode("1-2").including(
                DefaultMutableTreeNode("2-1"),
                DefaultMutableTreeNode("2-2"),
                DefaultMutableTreeNode("2-3")
            ),
            DefaultMutableTreeNode("1-3")
        )

        val resultTreeRoot = DefaultMutableTreeNode("1").including(
            DefaultMutableTreeNode("1-1"),
            DefaultMutableTreeNode("1-2").including(
                DefaultMutableTreeNode("2-1"),
                DefaultMutableTreeNode("2-3"),
                DefaultMutableTreeNode("2-4")
            ),
            DefaultMutableTreeNode("1-3")
        )

        val mutationsTree = diffManager.makeMutationsTree(sourceTreeRoot, resultTreeRoot)

        assertNotNull(mutationsTree, "Mutations tree must be non null")

        val rootNodeModel = mutationsTree.userObject as? TreeDiffManager.DiffNodeModel<*>

        assertNotNull(rootNodeModel, "Model must be non null")

        assertEquals(rootNodeModel.mutations.size, 0)

        assertEquals(mutationsTree.childCount, 1)

        val childNode = mutationsTree.firstChild

        childNode.assertedCast<DefaultMutableTreeNode> { "Child node must `DefaultMutableTreeNode`" }

        val childNodeModel = (childNode as DefaultMutableTreeNode).userObject as? TreeDiffManager.DiffNodeModel<*>

        assertNotNull(childNodeModel, "Inner model must be non null")

        val rootMutations = childNodeModel.mutations

        assertEquals(rootMutations.size, 2)
    }

    private fun DefaultMutableTreeNode.including(vararg nodes: DefaultMutableTreeNode): DefaultMutableTreeNode {
        nodes.asList().forEach {
            this.add(it)
        }
        return this
    }
}