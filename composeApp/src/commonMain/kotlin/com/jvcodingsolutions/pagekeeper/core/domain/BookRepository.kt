package com.jvcodingsolutions.pagekeeper.core.domain

interface BookRepository {
    suspend fun importBook(fileName: String, fileBytes: ByteArray): Result<Book, DataError.Local>
    suspend fun getBooks(): List<Book>
    suspend fun deleteBook(bookId: String)
    suspend fun updateBook(book: Book)
    suspend fun getBookFilePath(bookId: String): String?
    suspend fun getBookFileBytes(bookId: String): ByteArray?
    suspend fun getBookFileExtension(bookId: String): String?
    suspend fun getBookById(bookId: String): Book?
    suspend fun markBookOpened(bookId: String)
    suspend fun getResumeBook(): Book?
}
