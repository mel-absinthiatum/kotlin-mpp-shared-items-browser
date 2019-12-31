package com.melabsinthiatum.extensionPoints

import com.intellij.util.messages.Topic
import com.melabsinthiatum.model.nodes.RootNode

class SharedElementsTopics {
    companion object {
        var SHARED_ELEMENTS_TREE_TOPIC: Topic<SharedElementsTopicsNotifier> =
            Topic.create("custom name", SharedElementsTopicsNotifier::class.java)
    }
}

interface SharedElementsTopicsNotifier {
    fun sharedElementsUpdated(root: RootNode)
}
