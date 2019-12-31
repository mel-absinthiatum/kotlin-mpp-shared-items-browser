package com.melabsinthiatum.model.nodes.model

import com.intellij.psi.stubs.Stub
import com.melabsinthiatum.imageManager.CustomIcons
import com.melabsinthiatum.model.DeclarationType
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