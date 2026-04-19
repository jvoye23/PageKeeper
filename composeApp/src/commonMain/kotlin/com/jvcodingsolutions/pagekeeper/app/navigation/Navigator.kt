package com.jvcodingsolutions.pagekeeper.app.navigation

import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList

class Navigator(startDestination: Any) {
    val backStack: SnapshotStateList<Any> = listOf(startDestination).toMutableStateList()

    fun goTo(destination: Any) {
        backStack.add(destination)
    }

    fun goBack() {
        if (backStack.size > 1) {
            backStack.removeLast()
        }
    }

    fun replaceWith(destination: Any) {
        if (backStack.isNotEmpty()) {
            backStack.removeLast()
        }
        backStack.add(destination)
    }
}
