package com.jvcodingsolutions.pagekeeper.feature.reader.data

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import com.jvcodingsolutions.pagekeeper.core.data.ZipReader
import com.jvcodingsolutions.pagekeeper.core.domain.DataError
import com.jvcodingsolutions.pagekeeper.core.domain.Result
import com.jvcodingsolutions.pagekeeper.feature.reader.domain.BookContentElement
import com.jvcodingsolutions.pagekeeper.feature.reader.domain.BookSection
import com.jvcodingsolutions.pagekeeper.feature.reader.domain.ChapterNode
import nl.adaptivity.xmlutil.EventType
import nl.adaptivity.xmlutil.XmlReader
import nl.adaptivity.xmlutil.xmlStreaming

class EpubContentParser {

    /**
     * Holds the EPUB structure needed for lazy chapter loading.
     * The zip and content paths are retained so individual chapters can be parsed on demand.
     */
    class EpubStructure(
        val zip: ZipReader,
        val contentPaths: List<String>,
        /** Hierarchical TOC parsed from EPUB3 nav.xhtml or EPUB2 toc.ncx. Empty if none was found. */
        val toc: List<TocEntry> = emptyList(),
        /** Directory containing the TOC file, used as the base for resolving TOC entry hrefs. */
        val tocBaseDir: String = "",
    ) {
        val chapterCount: Int get() = contentPaths.size

        fun close() {
            zip.close()
        }
    }

    /** A single TOC entry with optional nested children (deep nesting is supported). */
    data class TocEntry(
        val title: String,
        /** Raw href from the TOC, may include "#fragment". Resolve against [EpubStructure.tocBaseDir]. */
        val href: String,
        val children: List<TocEntry>,
    )

    /**
     * Lightweight initial parse: reads container.xml and OPF to determine the spine order and
     * locate the table of contents. Does NOT parse any XHTML content yet.
     */
    fun extractStructure(epubBytes: ByteArray): Result<EpubStructure, DataError.Local> {
        val zip = ZipReader(epubBytes)
        return try {
            val containerBytes = zip.readEntry("META-INF/container.xml") ?: run {
                zip.close()
                return Result.Error(DataError.Local.UNKNOWN)
            }
            val opfPath = parseContainerXml(containerBytes.decodeToString()) ?: run {
                zip.close()
                return Result.Error(DataError.Local.UNKNOWN)
            }
            val opfDir = opfPath.substringBeforeLast("/", "")

            val opfBytes = zip.readEntry(opfPath) ?: run {
                zip.close()
                return Result.Error(DataError.Local.UNKNOWN)
            }
            val opf = parseOpf(opfBytes.decodeToString())

            val contentPaths = opf.spineHrefs.map { resolvePath(opfDir, it) }

            if (contentPaths.isEmpty()) {
                zip.close()
                return Result.Error(DataError.Local.UNKNOWN)
            }

            // Try to load the TOC. Prefer EPUB3 nav.xhtml; fall back to EPUB2 NCX.
            val (toc, tocBaseDir) = loadToc(zip, opfDir, opf)

            Result.Success(EpubStructure(zip, contentPaths, toc, tocBaseDir))
        } catch (_: Exception) {
            zip.close()
            Result.Error(DataError.Local.UNKNOWN)
        }
    }

    /**
     * Peek the first heading (h1-h6) text in a spine document, used as a fallback chapter title
     * when the EPUB has no TOC. Returns null when no heading is present.
     */
    fun peekChapterTitle(structure: EpubStructure, index: Int): String? {
        if (index !in structure.contentPaths.indices) return null
        val bytes = structure.zip.readEntry(structure.contentPaths[index]) ?: return null
        val xhtml = bytes.decodeToString()
        val reader = xmlStreaming.newReader(xhtml)
        try {
            while (reader.hasNext()) {
                val event = reader.next()
                if (event == EventType.START_ELEMENT) {
                    val name = reader.localName
                    if (name == "h1" || name == "h2" || name == "h3" || name == "h4" || name == "h5" || name == "h6") {
                        val text = collectText(reader, name)
                        if (text.isNotBlank()) return text
                    }
                }
            }
        } catch (_: Exception) {
            return null
        } finally {
            reader.close()
        }
        return null
    }

