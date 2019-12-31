package com.melabsinthiatum.model.nodes

import javax.swing.tree.MutableTreeNode

interface CustomNodeInterface : MutableTreeNode {
    fun removeNodeParent()
    fun nodeModel(): Any?
    fun childNodes(): List<CustomNodeInterface>
    fun nodeParent(): CustomNodeInterface?
}