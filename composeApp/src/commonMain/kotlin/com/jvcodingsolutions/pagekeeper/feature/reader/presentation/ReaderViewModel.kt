package com.jvcodingsolutions.pagekeeper.feature.reader.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jvcodingsolutions.pagekeeper.core.domain.BookRepository
import com.jvcodingsolutions.pagekeeper.core.domain.ReadingPosition
import com.jvcodingsolutions.pagekeeper.core.domain.onFailure
import com.jvcodingsolutions.pagekeeper.core.domain.onSuccess
import com.jvcodingsolutions.pagekeeper.core.presentation.UiText
import com.jvcodingsolutions.pagekeeper.feature.reader.data.EpubContentParser
import com.jvcodingsolutions.pagekeeper.feature.reader.data.Fb2ContentParser
import com.jvcodingsolutions.pagekeeper.feature.reader.data.PdfPageRenderer
import com.jvcodingsolutions.pagekeeper.feature.reader.data.ReaderSettingsStorage
import com.jvcodingsolutions.pagekeeper.feature.reader.domain.BookContentElement
import com.jvcodingsolutions.pagekeeper.feature.reader.domain.BookStructure
import com.jvcodingsolutions.pagekeeper.feature.reader.domain.ChapterNode
import com.jvcodingsolutions.pagekeeper.feature.reader.domain.ReadingProgressCalculator
import com.jvcodingsolutions.pagekeeper.feature.reader.domain.StructureSection
import com.jvcodingsolutions.pagekeeper.feature.reader.domain.findChapterTitleAt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ReaderViewModel(
    private val bookId: String,
    private val repository: BookRepository,
    private val fb2ContentParser: Fb2ContentParser,
    private val epubContentParser: EpubContentParser,
    private val readerSettingsStorage: ReaderSettingsStorage,
    private val readerSession: ReaderSession,
) : ViewModel() {

    private val _state = MutableStateFlow(ReaderState())
    val state = _state.asStateFlow()

    private val _events = Channel<ReaderEvent>()
    val events = _events.receiveAsFlow()

    private var pdfRenderer: PdfPageRenderer? = null

    // Lazy parsing state
    private var fb2SectionChunks: List<String>? = null
    private var epubStructure: EpubContentParser.EpubStructure? = null

    // Item-index of each parsed section's first element (chapterStartItemIndices[i]
    // = LazyColumn index where section i begins). Used for jump-to-chapter scroll.
    private val chapterStartItemIndices = mutableListOf<Int>()

    // Maps (sectionIndex, anchorId) -> flat LazyColumn item index. Populated for EPUB
    // sections as they are parsed; used for anchor-precise sub-chapter jumps.
    private val anchorItemIndices = mutableMapOf<Pair<Int, String>, Int>()

    // Track last known scroll position for saving
    private var lastItemIndex: Int = 0
    private var lastScrollOffset: Int = 0

    init {
        loadBook()
        observeChapterJumps()
        observeBookmarkJumps()
    }

    private fun observeChapterJumps() {
        viewModelScope.launch {
            readerSession.jumpRequests.collect { target ->
                jumpToChapter(target.sectionIndex, target.anchorId)
            }
        }
    }

    private fun observeBookmarkJumps() {
        viewModelScope.launch {
            readerSession.bookmarkJumps.collect { target ->
                jumpToBookmark(target)
            }
        }
    }

    private fun loadBook() {
        viewModelScope.launch {
            val savedFontSize = withContext(Dispatchers.Default) {
                readerSettingsStorage.getFontSize()
            }
            _state.update { it.copy(fontSize = savedFontSize) }

            val book = repository.getBookById(bookId)
            if (book == null) {
                _events.send(
                    ReaderEvent.ShowSnackbar(
                        UiText.DynamicString("Failed to read the book. The file may be empty or corrupted.")
                    )
                )
                _events.send(ReaderEvent.NavigateBack)
                return@launch
            }

            repository.markBookOpened(bookId)

            val savedPosition = book.readingPosition
            lastItemIndex = savedPosition.firstVisibleItemIndex
            lastScrollOffset = savedPosition.firstVisibleItemScrollOffset

            _state.update {
                it.copy(
                    bookTitle = book.title,
                    isFavorite = book.isFavorite,
                    progressFraction = savedPosition.progressFraction,
                )
            }

            val extension = repository.getBookFileExtension(bookId)

            if (extension == "pdf") {
                loadPdf(savedPosition)
            } else {
                loadTextContent(extension, savedPosition, book.title)
            }
        }
    }

    private suspend fun loadTextContent(
        extension: String?,
        savedPosition: ReadingPosition,
        bookTitle: String,
    ) {
        val fileBytes = repository.getBookFileBytes(bookId)
        if (fileBytes == null) {
            showParseError()
            return
        }

        when (extension) {
            "fb2" -> loadFb2(fileBytes, savedPosition, bookTitle)
            "epub" -> loadEpub(fileBytes, savedPosition, bookTitle)
            else -> showParseError()
        }
    }

    private suspend fun loadFb2(
        fileBytes: ByteArray,
        savedPosition: ReadingPosition,
        bookTitle: String,
    ) {
        val parsedResult = withContext(Dispatchers.Default) {
            fb2ContentParser.extractStructure(fileBytes, defaultSectionTitle = bookTitle)
        }

        parsedResult
            .onSuccess { parsed ->
                val chunks = parsed.chunks
                if (chunks.isEmpty()) {
                    showParseError()
                    return
                }
                fb2SectionChunks = chunks

                // Parse chapters up to the saved position
                val sectionsToLoad = savedPosition.loadedSectionCount.coerceIn(1, chunks.size)
                val (allElements, startIndices) = withContext(Dispatchers.Default) {
                    val elements = mutableListOf<com.jvcodingsolutions.pagekeeper.feature.reader.domain.BookContentElement>()
                    val starts = mutableListOf<Int>()
                    for (i in 0 until sectionsToLoad) {
                        starts.add(elements.size)
                        elements.addAll(fb2ContentParser.parseSection(chunks[i]).elements)
                    }
                    elements.toList() to starts.toList()
                }

                chapterStartItemIndices.clear()
                chapterStartItemIndices.addAll(startIndices)

                _state.update {
                    it.copy(
                        totalSectionCount = chunks.size,
                        contentElements = allElements,
                        loadedSectionCount = sectionsToLoad,
                        initialItemIndex = savedPosition.firstVisibleItemIndex,
                        initialScrollOffset = savedPosition.firstVisibleItemScrollOffset,
                        isLoading = false,
                        isPdf = false,
                        bookStructure = parsed.structure,
                    )
                }
            }
            .onFailure {
                showParseError()
            }
    }

    private suspend fun loadEpub(
        fileBytes: ByteArray,
        savedPosition: ReadingPosition,
        bookTitle: String,
    ) {
        val structureResult = withContext(Dispatchers.Default) {
            epubContentParser.extractStructure(fileBytes)
        }

        structureResult
            .onSuccess { structure ->
                if (structure.chapterCount == 0) {
                    structure.close()
                    showParseError()
                    return
                }
                epubStructure = structure

                // Parse chapters up to the saved position
                val sectionsToLoad = savedPosition.loadedSectionCount.coerceIn(1, structure.chapterCount)
                data class EpubLoadResult(
                    val elements: List<com.jvcodingsolutions.pagekeeper.feature.reader.domain.BookContentElement>,
                    val starts: List<Int>,
                    val anchors: Map<Pair<Int, String>, Int>,
                )
                val loadResult = withContext(Dispatchers.Default) {
                    val elements = mutableListOf<com.jvcodingsolutions.pagekeeper.feature.reader.domain.BookContentElement>()
                    val starts = mutableListOf<Int>()
                    val anchors = mutableMapOf<Pair<Int, String>, Int>()
                    for (i in 0 until sectionsToLoad) {
                        val baseIndex = elements.size
                        starts.add(baseIndex)
                        val section = epubContentParser.parseChapter(structure, i)
                        elements.addAll(section.elements)
                        section.anchors.forEach { (anchorId, localIndex) ->
                            anchors[i to anchorId] = baseIndex + localIndex
                        }
                    }
                    EpubLoadResult(elements.toList(), starts.toList(), anchors.toMap())
                }
                val allElements = loadResult.elements
                val startIndices = loadResult.starts

                chapterStartItemIndices.clear()
                chapterStartItemIndices.addAll(startIndices)
                anchorItemIndices.clear()
                anchorItemIndices.putAll(loadResult.anchors)

                val bookStructure = withContext(Dispatchers.Default) {
                    buildEpubStructure(structure, bookTitle)
                }

                _state.update {
                    it.copy(
                        totalSectionCount = structure.chapterCount,
                        contentElements = allElements,
                        loadedSectionCount = sectionsToLoad,
                        initialItemIndex = savedPosition.firstVisibleItemIndex,
                        initialScrollOffset = savedPosition.firstVisibleItemScrollOffset,
                        isLoading = false,
                        isPdf = false,
                        bookStructure = bookStructure,
                    )
                }
            }
            .onFailure {
                showParseError()
            }
    }

    private fun buildEpubStructure(
        structure: EpubContentParser.EpubStructure,
        bookTitle: String,
    ): BookStructure {
        val chapters = epubContentParser.buildChapterNodes(structure)
        return BookStructure(
            sections = listOf(StructureSection(title = bookTitle, chapters = chapters)),
        )
    }

    private suspend fun loadPdf(savedPosition: ReadingPosition) {
        val filePath = repository.getBookFilePath(bookId)
        if (filePath == null) {
            showParseError()
            return
        }

        try {
            val renderer = withContext(Dispatchers.Default) {
                PdfPageRenderer(filePath)
            }
            pdfRenderer = renderer
            val pageCount = renderer.getPageCount()

            if (pageCount == 0) {
                showParseError()
                return
            }

            // Load pages up to saved position or at least first 3
            val pagesToLoad = maxOf(3, savedPosition.loadedSectionCount).coerceAtMost(pageCount)
            val initialPages = withContext(Dispatchers.Default) {
                (0 until pagesToLoad).mapNotNull { i ->
                    renderer.renderPage(i, 1080)
                }
            }

            _state.update {
                it.copy(
                    isPdf = true,
                    pdfPageCount = pageCount,
                    pdfPages = initialPages,
                    loadedPdfPageCount = initialPages.size,
                    initialItemIndex = savedPosition.firstVisibleItemIndex,
                    initialScrollOffset = savedPosition.firstVisibleItemScrollOffset,
                    isLoading = false,
                )
            }
        } catch (_: Exception) {
            showParseError()
        }
    }

    private suspend fun showParseError() {
        _state.update { it.copy(isLoading = false) }
        _events.send(
            ReaderEvent.ShowSnackbar(
                UiText.DynamicString("Failed to read the book. The file may be empty or corrupted.")
            )
        )
        _events.send(ReaderEvent.NavigateBack)
    }

    fun onAction(action: ReaderAction) {
        when (action) {
            is ReaderAction.OnScreenTap -> {
                _state.update {
                    val newVisible = !it.isControlsVisible
                    it.copy(
                        isControlsVisible = newVisible,
                        isFontSizeMode = if (!newVisible) false else it.isFontSizeMode,
                    )
                }
            }

            is ReaderAction.OnBackClick -> {
                viewModelScope.launch {
                    persistReadingPosition()
                    _events.send(ReaderEvent.NavigateBack)
                }
            }

            is ReaderAction.OnToggleFavorite -> toggleFavorite()

            is ReaderAction.OnToggleOrientation -> {
                _state.update { it.copy(isLandscapeLocked = !it.isLandscapeLocked) }
            }

            is ReaderAction.OnFontSizeClick -> {
                _state.update { it.copy(isFontSizeMode = !it.isFontSizeMode) }
            }

            is ReaderAction.OnFontSizeChanged -> {
                val clamped = action.size.coerceIn(
                    ReaderSettingsStorage.MIN_FONT_SIZE,
                    ReaderSettingsStorage.MAX_FONT_SIZE,
                )
                _state.update { it.copy(fontSize = clamped) }
                viewModelScope.launch(Dispatchers.Default) {
                    readerSettingsStorage.saveFontSize(clamped)
                }
            }

            is ReaderAction.OnLoadMoreSections -> loadMoreContent()

            is ReaderAction.OnDismissError -> {
                _state.update { it.copy(errorMessage = null) }
            }

            is ReaderAction.OnChaptersClick -> {
                viewModelScope.launch {
                    _events.send(ReaderEvent.NavigateToChapters(bookId))
                }
            }

            is ReaderAction.OnBookmarksClick -> {
                viewModelScope.launch {
                    persistReadingPosition()
                    publishCurrentAnchor()
                    _events.send(ReaderEvent.NavigateToBookmarks(bookId))
                }
            }

            is ReaderAction.OnDismissBookmarkIndicator -> {
                _state.update {
                    it.copy(
                        isBookmarkIndicatorVisible = false,
                        activeBookmarkAnchorItemIndex = null,
                    )
                }
            }

            is ReaderAction.OnSaveReadingPosition -> {
                lastItemIndex = action.firstVisibleItemIndex
                lastScrollOffset = action.firstVisibleItemScrollOffset
                _state.update { it.copy(progressFraction = computeProgress(it, action.firstVisibleItemIndex)) }
                publishCurrentAnchor()
            }
        }
    }

    private fun publishCurrentAnchor() {
        val current = _state.value
        if (current.isLoading || current.isPdf) return
        val itemIndex = lastItemIndex
        val sectionIndex = sectionIndexForItem(itemIndex)
        val chapterTitle = current.bookStructure?.findChapterTitleAt(sectionIndex).orEmpty()
        val fragment = firstParagraphTextFrom(current.contentElements, itemIndex)
        readerSession.updateCurrentAnchor(
            CurrentReaderAnchor(
                bookId = bookId,
                firstVisibleItemIndex = itemIndex,
                firstVisibleItemScrollOffset = lastScrollOffset,
                loadedSectionCount = current.loadedSectionCount,
                sectionIndex = sectionIndex,
                chapterTitle = chapterTitle,
                firstVisibleParagraphText = fragment,
            )
        )
    }

    private fun sectionIndexForItem(itemIndex: Int): Int {
        if (chapterStartItemIndices.isEmpty()) return 0
        // Find the largest start-index <= itemIndex.
        var found = 0
        chapterStartItemIndices.forEachIndexed { i, start ->
            if (start <= itemIndex) found = i
        }
        return found
    }

    private fun firstParagraphTextFrom(
        elements: List<BookContentElement>,
        startIndex: Int,
    ): String {
        val from = startIndex.coerceIn(0, (elements.size - 1).coerceAtLeast(0))
        for (i in from until elements.size) {
            val el = elements[i]
            if (el is BookContentElement.Paragraph) {
                val text = el.text.text.trim()
                if (text.isNotEmpty()) return text.take(160)
            }
        }
        // Fallback: walk back to find any paragraph.
        for (i in (from - 1) downTo 0) {
            val el = elements[i]
            if (el is BookContentElement.Paragraph) {
                val text = el.text.text.trim()
                if (text.isNotEmpty()) return text.take(160)
            }
        }
        return ""
    }

    private fun jumpToBookmark(target: BookmarkJumpTarget) {
        if (_state.value.isPdf) return
        viewModelScope.launch {
            // Make sure enough sections are loaded so the target item exists.
            val sectionsNeeded = target.loadedSectionCount.coerceAtLeast(1)
            while (_state.value.loadedSectionCount < sectionsNeeded) {
                loadNextChapterBlocking()
            }
            val total = _state.value.contentElements.size
            if (total == 0) return@launch
            val itemIndex = target.firstVisibleItemIndex.coerceIn(0, total - 1)
            lastItemIndex = itemIndex
            lastScrollOffset = target.firstVisibleItemScrollOffset
            _state.update {
                it.copy(
                    progressFraction = computeProgress(it, itemIndex),
                    activeBookmarkAnchorItemIndex = itemIndex,
                    isBookmarkIndicatorVisible = true,
                )
            }
            _events.send(
                ReaderEvent.ScrollToBookmark(
                    itemIndex = itemIndex,
                    scrollOffset = target.firstVisibleItemScrollOffset,
                )
            )
        }
    }

    private fun jumpToChapter(targetSectionIndex: Int, anchorId: String? = null) {
        val current = _state.value
        if (current.isPdf) return // PDFs have no chapters
        val total = current.totalSectionCount
        if (targetSectionIndex !in 0 until total) return

        viewModelScope.launch {
            // Load sections up to and including the target if needed
            while (_state.value.loadedSectionCount <= targetSectionIndex) {
                loadNextChapterBlocking()
            }

            val anchorTarget = anchorId?.let { anchorItemIndices[targetSectionIndex to it] }
            val targetItemIndex = anchorTarget
                ?: chapterStartItemIndices.getOrNull(targetSectionIndex)
                ?: 0
            lastItemIndex = targetItemIndex
            lastScrollOffset = 0
            _state.update {
                it.copy(progressFraction = computeProgress(it, targetItemIndex))
            }
            _events.send(ReaderEvent.ScrollToItem(targetItemIndex))
        }
    }

    private suspend fun loadNextChapterBlocking() {
        val nextIndex = _state.value.loadedSectionCount
        val section = withContext(Dispatchers.Default) {
            fb2SectionChunks?.let { chunks ->
                if (nextIndex < chunks.size) fb2ContentParser.parseSection(chunks[nextIndex]) else null
            } ?: epubStructure?.let { structure ->
                if (nextIndex < structure.chapterCount) {
                    epubContentParser.parseChapter(structure, nextIndex)
                } else null
            }
        }

        val baseIndex = _state.value.contentElements.size
        chapterStartItemIndices.add(baseIndex)
        section?.anchors?.forEach { (anchorId, localIndex) ->
            anchorItemIndices[nextIndex to anchorId] = baseIndex + localIndex
        }
        if (section != null && section.elements.isNotEmpty()) {
            _state.update {
                it.copy(
                    contentElements = it.contentElements + section.elements,
                    loadedSectionCount = nextIndex + 1,
                )
            }
        } else {
            _state.update { it.copy(loadedSectionCount = nextIndex + 1) }
        }
    }

    private fun computeProgress(state: ReaderState, firstVisibleItemIndex: Int): Float {
        return if (state.isPdf) {
            ReadingProgressCalculator.forPdf(firstVisibleItemIndex, state.pdfPageCount)
        } else {
            ReadingProgressCalculator.forText(
                firstVisibleItemIndex = firstVisibleItemIndex,
                totalLoadedItemCount = state.contentElements.size,
                loadedSectionCount = state.loadedSectionCount,
                totalSectionCount = state.totalSectionCount,
            )
        }
    }

    private fun toggleFavorite() {
        _state.update { it.copy(isFavorite = !it.isFavorite) }
        viewModelScope.launch {
            val book = repository.getBookById(bookId) ?: return@launch
            repository.updateBook(book.copy(isFavorite = _state.value.isFavorite))
        }
    }

    private suspend fun persistReadingPosition() {
        val currentState = _state.value
        val loadedCount = if (currentState.isPdf) currentState.loadedPdfPageCount
        else currentState.loadedSectionCount

        val book = repository.getBookById(bookId) ?: return
        val progress = computeProgress(currentState, lastItemIndex)
        repository.updateBook(
            book.copy(
                readingPosition = ReadingPosition(
                    firstVisibleItemIndex = lastItemIndex,
                    firstVisibleItemScrollOffset = lastScrollOffset,
                    loadedSectionCount = loadedCount,
                    progressFraction = progress,
                )
            )
        )
    }

    private fun loadMoreContent() {
        val currentState = _state.value
        if (!currentState.hasMoreSections || currentState.isLoadingMore) return

        if (currentState.isPdf) {
            loadMorePdfPages()
        } else {
            loadNextChapter()
        }
    }

    private fun loadNextChapter() {
        val nextIndex = _state.value.loadedSectionCount

        viewModelScope.launch {
            _state.update { it.copy(isLoadingMore = true) }

            // TODO: Remove this delay — added for testing the loading spinner
            delay(250)

            val section = withContext(Dispatchers.Default) {
                fb2SectionChunks?.let { chunks ->
                    if (nextIndex < chunks.size) {
                        fb2ContentParser.parseSection(chunks[nextIndex])
                    } else null
                } ?: epubStructure?.let { structure ->
                    if (nextIndex < structure.chapterCount) {
                        epubContentParser.parseChapter(structure, nextIndex)
                    } else null
                }
            }

            val baseIndex = _state.value.contentElements.size
            chapterStartItemIndices.add(baseIndex)
            section?.anchors?.forEach { (anchorId, localIndex) ->
                anchorItemIndices[nextIndex to anchorId] = baseIndex + localIndex
            }
            if (section != null && section.elements.isNotEmpty()) {
                _state.update {
                    it.copy(
                        contentElements = it.contentElements + section.elements,
                        loadedSectionCount = nextIndex + 1,
                        isLoadingMore = false,
                    )
                }
            } else {
                _state.update {
                    it.copy(
                        loadedSectionCount = nextIndex + 1,
                        isLoadingMore = false,
                    )
                }
            }
        }
    }

    private fun loadMorePdfPages() {
        val currentState = _state.value
        val renderer = pdfRenderer ?: return
        val nextIndex = currentState.loadedPdfPageCount

        _state.update { it.copy(isLoadingMore = true) }

        viewModelScope.launch {
            val newPages = withContext(Dispatchers.Default) {
                (nextIndex until minOf(nextIndex + 3, currentState.pdfPageCount)).mapNotNull { i ->
                    renderer.renderPage(i, 1080)
                }
            }
            _state.update {
                it.copy(
                    pdfPages = it.pdfPages + newPages,
                    loadedPdfPageCount = it.loadedPdfPageCount + newPages.size,
                    isLoadingMore = false,
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch(NonCancellable) {
            persistReadingPosition()
        }
        pdfRenderer?.close()
        epubStructure?.close()
    }
}
