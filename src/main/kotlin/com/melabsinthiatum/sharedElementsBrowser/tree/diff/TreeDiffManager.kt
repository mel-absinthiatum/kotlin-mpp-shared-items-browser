package com.melabsinthiatum.sharedElementsBrowser.tree.diff

import javax.swing.tree.DefaultMutableTreeNode


/**
 * <code>TreeDiffManager</code> provides a hierarchical object (diff-tree) representing
 * a set of editorial operations necessary for converting the source tree to the target tree.
 */
//TODO change to object
class TreeDiffManager {

    /**
     * The tuple of two comparing nodes.
     */
    class NodesTuple(val oldNode: DefaultMutableTreeNode, val newNode: DefaultMutableTreeNode)

    /**
     * The model containing the information about mutations (insert, remove) on the current
     * node level and the list of children with mutations or their ever mutated descendants.
     * It used for constructing diff-tree nodes with their models.
     */
    class SiftResult(val mutations: List<TreeMutation>, val unchanged: List<NodesTuple>)

    /**
     * The diff-tree node model containing the information about mutations (insert, remove)
     * on the current node level as well as the object indicating a specific original tree
     * node to which these mutations relate.
     */
    class DiffNodeModel<M : Any>(val sourceNodeModel: M, val mutations: List<TreeMutation>)


    /**
     * Retrieve the diff-tree for specified original and target trees.
     *
     * @param   oldNode     original tree root node
     * @param   newNode     target tree root node
     *
     * @return      a diff-tree represented by the tree constructed of DefaultMutableTreeNode-s
     *              with associated DiffNodeModel objects as default node userObject-s
     */
    fun makeMutationsTree(oldNode: DefaultMutableTreeNode, newNode: DefaultMutableTreeNode): DefaultMutableTreeNode? {
        return makeMutationsNode(oldNode,newNode)
    }


    //
    //  Private
    //

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
