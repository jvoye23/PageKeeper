package com.jvcodingsolutions.pagekeeper.feature.reader.presentation

import androidx.compose.ui.graphics.ImageBitmap
import com.jvcodingsolutions.pagekeeper.core.presentation.UiText
import com.jvcodingsolutions.pagekeeper.feature.reader.domain.BookContentElement
import com.jvcodingsolutions.pagekeeper.feature.reader.domain.BookStructure

data class ReaderState(
    val bookTitle: String = "",
    val isFavorite: Boolean = false,
    val contentElements: List<BookContentElement> = emptyList(),
    val isLoading: Boolean = true,
    val isLoadingMore: Boolean = false,
    val isControlsVisible: Boolean = false,
    val fontSize: Int = 18,
    val isLandscapeLocked: Boolean = false,
    val isFontSizeMode: Boolean = false,
    val totalSectionCount: Int = 0,
    val loadedSectionCount: Int = 0,
    val initialItemIndex: Int = 0,
    val initialScrollOffset: Int = 0,
    val progressFraction: Float = 0f,
    val bookStructure: BookStructure? = null,
    val errorMessage: UiText? = null,
    // PDF-specific
    val isPdf: Boolean = false,
    val pdfPages: List<ImageBitmap> = emptyList(),
    val pdfPageCount: Int = 0,
    val loadedPdfPageCount: Int = 0,
    // Bookmark indicator (visible only while the anchored item is on-screen)
    val isBookmarkIndicatorVisible: Boolean = false,
    val activeBookmarkAnchorItemIndex: Int? = null,
) {
    val hasMoreSections: Boolean
        get() = if (isPdf) loadedPdfPageCount < pdfPageCount
                else loadedSectionCount < totalSectionCount
}
