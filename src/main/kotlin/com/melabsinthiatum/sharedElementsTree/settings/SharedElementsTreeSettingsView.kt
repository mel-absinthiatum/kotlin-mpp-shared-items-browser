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

package com.melabsinthiatum.sharedElementsTree.settings

import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.ui.DialogWrapper
import com.melabsinthiatum.services.persistence.TreeSettingsComponent
import com.sun.java.accessibility.util.AWTEventMonitor.addWindowListener
import java.awt.Component
import java.awt.event.*
import javax.swing.*


class SharedElementsTreeSettingsViewWrapper : DialogWrapper(true) {

    init {
        title = "Shared elements tree settings"
    }

    private val defaultReloadInterval: Long = 10
    private val minReloadInterval: Long = 0
    private val maxReloadInterval: Long = 10 * 60
    private val reloadIntervalStepSize = 1

    private val settingsState: TreeSettingsComponent.State?
    private var reloadIntervalSpinner: JSpinner

    init {
        val settingsService = ServiceManager.getService(TreeSettingsComponent::class.java)
        settingsState = settingsService?.state

        val initialReloadInterval = settingsState?.reloadInterval
        val spinnerInitialValue =
            if (initialReloadInterval != null && initialReloadInterval in minReloadInterval..maxReloadInterval) {
                initialReloadInterval
            } else {
                defaultReloadInterval
            }

        reloadIntervalSpinner = JSpinner(
            SpinnerNumberModel(
                spinnerInitialValue,
                minReloadInterval,
                maxReloadInterval,
                reloadIntervalStepSize
            )
        )
        init()
    }

    override fun createCenterPanel(): JComponent? {
        val dialogPanel = JPanel()
        dialogPanel.layout = BoxLayout(dialogPanel, BoxLayout.LINE_AXIS)

        val label = JLabel("Tree reloading interval. Zero (0) is for `never`: ")
        label.alignmentX = Component.LEFT_ALIGNMENT

        reloadIntervalSpinner.alignmentX = Component.LEFT_ALIGNMENT

        dialogPanel.add(label)
        dialogPanel.add(reloadIntervalSpinner)

        addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent?) {
                onCancel()
            }
        })

        dialogPanel.registerKeyboardAction(
            { onCancel() },
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
            JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT
        )

        return dialogPanel
    }

    override fun createButtonsPanel(buttons: MutableList<out JButton>): JPanel {
        val buttonsPane = JPanel()
        buttonsPane.layout = BoxLayout(buttonsPane, BoxLayout.LINE_AXIS)

        val cancelButton = JButton(cancelAction)
        cancelButton.addActionListener { onCancel() }

        val okButton = JButton(okAction)
        okButton.addActionListener { onOK() }

        buttonsPane.add(cancelButton)
        buttonsPane.add(okButton)

        rootPane.defaultButton = okButton
        return buttonsPane

    }

    private fun isModified(): Boolean {
        val initialReloadInterval = settingsState?.reloadInterval ?: return true
        return initialReloadInterval != (reloadIntervalSpinner.model.value as? Number)?.toLong()
    }


    private fun onOK() {
        if (isModified()) {
            val newReloadInterval = reloadIntervalSpinner.model.value as? Number
            if (newReloadInterval == null) {
                dispose()
                return
            }
            val reloadInterval = newReloadInterval.toLong()
            ServiceManager.getService(TreeSettingsComponent::class.java)
                ?.loadState(TreeSettingsComponent.State(reloadInterval))
        }
        dispose()
    }

    private fun onCancel() {
        dispose()
    }
}