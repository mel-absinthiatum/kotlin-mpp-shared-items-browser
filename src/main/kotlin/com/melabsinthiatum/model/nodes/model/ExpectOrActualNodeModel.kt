package com.melabsinthiatum.model.nodes.model

import com.intellij.psi.PsiElement
import com.intellij.psi.stubs.Stub
import com.melabsinthiatum.imageManager.CustomIcons
import com.melabsinthiatum.model.SharedType
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