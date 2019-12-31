package com.melabsinthiatum.view.toolWindow

import com.intellij.codeInsight.highlighting.HighlightManager
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.ScrollType
import com.intellij.openapi.editor.colors.EditorColors
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.editor.event.EditorMouseEvent
import com.intellij.openapi.editor.event.EditorMouseListener
import com.intellij.openapi.editor.markup.RangeHighlighter
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.project.ProjectManagerListener
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ex.WindowManagerEx
import com.intellij.ui.AnActionButton
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.treeStructure.Tree
import com.intellij.util.messages.MessageBus
import com.melabsinthiatum.extensionPoints.SharedElementsTopics
import com.melabsinthiatum.extensionPoints.SharedElementsTopicsNotifier
import com.melabsinthiatum.imageManager.CustomIcons
import com.melabsinthiatum.model.nodes.CustomNodeInterface
import com.melabsinthiatum.model.nodes.ExpectOrActualNode
import com.melabsinthiatum.model.nodes.RootNode
import com.melabsinthiatum.model.nodes.TemplateNodeInterface
import com.melabsinthiatum.tree.UpdateManager
import com.melabsinthiatum.tree.diff.Insert
import com.melabsinthiatum.tree.diff.Remove
import com.melabsinthiatum.tree.diff.TreeMutation
import com.melabsinthiatum.tree.diff.TreesDiffManager
import com.melabsinthiatum.view.MppSharedItemsTreeCellRenderer
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.kotlin.psi.psiUtil.endOffset
import org.jetbrains.kotlin.psi.psiUtil.startOffset
import java.util.*
import javax.swing.JPanel
import javax.swing.JTree
import javax.swing.SwingUtilities
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreePath
import javax.swing.tree.TreeSelectionModel


class MppToolWindow(private val project: Project, private val toolWindow: ToolWindow) {

    var content: JPanel

    private val sharedElementsTree: Tree
    private val treeModel: DefaultTreeModel
    private var treeRoot: RootNode = RootNode()
    private val updateManager = UpdateManager(project)
    private var updateJob: Job? = null
    private val updateInterval: Long = 5 * 1000
    private var msgBus = project.messageBus.connect(project)

    init {
        treeModel = DefaultTreeModel(treeRoot)
        sharedElementsTree = Tree(treeModel)

        val decorator = decorator(sharedElementsTree)
        val panel = decorator.createPanel()
        content = panel

        subscribe(project.messageBus)

        configureSharedElementsTree()

        WindowManagerEx.getInstanceEx().getFocusedComponent(project)
        updateJob = startCoroutineTimer(repeatMillis = updateInterval) { runUpdate() }

        msgBus.subscribe(ProjectManager.TOPIC, object : ProjectManagerListener {
            override fun projectClosingBeforeSave(project: Project) {
                updateJob?.cancel()
            }
        })
    }

    private fun decorator(tree: Tree): ToolbarDecorator {
        val refreshActionButton = AnActionButton.fromAction(ActionManager.getInstance().getAction("RefreshTree"))
        refreshActionButton.templatePresentation.icon = CustomIcons.Actions.Refresh

        return ToolbarDecorator.createDecorator(tree)
            .initPosition()
            .disableAddAction().disableRemoveAction().disableDownAction().disableUpAction()
            .addExtraAction(refreshActionButton)
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

    private fun subscribe(bus: MessageBus) {
        bus.connect().subscribe(SharedElementsTopics.SHARED_ELEMENTS_TREE_TOPIC, object : SharedElementsTopicsNotifier {
            override fun sharedElementsUpdated(root: RootNode) {
                val diffTree = TreesDiffManager().makeMutationsTree(treeRoot, root) ?: return
                updateTree(treeRoot, diffTree)
            }
        })
    }

    private fun updateTree(sourceNode: CustomNodeInterface, diffNode: DefaultMutableTreeNode) {
        val diffNodeModel = diffNode.userObject as? TreesDiffManager.DiffNodeModel<*> ?: return
        if (sourceNode.nodeModel() != diffNodeModel.sourceNodeModel) {
            println("sourceNodeModel type cast error")
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
            while (true) {
                action()
                delay(repeatMillis)
            }
        } else {
            action()
        }
    }

