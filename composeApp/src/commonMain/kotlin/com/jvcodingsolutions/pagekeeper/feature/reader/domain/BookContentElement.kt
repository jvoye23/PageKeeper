package com.jvcodingsolutions.pagekeeper.feature.reader.domain

import androidx.compose.ui.text.AnnotatedString

sealed interface BookContentElement {
    data class ChapterTitle(val text: String) : BookContentElement
    data class Paragraph(val text: AnnotatedString) : BookContentElement
    data class Quote(val text: AnnotatedString) : BookContentElement
}
