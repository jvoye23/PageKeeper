package com.jvcodingsolutions.pagekeeper.feature.reader.domain

data class BookSection(
    val title: String?,
    val elements: List<BookContentElement>,
    /** Maps anchor id -> element index within [elements] (used by EPUB TOC sub-chapter jumps). */
    val anchors: Map<String, Int> = emptyMap(),
)
