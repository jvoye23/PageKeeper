package com.jvcodingsolutions.pagekeeper.feature.reader.presentation

sealed interface ReaderAction {
    data object OnScreenTap : ReaderAction
    data object OnBackClick : ReaderAction
    data object OnToggleFavorite : ReaderAction
    data object OnToggleOrientation : ReaderAction
    data object OnFontSizeClick : ReaderAction
    data class OnFontSizeChanged(val size: Int) : ReaderAction
    data object OnLoadMoreSections : ReaderAction
    data object OnDismissError : ReaderAction
    data object OnChaptersClick : ReaderAction
    data object OnBookmarksClick : ReaderAction
    data object OnDismissBookmarkIndicator : ReaderAction
    data class OnSaveReadingPosition(
        val firstVisibleItemIndex: Int,
        val firstVisibleItemScrollOffset: Int,
    ) : ReaderAction
}
