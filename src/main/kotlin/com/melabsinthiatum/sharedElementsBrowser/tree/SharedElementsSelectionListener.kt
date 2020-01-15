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

package com.melabsinthiatum.sharedElementsBrowser.tree

import com.intellij.codeInsight.highlighting.HighlightManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.ScrollType
import com.intellij.openapi.editor.colors.EditorColors
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.editor.event.EditorMouseEvent
import com.intellij.openapi.editor.event.EditorMouseListener
import com.intellij.openapi.editor.markup.RangeHighlighter
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.melabsinthiatum.model.nodes.model.ExpectOrActualModelInterface
import org.jetbrains.kotlin.psi.psiUtil.endOffset
import org.jetbrains.kotlin.psi.psiUtil.startOffset
import javax.swing.JTree
import javax.swing.event.TreeSelectionEvent
import javax.swing.event.TreeSelectionListener
import javax.swing.tree.DefaultMutableTreeNode


class SharedElementsSelectionListener(val project: Project) : TreeSelectionListener{
    override fun valueChanged(event: TreeSelectionEvent?) {
        val source = event?.source as? JTree

        val node = source?.lastSelectedPathComponent as? DefaultMutableTreeNode
        val nodeModel = node?.userObject as? ExpectOrActualModelInterface
        val psi = nodeModel?.psi
        val file = psi?.containingFile?.virtualFile

        if (psi != null && file != null && file.isValid && !file.isDirectory) {
            val fileEditorManager = FileEditorManager.getInstance(project)
            fileEditorManager.openFile(file, true)
            fileEditorManager.selectedTextEditor?.highlightOnce(psi.startOffset, psi.endOffset)
        }

        source?.clearSelection()
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

