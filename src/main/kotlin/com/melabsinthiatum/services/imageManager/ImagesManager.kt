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

package com.melabsinthiatum.services.imageManager

import com.intellij.icons.AllIcons
import com.intellij.openapi.util.IconLoader
import javax.swing.Icon


class CustomIcons {

    companion object {
        private fun load(path: String): Icon {
            return IconLoader.getIcon(path)
        }
    }

    class Nodes {
        companion object {
            val Root = AllIcons.Nodes.Folder
            val File = load("com/melabsinthiatum/file.png")
            val Annotation = load("com/melabsinthiatum/annotation.png")
            val Property = load("com/melabsinthiatum/property.png")
            val Function = load("com/melabsinthiatum/func.png")
            val Class = load("com/melabsinthiatum/class.png")
            val Object = load("com/melabsinthiatum/obj.png")
            val Expect = load("com/melabsinthiatum/expect.png")
            val Actual = load("com/melabsinthiatum/actual.png")
        }
    }

    class Actions {
        companion object {
            val Refresh = AllIcons.Actions.Refresh
            val Settings = AllIcons.General.Settings
        }
    }
}
