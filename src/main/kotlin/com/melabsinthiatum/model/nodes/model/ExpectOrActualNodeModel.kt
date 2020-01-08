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
import com.intellij.psi.stubs.Stub
import com.melabsinthiatum.model.SharedType
import com.melabsinthiatum.services.imageManager.CustomIcons
import org.jetbrains.kotlin.idea.util.module
import javax.swing.Icon

interface ExpectOrActualModelInterface {
    val name: String
    val psi: PsiElement
    val type: SharedType
    val stub: Stub?
}

data class ExpectOrActualModel(
    override val name: String,
    override val psi: PsiElement,
    override val type: SharedType,
    override val stub: Stub?
) : ExpectOrActualModelInterface, NodeModel {
    override fun getLabelText(): String = when (type) {
        SharedType.EXPECTED -> psi.module?.name ?: "Common"
        SharedType.ACTUAL -> psi.module?.name ?: "Actual"
    }

    override fun getIcon(): Icon? = when (type) {
        SharedType.EXPECTED -> CustomIcons.Nodes.Expect
        SharedType.ACTUAL -> CustomIcons.Nodes.Actual
    }
}