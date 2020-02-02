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

package com.melabsinthiatum.sharedElementsBrowser.toolWindow

import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.project.*
import com.intellij.openapi.wm.ToolWindow
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.treeStructure.Tree
import com.intellij.util.messages.MessageBusConnection
import com.intellij.util.ui.JBUI
import com.melabsinthiatum.model.nodes.RootNode
import com.melabsinthiatum.model.nodes.model.NodeModel
import com.melabsinthiatum.model.nodes.model.RootNodeModel
import com.melabsinthiatum.model.nodes.toDefaultTree
import com.melabsinthiatum.services.extensionPoints.*
import com.melabsinthiatum.services.logging.Loggable
import com.melabsinthiatum.services.logging.logger
import com.melabsinthiatum.services.persistence.TreeSettingsComponent
import com.melabsinthiatum.sharedElements.SharedElementsUpdateManager
import com.melabsinthiatum.sharedElements.diff.*
import com.melabsinthiatum.sharedElementsBrowser.editor.SharedElementNavigationManager
import com.melabsinthiatum.sharedElementsBrowser.toolWindow.tree.*
import kotlinx.coroutines.*
import java.awt.BorderLayout
import javax.swing.Box
import javax.swing.JPanel
import javax.swing.tree.*

/**
 * <code>SharedElementsBrowser</code> is a tool window content panel containing
 * a shared elements tree providing the ability to navigate to objects as well
 * as settings and update buttons.
 *
 * Also, the browser can perform updates by a predefined in the settings time interval.
 *
 * The tree updates only if the tool window is visible.
 */
class SharedElementsBrowser(private val project: Project, private val toolWindow: ToolWindow) : JPanel(), Loggable {

    private val logger = logger()
    private var treeRoot: DefaultMutableTreeNode = DefaultMutableTreeNode(RootNodeModel(project.name))
    private val treeModel: DefaultTreeModel = DefaultTreeModel(treeRoot)
    private val sharedElementsTree: Tree = Tree(treeModel)
    private val updateManager = SharedElementsUpdateManager
    private val diffManager = TreeDiffManager
    private var updateJob: Job? = null
    private var reloadInterval: Long = 0
    private var msgBus = project.messageBus.connect(project)
    private val elementsSelectionHandler = SharedElementsTreeSelectionHandler(SharedElementNavigationManager(project))

    // DEBUG
    private fun log(msg: String) = println("[${Thread.currentThread().name}] $msg")

    init {
        createPanel()
        launchFlows()

        logger.info("Tool window is created.")
    }


    //
    //  Initial configuration
    //

    private fun createPanel() {
        super.setLayout(BorderLayout())

        val mainActionGroup =
            ActionManager.getInstance().getAction("SharedElementsBrowserActions") as ActionGroup
        val toolbar =
            ActionManager.getInstance().createActionToolbar(
                "Expect-Actual Interface", mainActionGroup, true
            )
        val toolBarBox = Box.createHorizontalBox()
        toolBarBox.add(toolbar.component)

        configureSharedElementsTree()
        val treePanel = JPanel(BorderLayout())
        treePanel.add(JBScrollPane(sharedElementsTree), BorderLayout.CENTER)

        border = JBUI.Borders.empty(-1)
        add(toolBarBox, BorderLayout.NORTH)
        add(treePanel, BorderLayout.CENTER)
        toolbar.component.isVisible = true
    }

    private fun configureSharedElementsTree() {
        sharedElementsTree.run {
            isRootVisible = false
            selectionModel.selectionMode = TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION
            cellRenderer = SharedElementsTreeCellRenderer(sharedElementsTree)
            addKeyListener(SharedElementsTreeKeyListener(this, elementsSelectionHandler))
            addMouseListener(SharedElementsTreeMouseListener(this, elementsSelectionHandler))
        }
    }


    //
    //  Initial flows
    //

    private fun launchFlows() {
        subscribe(msgBus)

        val service = ServiceManager.getService(TreeSettingsComponent::class.java)
        service?.state?.reloadInterval?.let { reloadInterval = it }

        val myBus = ApplicationManager.getApplication().messageBus
        val publisher: SharedElementsTreeSettingsNotifier =
            myBus.syncPublisher(SharedElementsTopics.SHARED_ELEMENTS_TREE_SETTINGS_TOPIC)
        publisher.reloadIntervalUpdated(reloadInterval, reloadInterval)
    }

