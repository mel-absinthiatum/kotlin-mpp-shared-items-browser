package com.melabsinthiatum.extensionPoints

import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity

class CustomStartupActivity : StartupActivity {
    override fun runActivity(project: Project) {
        println("Project ${project.name} started")
//        SharedElementsUpdatesManager().updateSharedTreeRoot(project)
    }
}

