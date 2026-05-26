package com.jvcodingsolutions.pagekeeper.designsystem.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.jvcodingsolutions.pagekeeper.core.domain.Book
import com.jvcodingsolutions.pagekeeper.designsystem.theme.AppIcons
import com.jvcodingsolutions.pagekeeper.designsystem.theme.BgActive
import com.jvcodingsolutions.pagekeeper.designsystem.theme.Icons as IconColor
import com.jvcodingsolutions.pagekeeper.designsystem.theme.StateAlert
import com.jvcodingsolutions.pagekeeper.designsystem.theme.TextPrimary
import com.jvcodingsolutions.pagekeeper.designsystem.theme.TextSecondary
import org.jetbrains.compose.resources.painterResource
import pagekeeper.composeapp.generated.resources.Res
import pagekeeper.composeapp.generated.resources.book_open_logo

@Composable
fun BookmarkBookCard(
    book: Book,
    bookmarkCount: Int,
    onClick: () -> Unit,
    onViewBookmarksClick: () -> Unit,
    onDeleteAllClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            BookCoverImage(coverImagePath = book.coverImagePath)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(top = 4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = book.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = book.author,
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(36.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        imageVector = AppIcons.Bookmark,
                        contentDescription = null,
                        tint = IconColor,
                        modifier = Modifier.size(20.dp),
                    )
                    Text(
                        text = bookmarkCount.toString(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary,
                    )
                }
            }
            Box {
                IconButton(
                    onClick = { menuExpanded = true },
                    modifier = Modifier.size(32.dp),
                ) {
                    Icon(
                        imageVector = AppIcons.MoreVert,
                        contentDescription = "More",
                        tint = IconColor,
                        modifier = Modifier.size(20.dp),
                    )
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                ) {
                    DropdownMenuItem(
                        text = { Text("View bookmarks", color = TextPrimary) },
                        leadingIcon = {
                            Icon(
                                imageVector = AppIcons.Visibility,
                                contentDescription = null,
                                tint = IconColor,
                                modifier = Modifier.size(20.dp),
                            )
                        },
                        onClick = {
                            menuExpanded = false
                            onViewBookmarksClick()
                        },
                    )
                    DropdownMenuItem(
                        text = { Text("Delete all bookmarks", color = StateAlert) },
                        leadingIcon = {
                            Icon(
                                imageVector = AppIcons.Delete,
                                contentDescription = null,
                                tint = StateAlert,
                                modifier = Modifier.size(20.dp),
                            )
                        },
                        onClick = {
                            menuExpanded = false
                            onDeleteAllClick()
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun BookCoverImage(
    coverImagePath: String?,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(4.dp)
    if (coverImagePath != null) {
        AsyncImage(
            model = coverImagePath,
            contentDescription = "Book cover",
            contentScale = ContentScale.Crop,
            modifier = modifier
                .width(96.dp)
                .height(132.dp)
                .clip(shape),
        )
    } else {
        Box(
            contentAlignment = Alignment.Center,
            modifier = modifier
                .width(96.dp)
                .height(132.dp)
                .clip(shape)
                .background(BgActive),
        ) {
            Image(
                painter = painterResource(Res.drawable.book_open_logo),
                contentDescription = null,
                modifier = Modifier
                    .size(48.dp)
                    .alpha(0.5f),
            )
        }
    }
}
