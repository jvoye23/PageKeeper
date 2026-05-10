package com.jvcodingsolutions.pagekeeper.feature.reader.chapters

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jvcodingsolutions.pagekeeper.core.domain.BookRepository
import com.jvcodingsolutions.pagekeeper.feature.reader.data.EpubContentParser
import com.jvcodingsolutions.pagekeeper.feature.reader.data.Fb2ContentParser
import com.jvcodingsolutions.pagekeeper.feature.reader.domain.BookStructure
import com.jvcodingsolutions.pagekeeper.feature.reader.domain.ChapterNode
import com.jvcodingsolutions.pagekeeper.feature.reader.domain.StructureSection
import com.jvcodingsolutions.pagekeeper.core.domain.onSuccess
import com.jvcodingsolutions.pagekeeper.feature.reader.presentation.ReaderSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChaptersViewModel(
    private val bookId: String,
    private val repository: BookRepository,
    private val fb2ContentParser: Fb2ContentParser,
    private val epubContentParser: EpubContentParser,
    private val readerSession: ReaderSession,
) : ViewModel() {

    private val _state = MutableStateFlow(ChaptersState())
    val state = _state.asStateFlow()

    private val _events = Channel<ChaptersEvent>()
    val events = _events.receiveAsFlow()

    init {
        loadStructure()
    }

    private fun loadStructure() {
        viewModelScope.launch {
            val book = repository.getBookById(bookId) ?: run {
                _events.send(ChaptersEvent.NavigateBack)
                return@launch
            }
            val extension = repository.getBookFileExtension(bookId)
            val fileBytes = repository.getBookFileBytes(bookId)
            if (fileBytes == null || extension == "pdf") {
                _events.send(ChaptersEvent.NavigateBack)
                return@launch
            }

            val structure = withContext(Dispatchers.Default) {
                when (extension) {
                    "fb2" -> {
                        var s: BookStructure? = null
                        fb2ContentParser.extractStructure(fileBytes, defaultSectionTitle = book.title)
                            .onSuccess { s = it.structure }
                        s
                    }
                    "epub" -> {
                        val structResult = epubContentParser.extractStructure(fileBytes)
                        var s: BookStructure? = null
                        structResult.onSuccess { epub ->
                            val chapters = epubContentParser.buildChapterNodes(epub)
                            s = BookStructure(
                                sections = listOf(StructureSection(title = book.title, chapters = chapters))
                            )
                            epub.close()
                        }
                        s
                    }
                    else -> null
                }
            }

            if (structure == null || structure.sections.isEmpty()) {
                _events.send(ChaptersEvent.NavigateBack)
                return@launch
            }

            // Use loadedSectionCount-1 as a proxy for the user's current chapter:
            // it is the highest section the reader has parsed so far.
            val currentSectionIndex = (book.readingPosition.loadedSectionCount - 1).coerceAtLeast(0)
            val expandedIndex = findContainingSectionIndex(structure, currentSectionIndex) ?: 0

            _state.update {
                it.copy(
                    isLoading = false,
                    bookTitle = book.title,
                    structure = structure,
                    currentSectionIndex = currentSectionIndex,
                    expandedSectionIndex = expandedIndex,
                )
            }
        }
    }

    private fun findContainingSectionIndex(structure: BookStructure, sectionIndex: Int): Int? {
        structure.sections.forEachIndexed { idx, sec ->
            if (containsSectionIndex(sec.chapters, sectionIndex)) return idx
        }
        return null
    }

    private fun containsSectionIndex(chapters: List<ChapterNode>, sectionIndex: Int): Boolean {
        chapters.forEach {
            if (it.sectionIndex == sectionIndex) return true
            if (containsSectionIndex(it.children, sectionIndex)) return true
        }
        return false
    }

    fun onAction(action: ChaptersAction) {
        when (action) {
            is ChaptersAction.OnBackClick -> {
                viewModelScope.launch { _events.send(ChaptersEvent.NavigateBack) }
            }
            is ChaptersAction.OnSectionToggle -> {
                _state.update {
                    val newExpanded = if (it.expandedSectionIndex == action.sectionListIndex) {
                        it.expandedSectionIndex // ignore re-tap to satisfy "only one section may be expanded"
                    } else {
                        action.sectionListIndex
                    }
                    it.copy(expandedSectionIndex = newExpanded)
                }
            }
            is ChaptersAction.OnChapterToggle -> {
                _state.update {
                    val updated = it.expandedNodeIds.toMutableSet()
                    if (!updated.add(action.nodeId)) updated.remove(action.nodeId)
                    it.copy(expandedNodeIds = updated)
                }
            }
            is ChaptersAction.OnChapterClick -> {
                readerSession.requestJump(action.sectionIndex, action.anchorId)
                viewModelScope.launch { _events.send(ChaptersEvent.NavigateBack) }
            }
        }
    }
}
