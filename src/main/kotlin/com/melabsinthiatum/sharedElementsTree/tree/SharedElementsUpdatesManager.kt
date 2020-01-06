package com.melabsinthiatum.sharedElementsTree.tree

import com.intellij.openapi.project.Project
import com.melabsinthiatum.services.extensionPoints.SharedElementsTopics
import com.melabsinthiatum.services.extensionPoints.SharedElementsTopicsNotifier
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class UpdateManager(private val project: Project) {
    fun update() {
        GlobalScope.launch {
            val root = SharedTreeProvider().sharedTreeRoot(project)
            val myBus = project.messageBus
            val publisher: SharedElementsTopicsNotifier =
                myBus.syncPublisher(SharedElementsTopics.SHARED_ELEMENTS_TREE_TOPIC)
            publisher.sharedElementsUpdated(root)
        }
    }
}
