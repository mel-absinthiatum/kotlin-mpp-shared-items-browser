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

package com.melabsinthiatum.model.nodes

import com.melabsinthiatum.model.nodes.model.NodeModel
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.TreeNode

interface CustomNodeInterface : TreeNode {
    fun removeNodeParent()
    fun nodeModel(): NodeModel?
    fun childNodes(): List<CustomNodeInterface>
    var nodeParent: CustomNodeInterface?
}

fun CustomNodeInterface.toDefaultTree(): DefaultMutableTreeNode = this.toDefaultNode()

private fun CustomNodeInterface.toDefaultNode(): DefaultMutableTreeNode {
    val node = DefaultMutableTreeNode(this.nodeModel())

    val universalNodeChildren = this.childNodes().map { it.toDefaultNode() }
    universalNodeChildren.reversed().forEach {
        node.insert(it, 0)
    }

    return node
}