    /**
     * Build a flat list of top-level [ChapterNode]s for the Chapters screen and the
     * "show Chapters button" check. When the EPUB has a TOC the tree comes straight
     * from it; otherwise we fall back to the legacy "first heading per spine file"
     * behavior so books without a TOC still get something usable.
     */
    fun buildChapterNodes(structure: EpubStructure): List<ChapterNode> {
        if (structure.toc.isNotEmpty()) {
            return structure.toc.mapNotNull { tocEntryToChapterNode(it, structure) }
        }
        return (0 until structure.chapterCount).map { idx ->
            val title = peekChapterTitle(structure, idx) ?: "Chapter ${idx + 1}"
            ChapterNode(title = title, sectionIndex = idx)
        }
    }

    private fun tocEntryToChapterNode(
        entry: TocEntry,
        structure: EpubStructure,
    ): ChapterNode? {
        val (file, anchor) = splitHref(entry.href)
        val sectionIndex = if (file.isNotEmpty()) resolveSpineIndex(file, structure) else -1
        if (sectionIndex < 0) return null

        val children = entry.children.mapNotNull { tocEntryToChapterNode(it, structure) }
        return ChapterNode(
            title = entry.title,
            sectionIndex = sectionIndex,
            anchorId = anchor,
            children = children,
        )
    }

    private fun splitHref(href: String): Pair<String, String?> {
        val hashIndex = href.indexOf('#')
        if (hashIndex < 0) return href to null
        val file = href.substring(0, hashIndex)
        val anchor = href.substring(hashIndex + 1).takeIf { it.isNotBlank() }
        return file to anchor
    }

    private fun resolveSpineIndex(relativeFile: String, structure: EpubStructure): Int {
        val resolved = resolvePath(structure.tocBaseDir, relativeFile)
        val direct = structure.contentPaths.indexOf(resolved)
        if (direct >= 0) return direct
        // Tolerant fallback by trailing filename for hrefs with unusual normalization.
        return structure.contentPaths.indexOfFirst { it.endsWith(relativeFile) }
    }

    /**
     * Parse a single spine document by index into a BookSection.
     * Called on demand when the user scrolls to a new chapter.
     */
    fun parseChapter(structure: EpubStructure, index: Int): BookSection {
        if (index !in structure.contentPaths.indices) {
            return BookSection(title = null, elements = emptyList())
        }
        val contentPath = structure.contentPaths[index]
        val contentBytes = structure.zip.readEntry(contentPath)
            ?: return BookSection(title = null, elements = emptyList())
        return parseXhtmlDocument(contentBytes.decodeToString())
    }

    private fun parseContainerXml(xml: String): String? {
        val reader = xmlStreaming.newReader(xml)
        try {
            while (reader.hasNext()) {
                val event = reader.next()
                if (event == EventType.START_ELEMENT && reader.localName == "rootfile") {
                    for (i in 0 until reader.attributeCount) {
                        if (reader.getAttributeLocalName(i) == "full-path") {
                            return reader.getAttributeValue(i)
                        }
                    }
                }
            }
        } finally {
            reader.close()
        }
        return null
    }

    private fun parseOpf(xml: String): OpfData {
        val manifestItems = mutableMapOf<String, ManifestItem>()
        val spineItemRefs = mutableListOf<String>()
        var spineTocId: String? = null

        val reader = xmlStreaming.newReader(xml)
        try {
            while (reader.hasNext()) {
                val event = reader.next()
                if (event == EventType.START_ELEMENT) {
                    when (reader.localName) {
                        "item" -> {
                            var id: String? = null
                            var href: String? = null
                            var mediaType: String? = null
                            var properties: String? = null
                            for (i in 0 until reader.attributeCount) {
                                when (reader.getAttributeLocalName(i)) {
                                    "id" -> id = reader.getAttributeValue(i)
                                    "href" -> href = reader.getAttributeValue(i)
                                    "media-type" -> mediaType = reader.getAttributeValue(i)
                                    "properties" -> properties = reader.getAttributeValue(i)
                                }
                            }
                            if (id != null && href != null) {
                                manifestItems[id] = ManifestItem(href, mediaType, properties)
                            }
                        }
                        "spine" -> {
                            for (i in 0 until reader.attributeCount) {
                                if (reader.getAttributeLocalName(i) == "toc") {
                                    spineTocId = reader.getAttributeValue(i)
                                }
                            }
                        }
                        "itemref" -> {
                            for (i in 0 until reader.attributeCount) {
                                if (reader.getAttributeLocalName(i) == "idref") {
                                    spineItemRefs.add(reader.getAttributeValue(i))
                                }
                            }
                        }
                    }
                }
            }
        } finally {
            reader.close()
        }

        val spineHrefs = spineItemRefs.mapNotNull { idref ->
            val item = manifestItems[idref] ?: return@mapNotNull null
            if (item.mediaType == "application/xhtml+xml" || item.mediaType == "text/html") item.href else null
        }
        val navHref = manifestItems.values
            .firstOrNull { it.properties?.contains("nav") == true }
            ?.href
        val ncxHref = spineTocId?.let { manifestItems[it]?.href }
            ?: manifestItems.values.firstOrNull { it.mediaType == "application/x-dtbncx+xml" }?.href

        return OpfData(spineHrefs = spineHrefs, navHref = navHref, ncxHref = ncxHref)
    }

