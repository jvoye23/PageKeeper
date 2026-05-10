package com.jvcodingsolutions.pagekeeper.feature.reader.chapters

import com.jvcodingsolutions.pagekeeper.feature.reader.domain.BookStructure

data class ChaptersState(
    val isLoading: Boolean = true,
    val bookTitle: String = "",
    val structure: BookStructure? = null,
    val currentSectionIndex: Int = -1,
    val expandedSectionIndex: Int? = null,
    /** Stable ChapterNode.id values that are currently expanded inside the section. */
    val expandedNodeIds: Set<String> = emptySet(),
)
