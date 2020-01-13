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
import javax.swing.tree.MutableTreeNode
import javax.swing.tree.TreeNode


interface TemplateNodeInterface<M: NodeModel, C: CustomNodeInterface> : CustomNodeInterface {
    var model: M

    fun add(node: C)

    fun add(nodes: List<C>)

    fun remove(node: C)

    fun remove(nodes: List<C>)
}


abstract class TemplateNode<M: NodeModel, C: CustomNodeInterface>(
    override var model: M,
    var nodeParent: CustomNodeInterface? = null,
    private val children: MutableList<C> = mutableListOf()
) : TemplateNodeInterface<M, C> {

    override fun nodeParent(): CustomNodeInterface? = nodeParent

    override fun childNodes(): List<C> = children

    override fun nodeModel(): NodeModel? = model

    override fun add(node: C) {
        children.add(node)
    }

    override fun add(nodes: List<C>) {
        children.addAll(nodes)
    }

    override fun remove(node: C) {
        node.removeNodeParent()
        children.removeIf { it == node }
    }

    override fun remove(nodes: List<C>) {
        nodes.forEach { it.removeNodeParent() }
        children.removeAll(nodes)
    }

    override fun remove(index: Int) {
        children.removeAt(index)
    }

    override fun remove(node: MutableTreeNode?) {
        children.removeIf { it == node }
    }

    override fun insert(child: MutableTreeNode, index: Int) {
        val newNode = child as? C ?: throw Exception("wrong type")
        children.add(index, newNode)
    }

    override fun setParent(newParent: MutableTreeNode) {
        val newParentNode = newParent as? CustomNodeInterface ?: throw Exception("wrong type")
        nodeParent = newParentNode
    }

    override fun setUserObject(`object`: Any) {
        val newModel = `object` as? M ?: throw Exception("wrong type")
        model = newModel
    }

    override fun removeFromParent() {
        nodeParent?.remove(this)
        nodeParent = null
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


}


abstract class TemplateLeaf<M: NodeModel>(model: M): TemplateNode<M, Nothing>(model) {
    override fun getAllowsChildren(): Boolean = false

    override fun add(node: Nothing) {
        assert(false) { "Not allowed." }
    }

    override fun add(nodes: List<Nothing>) {
        assert(false) { "Not allowed." }
    }

    override fun remove(node: Nothing) {
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

abstract class TemplateRootNode<M: NodeModel, C: CustomNodeInterface>(
    model: M
) : TemplateNode<M, C>(model) {
    override fun nodeParent(): CustomNodeInterface? = null

    override fun getParent(): TreeNode? = null

    override fun removeNodeParent() {}
}
