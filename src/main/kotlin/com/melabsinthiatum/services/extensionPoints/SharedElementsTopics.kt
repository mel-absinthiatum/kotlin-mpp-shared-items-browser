package com.melabsinthiatum.services.extensionPoints

import com.intellij.util.messages.Topic
import com.melabsinthiatum.model.nodes.RootNode

class SharedElementsTopics {
    companion object {
        var SHARED_ELEMENTS_TREE_TOPIC: Topic<SharedElementsTopicsNotifier> =
            Topic.create("custom name", SharedElementsTopicsNotifier::class.java)
        var SHARED_ELEMENTS_TREE_SETTINGS_TOPIC: Topic<SharedElementsTreeSettingsNotifier> =
            Topic.create("Shared elements tree settings notifier", SharedElementsTreeSettingsNotifier::class.java)
    }
}

interface SharedElementsTopicsNotifier {
    fun sharedElementsUpdated(root: RootNode)
}

interface SharedElementsTreeSettingsNotifier {
    fun reloadIntervalUpdated(oldValue: Long, newValue: Long)
}
