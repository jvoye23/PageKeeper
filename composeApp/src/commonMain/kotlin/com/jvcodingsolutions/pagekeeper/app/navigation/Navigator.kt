package com.jvcodingsolutions.pagekeeper.app.navigation

import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList

class Navigator(startDestination: Any) {
    val backStack: SnapshotStateList<Any> = listOf(startDestination).toMutableStateList()

    constructor(backStack: List<Any>) : this(backStack.first()) {
        this.backStack.clear()
        this.backStack.addAll(backStack)
    }

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

    companion object {
        val Saver: Saver<Navigator, Any> = listSaver(
            save = { navigator ->
                navigator.backStack.map { route -> encodeRoute(route) }
            },
            restore = { encoded ->
                val routes = encoded.map { decodeRoute(it) }
                Navigator(routes)
            },
        )

        private fun encodeRoute(route: Any): String = when (route) {
            is SplashRoute -> "splash"
            is LibraryRoute -> "library"
            is FavoritesRoute -> "favorites"
            is FinishedRoute -> "finished"
            is GlobalBookmarksRoute -> "global_bookmarks"
            is ReaderRoute -> "reader:${route.bookId}"
            is ChaptersRoute -> "chapters:${route.bookId}"
            is BookmarksRoute -> "bookmarks:${route.bookId}"
            else -> throw IllegalArgumentException("Unknown route: $route")
        }

        private fun decodeRoute(encoded: String): Any = when {
            encoded == "splash" -> SplashRoute
            encoded == "library" -> LibraryRoute
            encoded == "favorites" -> FavoritesRoute
            encoded == "finished" -> FinishedRoute
            encoded == "global_bookmarks" -> GlobalBookmarksRoute
            encoded.startsWith("reader:") -> ReaderRoute(encoded.removePrefix("reader:"))
            encoded.startsWith("chapters:") -> ChaptersRoute(encoded.removePrefix("chapters:"))
            encoded.startsWith("bookmarks:") -> BookmarksRoute(encoded.removePrefix("bookmarks:"))
            else -> throw IllegalArgumentException("Unknown encoded route: $encoded")
        }
    }
}
