package com.jvcodingsolutions.pagekeeper.core.domain

data class Book(
    val id: String,
    val title: String,
    val author: String,
    val coverImagePath: String?,
    val isFavorite: Boolean = false,
    val isFinished: Boolean = false,
    val dateAdded: Long
)
