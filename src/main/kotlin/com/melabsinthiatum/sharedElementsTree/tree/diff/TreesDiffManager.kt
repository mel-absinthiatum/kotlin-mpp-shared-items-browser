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

package com.melabsinthiatum.sharedElementsTree.tree.diff

import com.melabsinthiatum.model.nodes.CustomNodeInterface
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.MutableTreeNode

class TreesDiffManager {

    fun makeMutationsTree(oldNode: CustomNodeInterface, newNode: CustomNodeInterface): DefaultMutableTreeNode? {
        return makeMutationsNode(oldNode,newNode)
    }

    private fun makeMutationsNode(oldNode: CustomNodeInterface, newNode: CustomNodeInterface): DefaultMutableTreeNode? {
        if (oldNode.isLeaf && newNode.isLeaf) {
            return null
        }
        val oldChildren = oldNode.childNodes().toList()
        val newChildren = newNode.childNodes().toList()

        val model = oldNode.nodeModel() ?: return null

        val result = sift(oldChildren, newChildren, oldNode)
        val mutations = result.mutations
        val unchanged = result.unchanged

        val nodes = unchanged.mapNotNull { makeMutationsNode(it.oldNode, it.newNode) }

        if (mutations.isEmpty() && nodes.isEmpty()) {
            return null
        }
        val diffNodeModel = DiffNodeModel(model, mutations)
        val resultNode = DefaultMutableTreeNode(diffNodeModel)
        nodes.forEach { resultNode.add(it) }
        return resultNode
    }

    private fun sift(
        oldChildren: List<CustomNodeInterface>,
        newChildren: List<CustomNodeInterface>,
        parent: CustomNodeInterface
    ): SiftResult {
        val mutableNewChildren = newChildren.toMutableList()
        val mutableOldChildren = oldChildren.toMutableList()
        val unchangedNodesTuples = mutableListOf<NodesTuple>()
        for (oldChild in oldChildren) {
            val matchedNode = mutableNewChildren.findLast { newChild ->
                oldChild.nodeModel() == newChild.nodeModel()
            }
            if (matchedNode != null) {
                unchangedNodesTuples.add(NodesTuple(oldChild, matchedNode))
                mutableOldChildren.remove(oldChild)
                mutableNewChildren.remove(matchedNode)
            }
        }

        val mutations = mutableOldChildren.map { Remove(it, parent) } + mutableNewChildren.map { Insert(it, parent) }
        return SiftResult(mutations, unchangedNodesTuples)
    }

    class SiftResult(val mutations: List<TreeMutation>, val unchanged: List<NodesTuple>)

    class NodesTuple(val oldNode: CustomNodeInterface, val newNode: CustomNodeInterface)

    class DiffNodeModel<M : Any>(val sourceNodeModel: M, val mutations: List<TreeMutation>)
}


sealed class TreeMutation

data class Insert(val node: MutableTreeNode, val parent: MutableTreeNode) : TreeMutation()
data class Remove(val node: MutableTreeNode, val parent: MutableTreeNode) : TreeMutation()
data class Move(val node: MutableTreeNode, val oldParent: MutableTreeNode, val newParent: MutableTreeNode) : TreeMutation()