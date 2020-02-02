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

package com.melabsinthiatum.sharedElementsBrowser.toolWindow.tree

import com.intellij.ui.treeStructure.Tree
import java.util.*
import javax.swing.tree.TreeNode
import javax.swing.tree.TreePath

/**
 * Expand all nodes of the tree starting from specific node.
 *
 * @param node  The node to expand from.
 */
fun Tree.expandAll(node: TreeNode) {
    getPath(node)?.let { path ->
        expandNode(node, path)
    }
}

/**
 * Expand the tree to the given level, starting from the given node.
 *
 * @param node  The node to expand from.
 * @param level The number of levels to expand to.
 */
fun Tree.expand(node: TreeNode, level: Int) {
    getPath(node)?.let { path ->
        expandNodeToLevel(node, path, level)
    }
}

/**
 * Collapse all nodes of the tree.
 */
fun Tree.collapseAll() {
    for (i in 1 until this.rowCount) {
        collapseRow(i)
    }
}

/**
 * Collect paths of all expanded nodes of the tree.
 *
 * @return List of paths of expanded nodes.
 */
fun Tree.getExpandedPaths(): List<TreePath> {
    val expanded = ArrayList<TreePath>()
    for (i in 0 until rowCount) {
        if (isExpanded(i)) {
            expanded.add(getPathForRow(i))
        }
    }
    return expanded
}

/**
 * Expand nodes of paths listed in the list.
 *
 * @param expanded  List of paths of nodes to be expanded.
 */
fun Tree.expand(expanded: List<TreePath>) {
    expanded.forEach { path -> expandPath(path) }
}

//
//  Private
//

/**
 * Get the path for the given tree node.
 *
 * @param treeNode  The given tree node.
 * @return  Tree path of the node.
 */
private fun getPath(treeNode: TreeNode?): TreePath? {
    var node = treeNode
    val nodes: MutableList<Any> = ArrayList()
    if (node != null) {
        nodes.add(node)
        node = node.parent
        while (node != null) {
            nodes.add(0, node)
            node = node.parent
        }
    }
    return if (nodes.isEmpty()) null else TreePath(nodes.toTypedArray())
}

/**
 * Expand the tree to the given level, starting from the given node and path.
 *
 * @param node  The node to start from.
 * @param path  The path to start from.
 * @param level The number of levels to expand to.
 */
private fun Tree.expandNodeToLevel(
    node: TreeNode,
    path: TreePath,
    level: Int
) {
    if (level <= 0) {
        return
    }
    expandPath(path)
    for (i in 0 until node.childCount) {
        val childNode = node.getChildAt(i)
        expandNodeToLevel(childNode, path.pathByAddingChild(childNode), level - 1)
    }
}

/**
 * Expand all nodes of the tree starting from the given node and path.
 *
 * @param node  The node to start from.
 * @param path  The path to start from.
 */
private fun Tree.expandNode(
    node: TreeNode,
    path: TreePath
) {
    expandPath(path)
    for (i in 0 until node.childCount) {
        val childNode = node.getChildAt(i)
        expandNode(childNode, path.pathByAddingChild(childNode))
    }
}
