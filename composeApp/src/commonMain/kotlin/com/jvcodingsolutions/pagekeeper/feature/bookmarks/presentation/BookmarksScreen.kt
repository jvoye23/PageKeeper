package com.jvcodingsolutions.pagekeeper.feature.bookmarks.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Devices.PIXEL_9_PRO
import androidx.compose.ui.tooling.preview.Devices.PIXEL_TABLET
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jvcodingsolutions.pagekeeper.core.domain.Bookmark
import com.jvcodingsolutions.pagekeeper.core.domain.BookmarkColor
import com.jvcodingsolutions.pagekeeper.core.presentation.ObserveAsEvents
import com.jvcodingsolutions.pagekeeper.designsystem.components.BookmarkItem
import com.jvcodingsolutions.pagekeeper.designsystem.components.BookmarksEmptyState
import com.jvcodingsolutions.pagekeeper.designsystem.theme.AppIcons
import com.jvcodingsolutions.pagekeeper.designsystem.theme.BgBottomNav
import com.jvcodingsolutions.pagekeeper.designsystem.theme.BgMain
import com.jvcodingsolutions.pagekeeper.designsystem.theme.Icons as IconColor
import com.jvcodingsolutions.pagekeeper.designsystem.theme.LoaderMain
import com.jvcodingsolutions.pagekeeper.designsystem.theme.LoaderSecondary
import com.jvcodingsolutions.pagekeeper.designsystem.theme.OnPrimary
import com.jvcodingsolutions.pagekeeper.designsystem.theme.PageKeeperTheme
import com.jvcodingsolutions.pagekeeper.designsystem.theme.Primary
import com.jvcodingsolutions.pagekeeper.designsystem.theme.TextPrimary
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun BookmarksScreenRoot(
    bookId: String,
    onBackClick: () -> Unit,
    onOpenReader: (String) -> Unit,
    viewModel: BookmarksViewModel = koinViewModel { parametersOf(bookId) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is BookmarksEvent.NavigateBack -> onBackClick()
            is BookmarksEvent.OpenReaderAtBookmark -> onOpenReader(event.bookId)
        }
    }

    BookmarksScreen(state = state, onAction = viewModel::onAction)
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class, ExperimentalMaterial3Api::class)
@Composable
fun BookmarksScreen(
    state: BookmarksState,
    onAction: (BookmarksAction) -> Unit,
) {
    val windowSizeClass = calculateWindowSizeClass()
    val isWideScreen = windowSizeClass.widthSizeClass >= WindowWidthSizeClass.Expanded

    Scaffold(
        containerColor = BgMain,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Bookmarks",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onAction(BookmarksAction.OnBackClick) }) {
                        Icon(
                            imageVector = AppIcons.ArrowBack,
                            contentDescription = "Back",
                            tint = IconColor,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = BgMain,
                ),
            )
        },
        floatingActionButton = {
            if (!state.isLoading) {
                ExtendedFloatingActionButton(
                    onClick = { onAction(BookmarksAction.OnAddBookmarkClick) },
                    containerColor = Primary,
                    contentColor = OnPrimary,
                    icon = {
                        Icon(
                            imageVector = AppIcons.BookmarkAdd,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                        )
                    },
                    text = { Text("Add bookmark") },
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = 6.dp,
                    ),
                )
            }
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.TopCenter,
        ) {
            val contentModifier = if (isWideScreen) {
                Modifier.widthIn(max = 800.dp).fillMaxSize()
            } else {
                Modifier.fillMaxSize()
            }

            when {
                state.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(48.dp),
                            color = LoaderMain,
                            trackColor = LoaderSecondary,
                            strokeWidth = 5.dp,
                        )
                    }
                }
                state.bookmarks.isEmpty() -> {
                    BookmarksEmptyState(modifier = contentModifier)
                }
                else -> {
                    val listState = rememberLazyListState()
                    LazyColumn(
                        state = listState,
                        modifier = contentModifier,
                        contentPadding = PaddingValues(
                            top = 4.dp,
                            bottom = 96.dp, // make room for FAB
                        ),
                        verticalArrangement = Arrangement.Top,
                    ) {
                        items(
                            count = state.bookmarks.size,
                            key = { state.bookmarks[it].id },
                        ) { index ->
                            val bookmark = state.bookmarks[index]
                            BookmarkItem(
                                bookmark = bookmark,
                                onClick = {
                                    onAction(BookmarksAction.OnBookmarkClick(bookmark.id))
                                },
                                onEditClick = {
                                    onAction(BookmarksAction.OnMenuEditClick(bookmark.id))
                                },
                                onDeleteClick = {
                                    onAction(BookmarksAction.OnMenuDeleteClick(bookmark.id))
                                },
                            )
                        }
                        item { Spacer(modifier = Modifier.size(8.dp)) }
                    }
                }
            }
        }

        when (val dialog = state.dialog) {
            is BookmarksDialog.None -> Unit
            is BookmarksDialog.AddOrEdit -> AddBookmarkDialog(state = dialog, onAction = onAction)
            is BookmarksDialog.ConfirmDelete -> DeleteBookmarkDialog(onAction = onAction)
        }
    }
}

@Preview(device = PIXEL_9_PRO, showSystemUi = true)
@Composable
private fun BookmarksScreenEmptyPreview() {
    PageKeeperTheme {
        BookmarksScreen(
            state = BookmarksState(
                isLoading = false
            ),
            onAction = {}
        )
    }
}

@Preview(device = PIXEL_9_PRO, showSystemUi = true)
@Composable
private fun BookmarksScreenPreview() {
    PageKeeperTheme {
        BookmarksScreen(
            state = BookmarksState(
                isLoading = false,
                bookmarks = listOf(
                    Bookmark(
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
                    )
                )
            ),
            onAction = {}
        )
    }
}

@Preview(device = PIXEL_TABLET, showSystemUi = true)
@Composable
private fun BookmarksScreenTabletLandscapePreview() {
    PageKeeperTheme {
        BookmarksScreen(
            state = BookmarksState(
                isLoading = false,
                bookmarks = listOf(
                    Bookmark(
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
                    )
                )
            ),
            onAction = {}
        )
    }
}
