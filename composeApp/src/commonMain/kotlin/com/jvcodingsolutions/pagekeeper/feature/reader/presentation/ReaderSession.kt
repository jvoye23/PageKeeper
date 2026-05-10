package com.jvcodingsolutions.pagekeeper.feature.reader.presentation

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Shared bus that lets the Chapters screen request a chapter jump in the
 * Reader, even though both run as separate Nav3 entries with their own
 * ViewModel scopes.
 *
 * Each request carries a target FB2/EPUB section index plus an optional
 * anchor id (from an EPUB TOC fragment). The Reader collects the flow,
 * loads the section if needed, then scrolls to either the anchor's parsed
 * element (when provided and known) or the section's first item.
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
}

data class JumpTarget(
    val sectionIndex: Int,
    val anchorId: String?,
)
