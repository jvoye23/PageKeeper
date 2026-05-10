package com.jvcodingsolutions.pagekeeper.feature.library.presentation

sealed interface LibraryAction {
    data object OnImportBookClick : LibraryAction
    data object OnSearchClick : LibraryAction
    data class OnBookClick(val bookId: String) : LibraryAction
    data class OnBookLongClick(val bookId: String) : LibraryAction
    data class OnToggleFavorite(val bookId: String) : LibraryAction
    data class OnToggleFinished(val bookId: String) : LibraryAction
    data class OnShareBook(val bookId: String) : LibraryAction
    data class OnDeleteBook(val bookId: String) : LibraryAction
    data object OnExitSelectionMode : LibraryAction
    data class OnFilterChanged(val filter: BookFilter) : LibraryAction
    data object OnFavoriteSelected : LibraryAction
    data object OnShareSelected : LibraryAction
    data object OnDeleteSelected : LibraryAction
    data class OnBookFileSelected(val fileName: String, val fileBytes: ByteArray) : LibraryAction
    data object OnConfirmDelete : LibraryAction
    data object OnDismissDeleteDialog : LibraryAction
    data object OnDismissErrorDialog : LibraryAction
    data object OnCloseSearch : LibraryAction
    data class OnSearchQueryChanged(val query: String) : LibraryAction
    data object OnResumeReadingClick : LibraryAction
}
