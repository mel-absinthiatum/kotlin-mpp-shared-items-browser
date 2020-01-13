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

package com.melabsinthiatum.sharedElementsBrowser

import com.intellij.ui.JBDefaultTreeCellRenderer
import com.intellij.ui.components.JBLabel
import com.melabsinthiatum.model.nodes.model.NodeModel
import java.awt.Component
import javax.swing.Icon
import javax.swing.JTree
import javax.swing.tree.DefaultMutableTreeNode


class MppSharedItemsTreeCellRenderer(tree: JTree) : JBDefaultTreeCellRenderer(tree) {

    override fun getTreeCellRendererComponent(tree: JTree?, value: Any?, sel: Boolean, expanded: Boolean,
                                              leaf: Boolean, row: Int, hasFocus: Boolean): Component {
        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus)
        if (value is DefaultMutableTreeNode) {
            val model =  value.userObject as? NodeModel ?: return this
            return makeComponent(model.getLabelText(), model.getIcon())
        }

        return this
    }

    private fun makeComponent(title: String, icon: Icon?): JBLabel {
        val label = JBLabel()
        label.text = title
        label.icon = icon
        return label
    }
}
