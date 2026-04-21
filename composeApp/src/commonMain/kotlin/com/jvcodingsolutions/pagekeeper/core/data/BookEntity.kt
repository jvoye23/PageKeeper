package com.jvcodingsolutions.pagekeeper.core.data

import com.jvcodingsolutions.pagekeeper.core.domain.Book
import com.jvcodingsolutions.pagekeeper.core.domain.ReadingPosition
import kotlinx.serialization.Serializable

@Serializable
data class BookEntity(
    val id: String,
    val title: String,
    val author: String,
    val coverImagePath: String?,
    val isFavorite: Boolean = false,
    val isFinished: Boolean = false,
    val dateAdded: Long,
    val fileHash: String,
    val storedFileName: String,
    val readingItemIndex: Int = 0,
    val readingScrollOffset: Int = 0,
    val readingSectionCount: Int = 1,
)

fun BookEntity.toBook(): Book = Book(
    id = id,
    title = title,
    author = author,
    coverImagePath = coverImagePath,
    isFavorite = isFavorite,
    isFinished = isFinished,
    dateAdded = dateAdded,
    readingPosition = ReadingPosition(
        firstVisibleItemIndex = readingItemIndex,
        firstVisibleItemScrollOffset = readingScrollOffset,
        loadedSectionCount = readingSectionCount,
    ),
)

fun Book.toEntity(fileHash: String, storedFileName: String): BookEntity = BookEntity(
    id = id,
    title = title,
    author = author,
    coverImagePath = coverImagePath,
    isFavorite = isFavorite,
    isFinished = isFinished,
    dateAdded = dateAdded,
    fileHash = fileHash,
    storedFileName = storedFileName,
    readingItemIndex = readingPosition.firstVisibleItemIndex,
    readingScrollOffset = readingPosition.firstVisibleItemScrollOffset,
    readingSectionCount = readingPosition.loadedSectionCount,
)
