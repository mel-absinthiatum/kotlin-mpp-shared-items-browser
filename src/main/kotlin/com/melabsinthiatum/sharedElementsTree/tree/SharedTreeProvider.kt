package com.melabsinthiatum.sharedElementsTree.tree

import com.intellij.openapi.project.DumbServiceImpl
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.psi.*
import com.melabsinthiatum.model.DeclarationType
import com.melabsinthiatum.model.SharedType
import com.melabsinthiatum.model.modulesRoutines.MppAuthorityManager
import com.melabsinthiatum.model.modulesRoutines.MppAuthorityZone
import com.melabsinthiatum.model.nodes.*
import com.melabsinthiatum.model.nodes.model.*
import kotlinx.coroutines.runBlocking
import org.jetbrains.kotlin.idea.util.*
import org.jetbrains.kotlin.psi.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


class SharedTreeProvider {

    suspend fun sharedTreeRoot(project: Project): RootNode = suspendCoroutine { cont ->
        DumbServiceImpl.getInstance(project).smartInvokeLater {
            runBlocking {
                val rootNode = RootNode()
                rootNode.add(iterateAllZones(project))
                cont.resume(rootNode)
            }
        }
    }

    private fun iterateAllZones(project: Project): List<MppAuthorityZoneNode> {
        val mppAuthorityZones = MppAuthorityManager().provideAuthorityZonesForProject(project)
        return mppAuthorityZones.mapNotNull { authorityZone ->
            val list = iterateTree(authorityZone, project)

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
//
//            }
        }
    }

    private fun iterateTree(authorityZone: MppAuthorityZone, project: Project): List<PackageNode> {
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
            val children = registerDeclaration(psiFile)

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

    private fun registerDeclaration(element: PsiElement): List<SharedElementNode> {
        val list = mutableListOf<SharedElementNode>()
        element.acceptChildren(
            namedDeclarationVisitor { declaration ->
                val node = makeElementNode(declaration) ?: return@namedDeclarationVisitor
                list.add(node)
            })
        return list
    }

    private fun registerNestedDeclaration(element: Array<PsiElement>): List<SharedElementNode> {
        val list = mutableListOf<SharedElementNode>()
        element.forEach {
            it.accept(
                namedDeclarationVisitor { declaration ->
                    val node = makeElementNode(declaration) ?: return@namedDeclarationVisitor
                    list.add(node)
                })
        }
        return list
    }

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
        if (element.name == null) {
            assert(false) { "Empty element name." }
            return null
        }
        val model = ExpectOrActualModel(element.name!!, element, SharedType.EXPECTED, null)
        return ExpectOrActualNode(model)
    }

    private fun makeActualNodesForElement(element: KtDeclaration): List<ExpectOrActualNode> {
        val actuals = element.actualsForExpected()
        return actuals.mapNotNull {
            if (it.name == null) {
                assert(false) { "Empty element name." }
                null
            } else {
                val model = ExpectOrActualModel(it.name!!, it, SharedType.ACTUAL, null)
                ExpectOrActualNode(model)
            }
        }
    }

    private fun registerAnnotation(annotationClass: KtClass): SharedElementNode {
        val stub = annotationClass.stub
        val model =
            SharedElementModel(annotationClass.name, DeclarationType.ANNOTATION, stub)
        return SharedElementNode(model)
    }

    private fun registerProperty(property: KtProperty): SharedElementNode {
        val stub = property.stub
        val model = SharedElementModel(property.name, DeclarationType.PROPERTY, stub)
        return SharedElementNode(model)
    }

    private fun registerNamedFunction(function: KtNamedFunction): SharedElementNode {
        val stub = function.stub

        val model = SharedElementModel(function.name, DeclarationType.NAMED_FUNCTION, stub)
        return SharedElementNode(model)
    }

    private fun registerClass(classDeclaration: KtClass): SharedElementNode {
        val stub = classDeclaration.stub

        val model = SharedElementModel(classDeclaration.name, DeclarationType.CLASS, stub)

        val node = SharedElementNode(model)

        val nested = classDeclaration.body?.children

        if (nested != null) {
            val children = registerNestedDeclaration(nested)
            node.add(children)
        }

        return node
    }

    private fun registerObject(
        objectDeclaration: KtObjectDeclaration
    ): SharedElementNode {
        val stub = objectDeclaration.stub

        val model =
            SharedElementModel(objectDeclaration.name, DeclarationType.OBJECT, stub)

        val node = SharedElementNode(model)

        val nested = objectDeclaration.body?.children

        if (nested != null) {
            val children = registerNestedDeclaration(nested)
            node.add(children)
        }

        return node
    }
}
