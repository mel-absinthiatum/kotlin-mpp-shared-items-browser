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

package com.melabsinthiatum.sharedElementsTree.toolWindow

import com.intellij.codeInsight.highlighting.HighlightManager
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.ScrollType
import com.intellij.openapi.editor.colors.EditorColors
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.editor.event.EditorMouseEvent
import com.intellij.openapi.editor.event.EditorMouseListener
import com.intellij.openapi.editor.markup.RangeHighlighter
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.*
import com.intellij.openapi.wm.ToolWindow
import com.intellij.ui.AnActionButton
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.treeStructure.Tree
import com.intellij.util.messages.MessageBusConnection
import com.melabsinthiatum.model.nodes.*
import com.melabsinthiatum.services.extensionPoints.*
import com.melabsinthiatum.services.imageManager.CustomIcons
import com.melabsinthiatum.services.logging.Loggable
import com.melabsinthiatum.services.logging.logger
import com.melabsinthiatum.services.persistence.TreeSettingsComponent
import com.melabsinthiatum.sharedElementsTree.MppSharedItemsTreeCellRenderer
import com.melabsinthiatum.sharedElementsTree.tree.UpdateManager
import com.melabsinthiatum.sharedElementsTree.tree.diff.*
import kotlinx.coroutines.*
import org.jetbrains.kotlin.psi.psiUtil.endOffset
import org.jetbrains.kotlin.psi.psiUtil.startOffset
import java.util.*
import javax.swing.*
import javax.swing.tree.*


class MppToolWindow(private val project: Project, private val toolWindow: ToolWindow): Loggable {

    var content: JPanel

    private val logger = logger()
    private val sharedElementsTree: Tree
    private val treeModel: DefaultTreeModel
    private var treeRoot: RootNode = RootNode(project.name)
    private val updateManager = UpdateManager(project)
    private var updateJob: Job? = null
    private var updateInterval: Long
    private var msgBus = project.messageBus.connect(project)

    init {
        logger.info("Tool window is created.")
        treeModel = DefaultTreeModel(treeRoot)
        sharedElementsTree = Tree(treeModel)
        configureSharedElementsTree()
        val decorator = decorator(sharedElementsTree)
        val panel = decorator.createPanel()
        content = panel

        subscribe(msgBus)

        val service = ServiceManager.getService(TreeSettingsComponent::class.java)
        updateInterval = service?.state?.reloadInterval ?: 10
        updateJob = runRepeatingUpdates(repeatMillis = updateInterval * 1000)
    }

    private fun decorator(tree: Tree): ToolbarDecorator {
        val refreshActionButton = AnActionButton.fromAction(
            ActionManager.getInstance()
                .getAction("com.melabsinthiatum.actions.RefreshTreeAction")
        )
        refreshActionButton.templatePresentation.icon = CustomIcons.Actions.Refresh

        val treeSettingsButton = AnActionButton.fromAction(
            ActionManager.getInstance()
                .getAction("com.melabsinthiatum.actions.TreeSettingsAction")
        )
        treeSettingsButton.templatePresentation.icon = CustomIcons.Actions.Settings

        return ToolbarDecorator.createDecorator(tree)
            .initPosition()
            .disableAddAction().disableRemoveAction().disableDownAction().disableUpAction()
            .addExtraActions(refreshActionButton)
            .addExtraAction(treeSettingsButton)
    }

    private fun configureSharedElementsTree() {
        sharedElementsTree.run {
            isRootVisible = false
            selectionModel.selectionMode = TreeSelectionModel.SINGLE_TREE_SELECTION
            cellRenderer = MppSharedItemsTreeCellRenderer(sharedElementsTree)
            addTreeSelectionListener { event ->
                val source = event.source as JTree
                val node = source.lastSelectedPathComponent as? ExpectOrActualNode
                source.clearSelection()
                val psi = node?.model?.psi
                val file = psi?.containingFile?.virtualFile
                if (psi != null && file != null && file.isValid && !file.isDirectory) {
                    val fileEditorManager = FileEditorManager.getInstance(project)
                    fileEditorManager.openFile(file, true)
                    fileEditorManager.selectedTextEditor?.highlightOnce(psi.startOffset, psi.endOffset)
                }
            }
        }
    }

