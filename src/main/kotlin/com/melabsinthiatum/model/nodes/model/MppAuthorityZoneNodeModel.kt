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

package com.melabsinthiatum.model.nodes.model

import com.melabsinthiatum.services.imageManager.CustomIcons
import javax.swing.Icon

/** <code>MppAuthorityZoneModelInterface</code> is a node model for an authority zone
 * representation in a Shared Elements tree.
 *
 * @see com.melabsinthiatum.model.modulesRoutines.MppAuthorityZone
 */
interface MppAuthorityZoneModelInterface {
    val title: String
}

data class MppAuthorityZoneModel(override val title: String): MppAuthorityZoneModelInterface, NodeModel {
    override fun getLabelText(): String = title
    override fun getIcon(): Icon? = CustomIcons.Nodes.Root
}
