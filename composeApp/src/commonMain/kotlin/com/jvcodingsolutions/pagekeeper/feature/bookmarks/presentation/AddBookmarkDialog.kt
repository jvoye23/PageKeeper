package com.jvcodingsolutions.pagekeeper.feature.bookmarks.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jvcodingsolutions.pagekeeper.core.domain.BookmarkColor
import com.jvcodingsolutions.pagekeeper.designsystem.theme.AppIcons
import com.jvcodingsolutions.pagekeeper.designsystem.theme.BgActive
import com.jvcodingsolutions.pagekeeper.designsystem.theme.BgMain
import com.jvcodingsolutions.pagekeeper.designsystem.theme.BgModalInput
import com.jvcodingsolutions.pagekeeper.designsystem.theme.BgModalOutline
import com.jvcodingsolutions.pagekeeper.designsystem.theme.Divider
import com.jvcodingsolutions.pagekeeper.designsystem.theme.PageKeeperTheme
import com.jvcodingsolutions.pagekeeper.designsystem.theme.Icons as IconColor
import com.jvcodingsolutions.pagekeeper.designsystem.theme.Primary
import com.jvcodingsolutions.pagekeeper.designsystem.theme.TextPrimary
import com.jvcodingsolutions.pagekeeper.designsystem.theme.TextSecondary
import com.jvcodingsolutions.pagekeeper.designsystem.theme.label
import com.jvcodingsolutions.pagekeeper.designsystem.theme.toComposeColor

@Composable
fun AddBookmarkDialog(
    state: BookmarksDialog.AddOrEdit,
    onAction: (BookmarksAction) -> Unit,
) {
    AlertDialog(
        onDismissRequest = { onAction(BookmarksAction.OnDialogCancel) },
        containerColor = BgMain,
        title = {
            Text(
                text = "Add bookmark",
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(
                    modifier = Modifier
                        .border(
                            width = 1.dp,
                            color = BgModalOutline,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .background(
                            color = BgModalInput,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(horizontal = 16.dp)
                        .padding(vertical = 8.dp)

                ) {
                    Text(
                        text = "Title",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = state.text,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextPrimary,
                        maxLines = 2
                    )
                }
                ColorSelector(
                    selected = state.color,
                    expanded = state.isColorDropdownExpanded,
                    onToggle = { onAction(BookmarksAction.OnDialogColorDropdownToggle(it)) },
                    onColorSelected = { onAction(BookmarksAction.OnDialogColorChange(it)) },
                )
                HorizontalDivider(
                    thickness = 1.dp,
                    color = Divider
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onAction(BookmarksAction.OnDialogSave) }) {
                Text(
                    text = "Save",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary
                )
            }
        },
        dismissButton = {
            TextButton(onClick = { onAction(BookmarksAction.OnDialogCancel) }) {
                Text(
                    text = "Cancel",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary
                )
            }
        }
    )
}

@Composable
private fun ColorSelector(
    selected: BookmarkColor,
    expanded: Boolean,
    onToggle: (Boolean) -> Unit,
    onColorSelected: (BookmarkColor) -> Unit,
) {
    val density = LocalDensity.current
    var fieldWidthPx by remember { mutableStateOf(0) }
    val fieldWidthDp = with(density) { fieldWidthPx.toDp() }

    Box(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = if (expanded) Divider else BgModalOutline,
                    shape = RoundedCornerShape(16.dp)
                )
                .onSizeChanged { fieldWidthPx = it.width }
                .clip(RoundedCornerShape(16.dp))
                .background(BgModalInput)
                .clickable { onToggle(!expanded) }
                .padding(all = 12.dp),
        ) {
            ColorSwatch(selected)
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = selected.label(),
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            // Down chevron when collapsed, up chevron when expanded
            // (Reusing the back-arrow asset: +90° = up, +270° = down.)
            Icon(
                imageVector = AppIcons.ChevronRight,
                contentDescription = if (expanded) "Collapse colors" else "Expand colors",
                tint = IconColor,
                modifier = Modifier
                    .size(20.dp)
                    .rotate(if (expanded) 90f else 0f),
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { onToggle(false) },
            shape = RoundedCornerShape(16.dp),
            containerColor = BgMain,
            modifier = Modifier.width(fieldWidthDp),
        ) {
            BookmarkColor.entries.forEach { color ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = color.label(),
                            color = TextPrimary,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    },
                    leadingIcon = { ColorSwatch(color) },
                    onClick = { onColorSelected(color) },
                    colors = MenuDefaults.itemColors(
                        textColor = TextPrimary,
                        leadingIconColor = IconColor,
                    ),
                    modifier = Modifier

                        .padding(
                            horizontal = if (color == selected) 4.dp else 0.dp
                        )
                        .padding(
                            vertical = if (color == selected) 2.dp else 0.dp
                        )
                        .background(
                            color = if (color == selected) BgActive else Color.Transparent,
                            shape = if (color == selected) RoundedCornerShape(12.dp) else RoundedCornerShape(0.dp)
                        )

                )
            }
        }
    }
}

@Composable
private fun ColorSwatch(color: BookmarkColor) {
    Box(
        modifier = Modifier
            .size(16.dp)
            .clip(CircleShape)
            .background(color.toComposeColor()),
    )
}

@Preview
@Composable
private fun AddBookmarkDialogPreview() {
    PageKeeperTheme {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            AddBookmarkDialog(
                state = BookmarksDialog.AddOrEdit(
                    editingId = null,
                    text = "The forest was unusually quiet that evening",
                    color = BookmarkColor.RED,
                    isColorDropdownExpanded = false,
                    firstVisibleItemIndex = 0,
                    firstVisibleItemScrollOffset = 0,
                    loadedSectionCount = 1,
                    sectionIndex = 1,
                    chapterTitle = "Chapter 1"
                ),
                onAction = {}
            )
        }
    }
}
