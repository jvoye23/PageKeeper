package com.jvcodingsolutions.pagekeeper.feature.reader.presentation

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Shared bus that lets the Chapters or Bookmarks screens request scroll changes
 * in the Reader, even though they run as separate Nav3 entries with their own
 * ViewModel scopes.
 *
 * Chapter jumps carry a target FB2/EPUB section index plus an optional anchor id
 * (from an EPUB TOC fragment). Bookmark jumps carry an explicit LazyColumn item
 * index + scroll offset (saved when the bookmark was created).
 *
 * The current-anchor StateFlow is updated by the Reader as the user scrolls so
 * the per-book Bookmarks screen can read where to place a new bookmark.
 */
class ReaderSession {
    private val _jumpRequests = MutableSharedFlow<JumpTarget>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val jumpRequests: SharedFlow<JumpTarget> = _jumpRequests.asSharedFlow()

    fun requestJump(sectionIndex: Int, anchorId: String? = null) {
        _jumpRequests.tryEmit(JumpTarget(sectionIndex, anchorId))
    }

    private val _bookmarkJumps = MutableSharedFlow<BookmarkJumpTarget>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val bookmarkJumps: SharedFlow<BookmarkJumpTarget> = _bookmarkJumps.asSharedFlow()

    fun requestBookmarkJump(target: BookmarkJumpTarget) {
        _bookmarkJumps.tryEmit(target)
    }

    private val _currentAnchor = MutableStateFlow(CurrentReaderAnchor())
    val currentAnchor: StateFlow<CurrentReaderAnchor> = _currentAnchor.asStateFlow()

    fun updateCurrentAnchor(anchor: CurrentReaderAnchor) {
        _currentAnchor.value = anchor
    }
}

data class JumpTarget(
    val sectionIndex: Int,
    val anchorId: String?,
)

data class BookmarkJumpTarget(
    val bookmarkId: String,
    val firstVisibleItemIndex: Int,
    val firstVisibleItemScrollOffset: Int,
    val loadedSectionCount: Int,
)

data class CurrentReaderAnchor(
    val bookId: String = "",
    val firstVisibleItemIndex: Int = 0,
    val firstVisibleItemScrollOffset: Int = 0,
    val loadedSectionCount: Int = 1,
    val sectionIndex: Int = 0,
    val chapterTitle: String = "",
    val firstVisibleParagraphText: String = "",
)
