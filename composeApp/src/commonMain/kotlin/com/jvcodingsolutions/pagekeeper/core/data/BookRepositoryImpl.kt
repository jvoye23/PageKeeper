package com.jvcodingsolutions.pagekeeper.core.data

import com.jvcodingsolutions.pagekeeper.currentTimeMillis
import com.jvcodingsolutions.pagekeeper.core.domain.Book
import com.jvcodingsolutions.pagekeeper.core.domain.BookRepository
import com.jvcodingsolutions.pagekeeper.core.domain.DataError
import com.jvcodingsolutions.pagekeeper.core.domain.Result
import okio.ByteString.Companion.toByteString
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class BookRepositoryImpl(
    private val fileStorage: FileStorage,
    private val fb2Parser: Fb2BookParser,
    private val epubParser: EpubBookParser,
    private val pdfParser: PdfBookParser,
    private val localBookDataSource: LocalBookDataSource,
) : BookRepository {

    companion object {
        private val SUPPORTED_EXTENSIONS = setOf("fb2", "epub", "pdf")
    }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun importBook(
        fileName: String,
        fileBytes: ByteArray,
    ): Result<Book, DataError.Local> {
        // 1. Validate file extension
        val extension = fileName.substringAfterLast('.', "").lowercase()
        if (extension !in SUPPORTED_EXTENSIONS) {
            return Result.Error(DataError.Local.UNSUPPORTED_FORMAT)
        }

        // 2. Compute SHA-256 hash for duplicate detection
        val fileHash = fileBytes.toByteString().sha256().hex()

        // 3. Check for duplicates
        val existing = localBookDataSource.findByHash(fileHash)
        if (existing != null) {
            return Result.Error(DataError.Local.ALREADY_EXISTS)
        }

        // 4. Parse metadata using the appropriate parser
        val parseResult = when (extension) {
            "fb2" -> fb2Parser.parse(fileBytes)
            "epub" -> epubParser.parse(fileBytes)
            "pdf" -> pdfParser.parse(fileBytes)
            else -> return Result.Error(DataError.Local.UNSUPPORTED_FORMAT)
        }
        val metadata = when (parseResult) {
            is Result.Success -> parseResult.data
            is Result.Error -> return parseResult
        }

        // 5. Generate unique ID
        val bookId = Uuid.random().toString()

        // 6. Save cover image if present
        var coverImagePath: String? = null
        if (metadata.coverBytes != null) {
            val coverExtension = when {
                metadata.coverContentType?.contains("png") == true -> "png"
                metadata.coverContentType?.contains("gif") == true -> "gif"
                else -> "jpg"
            }
            val coverFileName = "covers/$bookId.$coverExtension"
            coverImagePath = fileStorage.writeFile(coverFileName, metadata.coverBytes)
        }

        // 7. Copy book file to internal storage
        val storedFileName = "files/$bookId.$extension"
        fileStorage.writeFile(storedFileName, fileBytes)

        // 8. Create and save book entity
        val book = Book(
            id = bookId,
            title = metadata.title,
            author = metadata.author,
            coverImagePath = coverImagePath,
            isFavorite = false,
            isFinished = false,
            dateAdded = currentTimeMillis(),
        )

        val entity = book.toEntity(
            fileHash = fileHash,
            storedFileName = storedFileName,
        )
        localBookDataSource.save(entity)

        return Result.Success(book)
    }

    override suspend fun getBooks(): List<Book> {
        return localBookDataSource.getAll().map { it.toBook() }
    }

    override suspend fun deleteBook(bookId: String) {
        val entities = localBookDataSource.getAll()
        val entity = entities.find { it.id == bookId } ?: return

        // Delete the stored book file
        val filePath = fileStorage.getFullPath(entity.storedFileName)
        fileStorage.deleteFile(filePath)

        // Delete cover image if it exists
        entity.coverImagePath?.let { fileStorage.deleteFile(it) }

        // Remove from data source
        localBookDataSource.delete(bookId)
    }

    override suspend fun updateBook(book: Book) {
        val entities = localBookDataSource.getAll()
        val existing = entities.find { it.id == book.id } ?: return

        val updated = existing.copy(
            title = book.title,
            author = book.author,
            coverImagePath = book.coverImagePath,
            isFavorite = book.isFavorite,
            isFinished = book.isFinished,
        )
        localBookDataSource.update(updated)
    }

    override suspend fun getBookFilePath(bookId: String): String? {
        val entity = localBookDataSource.getAll().find { it.id == bookId } ?: return null
        return fileStorage.getFullPath(entity.storedFileName)
    }
}
