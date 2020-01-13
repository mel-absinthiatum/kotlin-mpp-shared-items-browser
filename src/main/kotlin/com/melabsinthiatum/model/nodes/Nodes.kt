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

class RootNode(projectName: String) : TemplateRootNode<NodeModel, MppAuthorityZoneNode>(RootNodeModel(projectName))
