package com.melabsinthiatum.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.melabsinthiatum.sharedElementsTree.settings.SharedElementsTreeSettingsViewWrapper

class TreeSettingsAction: AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        SharedElementsTreeSettingsViewWrapper().showAndGet()
    }
}