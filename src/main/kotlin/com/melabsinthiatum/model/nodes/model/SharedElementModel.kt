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

package com.melabsinthiatum.model.nodes.model

import com.melabsinthiatum.model.DeclarationType
import javax.swing.Icon


/**
 * <code>SharedElementModelInterface</code> is a model for as such Shared Element
 * with its AST declaration type info. Below this element in the tree is a set of leaf elements
 * suggesting the location of general and platform declarations of the element in the code.
 */
interface SharedElementModelInterface {
    val name: String?
    val type: DeclarationType
    val typeIcon: Icon?
}

data class SharedElementModel(
    override val name: String?,
    override val type: DeclarationType,
    override val typeIcon: Icon?
) : SharedElementModelInterface, NodeModel {

    override fun getLabelText(): String {
        return name ?: "#error"
    }

    override fun getIcon(): Icon? = typeIcon
}