    private fun subscribe(bus: MessageBusConnection) {
        bus.subscribe(ProjectManager.TOPIC, object : ProjectManagerListener {
            override fun projectClosingBeforeSave(project: Project) {
                updateJob?.cancel()
            }
        })

        bus.subscribe(SharedElementsTopics.SHARED_ELEMENTS_TREE_TOPIC, object : SharedElementsTopicsNotifier {
            override fun sharedElementsUpdated(root: RootNode) {
                val diffTree = TreesDiffManager().makeMutationsTree(treeRoot, root) ?: return
                updateTree(treeRoot, diffTree)
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

    private fun updateTree(sourceNode: CustomNodeInterface, diffNode: DefaultMutableTreeNode) {
        val diffNodeModel = diffNode.userObject as? TreesDiffManager.DiffNodeModel<*> ?: return
        if (sourceNode.nodeModel() != diffNodeModel.sourceNodeModel) {
            return
        }

        val mutatedChildren = diffNode.children().toList()
        mutatedChildren.forEach {
            val mutatedChildNode = it as? DefaultMutableTreeNode ?: return@forEach
            val mutatedChildModel = mutatedChildNode.userObject as? TreesDiffManager.DiffNodeModel<*> ?: return@forEach
            for (sourceChild in sourceNode.childNodes()) {
                if (sourceChild.nodeModel() == mutatedChildModel.sourceNodeModel) {
                    updateTree(sourceChild, mutatedChildNode)
                    break
                }
            }
        }

        val mutations = diffNodeModel.mutations
        mutations.forEach { handleMutation(sourceNode, it) }

        reloadTree()
    }

    private fun handleMutation(node: CustomNodeInterface, mutation: TreeMutation) {
        val targetNode = node as? TemplateNodeInterface<*, *> ?: return
        when (mutation) {
            is Insert -> targetNode.insert(mutation.node, targetNode.childCount)
            is Remove -> targetNode.remove(mutation.node)
        }
    }

    private fun reloadTree() {
        val expanded = ArrayList<TreePath>()
        for (i in 0 until sharedElementsTree.rowCount) {
            if (sharedElementsTree.isExpanded(i)) {
                expanded.add(sharedElementsTree.getPathForRow(i))
            }
        }
        treeModel.reload()
        for (path in expanded) {
            sharedElementsTree.expandPath(path)
        }
    }

    private fun runRepeatingUpdates(delayMillis: Long = 0, repeatMillis: Long = 0): Job {
        logger.debug("Run repeating updates $repeatMillis.")
        return startCoroutineTimer(delayMillis, repeatMillis) { runUpdate() }
    }

    private fun runUpdate() {
        val isAnyFocusedEditor = FileEditorManager.getInstance(project).selectedEditors.any { editor ->
            val window = SwingUtilities.getWindowAncestor(editor.component)
            window != null && window.isFocused
        }

        val isToolWindowVisible = toolWindow.isVisible

        if (isAnyFocusedEditor && isToolWindowVisible) {
            updateManager.update()
        }
    }
}

private fun Editor.highlightOnce(startOffset: Int, endOffset: Int) {
    val attributes = EditorColorsManager.getInstance().globalScheme.getAttributes(
        EditorColors.SEARCH_RESULT_ATTRIBUTES
    )

    val outHighlighters = mutableListOf<RangeHighlighter>()
    HighlightManager.getInstance(project).addOccurrenceHighlight(
        this,
        startOffset,
        endOffset,
        attributes,
        HighlightManager.HIDE_BY_ANY_KEY,
        outHighlighters,
        null
    )
    this.addEditorMouseListener(object : EditorMouseListener {
        override fun mouseReleased(event: EditorMouseEvent) {
            outHighlighters.forEach { it.dispose() }
        }
    })

    val caretModel = this.caretModel
    caretModel.primaryCaret.moveToOffset(startOffset)
    val scrollingModel = this.scrollingModel

    scrollingModel.scrollToCaret(ScrollType.CENTER_UP)
}

private fun startCoroutineTimer(delayMillis: Long = 0, repeatMillis: Long = 0, action: () -> Unit) =
    GlobalScope.launch {
        delay(delayMillis)
        if (repeatMillis > 0) {
            while (isActive) {
                action()
                delay(repeatMillis)
            }
        } else {
            action()
        }
    }

