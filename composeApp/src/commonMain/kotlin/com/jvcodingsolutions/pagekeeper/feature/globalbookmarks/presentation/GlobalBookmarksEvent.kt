package com.jvcodingsolutions.pagekeeper.feature.globalbookmarks.presentation

sealed interface GlobalBookmarksEvent {
    data class OpenBookBookmarks(val bookId: String) : GlobalBookmarksEvent
    data object OpenMenu : GlobalBookmarksEvent
}
