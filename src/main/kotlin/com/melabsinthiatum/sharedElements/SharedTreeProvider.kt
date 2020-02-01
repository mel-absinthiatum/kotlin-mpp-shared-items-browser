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

package com.melabsinthiatum.sharedElements

import com.intellij.openapi.project.DumbServiceImpl
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Iconable.ICON_FLAG_READ_STATUS
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.psi.*
import com.melabsinthiatum.model.DeclarationType
import com.melabsinthiatum.model.SharedType
import com.melabsinthiatum.model.modulesRoutines.MppAuthorityZone
import com.melabsinthiatum.model.modulesRoutines.MppAuthorityZonesManager
import com.melabsinthiatum.model.nodes.*
import com.melabsinthiatum.model.nodes.model.*
import org.jetbrains.kotlin.idea.util.*
import org.jetbrains.kotlin.psi.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


/**
 * <code>SharedTreeProvider</code> provides the shared elements tree for a specific project.
 */
class SharedTreeProvider {

    private val mppAuthorityZonesManager = MppAuthorityZonesManager

    suspend fun sharedTreeRoot(project: Project): RootNode = suspendCoroutine { cont ->
        DumbServiceImpl.getInstance(project).smartInvokeLater {
            val rootNode = RootNode(project.name)
            rootNode.add(iterateAllZones(project))
            cont.resume(rootNode)
        }
    }


    //
    //  Iterate through files
    //

    private fun iterateAllZones(project: Project): List<MppAuthorityZoneNode> {
        val mppAuthorityZones = mppAuthorityZonesManager.provideAuthorityZonesForProject(project)
        return mppAuthorityZones.mapNotNull { authorityZone ->
            val list = iterateThroughAuthorityZone(authorityZone, project)

            if (list.isNotEmpty()) {
                val mppNodeModel = MppAuthorityZoneModel(authorityZone.commonModule.name)
                val node = MppAuthorityZoneNode(mppNodeModel)
                node.add(list)
                node
            } else {
                null
            }

            // DEBUG

//            list.forEach { fileNode ->
//                println("file node: ${fileNode.sourceNodeModel.title}")
//
//                fileNode.children.forEach { node ->
//                    println("__collected ${node.sourceNodeModel.name}")
//
//                    node.children.forEach { child ->
//                        when (child) {
//                            is SharedItemNode -> println("____collected: ${child.sourceNodeModel.name}")
//                            is ExpectOrActuaItemlNode -> println("_____expect or actual")
//                        }
//                    }
//                }
//            }
        }
    }

    private fun iterateThroughAuthorityZone(authorityZone: MppAuthorityZone, project: Project): List<PackageNode> {
        val sourceRoots = authorityZone.commonModule.sourceRoots

        val psiFiles = mutableListOf<PsiFile>()
        sourceRoots.forEach { vf ->
            VfsUtilCore.iterateChildrenRecursively(vf, null, { virtualFile ->
                val psiFile = PsiManager.getInstance(project).findFile(virtualFile)
                if (psiFile != null) {
                    psiFiles.add(psiFile)
                }
                true
            })
        }

        return psiFiles.mapNotNull { psiFile ->
            val children = registerGlobalLevelDeclaration(psiFile)

            if (children.isNotEmpty()) {
                val fileNodeModel = FileNodeModel(psiFile.name, psiFile.virtualFile)
                val node = PackageNode(fileNodeModel)
                node.add(children)
                node
            } else {
                null
            }
        }
    }


    //
    // Handle shared declaration
    //

    private fun registerGlobalLevelDeclaration(element: PsiElement): List<SharedElementNode> {
        val list = mutableListOf<SharedElementNode>()
        element.acceptChildren(
            namedDeclarationVisitor { declaration ->
                makeElementNode(declaration)?.let { node -> list.add(node) }
            })
        return list
    }

    private fun registerNestedDeclaration(element: Array<PsiElement>): List<SharedElementNode> {
        val list = mutableListOf<SharedElementNode>()
        element.forEach {
            it.accept(
                namedDeclarationVisitor { declaration ->
                    makeElementNode(declaration)?.let { node -> list.add(node) }
                })
        }
        return list
    }


    //
    //  Register shared element with common and platform declarations
    //

    private fun makeElementNode(declaration: KtDeclaration): SharedElementNode? {
        val isConformingSharedDeclaration = declaration.isExpectDeclaration()
        if (!isConformingSharedDeclaration) {
            return null
        }

        val node = when (declaration) {
            is KtClass -> {
                if (declaration.isAnnotation()) registerAnnotation(declaration) else registerClass(declaration)
            }
            is KtNamedFunction -> registerNamedFunction(declaration)
            is KtProperty -> registerProperty(declaration)
            is KtObjectDeclaration -> registerObject(declaration)
            else -> null
        }
        val expectDeclarationNode = makeExpectNodeForElement(declaration)
        if (expectDeclarationNode != null) {
            node?.add(expectDeclarationNode)
        }
        node?.add(makeActualNodesForElement(declaration))

        return node
    }

    private fun makeExpectNodeForElement(element: KtDeclaration): ExpectOrActualNode? {
        return makeExpectOrActualNode(element, SharedType.EXPECT)
    }

    private fun makeActualNodesForElement(element: KtDeclaration): List<ExpectOrActualNode> =
        try {
            element.actualsForExpected().mapNotNull {
                makeExpectOrActualNode(it, SharedType.ACTUAL)
            }
        } catch (e: Throwable) {
            emptyList()
        }

    private fun makeExpectOrActualNode(element: KtDeclaration, type: SharedType): ExpectOrActualNode? {
        if (element.name == null) {
            assert(false) { "Empty element name." }
            return null
        }
        val model = ExpectOrActualModel(element.name!!, element, type)
        return ExpectOrActualNode(model)
    }


    //
    //  Register different PSI Declarations
    //

    private fun registerAnnotation(annotationClass: KtClass): SharedElementNode {
        val model = SharedElementModel(
            annotationClass.name,
            DeclarationType.ANNOTATION,
            annotationClass.getIcon(ICON_FLAG_READ_STATUS)
        )
        return SharedElementNode(model)
    }

    private fun registerProperty(property: KtProperty): SharedElementNode {
        val model = SharedElementModel(property.name, DeclarationType.PROPERTY, property.getIcon(ICON_FLAG_READ_STATUS))
        return SharedElementNode(model)
    }

    private fun registerNamedFunction(function: KtNamedFunction): SharedElementNode {
        val model =
            SharedElementModel(function.name, DeclarationType.NAMED_FUNCTION, function.getIcon(ICON_FLAG_READ_STATUS))
        return SharedElementNode(model)
    }

    private fun registerClass(classDeclaration: KtClass): SharedElementNode {
        val model = SharedElementModel(
            classDeclaration.name,
            DeclarationType.CLASS,
            classDeclaration.getIcon(ICON_FLAG_READ_STATUS)
        )

        val node = SharedElementNode(model)

        val nested = classDeclaration.body?.children

        if (nested != null) {
            val children = registerNestedDeclaration(nested)
            node.add(children)
        }

        return node
    }

    private fun registerObject(objectDeclaration: KtObjectDeclaration): SharedElementNode {
        val model = SharedElementModel(
            objectDeclaration.name,
            DeclarationType.OBJECT,
            objectDeclaration.getIcon(ICON_FLAG_READ_STATUS)
        )

        val node = SharedElementNode(model)

        val nested = objectDeclaration.body?.children

        if (nested != null) {
            val children = registerNestedDeclaration(nested)
            node.add(children)
        }

        return node
    }
}
