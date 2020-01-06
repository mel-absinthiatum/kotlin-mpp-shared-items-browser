package com.melabsinthiatum.services.persistence

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.*
import com.melabsinthiatum.services.extensionPoints.SharedElementsTopics
import com.melabsinthiatum.services.extensionPoints.SharedElementsTreeSettingsNotifier


@State(name = "com.melabsinthiatum.services.persistence.TreeSettingsComponent", storages = [Storage(value = "TreeSettingsComponent.xml")])
class TreeSettingsComponent: PersistentStateComponent<TreeSettingsComponent.State> {
    class State(
        var reloadInterval: Long = 10
    )

    private var myState = State()

    override fun getState(): State {
        return myState
    }

    override fun loadState(state: State) {
        val myBus = ApplicationManager.getApplication().messageBus
        val publisher: SharedElementsTreeSettingsNotifier =
            myBus.syncPublisher(SharedElementsTopics.SHARED_ELEMENTS_TREE_SETTINGS_TOPIC)
        publisher.reloadIntervalUpdated(myState.reloadInterval, state.reloadInterval)

        myState = state
    }
}
