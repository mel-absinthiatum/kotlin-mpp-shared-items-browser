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

import org.jetbrains.intellij.tasks.PatchPluginXmlTask
import org.jetbrains.intellij.tasks.PublishTask

group = "com.melabsinthiatum"
version = "1.1.0"

buildscript {
    repositories { mavenCentral() }
    dependencies { classpath(kotlin("gradle-plugin", "1.3.61")) }
}

plugins {
    id("java")
    id("org.jetbrains.intellij") version "0.4.15"
    kotlin("jvm") version "1.3.61"
}

intellij {
    updateSinceUntilBuild = false
    instrumentCode = true
    version = "2019.2"
    setPlugins("java", "Kotlin")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    testImplementation(kotlin(module = "test-junit"))
    testImplementation(group = "junit", name = "junit", version = "4.12")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.3.3")
}

sourceSets {
    main {
        withConvention(org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet::class) {
            listOf(java, kotlin).forEach { it.srcDirs("main") }
        }
        resources.srcDir("resources")
    }

    test {
        withConvention(org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet::class) {
            listOf(java, kotlin).forEach { it.srcDirs("test") }
        }
        resources.srcDir("resources")
    }
}


fun htmlFixer(filename: String): String {
    if (!File(filename).exists()) {
        logger.error("File $filename not found.")
    } else {
        return File(filename).readText().replace("<html>", "").replace("</html>", "")
    }
    return ""
}

tasks {
    named<PatchPluginXmlTask>("patchPluginXml") {
        changeNotes(htmlFixer("resources/META-INF/change-notes.html"))
        pluginDescription(htmlFixer("resources/META-INF/description.html"))
        sinceBuild("191")
    }

    named<PublishTask>("publishPlugin") {
        if (project.hasProperty("intellijPublishToken")) {
            token(project.property("intellijPublishToken"))
        }
    }
}