/*
 * Copyright (c) 2019 mel-absinthiatum
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package com.melabsinthiatum.services.persistence

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.*
import com.melabsinthiatum.services.extensionPoints.SharedElementsTopics
import com.melabsinthiatum.services.extensionPoints.SharedElementsTreeSettingsNotifier

/**
 * <code>TreeSettingsComponent</code> is a persistent store service
 * for Shared Elements Browser settings. These settings are currently common
 * to the entire application.
 */
@State(
    name = "com.melabsinthiatum.services.persistence.TreeSettingsComponent",
    storages = [
        Storage(value = "TreeSettingsComponent.xml")
    ]
)
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