    private fun loadToc(zip: ZipReader, opfDir: String, opf: OpfData): Pair<List<TocEntry>, String> {
        // Prefer EPUB3 nav.xhtml.
        opf.navHref?.let { href ->
            val path = resolvePath(opfDir, href)
            val bytes = zip.readEntry(path)
            if (bytes != null) {
                val parsed = runCatching { parseNavXhtml(bytes.decodeToString()) }.getOrNull().orEmpty()
                if (parsed.isNotEmpty()) {
                    return parsed to path.substringBeforeLast("/", "")
                }
            }
        }
        // Fall back to EPUB2 NCX.
        opf.ncxHref?.let { href ->
            val path = resolvePath(opfDir, href)
            val bytes = zip.readEntry(path)
            if (bytes != null) {
                val parsed = runCatching { parseNcx(bytes.decodeToString()) }.getOrNull().orEmpty()
                if (parsed.isNotEmpty()) {
                    return parsed to path.substringBeforeLast("/", "")
                }
            }
        }
        return emptyList<TocEntry>() to ""
    }

    /** Parse an EPUB3 nav document — looks for `<nav epub:type="toc">` and walks its `<ol>` tree. */
    private fun parseNavXhtml(xml: String): List<TocEntry> {
        val reader = xmlStreaming.newReader(xml)
        try {
            while (reader.hasNext()) {
                val event = reader.next()
                if (event == EventType.START_ELEMENT && reader.localName == "nav") {
                    if (isTocNav(reader)) {
                        return parseNavBody(reader)
                    }
                }
            }
        } finally {
            reader.close()
        }
        return emptyList()
    }

    private fun isTocNav(reader: XmlReader): Boolean {
        for (i in 0 until reader.attributeCount) {
            if (reader.getAttributeLocalName(i) == "type" && reader.getAttributeValue(i) == "toc") {
                return true
            }
        }
        return false
    }

    /** Inside a `<nav epub:type="toc">`, find and parse the first `<ol>`. */
    private fun parseNavBody(reader: XmlReader): List<TocEntry> {
        while (reader.hasNext()) {
            val event = reader.next()
            when (event) {
                EventType.START_ELEMENT -> {
                    if (reader.localName == "ol") return parseOl(reader)
                    skipElement(reader)
                }
                EventType.END_ELEMENT -> {
                    if (reader.localName == "nav") return emptyList()
                }
                else -> {}
            }
        }
        return emptyList()
    }

    private fun parseOl(reader: XmlReader): List<TocEntry> {
        val entries = mutableListOf<TocEntry>()
        while (reader.hasNext()) {
            val event = reader.next()
            when (event) {
                EventType.START_ELEMENT -> {
                    if (reader.localName == "li") {
                        parseLi(reader)?.let { entries.add(it) }
                    } else {
                        skipElement(reader)
                    }
                }
                EventType.END_ELEMENT -> {
                    if (reader.localName == "ol") return entries
                }
                else -> {}
            }
        }
        return entries
    }

    private fun parseLi(reader: XmlReader): TocEntry? {
        var title: String? = null
        var href: String? = null
        var children: List<TocEntry> = emptyList()

        while (reader.hasNext()) {
            val event = reader.next()
            when (event) {
                EventType.START_ELEMENT -> {
                    when (reader.localName) {
                        "a" -> {
                            for (i in 0 until reader.attributeCount) {
                                if (reader.getAttributeLocalName(i) == "href") {
                                    href = reader.getAttributeValue(i)
                                }
                            }
                            title = collectText(reader, "a")
                        }
                        "span" -> {
                            // Group entries without a link.
                            if (title == null) title = collectText(reader, "span")
                        }
                        "ol" -> {
                            children = parseOl(reader)
                        }
                        else -> skipElement(reader)
                    }
                }
                EventType.END_ELEMENT -> {
                    if (reader.localName == "li") {
                        val finalTitle = title?.trim().orEmpty()
                        if (finalTitle.isBlank()) return null
                        return TocEntry(
                            title = finalTitle,
                            href = href ?: "",
                            children = children,
                        )
                    }
                }
                else -> {}
            }
        }
        return null
    }

