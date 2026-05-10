package com.jvcodingsolutions.pagekeeper.feature.reader.data

import com.jvcodingsolutions.pagekeeper.core.data.FileStorage
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class ReaderSettingsStorage(private val fileStorage: FileStorage) {

    companion object {
        private const val SETTINGS_FILE = "reader_settings.json"
        const val DEFAULT_FONT_SIZE = 18
        const val MIN_FONT_SIZE = 10
        const val MAX_FONT_SIZE = 40
    }

    private val json = Json { ignoreUnknownKeys = true }

    fun getFontSize(): Int {
        return try {
            val path = fileStorage.getFullPath(SETTINGS_FILE)
            val bytes = fileStorage.readFile(path)
            val settings = json.decodeFromString<ReaderSettings>(bytes.decodeToString())
            settings.fontSize.coerceIn(MIN_FONT_SIZE, MAX_FONT_SIZE)
        } catch (_: Exception) {
            DEFAULT_FONT_SIZE
        }
    }

    fun saveFontSize(size: Int) {
        val clamped = size.coerceIn(MIN_FONT_SIZE, MAX_FONT_SIZE)
        val settings = ReaderSettings(fontSize = clamped)
        val content = json.encodeToString(ReaderSettings.serializer(), settings)
        fileStorage.writeFile(SETTINGS_FILE, content.encodeToByteArray())
    }

    @Serializable
    private data class ReaderSettings(
        val fontSize: Int = DEFAULT_FONT_SIZE,
    )
}
