package com.jvcodingsolutions.pagekeeper.core.data

data class BookMetadata(
    val title: String,
    val author: String,
    val coverBytes: ByteArray?,
    val coverContentType: String?,
)
