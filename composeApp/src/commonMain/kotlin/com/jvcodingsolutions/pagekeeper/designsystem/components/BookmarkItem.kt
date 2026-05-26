package com.jvcodingsolutions.pagekeeper.designsystem.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Devices.PIXEL_9_PRO
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jvcodingsolutions.pagekeeper.core.domain.Bookmark
import com.jvcodingsolutions.pagekeeper.core.domain.BookmarkColor
import com.jvcodingsolutions.pagekeeper.core.presentation.toBookmarkDisplayString
import com.jvcodingsolutions.pagekeeper.designsystem.theme.AppIcons
import com.jvcodingsolutions.pagekeeper.designsystem.theme.BgMain
import com.jvcodingsolutions.pagekeeper.designsystem.theme.Divider
import com.jvcodingsolutions.pagekeeper.designsystem.theme.PageKeeperTheme
import com.jvcodingsolutions.pagekeeper.designsystem.theme.Primary
import com.jvcodingsolutions.pagekeeper.designsystem.theme.Icons as IconColor
import com.jvcodingsolutions.pagekeeper.designsystem.theme.StateAlert
import com.jvcodingsolutions.pagekeeper.designsystem.theme.TextPrimary
import com.jvcodingsolutions.pagekeeper.designsystem.theme.TextSecondary
import com.jvcodingsolutions.pagekeeper.designsystem.theme.toComposeColor
import com.jvcodingsolutions.pagekeeper.feature.bookmarks.presentation.BookmarksScreen
import com.jvcodingsolutions.pagekeeper.feature.bookmarks.presentation.BookmarksState

@Composable
fun BookmarkItem(
    bookmark: Bookmark,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = AppIcons.BookmarkFilled,
                    contentDescription = null,
                    tint = bookmark.color.toComposeColor(),
                    modifier = Modifier
                        .size(24.dp),
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = bookmark.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = bookmark.chapterTitle.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = bookmark.createdAt.toBookmarkDisplayString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        maxLines = 1,
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Box {
                IconButton(
                    onClick = { menuExpanded = true },
                    modifier = Modifier.size(48.dp),
                    shape = if(menuExpanded) RoundedCornerShape(16.dp) else RoundedCornerShape(0.dp),
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = if (menuExpanded) Primary else Color.Transparent
                    )
                ) {
                    Icon(
                        imageVector = AppIcons.MoreVert,
                        contentDescription = "More",
                        tint = if (menuExpanded) BgMain else IconColor,
                        modifier = Modifier.size(24.dp),
                    )
                }
                BookmarkActionsMenu(
                    modifier = Modifier
                        .offset(y = 56.dp, x = 48.dp),
                    expanded = menuExpanded,
                    onDismiss = { menuExpanded = false },
                    onEditClick = {
                        menuExpanded = false
                        onEditClick()
                    },
                    onDeleteClick = {
                        menuExpanded = false
                        onDeleteClick()
                    },
                )
            }
        }
        HorizontalDivider(thickness = 1.dp, color = Divider)
    }
}

@Composable
private fun BookmarkActionsMenu(
    modifier: Modifier = Modifier,
    expanded: Boolean,
    onDismiss: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    Box(
        modifier = modifier
    ) {

        DropdownMenu(
            modifier = Modifier
                .defaultMinSize(minWidth = 124.dp),
            expanded = expanded,
            onDismissRequest = onDismiss,
            shape = RoundedCornerShape(16.dp),
            containerColor = BgMain
        ) {
            DropdownMenuItem(
                text = { Text("Edit", color = TextPrimary) },
                leadingIcon = {
                    Icon(
                        imageVector = AppIcons.Edit,
                        contentDescription = null,
                        tint = IconColor,
                        modifier = Modifier.size(20.dp),
                    )
                },
                onClick = onEditClick,
            )
            DropdownMenuItem(
                text = { Text("Delete", color = StateAlert) },
                leadingIcon = {
                    Icon(
                        imageVector = AppIcons.Delete,
                        contentDescription = null,
                        tint = StateAlert,
                        modifier = Modifier.size(20.dp),
                    )
                },
                onClick = onDeleteClick,
            )
        }
    }
}

@Preview(device = PIXEL_9_PRO, showSystemUi = true)
@Composable
private fun BookmarkItemPreview() {
    PageKeeperTheme {
        Scaffold {paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)

            ) {
                BookmarkItem(
                    bookmark = Bookmark(
                        id = "1",
                        bookId = "12345",
                        text = "The forest was unusually quiet that evening. The trees stood still, casting long shadows across the narrow path.",
                        color = BookmarkColor.YELLOW,
                        chapterTitle = "CHAPTER I. Y-o-u-u Tom—Aunt Polly Decides Upon her ",
                        sectionIndex = 5,
                        firstVisibleItemIndex = 1,
                        firstVisibleItemScrollOffset = 1,
                        loadedSectionCount = 1,
                        createdAt = 13245235
                    ),
                    onClick = {},
                    onEditClick = {},
                    onDeleteClick = {},
                )
            }
        }
    }
}
