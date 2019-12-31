package com.melabsinthiatum.model.nodes.model

import com.melabsinthiatum.imageManager.CustomIcons
import javax.swing.Icon

interface MppAuthorityZoneModelInterface {
    val title: String
}

data class MppAuthorityZoneModel(override val title: String): MppAuthorityZoneModelInterface, NodeModel {
    override fun getLabelText(): String = title
    override fun getIcon(): Icon? = CustomIcons.Nodes.Root
}
