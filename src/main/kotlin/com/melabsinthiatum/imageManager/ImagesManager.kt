package com.melabsinthiatum.imageManager

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
        }
    }
}
