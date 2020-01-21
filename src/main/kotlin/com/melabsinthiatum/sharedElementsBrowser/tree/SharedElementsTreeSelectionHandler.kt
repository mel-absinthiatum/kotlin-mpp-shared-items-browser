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

import com.melabsinthiatum.sharedElementsBrowser.editor.SharedElementsNavigationManagerInterface
import javax.swing.tree.DefaultMutableTreeNode

/**
 * SharedElementsTreeSelectionHandlerInterface is used for handling item selection,
 * performed by `enter` key and mouse double-click.
 *
 * @see SharedElementsTreeKeyListener
 * @see SharedElementsTreeMouseListener
 */
interface SharedElementsTreeSelectionHandlerInterface {
    fun nodeSelected(node: DefaultMutableTreeNode)
}

/**
 * Default SharedElementsTreeSelectionHandler passes the selected node to the
 * navigation manager for displaying the selected element in a code editor.
 *
 * @see SharedElementsNavigationManagerInterface
 */
class SharedElementsTreeSelectionHandler(private val navigationManager: SharedElementsNavigationManagerInterface) :
    SharedElementsTreeSelectionHandlerInterface {
    override fun nodeSelected(node: DefaultMutableTreeNode) {
        navigationManager.navigate(node)
    }
}