package com.jvcodingsolutions.pagekeeper.feature.bookmarks.presentation

sealed interface BookmarksEvent {
    data object NavigateBack : BookmarksEvent
    data class OpenReaderAtBookmark(val bookId: String, val bookmarkId: String) : BookmarksEvent
}
