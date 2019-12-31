package com.melabsinthiatum.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.melabsinthiatum.tree.UpdateManager


class RefreshTreeAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        UpdateManager(project).update()
    }
}
