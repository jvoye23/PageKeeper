package com.jvcodingsolutions.pagekeeper.feature.reader.chapters

sealed interface ChaptersAction {
    data object OnBackClick : ChaptersAction
    data class OnSectionToggle(val sectionListIndex: Int) : ChaptersAction
    data class OnChapterToggle(val nodeId: String) : ChaptersAction
    data class OnChapterClick(
        val sectionIndex: Int,
        val anchorId: String?,
    ) : ChaptersAction
}
