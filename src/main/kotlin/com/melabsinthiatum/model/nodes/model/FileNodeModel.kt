package com.melabsinthiatum.model.nodes.model

import com.intellij.openapi.vfs.VirtualFile
import com.melabsinthiatum.imageManager.CustomIcons
import javax.swing.Icon


interface FileNodeModelInterface: NodeModel {
    val title: String
    val virtualFile: VirtualFile
}


data class FileNodeModel(
    override val title: String,
    override val virtualFile: VirtualFile
): FileNodeModelInterface {

    override fun getLabelText(): String = title

    override fun getIcon(): Icon? = CustomIcons.Nodes.File
}
