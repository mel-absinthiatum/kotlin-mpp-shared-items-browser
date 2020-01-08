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

import com.intellij.psi.stubs.Stub
import com.melabsinthiatum.model.DeclarationType
import com.melabsinthiatum.services.imageManager.CustomIcons
import javax.swing.Icon

interface SharedElementModelInterface {
    val name: String?
    val type: DeclarationType
    val stub: Stub?
}

data class SharedElementModel(
    override val name: String?,
    override val type: DeclarationType,
    override val stub: Stub?
) : SharedElementModelInterface, NodeModel {

    override fun getLabelText(): String {
        return name ?: "#error"
    }

    override fun getIcon(): Icon? = when (type) {
        DeclarationType.ANNOTATION -> CustomIcons.Nodes.Annotation
        DeclarationType.CLASS -> CustomIcons.Nodes.Class
        DeclarationType.OBJECT -> CustomIcons.Nodes.Object
        DeclarationType.PROPERTY -> CustomIcons.Nodes.Property
        DeclarationType.NAMED_FUNCTION -> CustomIcons.Nodes.Function
        DeclarationType.UNRESOLVED -> null
    }
}