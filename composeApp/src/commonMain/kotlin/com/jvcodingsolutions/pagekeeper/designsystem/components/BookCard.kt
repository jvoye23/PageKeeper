package com.jvcodingsolutions.pagekeeper.designsystem.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jvcodingsolutions.pagekeeper.core.domain.Book
import com.jvcodingsolutions.pagekeeper.designsystem.theme.AppIcons
import com.jvcodingsolutions.pagekeeper.designsystem.theme.BgActive
import com.jvcodingsolutions.pagekeeper.designsystem.theme.BgCard
import com.jvcodingsolutions.pagekeeper.designsystem.theme.BgMain
import com.jvcodingsolutions.pagekeeper.designsystem.theme.Divider
import com.jvcodingsolutions.pagekeeper.designsystem.theme.Icons
import com.jvcodingsolutions.pagekeeper.designsystem.theme.PageKeeperTheme
import com.jvcodingsolutions.pagekeeper.designsystem.theme.Primary
import com.jvcodingsolutions.pagekeeper.designsystem.theme.TabletBlockBg
import org.jetbrains.compose.resources.painterResource
import pagekeeper.composeapp.generated.resources.Res
import pagekeeper.composeapp.generated.resources.book_open_logo

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BookCard(
    book: Book,
    onFavoriteClick: () -> Unit,
    onFinishedClick: () -> Unit,
    onShareClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSelectionMode: Boolean = false,
    isSelected: Boolean = false,
    isTablet: Boolean = false,
) {
    val containerColor = when {
        isSelected -> BgCard
        isSelectionMode && isTablet -> BgMain
        isTablet -> TabletBlockBg
        else -> Color.Transparent
    }

    val borderStroke = if (isSelectionMode && !isSelected && !isTablet) {
        androidx.compose.foundation.BorderStroke(1.dp, Divider)
    } else {
        null
    }

    val progressFraction = if (book.isFinished) 1f else book.readingPosition.progressFraction

    Surface(color = Color.Transparent) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick,
                ),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = containerColor,
            ),
            border = borderStroke,
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    if (isSelectionMode) {
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = { onClick() },
                            colors = CheckboxDefaults.colors(
                                checkedColor = Primary,
                                uncheckedColor = Icons,
                                checkmarkColor = BgMain,
                            ),
                        )
                    }

                    BookCoverImage(
                        coverImagePath = book.coverImagePath,
                    )

                    Column(
                        modifier = Modifier
                            .padding(vertical = 8.dp),
                        verticalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text(
                                text = book.title,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onBackground,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                text = book.author,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )

                        }
                        Spacer(modifier = Modifier.height(48.dp))

                        LinearProgressIndicator(
                            progress = { progressFraction },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp),
                            color = Primary,
                            trackColor = BgActive,
                            gapSize = 6.dp,
                            strokeCap = StrokeCap.Round
                        )
                        ActionIconsRow(
                            isFavorite = book.isFavorite,
                            isFinished = book.isFinished,
                            onFavoriteClick = onFavoriteClick,
                            onFinishedClick = onFinishedClick,
                            onShareClick = onShareClick,
                            onDeleteClick = onDeleteClick,
                        )
                    }
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
    val coverShape = RoundedCornerShape(4.dp)

    if (coverImagePath != null) {
        AsyncImage(
            model = coverImagePath,
            contentDescription = "Book cover",
            contentScale = ContentScale.Crop,
            modifier = modifier
                .width(104.dp)
                .height(156.dp)
                .clip(coverShape),
        )
    } else {
        Box(
            contentAlignment = Alignment.Center,
            modifier = modifier
                .width(104.dp)
                .height(156.dp)
                .clip(coverShape)
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

@Composable
private fun ActionIconsRow(
    isFavorite: Boolean,
    isFinished: Boolean,
    onFavoriteClick: () -> Unit,
    onFinishedClick: () -> Unit,
    onShareClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = onFavoriteClick,
                modifier = Modifier.size(32.dp),
            ) {
                Icon(
                    imageVector = if (isFavorite) AppIcons.StarFilled else AppIcons.StarOutline,
                    contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                    tint = Icons,
                    modifier = Modifier.size(20.dp),
                )
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(32.dp)
                    .then(
                        if (isFinished) Modifier.background(BgActive, CircleShape)
                        else Modifier
                    ),
            ) {
                IconButton(
                    onClick = onFinishedClick,
                    modifier = Modifier.size(32.dp),
                ) {
                    Icon(
                        imageVector = if (isFinished) AppIcons.FlagFilled else AppIcons.FlagOutline,
                        contentDescription = if (isFinished) "Unmark as finished" else "Mark as finished",
                        tint = Icons,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }

            IconButton(
                onClick = onShareClick,
                modifier = Modifier.size(32.dp),
            ) {
                Icon(
                    imageVector = AppIcons.Share,
                    contentDescription = "Share",
                    tint = Icons,
                    modifier = Modifier.size(20.dp),
                )
            }
        }

        IconButton(
            onClick = onDeleteClick,
            modifier = Modifier.size(32.dp),
        ) {
            Icon(
                imageVector = AppIcons.Delete,
                contentDescription = "Delete",
                tint = Icons,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Preview
@Composable
private fun BookCardPreview() {
    PageKeeperTheme {
        BookCard(
            book = Book(
                id = "",
                title = "The Adventures of Tom Sawyer",
                author = "Mark Twain",
                coverImagePath = null,
                isFavorite = true,
                isFinished = false,
                dateAdded = 25058

            ),
            onFavoriteClick = {},
            onFinishedClick = {},
            onShareClick = {},
            onDeleteClick = {},
            onClick = {},
            onLongClick = {
            },
        )
    }
}
