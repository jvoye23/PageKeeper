package com.jvcodingsolutions.pagekeeper.core.data

import com.jvcodingsolutions.pagekeeper.core.domain.Book
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
)

fun BookEntity.toBook(): Book = Book(
    id = id,
    title = title,
    author = author,
    coverImagePath = coverImagePath,
    isFavorite = isFavorite,
    isFinished = isFinished,
    dateAdded = dateAdded,
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
)
