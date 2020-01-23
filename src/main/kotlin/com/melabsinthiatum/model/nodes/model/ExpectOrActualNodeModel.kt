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

import com.intellij.psi.PsiElement
import com.melabsinthiatum.model.SharedType
import com.melabsinthiatum.services.imageManager.CustomIcons
import org.jetbrains.kotlin.idea.util.module
import javax.swing.Icon


/**
 * <code>ExpectOrActualModelInterface</code> is a model for Shared Elements Tree leaves.
 * This object provides the psi- and document-level info for a specific element
 * on a specific platform or common module, what is suggested in the <code>SharedType</code> field.
 */
interface ExpectOrActualModelInterface : NodeModel {
    val name: String
    // TODO PsiNameIdentifierOwner
    val psi: PsiElement
    val type: SharedType
}

data class ExpectOrActualModel(
    override val name: String,
    override val psi: PsiElement,
    override val type: SharedType
) : ExpectOrActualModelInterface {
    override fun getLabelText(): String = when (type) {
        SharedType.EXPECT -> psi.module?.name ?: "Common"
        SharedType.ACTUAL -> psi.module?.name ?: "Actual"
    }

    override fun getIcon(): Icon? = when (type) {
        SharedType.EXPECT -> CustomIcons.Nodes.Expect
        SharedType.ACTUAL -> CustomIcons.Nodes.Actual
    }
}