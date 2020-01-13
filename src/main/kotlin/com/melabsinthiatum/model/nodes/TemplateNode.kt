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
import com.melabsinthiatum.model.nodes.utils.emptyEnumeration
import com.melabsinthiatum.model.nodes.utils.toEnumeration
import java.util.*
import javax.swing.tree.TreeNode


interface TemplateNodeInterface<M : NodeModel, C : CustomNodeInterface> : CustomNodeInterface {
    var model: M

    fun add(node: C)

    fun add(nodes: List<C>)
}


abstract class TemplateNode<M : NodeModel, C : CustomNodeInterface>(
    override var model: M,
    override var nodeParent: CustomNodeInterface? = null,
    private val children: MutableList<C> = mutableListOf()
) : TemplateNodeInterface<M, C> {

    override fun childNodes(): List<C> = children

    override fun nodeModel(): NodeModel? = model

    override fun add(node: C) {
        check(allowsChildren) { "node does not allow children" }
        require(!isNodeAncestor(node)) { "new child is an ancestor" }

        node.nodeParent = this
        children.add(node)
    }

    override fun add(nodes: List<C>) {
        nodes.forEach { add(it) }
    }

    override fun children(): Enumeration<out TreeNode> = children.toEnumeration()

    override fun isLeaf(): Boolean = childCount == 0

    override fun getChildCount(): Int = children.size

    override fun getParent(): TreeNode? = nodeParent

    override fun getChildAt(childIndex: Int): TreeNode? = children[childIndex]

    override fun getIndex(node: TreeNode?): Int = children.indexOfFirst { node == it }

    override fun removeNodeParent() {
        nodeParent = null
    }

    override fun getAllowsChildren(): Boolean = true

    fun including(vararg nodes: C): TemplateNode<M, C> {
        this.add(nodes.asList())
        return this
    }

    private fun isNodeAncestor(anotherNode: TreeNode?): Boolean {
        if (anotherNode == null) {
            return false
        }
        var ancestor: TreeNode? = this
        do {
            if (ancestor === anotherNode) {
                return true
            }
            ancestor = ancestor?.parent
        } while (ancestor?.parent != null)
        return false
    }
}


abstract class TemplateLeaf<M : NodeModel>(model: M) : TemplateNode<M, Nothing>(model) {
    override fun getAllowsChildren(): Boolean = false

    override fun add(node: Nothing) {
        assert(false) { "Not allowed." }
    }

    override fun add(nodes: List<Nothing>) {
        assert(false) { "Not allowed." }
    }

    override fun children(): Enumeration<out TreeNode> = emptyEnumeration()

    override fun isLeaf(): Boolean = true

    override fun getChildCount(): Int = 0

    override fun getChildAt(childIndex: Int): TreeNode? {
        assert(false) { "Not allowed." }
        return null
    }

    override fun getIndex(node: TreeNode?): Int {
        assert(false) { "Not allowed." }
        return -1
    }
}

abstract class TemplateRootNode<M : NodeModel, C : CustomNodeInterface>(
    model: M
) : TemplateNode<M, C>(model) {
    @Suppress("UNUSED_PARAMETER")
    override var nodeParent: CustomNodeInterface?
        get() = null
        set(value) {
            assert(false) { "Not allowed." }
        }

    override fun getParent(): TreeNode? = null

    override fun removeNodeParent() {}
}
