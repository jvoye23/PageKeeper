package com.jvcodingsolutions.pagekeeper.feature.bookmarks.presentation

import com.jvcodingsolutions.pagekeeper.core.domain.BookmarkColor

sealed interface BookmarksAction {
    data object OnBackClick : BookmarksAction
    data object OnAddBookmarkClick : BookmarksAction
    data class OnBookmarkClick(val bookmarkId: String) : BookmarksAction
    data class OnMenuEditClick(val bookmarkId: String) : BookmarksAction
    data class OnMenuDeleteClick(val bookmarkId: String) : BookmarksAction
    data class OnDialogTextChange(val text: String) : BookmarksAction
    data class OnDialogColorChange(val color: BookmarkColor) : BookmarksAction
    data class OnDialogColorDropdownToggle(val expanded: Boolean) : BookmarksAction
    data object OnDialogSave : BookmarksAction
    data object OnDialogCancel : BookmarksAction
    data object OnConfirmDelete : BookmarksAction
}