    private fun subscribe(bus: MessageBusConnection) {
        bus.subscribe(ProjectManager.TOPIC, object : ProjectManagerListener {
            override fun projectClosingBeforeSave(project: Project) {
                updateJob?.cancel()
            }
        })

        bus.subscribe(SharedElementsTopics.SHARED_ELEMENTS_TREE_TOPIC, object : SharedElementsTopicsNotifier {
            override fun sharedElementsUpdated(root: RootNode) {
                val defaultRoot = root.toDefaultTree()
                val diffTree = diffManager.makeMutationsTree(oldNode = treeRoot, newNode = defaultRoot)
                if (diffTree != null) {
                    updateTree(treeRoot, diffTree)
                    reloadTree()
                }
            }
        })

        bus.subscribe(
            SharedElementsTopics.SHARED_ELEMENTS_TREE_SETTINGS_TOPIC,
            object : SharedElementsTreeSettingsNotifier {
                override fun reloadIntervalUpdated(oldValue: Long, newValue: Long) {
                    updateJob?.cancel()
                    updateJob = runRepeatingUpdates(repeatMillis = newValue * 1000)
                }
            })
    }


    //
    //  Updates launching
    //

    private fun runRepeatingUpdates(delayMillis: Long = 0, repeatMillis: Long = 0): Job {
        logger.debug("Run repeating updates $repeatMillis.")
        return startCoroutineTimer(delayMillis, repeatMillis) { runUpdate() }
    }

    private fun runUpdate() {
        // DEBUG
//        Notifications.Bus.notify(
//            Notification(
//                "com.melabsinthiatum.tree_reload_debug_notification",
//                "update ${Calendar.getInstance().time}",
//                "interval $reloadInterval",
//                NotificationType.INFORMATION
//            )
//        )
        if (toolWindow.isVisible) {
            updateManager.update(project)
        }
    }


    //
    //  Tree reloading
    //

    private fun updateTree(sourceNode: DefaultMutableTreeNode, diffNode: DefaultMutableTreeNode) {
        val diffNodeModel = diffNode.userObject as? TreeDiffManager.DiffNodeModel<*> ?: return
        val sourceNodeModel = sourceNode.userObject as? NodeModel ?: return

        if (sourceNodeModel != diffNodeModel.sourceNodeModel) {
            return
        }
        val sourceNodeChildren = sourceNode.children().toList().filterIsInstance<DefaultMutableTreeNode>()

        val mutatedChildren = diffNode.children().toList()
        mutatedChildren.forEach {
            val mutatedChildNode = it as? DefaultMutableTreeNode ?: return@forEach
            val mutatedChildModel = mutatedChildNode.userObject as? TreeDiffManager.DiffNodeModel<*> ?: return@forEach
            for (sourceChild in sourceNodeChildren) {
                if (sourceChild.userObject == mutatedChildModel.sourceNodeModel) {
                    updateTree(sourceChild, mutatedChildNode)
                    break
                }
            }
        }
        val mutations = diffNodeModel.mutations
        mutations.forEach { handleMutation(sourceNode, it) }
    }

    private fun handleMutation(node: DefaultMutableTreeNode, mutation: TreeMutation) {
        when (mutation) {
            is Insert -> node.add(mutation.node)
            is Remove -> node.remove(mutation.node)
        }
    }

    private fun reloadTree() {
        val expanded = sharedElementsTree.getExpandedPaths()
        treeModel.reload()
        sharedElementsTree.expand(expanded)
    }
}

private fun startCoroutineTimer(delayMillis: Long = 0, repeatMillis: Long = 0, action: () -> Unit) =
    GlobalScope.launch {
        delay(delayMillis)
        if (repeatMillis > 0) {
            while (isActive) {
                launch(Dispatchers.Main) {
                    action()
                }
                delay(repeatMillis)
            }
        } else {
            action()
        }
    }