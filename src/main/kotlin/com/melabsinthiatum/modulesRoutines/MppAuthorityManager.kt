package com.melabsinthiatum.modulesRoutines

import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import org.jetbrains.kotlin.idea.caches.project.isMPPModule
import org.jetbrains.kotlin.idea.project.platform
import org.jetbrains.kotlin.platform.impl.CommonIdePlatformKind


class MppAuthorityManager {

    fun provideAuthorityZonesForProject(project: Project): Collection<MppAuthorityZone> {
        val modules = ModuleManager.getInstance(project).modules

        return modules.filter{ it.isMPPModule && it.platform?.kind == CommonIdePlatformKind }.map { module ->
            MppAuthorityZone(module)
        }
    }
}