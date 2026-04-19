package com.jvcodingsolutions.pagekeeper.feature.library.presentation

data class ShareableBook(
    val filePath: String,
    val title: String,
)

sealed interface LibraryEvent {
    data object OpenFilePicker : LibraryEvent
    data class ShareBook(val book: ShareableBook) : LibraryEvent
    data class ShareBooks(val books: List<ShareableBook>) : LibraryEvent
}
