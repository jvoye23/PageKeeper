package com.jvcodingsolutions.pagekeeper.core.domain

data class Book(
    val id: String,
    val title: String,
    val author: String,
    val coverImagePath: String?,
    val isFavorite: Boolean = false,
    val isFinished: Boolean = false,
    val dateAdded: Long,
    val readingPosition: ReadingPosition = ReadingPosition(),
)

data class ReadingPosition(
    val firstVisibleItemIndex: Int = 0,
    val firstVisibleItemScrollOffset: Int = 0,
    val loadedSectionCount: Int = 1,
)
