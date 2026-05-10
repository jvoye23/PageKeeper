package com.jvcodingsolutions.pagekeeper.feature.reader.chapters

sealed interface ChaptersEvent {
    data object NavigateBack : ChaptersEvent
}
