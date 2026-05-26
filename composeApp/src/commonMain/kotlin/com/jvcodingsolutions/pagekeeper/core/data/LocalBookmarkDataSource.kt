package com.jvcodingsolutions.pagekeeper.core.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class LocalBookmarkDataSource(
    private val fileStorage: FileStorage,
) {
    private val mutex = Mutex()
    private val json = Json { prettyPrint = true }
    private val metadataFileName = "bookmarks_metadata.json"

    private var initialized = false
    private val _entities = MutableStateFlow<List<BookmarkEntity>>(emptyList())
    val entities: StateFlow<List<BookmarkEntity>> = _entities.asStateFlow()

    suspend fun getAll(): List<BookmarkEntity> = mutex.withLock {
        ensureLoaded()
        _entities.value
    }

    suspend fun add(entity: BookmarkEntity) = mutex.withLock {
        ensureLoaded()
        val updated = listOf(entity) + _entities.value.filterNot { it.id == entity.id }
        persist(updated)
    }

    suspend fun update(entity: BookmarkEntity) = mutex.withLock {
        ensureLoaded()
        val current = _entities.value
        val index = current.indexOfFirst { it.id == entity.id }
        if (index >= 0) {
            val updated = current.toMutableList().apply { this[index] = entity }
            persist(updated)
        }
    }

    suspend fun deleteById(id: String) = mutex.withLock {
        ensureLoaded()
        val updated = _entities.value.filterNot { it.id == id }
        persist(updated)
    }

    suspend fun deleteAllForBook(bookId: String) = mutex.withLock {
        ensureLoaded()
        val updated = _entities.value.filterNot { it.bookId == bookId }
        persist(updated)
    }

    private fun ensureLoaded() {
        if (initialized) return
        initialized = true
        val fullPath = fileStorage.getFullPath(metadataFileName)
        val loaded = try {
            val bytes = fileStorage.readFile(fullPath)
            val text = bytes.decodeToString()
            json.decodeFromString<List<BookmarkEntity>>(text)
        } catch (_: Exception) {
            emptyList()
        }
        _entities.value = loaded
    }

    private fun persist(entities: List<BookmarkEntity>) {
        _entities.value = entities
        val text = json.encodeToString(entities)
        fileStorage.writeFile(metadataFileName, text.encodeToByteArray())
    }
}
