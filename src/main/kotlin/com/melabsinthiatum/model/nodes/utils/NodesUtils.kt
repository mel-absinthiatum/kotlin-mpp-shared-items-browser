package com.melabsinthiatum.model.nodes.utils

import java.util.*


/**
 * Extension function for converting a {@link List} to an {@link Enumeration}
 */
fun <T> List<T>.toEnumeration(): Enumeration<T> {
    return object : Enumeration<T> {
        var count = 0

        override fun hasMoreElements(): Boolean {
            return this.count < size
        }

        override fun nextElement(): T {
            if (this.count < size) {
                return get(this.count++)
            }
            throw NoSuchElementException("List enumeration asked for more elements than present")
        }
    }
}

fun <T> emptyEnumeration(): Enumeration<T> {
    return object : Enumeration<T> {

        override fun hasMoreElements(): Boolean {
            return false
        }

        override fun nextElement(): T {
            throw NoSuchElementException("Empty enumeration asked for an element")
        }
    }
}