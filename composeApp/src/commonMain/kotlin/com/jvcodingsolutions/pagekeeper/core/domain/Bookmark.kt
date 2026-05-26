package com.jvcodingsolutions.pagekeeper.core.domain

data class Bookmark(
    val id: String,
    val bookId: String,
    val text: String,
    val color: BookmarkColor,
    val chapterTitle: String,
    val sectionIndex: Int,
    val firstVisibleItemIndex: Int,
    val firstVisibleItemScrollOffset: Int,
    val loadedSectionCount: Int,
    val createdAt: Long,
)

enum class BookmarkColor {
    BLUE,
    GREEN,
    YELLOW,
    RED,
    PURPLE,
}
