package com.jvcodingsolutions.pagekeeper.feature.bookmarks.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jvcodingsolutions.pagekeeper.core.domain.Bookmark
import com.jvcodingsolutions.pagekeeper.core.domain.BookmarkColor
import com.jvcodingsolutions.pagekeeper.core.domain.BookmarkRepository
import com.jvcodingsolutions.pagekeeper.core.presentation.currentTimeMillis
import com.jvcodingsolutions.pagekeeper.feature.reader.presentation.BookmarkJumpTarget
import com.jvcodingsolutions.pagekeeper.feature.reader.presentation.CurrentReaderAnchor
import com.jvcodingsolutions.pagekeeper.feature.reader.presentation.ReaderSession
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

class BookmarksViewModel(
    private val bookId: String,
    private val bookmarkRepository: BookmarkRepository,
    private val readerSession: ReaderSession,
) : ViewModel() {

    private val _state = MutableStateFlow(BookmarksState(bookId = bookId))
    val state = _state.asStateFlow()

    private val _events = Channel<BookmarksEvent>()
    val events = _events.receiveAsFlow()

    init {
        observeBookmarks()
    }

    private fun observeBookmarks() {
        viewModelScope.launch {
            bookmarkRepository.observeForBook(bookId).collect { bookmarks ->
                val sorted = bookmarks.sortedByDescending { it.createdAt }
                _state.update {
                    it.copy(bookmarks = sorted, isLoading = false)
                }
            }
        }
    }

    fun onAction(action: BookmarksAction) {
        when (action) {
            is BookmarksAction.OnBackClick -> {
                viewModelScope.launch { _events.send(BookmarksEvent.NavigateBack) }
            }

            is BookmarksAction.OnAddBookmarkClick -> openAddDialog()

            is BookmarksAction.OnBookmarkClick -> openBookmark(action.bookmarkId)

            is BookmarksAction.OnMenuEditClick -> openEditDialog(action.bookmarkId)

            is BookmarksAction.OnMenuDeleteClick -> {
                _state.update { it.copy(dialog = BookmarksDialog.ConfirmDelete(action.bookmarkId)) }
            }

            is BookmarksAction.OnDialogTextChange -> updateDialog { it.copy(text = action.text) }
            is BookmarksAction.OnDialogColorChange -> updateDialog {
                it.copy(color = action.color, isColorDropdownExpanded = false)
            }
            is BookmarksAction.OnDialogColorDropdownToggle -> updateDialog {
                it.copy(isColorDropdownExpanded = action.expanded)
            }

            is BookmarksAction.OnDialogSave -> saveDialog()
            is BookmarksAction.OnDialogCancel -> dismissDialog()
            is BookmarksAction.OnConfirmDelete -> confirmDelete()
        }
    }

    private fun openAddDialog() {
        val anchor = readerSession.currentAnchor.value.takeIf { it.bookId == bookId }
            ?: CurrentReaderAnchor(bookId = bookId)
        _state.update {
            it.copy(
                dialog = BookmarksDialog.AddOrEdit(
                    editingId = null,
                    text = anchor.firstVisibleParagraphText,
                    color = BookmarkColor.BLUE,
                    firstVisibleItemIndex = anchor.firstVisibleItemIndex,
                    firstVisibleItemScrollOffset = anchor.firstVisibleItemScrollOffset,
                    loadedSectionCount = anchor.loadedSectionCount,
                    sectionIndex = anchor.sectionIndex,
                    chapterTitle = anchor.chapterTitle,
                )
            )
        }
    }

    private fun openEditDialog(bookmarkId: String) {
        val existing = _state.value.bookmarks.firstOrNull { it.id == bookmarkId } ?: return
        _state.update {
            it.copy(
                dialog = BookmarksDialog.AddOrEdit(
                    editingId = existing.id,
                    text = existing.text,
                    color = existing.color,
                    firstVisibleItemIndex = existing.firstVisibleItemIndex,
                    firstVisibleItemScrollOffset = existing.firstVisibleItemScrollOffset,
                    loadedSectionCount = existing.loadedSectionCount,
                    sectionIndex = existing.sectionIndex,
                    chapterTitle = existing.chapterTitle,
                )
            )
        }
    }

    private fun openBookmark(bookmarkId: String) {
        val bookmark = _state.value.bookmarks.firstOrNull { it.id == bookmarkId } ?: return
        readerSession.requestBookmarkJump(
            BookmarkJumpTarget(
                bookmarkId = bookmark.id,
                firstVisibleItemIndex = bookmark.firstVisibleItemIndex,
                firstVisibleItemScrollOffset = bookmark.firstVisibleItemScrollOffset,
                loadedSectionCount = bookmark.loadedSectionCount,
            )
        )
        viewModelScope.launch {
            _events.send(BookmarksEvent.OpenReaderAtBookmark(bookId, bookmark.id))
        }
    }

    private fun saveDialog() {
        val dialog = _state.value.dialog as? BookmarksDialog.AddOrEdit ?: return
        val trimmedText = dialog.text.trim()
        if (trimmedText.isEmpty()) {
            dismissDialog()
            return
        }
        viewModelScope.launch {
            val editingId = dialog.editingId
            if (editingId != null) {
                val existing = _state.value.bookmarks.firstOrNull { it.id == editingId }
                    ?: return@launch
                bookmarkRepository.update(
                    existing.copy(text = trimmedText, color = dialog.color)
                )
            } else {
                val now = currentTimeMillis()
                val newId = generateBookmarkId(now)
                bookmarkRepository.add(
                    Bookmark(
                        id = newId,
                        bookId = bookId,
                        text = trimmedText,
                        color = dialog.color,
                        chapterTitle = dialog.chapterTitle,
                        sectionIndex = dialog.sectionIndex,
                        firstVisibleItemIndex = dialog.firstVisibleItemIndex,
                        firstVisibleItemScrollOffset = dialog.firstVisibleItemScrollOffset,
                        loadedSectionCount = dialog.loadedSectionCount.coerceAtLeast(1),
                        createdAt = now,
                    )
                )
            }
            dismissDialog()
        }
    }

    private fun confirmDelete() {
        val dialog = _state.value.dialog as? BookmarksDialog.ConfirmDelete ?: return
        viewModelScope.launch {
            bookmarkRepository.deleteById(dialog.bookmarkId)
            dismissDialog()
        }
    }

    private fun dismissDialog() {
        _state.update { it.copy(dialog = BookmarksDialog.None) }
    }

    private inline fun updateDialog(transform: (BookmarksDialog.AddOrEdit) -> BookmarksDialog.AddOrEdit) {
        _state.update {
            val current = it.dialog as? BookmarksDialog.AddOrEdit ?: return@update it
            it.copy(dialog = transform(current))
        }
    }

    private fun generateBookmarkId(timestamp: Long): String =
        "bm-$timestamp-${Random.nextInt(100_000, 999_999)}"
}
