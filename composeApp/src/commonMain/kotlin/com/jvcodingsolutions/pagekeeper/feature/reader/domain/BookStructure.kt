package com.jvcodingsolutions.pagekeeper.feature.reader.domain

data class BookStructure(
    val sections: List<StructureSection>,
)

data class StructureSection(
    val title: String,
    val chapters: List<ChapterNode>,
)

data class ChapterNode(
    val title: String,
    /** Index into the flat parsed-section list (FB2 chunks / EPUB spine). */
    val sectionIndex: Int,
    /** Optional fragment id from a TOC href ("section_b" from "chapter1.xhtml#section_b"). */
    val anchorId: String? = null,
    val children: List<ChapterNode> = emptyList(),
) {
    /** Stable id used for tracking expansion state. */
    val id: String get() = "$sectionIndex#${anchorId ?: ""}"
}

/**
 * Returns the title of the chapter containing the given section index, walking
 * the tree depth-first and preferring the most specific (deepest) match.
 * Falls back to the section header title, then to an empty string.
 */
fun BookStructure.findChapterTitleAt(sectionIndex: Int): String {
    sections.forEach { section ->
        val match = section.chapters.firstNotNullOfOrNull {
            findNodeForSectionIndex(it, sectionIndex)
        }
        if (match != null) return match.title
        if (section.chapters.any { it.sectionIndex == sectionIndex }) return section.title
    }
    return sections.firstOrNull()?.title.orEmpty()
}

private fun findNodeForSectionIndex(node: ChapterNode, sectionIndex: Int): ChapterNode? {
    // Prefer the deepest descendant whose sectionIndex matches; fall back to this
    // node if no child matches and this one matches.
    node.children.forEach { child ->
        val nested = findNodeForSectionIndex(child, sectionIndex)
        if (nested != null) return nested
    }
    return if (node.sectionIndex == sectionIndex) node else null
}
