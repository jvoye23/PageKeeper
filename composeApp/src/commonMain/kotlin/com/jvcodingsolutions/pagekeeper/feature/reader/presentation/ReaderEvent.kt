package com.jvcodingsolutions.pagekeeper.feature.reader.presentation

import com.jvcodingsolutions.pagekeeper.core.presentation.UiText

sealed interface ReaderEvent {
    data object NavigateBack : ReaderEvent
    data class ShowSnackbar(val message: UiText) : ReaderEvent
    data class NavigateToChapters(val bookId: String) : ReaderEvent
    data class ScrollToItem(val itemIndex: Int) : ReaderEvent
}
