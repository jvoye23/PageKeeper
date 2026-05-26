package com.jvcodingsolutions.pagekeeper.core.data

import com.jvcodingsolutions.pagekeeper.core.domain.Bookmark
import com.jvcodingsolutions.pagekeeper.core.domain.BookmarkColor
import kotlinx.serialization.Serializable

@Serializable
data class BookmarkEntity(
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

fun BookmarkEntity.toBookmark(): Bookmark = Bookmark(
    id = id,
    bookId = bookId,
    text = text,
    color = color,
    chapterTitle = chapterTitle,
    sectionIndex = sectionIndex,
    firstVisibleItemIndex = firstVisibleItemIndex,
    firstVisibleItemScrollOffset = firstVisibleItemScrollOffset,
    loadedSectionCount = loadedSectionCount,
    createdAt = createdAt,
)

fun Bookmark.toEntity(): BookmarkEntity = BookmarkEntity(
    id = id,
    bookId = bookId,
    text = text,
    color = color,
    chapterTitle = chapterTitle,
    sectionIndex = sectionIndex,
    firstVisibleItemIndex = firstVisibleItemIndex,
    firstVisibleItemScrollOffset = firstVisibleItemScrollOffset,
    loadedSectionCount = loadedSectionCount,
    createdAt = createdAt,
)