    /** Parse an EPUB2 NCX — walks `<navMap>` and recursive `<navPoint>` elements. */
    private fun parseNcx(xml: String): List<TocEntry> {
        val reader = xmlStreaming.newReader(xml)
        try {
            while (reader.hasNext()) {
                val event = reader.next()
                if (event == EventType.START_ELEMENT && reader.localName == "navMap") {
                    return parseNavPoints(reader, "navMap")
                }
            }
        } finally {
            reader.close()
        }
        return emptyList()
    }

    private fun parseNavPoints(reader: XmlReader, endTag: String): List<TocEntry> {
        val entries = mutableListOf<TocEntry>()
        while (reader.hasNext()) {
            val event = reader.next()
            when (event) {
                EventType.START_ELEMENT -> {
                    if (reader.localName == "navPoint") {
                        parseNavPoint(reader)?.let { entries.add(it) }
                    } else {
                        skipElement(reader)
                    }
                }
                EventType.END_ELEMENT -> {
                    if (reader.localName == endTag) return entries
                }
                else -> {}
            }
        }
        return entries
    }

    private fun parseNavPoint(reader: XmlReader): TocEntry? {
        var title: String? = null
        var href: String? = null
        val children = mutableListOf<TocEntry>()

        while (reader.hasNext()) {
            val event = reader.next()
            when (event) {
                EventType.START_ELEMENT -> {
                    when (reader.localName) {
                        "navLabel" -> title = parseNavLabel(reader)
                        "content" -> {
                            for (i in 0 until reader.attributeCount) {
                                if (reader.getAttributeLocalName(i) == "src") {
                                    href = reader.getAttributeValue(i)
                                }
                            }
                            skipElement(reader)
                        }
                        "navPoint" -> parseNavPoint(reader)?.let { children.add(it) }
                        else -> skipElement(reader)
                    }
                }
                EventType.END_ELEMENT -> {
                    if (reader.localName == "navPoint") {
                        val finalTitle = title?.trim().orEmpty()
                        val finalHref = href ?: return null
                        if (finalTitle.isBlank()) return null
                        return TocEntry(title = finalTitle, href = finalHref, children = children)
                    }
                }
                else -> {}
            }
        }
        return null
    }

    private fun parseNavLabel(reader: XmlReader): String {
        val sb = StringBuilder()
        while (reader.hasNext()) {
            val event = reader.next()
            when (event) {
                EventType.START_ELEMENT -> {
                    if (reader.localName == "text") sb.append(collectText(reader, "text"))
                    else skipElement(reader)
                }
                EventType.END_ELEMENT -> {
                    if (reader.localName == "navLabel") return sb.toString()
                }
                else -> {}
            }
        }
        return sb.toString()
    }

    private fun parseXhtmlDocument(xhtml: String): BookSection {
        val elements = mutableListOf<BookContentElement>()
        val anchors = mutableMapOf<String, Int>()
        var sectionTitle: String? = null

        val reader = xmlStreaming.newReader(xhtml)
        try {
            while (reader.hasNext()) {
                val event = reader.next()
                if (event == EventType.START_ELEMENT) {
                    // Capture id as an anchor pointing at the next element to be appended.
                    var elementId: String? = null
                    for (i in 0 until reader.attributeCount) {
                        if (reader.getAttributeLocalName(i) == "id") {
                            val id = reader.getAttributeValue(i)
                            if (id.isNotBlank()) elementId = id
                        }
                    }
                    if (elementId != null && elementId !in anchors) {
                        anchors[elementId] = elements.size
                    }

                    when (reader.localName) {
                        "h1", "h2", "h3", "h4", "h5", "h6" -> {
                            val tag = reader.localName
                            val titleText = collectText(reader, tag)
                            if (titleText.isNotBlank()) {
                                if (sectionTitle == null) sectionTitle = titleText
                                elements.add(BookContentElement.ChapterTitle(titleText))
                            }
                        }
                        "p" -> {
                            val text = parseFormattedText(reader, "p")
                            if (text.text.isNotBlank()) {
                                elements.add(BookContentElement.Paragraph(text))
                            }
                        }
                        "blockquote" -> {
                            val quoteElements = parseBlockquote(reader)
                            elements.addAll(quoteElements)
                        }
                    }
                }
            }
        } finally {
            reader.close()
        }

        return BookSection(title = sectionTitle, elements = elements, anchors = anchors)
    }

