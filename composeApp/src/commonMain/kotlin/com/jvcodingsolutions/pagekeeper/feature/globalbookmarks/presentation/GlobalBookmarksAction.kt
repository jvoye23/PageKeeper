package com.jvcodingsolutions.pagekeeper.feature.globalbookmarks.presentation

sealed interface GlobalBookmarksAction {
    data object OnMenuClick : GlobalBookmarksAction
    data object OnSearchToggle : GlobalBookmarksAction
    data class OnSearchQueryChange(val query: String) : GlobalBookmarksAction
    data class OnBookClick(val bookId: String) : GlobalBookmarksAction
    data class OnViewBookmarksClick(val bookId: String) : GlobalBookmarksAction
    data class OnDeleteAllClick(val bookId: String) : GlobalBookmarksAction
    data object OnConfirmDeleteAll : GlobalBookmarksAction
    data object OnDismissDialog : GlobalBookmarksAction
}
