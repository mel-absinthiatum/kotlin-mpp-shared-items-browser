[![Version](https://img.shields.io/jetbrains/plugin/v/13652-kotlin-mpp-shared-items-browser.svg)](https://plugins.jetbrains.com/plugin/13652-kotlin-mpp-shared-items-browser/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

# kotlin-mpp-shared-items-browser
Plugin for Intellij IDEA and Android Studio, providing a tree of shared (expect/actual) project elements

## Plugin page
[JetBrains Plugin Repository](https://plugins.jetbrains.com/plugin/13652-kotlin-mpp-shared-items-browser/)

## Features
Kotlin MPP Shared Items Browser: 
* Builds the tree of shared elements in a special tool window with types indication, lists  places of common `expect` and platform `actual` element’s declarations.
* Includes hierarchic displaying of nested shared elements.
* Provides navigation to elements (opening the editor, scrolling to position in code, highlighting).
* Partially reloads the tree of elements by diffs, obtained when comparing _before_ and _after_ update trees.
* Updates by using a button and by a timer, if the plugin tool window is visible and there is an active editor.
* Allows to set the update interval. The settings are stored persistently.

<br>

![demo](https://user-images.githubusercontent.com/56015356/73127710-29dc5900-3ff7-11ea-8bc6-c82fe6bda0ed.gif)

## Installation
The plugin can be installed with **Settings | Plugins | Browse Repositories** and no further set up is required.
