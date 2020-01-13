package com.melabsinthiatum.sharedElementsTree.tree.diff

import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.MutableTreeNode

class TreeDiffManager {

    class NodesTuple(val oldNode: DefaultMutableTreeNode, val newNode: DefaultMutableTreeNode)

    class SiftResult(val mutations: List<TreeMutation>, val unchanged: List<NodesTuple>)

    class DiffNodeModel<M : Any>(val sourceNodeModel: M, val mutations: List<TreeMutation>)

    fun makeMutationsTree(oldNode: DefaultMutableTreeNode, newNode: DefaultMutableTreeNode): DefaultMutableTreeNode? {
        return makeMutationsNode(oldNode,newNode)
    }

    private fun makeMutationsNode(
        oldNode: DefaultMutableTreeNode,
        newNode: DefaultMutableTreeNode
    ): DefaultMutableTreeNode? {
        if (oldNode.isLeaf && newNode.isLeaf) {
            return null
        }

        val oldChildren = oldNode.defaultChildren() ?: return null
        val newChildren = newNode.defaultChildren() ?: return null
        val model = oldNode.userObject ?: return null

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
        oldChildren: List<DefaultMutableTreeNode>,
        newChildren: List<DefaultMutableTreeNode>,
        parent: DefaultMutableTreeNode
    ): SiftResult {
        val mutableNewChildren = newChildren.toMutableList()
        val mutableOldChildren = oldChildren.toMutableList()
        val unchangedNodesTuples = mutableListOf<NodesTuple>()
        for (oldChild in oldChildren) {
            val matchedNode = mutableNewChildren.findLast { newChild ->
                oldChild.userObject == newChild.userObject
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

    private fun DefaultMutableTreeNode.defaultChildren(): List<DefaultMutableTreeNode>? {
        val childrenList = this.children().toList()
        return childrenList.filterIsInstance<DefaultMutableTreeNode>().takeIf { it.size == childrenList.size }
    }
}

sealed class TreeMutation

data class Insert(val node: MutableTreeNode, val parent: MutableTreeNode) : TreeMutation()
data class Remove(val node: MutableTreeNode, val parent: MutableTreeNode) : TreeMutation()
data class Move(val node: MutableTreeNode, val oldParent: MutableTreeNode, val newParent: MutableTreeNode) : TreeMutation()