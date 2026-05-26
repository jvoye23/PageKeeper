package com.jvcodingsolutions.pagekeeper.core.domain

import kotlinx.coroutines.flow.Flow

interface BookmarkRepository {
    fun observeAll(): Flow<List<Bookmark>>
    fun observeForBook(bookId: String): Flow<List<Bookmark>>
    suspend fun getAll(): List<Bookmark>
    suspend fun getForBook(bookId: String): List<Bookmark>
    suspend fun getById(id: String): Bookmark?
    suspend fun add(bookmark: Bookmark)
    suspend fun update(bookmark: Bookmark)
    suspend fun deleteById(id: String)
    suspend fun deleteAllForBook(bookId: String)
}
