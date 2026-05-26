package com.jvcodingsolutions.pagekeeper.feature.bookmarks.presentation

import com.jvcodingsolutions.pagekeeper.core.domain.Bookmark
import com.jvcodingsolutions.pagekeeper.core.domain.BookmarkColor

data class BookmarksState(
    val bookId: String = "",
    val bookmarks: List<Bookmark> = emptyList(),
    val isLoading: Boolean = true,
    val dialog: BookmarksDialog = BookmarksDialog.None,
)

sealed interface BookmarksDialog {
    data object None : BookmarksDialog

    data class AddOrEdit(
        val editingId: String?,
        val text: String,
        val color: BookmarkColor,
        val isColorDropdownExpanded: Boolean = false,
        val firstVisibleItemIndex: Int,
        val firstVisibleItemScrollOffset: Int,
        val loadedSectionCount: Int,
        val sectionIndex: Int,
        val chapterTitle: String,
    ) : BookmarksDialog

    data class ConfirmDelete(val bookmarkId: String) : BookmarksDialog
}
