package com.jvcodingsolutions.pagekeeper.feature.library.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jvcodingsolutions.pagekeeper.core.domain.BookRepository
import com.jvcodingsolutions.pagekeeper.core.domain.DataError
import com.jvcodingsolutions.pagekeeper.core.domain.onFailure
import com.jvcodingsolutions.pagekeeper.core.domain.onSuccess
import com.jvcodingsolutions.pagekeeper.core.presentation.UiText
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LibraryViewModel(
    private val repository: BookRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(LibraryState())
    val state = _state.asStateFlow()

    private val _events = Channel<LibraryEvent>()
    val events = _events.receiveAsFlow()

    init {
        loadBooks()
    }

    private fun loadBooks() {
        viewModelScope.launch {
            val books = repository.getBooks()
            _state.update { it.copy(books = books) }
        }
    }

    fun onAction(action: LibraryAction) {
        when (action) {
            is LibraryAction.OnImportBookClick -> {
                viewModelScope.launch {
                    _events.send(LibraryEvent.OpenFilePicker)
                }
            }

            is LibraryAction.OnBookFileSelected -> importBook(action.fileName, action.fileBytes)
            is LibraryAction.OnDismissErrorDialog -> dismissErrorDialog()
            is LibraryAction.OnSearchClick -> _state.update { it.copy(isSearchActive = true) }
            is LibraryAction.OnCloseSearch -> _state.update { it.copy(isSearchActive = false, searchQuery = "") }
            is LibraryAction.OnSearchQueryChanged -> _state.update { it.copy(searchQuery = action.query) }
            is LibraryAction.OnToggleFavorite -> toggleFavorite(action.bookId)
            is LibraryAction.OnToggleFinished -> toggleFinished(action.bookId)
            is LibraryAction.OnBookClick -> onBookClick(action.bookId)
            is LibraryAction.OnBookLongClick -> onBookLongClick(action.bookId)
            is LibraryAction.OnShareBook -> shareBook(action.bookId)
            is LibraryAction.OnDeleteBook -> showDeleteConfirmation(action.bookId)
            is LibraryAction.OnConfirmDelete -> confirmDelete()
            is LibraryAction.OnDismissDeleteDialog -> _state.update { it.copy(deleteConfirmation = null) }
            is LibraryAction.OnExitSelectionMode -> exitSelectionMode()
            is LibraryAction.OnFilterChanged -> setFilter(action.filter)
            is LibraryAction.OnFavoriteSelected -> favoriteSelected()
            is LibraryAction.OnShareSelected -> shareSelected()
            is LibraryAction.OnDeleteSelected -> showDeleteSelectedConfirmation()
        }
    }

    private fun importBook(fileName: String, fileBytes: ByteArray) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            repository.importBook(fileName, fileBytes)
                .onSuccess { book ->
                    _state.update { currentState ->
                        currentState.copy(
                            isLoading = false,
                            books = listOf(book) + currentState.books,
                        )
                    }
                }
                .onFailure { error ->
                    val message = when (error) {
                        DataError.Local.UNSUPPORTED_FORMAT ->
                            UiText.DynamicString("This format is not supported. Only FB2, EPUB, and PDF files are supported.")

                        DataError.Local.ALREADY_EXISTS ->
                            UiText.DynamicString("This book is already in your library.")

                        DataError.Local.DISK_FULL ->
                            UiText.DynamicString("Not enough storage space.")

                        else ->
                            UiText.DynamicString("An error occurred while importing the book.")
                    }
                    _state.update { it.copy(isLoading = false, errorMessage = message) }
                }
        }
    }

    private fun dismissErrorDialog() {
        _state.update { it.copy(errorMessage = null) }
    }

    private fun toggleFavorite(bookId: String) {
        _state.update { currentState ->
            val updatedBooks = currentState.books.map { book ->
                if (book.id == bookId) book.copy(isFavorite = !book.isFavorite)
                else book
            }
            currentState.copy(books = updatedBooks)
        }
        persistBookUpdate(bookId)
    }

    private fun toggleFinished(bookId: String) {
        _state.update { currentState ->
            val updatedBooks = currentState.books.map { book ->
                if (book.id == bookId) book.copy(isFinished = !book.isFinished)
                else book
            }
            currentState.copy(books = updatedBooks)
        }
        persistBookUpdate(bookId)
    }

    private fun persistBookUpdate(bookId: String) {
        val book = _state.value.books.find { it.id == bookId } ?: return
        viewModelScope.launch {
            repository.updateBook(book)
        }
    }

    private fun onBookClick(bookId: String) {
        _state.update { currentState ->
            if (!currentState.isSelectionMode) return@update currentState

            val updatedSelection = if (bookId in currentState.selectedBookIds) {
                currentState.selectedBookIds - bookId
            } else {
                currentState.selectedBookIds + bookId
            }

            if (updatedSelection.isEmpty()) {
                currentState.copy(
                    isSelectionMode = false,
                    selectedBookIds = emptySet(),
                )
            } else {
                currentState.copy(selectedBookIds = updatedSelection)
            }
        }
    }

    private fun onBookLongClick(bookId: String) {
        _state.update { currentState ->
            val updatedSelection = if (bookId in currentState.selectedBookIds) {
                currentState.selectedBookIds - bookId
            } else {
                currentState.selectedBookIds + bookId
            }

            if (updatedSelection.isEmpty()) {
                currentState.copy(
                    isSelectionMode = false,
                    selectedBookIds = emptySet(),
                )
            } else {
                currentState.copy(
                    isSelectionMode = true,
                    selectedBookIds = updatedSelection,
                )
            }
        }
    }

    private fun shareBook(bookId: String) {
        viewModelScope.launch {
            val path = repository.getBookFilePath(bookId) ?: return@launch
            val book = _state.value.books.find { it.id == bookId } ?: return@launch
            _events.send(LibraryEvent.ShareBook(ShareableBook(path, book.title)))
        }
    }

    private fun showDeleteConfirmation(bookId: String) {
        val book = _state.value.books.find { it.id == bookId } ?: return
        _state.update {
            it.copy(deleteConfirmation = DeleteConfirmation.SingleBook(bookId, book.title))
        }
    }

    private fun showDeleteSelectedConfirmation() {
        val count = _state.value.selectedBookIds.size
        if (count == 0) return
        _state.update {
            it.copy(deleteConfirmation = DeleteConfirmation.MultipleBooks(count))
        }
    }

    private fun confirmDelete() {
        val confirmation = _state.value.deleteConfirmation ?: return
        when (confirmation) {
            is DeleteConfirmation.SingleBook -> {
                val bookId = confirmation.bookId
                _state.update { currentState ->
                    val updatedBooks = currentState.books.filter { it.id != bookId }
                    val updatedSelection = currentState.selectedBookIds - bookId
                    currentState.copy(
                        books = updatedBooks,
                        selectedBookIds = updatedSelection,
                        isSelectionMode = if (updatedSelection.isEmpty()) false else currentState.isSelectionMode,
                        deleteConfirmation = null,
                    )
                }
                viewModelScope.launch {
                    repository.deleteBook(bookId)
                }
            }
            is DeleteConfirmation.MultipleBooks -> {
                val selectedIds = _state.value.selectedBookIds
                _state.update { currentState ->
                    currentState.copy(
                        books = currentState.books.filter { it.id !in selectedIds },
                        isSelectionMode = false,
                        selectedBookIds = emptySet(),
                        deleteConfirmation = null,
                    )
                }
                viewModelScope.launch {
                    selectedIds.forEach { bookId ->
                        repository.deleteBook(bookId)
                    }
                }
            }
        }
    }

    private fun exitSelectionMode() {
        _state.update {
            it.copy(
                isSelectionMode = false,
                selectedBookIds = emptySet(),
            )
        }
    }

    private fun favoriteSelected() {
        _state.update { currentState ->
            val updatedBooks = currentState.books.map { book ->
                if (book.id in currentState.selectedBookIds) book.copy(isFavorite = true)
                else book
            }
            currentState.copy(
                books = updatedBooks,
                isSelectionMode = false,
                selectedBookIds = emptySet(),
            )
        }
        // Persist updates for all previously selected books
        val selectedIds = _state.value.books.filter { it.isFavorite }.map { it.id }
        viewModelScope.launch {
            _state.value.books.filter { it.id in selectedIds }.forEach { book ->
                repository.updateBook(book)
            }
        }
    }

    private fun shareSelected() {
        val selectedIds = _state.value.selectedBookIds
        val books = _state.value.books
        viewModelScope.launch {
            val shareableBooks = selectedIds.mapNotNull { id ->
                val path = repository.getBookFilePath(id) ?: return@mapNotNull null
                val book = books.find { it.id == id } ?: return@mapNotNull null
                ShareableBook(path, book.title)
            }
            if (shareableBooks.isNotEmpty()) {
                _events.send(LibraryEvent.ShareBooks(shareableBooks))
            }
        }
        exitSelectionMode()
    }


    private fun setFilter(filter: BookFilter) {
        _state.update {
            it.copy(
                activeFilter = filter,
                isSelectionMode = false,
                selectedBookIds = emptySet(),
            )
        }
    }
}
