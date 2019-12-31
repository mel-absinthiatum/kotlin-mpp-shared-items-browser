package com.melabsinthiatum.model.nodes.model

import javax.swing.Icon

interface NodeModel {
    fun getLabelText(): String
    fun getIcon(): Icon?
}