package com.jvcodingsolutions.pagekeeper.core.data

import com.jvcodingsolutions.pagekeeper.core.domain.Bookmark
import com.jvcodingsolutions.pagekeeper.core.domain.BookmarkRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

class BookmarkRepositoryImpl(
    private val dataSource: LocalBookmarkDataSource,
) : BookmarkRepository {

    override fun observeAll(): Flow<List<Bookmark>> =
        dataSource.entities
            .onStart { dataSource.getAll() }
            .map { list -> list.map { it.toBookmark() } }

    override fun observeForBook(bookId: String): Flow<List<Bookmark>> =
        dataSource.entities
            .onStart { dataSource.getAll() }
            .map { list ->
                list.filter { it.bookId == bookId }.map { it.toBookmark() }
            }

    override suspend fun getAll(): List<Bookmark> =
        dataSource.getAll().map { it.toBookmark() }

    override suspend fun getForBook(bookId: String): List<Bookmark> =
        dataSource.getAll()
            .filter { it.bookId == bookId }
            .map { it.toBookmark() }

    override suspend fun getById(id: String): Bookmark? =
        dataSource.getAll().firstOrNull { it.id == id }?.toBookmark()

    override suspend fun add(bookmark: Bookmark) {
        dataSource.add(bookmark.toEntity())
    }

    override suspend fun update(bookmark: Bookmark) {
        dataSource.update(bookmark.toEntity())
    }

    override suspend fun deleteById(id: String) {
        dataSource.deleteById(id)
    }

    override suspend fun deleteAllForBook(bookId: String) {
        dataSource.deleteAllForBook(bookId)
    }
}
