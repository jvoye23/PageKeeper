package com.jvcodingsolutions.pagekeeper.feature.globalbookmarks.presentation

import com.jvcodingsolutions.pagekeeper.core.domain.Book

data class GlobalBookmarksState(
    val items: List<BookWithBookmarkCount> = emptyList(),
    val isLoading: Boolean = true,
    val isSearchActive: Boolean = false,
    val searchQuery: String = "",
    val confirmDeleteAllBookId: String? = null,
)

data class BookWithBookmarkCount(
    val book: Book,
    val bookmarkCount: Int,
    val mostRecentBookmarkAt: Long,
)
