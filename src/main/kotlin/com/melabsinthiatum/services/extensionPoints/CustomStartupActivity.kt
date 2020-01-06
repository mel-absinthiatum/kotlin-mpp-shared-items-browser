package com.melabsinthiatum.services.extensionPoints

import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import com.melabsinthiatum.services.logging.Loggable
import com.melabsinthiatum.services.logging.logger


class CustomStartupActivity : StartupActivity, Loggable {
    private val logger = logger()

    override fun runActivity(project: Project) {
        logger.info("Project ${project.name} started")
    }
}

