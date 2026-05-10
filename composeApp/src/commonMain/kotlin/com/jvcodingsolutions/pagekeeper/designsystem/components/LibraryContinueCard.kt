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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.jvcodingsolutions.pagekeeper.core.domain.Book
import com.jvcodingsolutions.pagekeeper.designsystem.theme.AppIcons
import com.jvcodingsolutions.pagekeeper.designsystem.theme.BgActive
import com.jvcodingsolutions.pagekeeper.designsystem.theme.BgCard
import com.jvcodingsolutions.pagekeeper.designsystem.theme.BgMain
import com.jvcodingsolutions.pagekeeper.designsystem.theme.Icons as IconColor
import com.jvcodingsolutions.pagekeeper.designsystem.theme.OnPrimary
import com.jvcodingsolutions.pagekeeper.designsystem.theme.Primary
import com.jvcodingsolutions.pagekeeper.designsystem.theme.TextPrimary
import org.jetbrains.compose.resources.painterResource
import pagekeeper.composeapp.generated.resources.Res
import pagekeeper.composeapp.generated.resources.book_open_logo

@Composable
fun LibraryContinueCard(
    book: Book,
    onFavoriteClick: () -> Unit,
    onFinishedClick: () -> Unit,
    onShareClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onContinueClick: () -> Unit,
    modifier: Modifier = Modifier,
    isTablet: Boolean = false,
) {
    val progressFraction = if (book.isFinished) 1f else book.readingPosition.progressFraction

    if (isTablet) {
        TabletLibraryContinueCard(
            book = book,
            progressFraction = progressFraction,
            onFavoriteClick = onFavoriteClick,
            onFinishedClick = onFinishedClick,
            onShareClick = onShareClick,
            onDeleteClick = onDeleteClick,
            onContinueClick = onContinueClick,
            modifier = modifier,
        )
        return
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .clickable(onClick = onContinueClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = BgCard,
        ),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Cover(coverImagePath = book.coverImagePath)

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 8.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
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
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }

                        Button(
                            onClick = onContinueClick,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Primary,
                                contentColor = OnPrimary,
                            ),
                        ) {
                            Icon(
                                imageVector = AppIcons.Library,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Continue",
                                style = MaterialTheme.typography.labelMedium,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
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
                                    imageVector = if (book.isFavorite) AppIcons.StarFilled else AppIcons.StarOutline,
                                    contentDescription = if (book.isFavorite) "Remove from favorites" else "Add to favorites",
                                    tint = IconColor,
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(32.dp)
                                    .then(
                                        if (book.isFinished) Modifier.background(BgActive, CircleShape)
                                        else Modifier
                                    ),
                            ) {
                                IconButton(
                                    onClick = onFinishedClick,
                                    modifier = Modifier.size(32.dp),
                                ) {
                                    Icon(
                                        imageVector = if (book.isFinished) AppIcons.FlagFilled else AppIcons.FlagOutline,
                                        contentDescription = if (book.isFinished) "Unmark as finished" else "Mark as finished",
                                        tint = IconColor,
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
                                    tint = IconColor,
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
                                tint = IconColor,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                }
            }

            LinearProgressIndicator(
                progress = { progressFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp),
                color = Primary,
                trackColor = BgMain,
                drawStopIndicator = {},
                gapSize = 0.dp,
            )
        }
    }
}

@Composable
private fun TabletLibraryContinueCard(
    book: Book,
    progressFraction: Float,
    onFavoriteClick: () -> Unit,
    onFinishedClick: () -> Unit,
    onShareClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onContinueClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .clickable(onClick = onContinueClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = BgMain,
        ),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Cover(coverImagePath = book.coverImagePath, isTablet = true)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = book.title,
                            style = MaterialTheme.typography.headlineSmall,
                            color = TextPrimary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = book.author,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }

                    Button(
                        onClick = onContinueClick,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Primary,
                            contentColor = OnPrimary,
                        ),
                    ) {
                        Icon(
                            imageVector = AppIcons.TabletContinueReading,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Continue",
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                LinearProgressIndicator(
                    progress = { progressFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp),
                    color = Primary,
                    trackColor = BgActive,
                    gapSize = 6.dp,
                    strokeCap = StrokeCap.Round,
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
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
                                imageVector = if (book.isFavorite) AppIcons.StarFilled else AppIcons.StarOutline,
                                contentDescription = if (book.isFavorite) "Remove from favorites" else "Add to favorites",
                                tint = IconColor,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(32.dp)
                                .then(
                                    if (book.isFinished) Modifier.background(BgActive, CircleShape)
                                    else Modifier
                                ),
                        ) {
                            IconButton(
                                onClick = onFinishedClick,
                                modifier = Modifier.size(32.dp),
                            ) {
                                Icon(
                                    imageVector = if (book.isFinished) AppIcons.FlagFilled else AppIcons.FlagOutline,
                                    contentDescription = if (book.isFinished) "Unmark as finished" else "Mark as finished",
                                    tint = IconColor,
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
                                tint = IconColor,
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
                            tint = IconColor,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Cover(
    coverImagePath: String?,
    modifier: Modifier = Modifier,
    isTablet: Boolean = false,
) {
    val coverShape = RoundedCornerShape(if (isTablet) 6.dp else 4.dp)
    val coverWidth = if (isTablet) 140.dp else 104.dp
    val coverHeight = if (isTablet) 210.dp else 156.dp

    if (coverImagePath != null) {
        AsyncImage(
            model = coverImagePath,
            contentDescription = "Book cover",
            contentScale = ContentScale.Crop,
            modifier = modifier
                .width(coverWidth)
                .height(coverHeight)
                .clip(coverShape),
        )
    } else {
        Box(
            contentAlignment = Alignment.Center,
            modifier = modifier
                .width(coverWidth)
                .height(coverHeight)
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
