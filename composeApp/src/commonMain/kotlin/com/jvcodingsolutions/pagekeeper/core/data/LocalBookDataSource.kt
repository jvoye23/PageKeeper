package com.jvcodingsolutions.pagekeeper.core.data

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class LocalBookDataSource(
    private val fileStorage: FileStorage,
) {
    private val mutex = Mutex()
    private val json = Json { prettyPrint = true }
    private val metadataFileName = "books_metadata.json"

    private var cachedEntities: MutableList<BookEntity>? = null

    suspend fun getAll(): List<BookEntity> = mutex.withLock {
        loadEntities()
    }

    suspend fun save(entity: BookEntity) = mutex.withLock {
        val entities = loadEntities().toMutableList()
        entities.add(0, entity)
        persistEntities(entities)
    }

    suspend fun delete(bookId: String) = mutex.withLock {
        val entities = loadEntities().toMutableList()
        entities.removeAll { it.id == bookId }
        persistEntities(entities)
    }

    suspend fun update(entity: BookEntity) = mutex.withLock {
        val entities = loadEntities().toMutableList()
        val index = entities.indexOfFirst { it.id == entity.id }
        if (index >= 0) {
            entities[index] = entity
            persistEntities(entities)
        }
    }

    suspend fun findByHash(hash: String): BookEntity? = mutex.withLock {
        loadEntities().find { it.fileHash == hash }
    }

    private fun loadEntities(): List<BookEntity> {
        cachedEntities?.let { return it }

        val fullPath = fileStorage.getFullPath(metadataFileName)
        return try {
            val bytes = fileStorage.readFile(fullPath)
            val text = bytes.decodeToString()
            val entities = json.decodeFromString<List<BookEntity>>(text).toMutableList()
            cachedEntities = entities
            entities
        } catch (_: Exception) {
            val empty = mutableListOf<BookEntity>()
            cachedEntities = empty
            empty
        }
    }

    private fun persistEntities(entities: List<BookEntity>) {
        cachedEntities = entities.toMutableList()
        val text = json.encodeToString(entities)
        fileStorage.writeFile(metadataFileName, text.encodeToByteArray())
    }
}
