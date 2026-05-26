package com.jvcodingsolutions.pagekeeper.feature.globalbookmarks.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jvcodingsolutions.pagekeeper.core.domain.Book
import com.jvcodingsolutions.pagekeeper.core.domain.BookRepository
import com.jvcodingsolutions.pagekeeper.core.domain.Bookmark
import com.jvcodingsolutions.pagekeeper.core.domain.BookmarkRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GlobalBookmarksViewModel(
    private val bookmarkRepository: BookmarkRepository,
    private val bookRepository: BookRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(GlobalBookmarksState())
    val state = _state.asStateFlow()

    private val _events = Channel<GlobalBookmarksEvent>()
    val events = _events.receiveAsFlow()

    private var allItems: List<BookWithBookmarkCount> = emptyList()

    init {
        observeBookmarks()
    }

    private fun observeBookmarks() {
        viewModelScope.launch {
            bookmarkRepository.observeAll().collect { bookmarks ->
                refreshItems(bookmarks)
            }
        }
    }

    private suspend fun refreshItems(bookmarks: List<Bookmark>) {
        val books = bookRepository.getBooks().associateBy { it.id }
        val grouped = bookmarks
            .groupBy { it.bookId }
            .mapNotNull { (bookId, bookmarksForBook) ->
                val book = books[bookId] ?: return@mapNotNull null
                BookWithBookmarkCount(
                    book = book,
                    bookmarkCount = bookmarksForBook.size,
                    mostRecentBookmarkAt = bookmarksForBook.maxOf { it.createdAt },
                )
            }
            .sortedByDescending { it.mostRecentBookmarkAt }
        allItems = grouped
        _state.update {
            it.copy(
                items = applyFilter(grouped, it.searchQuery, it.isSearchActive),
                isLoading = false,
            )
        }
    }

    private fun applyFilter(
        source: List<BookWithBookmarkCount>,
        query: String,
        searchActive: Boolean,
    ): List<BookWithBookmarkCount> {
        if (!searchActive || query.isBlank()) return source
        val q = query.trim().lowercase()
        return source.filter { item ->
            item.book.title.lowercase().contains(q) ||
                item.book.author.lowercase().contains(q)
        }
    }

    fun onAction(action: GlobalBookmarksAction) {
        when (action) {
            is GlobalBookmarksAction.OnMenuClick -> {
                viewModelScope.launch { _events.send(GlobalBookmarksEvent.OpenMenu) }
            }
            is GlobalBookmarksAction.OnSearchToggle -> {
                _state.update {
                    val nowActive = !it.isSearchActive
                    val newQuery = if (nowActive) it.searchQuery else ""
                    it.copy(
                        isSearchActive = nowActive,
                        searchQuery = newQuery,
                        items = applyFilter(allItems, newQuery, nowActive),
                    )
                }
            }
            is GlobalBookmarksAction.OnSearchQueryChange -> {
                _state.update {
                    it.copy(
                        searchQuery = action.query,
                        items = applyFilter(allItems, action.query, it.isSearchActive),
                    )
                }
            }
            is GlobalBookmarksAction.OnBookClick -> {
                viewModelScope.launch {
                    _events.send(GlobalBookmarksEvent.OpenBookBookmarks(action.bookId))
                }
            }
            is GlobalBookmarksAction.OnViewBookmarksClick -> {
                viewModelScope.launch {
                    _events.send(GlobalBookmarksEvent.OpenBookBookmarks(action.bookId))
                }
            }
            is GlobalBookmarksAction.OnDeleteAllClick -> {
                _state.update { it.copy(confirmDeleteAllBookId = action.bookId) }
            }
            is GlobalBookmarksAction.OnConfirmDeleteAll -> {
                val bookId = _state.value.confirmDeleteAllBookId ?: return
                viewModelScope.launch {
                    bookmarkRepository.deleteAllForBook(bookId)
                    _state.update { it.copy(confirmDeleteAllBookId = null) }
                }
            }
            is GlobalBookmarksAction.OnDismissDialog -> {
                _state.update { it.copy(confirmDeleteAllBookId = null) }
            }
        }
    }
}
