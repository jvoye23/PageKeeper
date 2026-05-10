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
