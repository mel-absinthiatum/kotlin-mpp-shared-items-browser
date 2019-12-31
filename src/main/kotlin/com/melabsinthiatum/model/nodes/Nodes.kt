package com.melabsinthiatum.model.nodes

import com.melabsinthiatum.model.nodes.model.*


interface SharedElementContent : CustomNodeInterface


class ExpectOrActualNode(model: ExpectOrActualModel) : TemplateLeaf<ExpectOrActualModel>(model),
    SharedElementContent

class SharedElementNode(model: SharedElementModel) :
    TemplateNode<SharedElementModel, SharedElementContent>(model),
    SharedElementContent

class PackageNode(model: FileNodeModel) : TemplateNode<FileNodeModel, SharedElementNode>(model)

class MppAuthorityZoneNode(model: MppAuthorityZoneModel) :
    TemplateNode<MppAuthorityZoneModel, PackageNode>(model)

class RootNode : TemplateRootNode<NodeModel, MppAuthorityZoneNode>(RootNodeModel)
