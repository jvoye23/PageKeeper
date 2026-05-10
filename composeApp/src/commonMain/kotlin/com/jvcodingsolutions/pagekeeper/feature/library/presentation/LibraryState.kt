package com.jvcodingsolutions.pagekeeper.feature.library.presentation

import com.jvcodingsolutions.pagekeeper.core.domain.Book
import com.jvcodingsolutions.pagekeeper.core.presentation.UiText

enum class BookFilter {
    ALL,
    FAVORITES,
    FINISHED,
}

sealed interface DeleteConfirmation {
    data class SingleBook(val bookId: String, val title: String) : DeleteConfirmation
    data class MultipleBooks(val count: Int) : DeleteConfirmation
}

data class LibraryState(
    val isLoading: Boolean = false,
    val books: List<Book> = emptyList(),
    val selectedBookIds: Set<String> = emptySet(),
    val isSelectionMode: Boolean = false,
    val activeFilter: BookFilter = BookFilter.ALL,
    val errorMessage: UiText? = null,
    val isSearchActive: Boolean = false,
    val searchQuery: String = "",
    val deleteConfirmation: DeleteConfirmation? = null,
) {

    val resumeBook: Book? get() = books
        .filter { it.lastOpenedAt != null && !it.isFinished }
        .maxByOrNull { it.lastOpenedAt ?: 0L }
    val displayedBooks: List<Book> get() {
        val filtered = when (activeFilter) {
            BookFilter.ALL -> books
            BookFilter.FAVORITES -> books.filter { it.isFavorite }
            BookFilter.FINISHED -> books.filter { it.isFinished }
        }
        if (searchQuery.isBlank()) return filtered
        val query = searchQuery.trim().lowercase()
        return filtered.filter { book ->
            book.title.lowercase().contains(query) ||
                book.author.lowercase().contains(query)
        }
    }

    val isEmpty: Boolean get() = displayedBooks.isEmpty()

    val hasNoSearchResults: Boolean get() = isSearchActive && searchQuery.isNotBlank() && displayedBooks.isEmpty()

    val screenTitle: String get() = when (activeFilter) {
        BookFilter.ALL -> "Library"
        BookFilter.FAVORITES -> "Favorites"
        BookFilter.FINISHED -> "Finished"
    }
}
