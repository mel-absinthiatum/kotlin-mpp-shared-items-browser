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

package com.melabsinthiatum.sharedElementsBrowser.tree

import com.intellij.ui.treeStructure.Tree
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import javax.swing.tree.DefaultMutableTreeNode

class SharedElementsTreeKeyListener(
    private val tree: Tree,
    private val selectionHandler: SharedElementsTreeSelectionHandlerInterface
): KeyAdapter() {
    override fun keyTyped(e: KeyEvent?) {
        if (e?.keyCode != KeyEvent.VK_ENTER) {
            return
        }

        val treePath = tree.selectionPath
        val node = treePath?.lastPathComponent as? DefaultMutableTreeNode

        node?.let {
            selectionHandler.nodeSelected(it)
        }
    }
}