    private fun parseBlockquote(reader: XmlReader): List<BookContentElement> {
        val elements = mutableListOf<BookContentElement>()

        while (reader.hasNext()) {
            val event = reader.next()
            when (event) {
                EventType.START_ELEMENT -> {
                    when (reader.localName) {
                        "p" -> {
                            val text = parseFormattedText(reader, "p")
                            if (text.text.isNotBlank()) {
                                elements.add(BookContentElement.Quote(text))
                            }
                        }
                        else -> {
                            val text = parseFormattedText(reader, reader.localName)
                            if (text.text.isNotBlank()) {
                                elements.add(BookContentElement.Quote(text))
                            }
                        }
                    }
                }
                EventType.TEXT, EventType.CDSECT -> {
                    val text = reader.text.trim()
                    if (text.isNotBlank()) {
                        elements.add(BookContentElement.Quote(AnnotatedString(text)))
                    }
                }
                EventType.END_ELEMENT -> {
                    if (reader.localName == "blockquote") break
                }
                else -> {}
            }
        }

        return elements
    }

    private fun parseFormattedText(reader: XmlReader, endTag: String): AnnotatedString {
        return buildAnnotatedString {
            parseInlineContent(reader, endTag, this)
        }
    }

    private fun parseInlineContent(
        reader: XmlReader,
        endTag: String,
        builder: AnnotatedString.Builder,
    ) {
        while (reader.hasNext()) {
            val event = reader.next()
            when (event) {
                EventType.START_ELEMENT -> {
                    when (reader.localName) {
                        "b", "strong" -> {
                            builder.withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) {
                                parseInlineContent(reader, reader.localName, builder)
                            }
                        }
                        "i", "em" -> {
                            builder.withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                                parseInlineContent(reader, reader.localName, builder)
                            }
                        }
                        "br" -> {
                            builder.append("\n")
                            skipElement(reader)
                        }
                        else -> {
                            parseInlineContent(reader, reader.localName, builder)
                        }
                    }
                }
                EventType.TEXT, EventType.CDSECT -> {
                    builder.append(reader.text)
                }
                EventType.END_ELEMENT -> {
                    if (reader.localName == endTag) return
                }
                else -> {}
            }
        }
    }

    private fun collectText(reader: XmlReader, endTag: String): String {
        val sb = StringBuilder()
        while (reader.hasNext()) {
            val event = reader.next()
            when (event) {
                EventType.TEXT, EventType.CDSECT -> sb.append(reader.text)
                EventType.START_ELEMENT -> sb.append(collectText(reader, reader.localName))
                EventType.END_ELEMENT -> {
                    if (reader.localName == endTag) return sb.toString().trim()
                }
                else -> {}
            }
        }
        return sb.toString().trim()
    }

    private fun skipElement(reader: XmlReader) {
        var depth = 1
        while (reader.hasNext() && depth > 0) {
            when (reader.next()) {
                EventType.START_ELEMENT -> depth++
                EventType.END_ELEMENT -> depth--
                else -> {}
            }
        }
    }

    /**
     * Resolves [href] against [baseDir] inside the EPUB zip. Handles "./", "../", leading "/",
     * and empty segments. The returned path is forward-slash-joined and zip-relative.
     */
    private fun resolvePath(baseDir: String, href: String): String {
        if (href.startsWith("/")) return href.removePrefix("/")
        val parts = mutableListOf<String>()
        if (baseDir.isNotEmpty()) {
            parts.addAll(baseDir.split("/").filter { it.isNotEmpty() })
        }
        href.split("/").forEach { part ->
            when (part) {
                "", "." -> { /* skip */ }
                ".." -> if (parts.isNotEmpty()) parts.removeAt(parts.size - 1)
                else -> parts.add(part)
            }
        }
        return parts.joinToString("/")
    }

    private data class ManifestItem(
        val href: String,
        val mediaType: String?,
        val properties: String?,
    )

    private data class OpfData(
        val spineHrefs: List<String>,
        val navHref: String?,
        val ncxHref: String?,
    )
}
