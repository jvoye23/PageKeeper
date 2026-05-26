package com.jvcodingsolutions.pagekeeper.designsystem.theme

import androidx.compose.ui.graphics.Color
import com.jvcodingsolutions.pagekeeper.core.domain.BookmarkColor

fun BookmarkColor.toComposeColor(): Color = when (this) {
    BookmarkColor.BLUE -> BookmarkBlue
    BookmarkColor.GREEN -> BookmarkGreen
    BookmarkColor.YELLOW -> BookmarkYellow
    BookmarkColor.RED -> BookmarkRed
    BookmarkColor.PURPLE -> BookmarkPurple
}

fun BookmarkColor.label(): String = when (this) {
    BookmarkColor.BLUE -> "Blue color"
    BookmarkColor.GREEN -> "Green color"
    BookmarkColor.YELLOW -> "Yellow color"
    BookmarkColor.RED -> "Red color"
    BookmarkColor.PURPLE -> "Purple color"
}
