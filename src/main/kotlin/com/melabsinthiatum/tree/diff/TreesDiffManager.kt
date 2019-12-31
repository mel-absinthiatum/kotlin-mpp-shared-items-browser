package com.melabsinthiatum.tree.diff

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