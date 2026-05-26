package com.jvcodingsolutions.pagekeeper.feature.globalbookmarks.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jvcodingsolutions.pagekeeper.core.presentation.ObserveAsEvents
import com.jvcodingsolutions.pagekeeper.designsystem.components.BookmarkBookCard
import com.jvcodingsolutions.pagekeeper.designsystem.components.BookmarksEmptyState
import com.jvcodingsolutions.pagekeeper.designsystem.theme.AppIcons
import com.jvcodingsolutions.pagekeeper.designsystem.theme.BgMain
import com.jvcodingsolutions.pagekeeper.designsystem.theme.Icons as IconColor
import com.jvcodingsolutions.pagekeeper.designsystem.theme.LoaderMain
import com.jvcodingsolutions.pagekeeper.designsystem.theme.LoaderSecondary
import com.jvcodingsolutions.pagekeeper.designsystem.theme.TextPrimary
import com.jvcodingsolutions.pagekeeper.designsystem.theme.TextSecondary
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun GlobalBookmarksScreenRoot(
    onMenuClick: () -> Unit,
    onOpenBookBookmarks: (String) -> Unit,
    viewModel: GlobalBookmarksViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is GlobalBookmarksEvent.OpenMenu -> onMenuClick()
            is GlobalBookmarksEvent.OpenBookBookmarks -> onOpenBookBookmarks(event.bookId)
        }
    }

    GlobalBookmarksScreen(state = state, onAction = viewModel::onAction)
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class, ExperimentalMaterial3Api::class)
@Composable
fun GlobalBookmarksScreen(
    state: GlobalBookmarksState,
    onAction: (GlobalBookmarksAction) -> Unit,
) {
    val windowSizeClass = calculateWindowSizeClass()
    val isWideScreen = windowSizeClass.widthSizeClass >= WindowWidthSizeClass.Expanded
    val showMenuIcon = windowSizeClass.widthSizeClass < WindowWidthSizeClass.Expanded

    Scaffold(
        containerColor = BgMain,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    if (state.isSearchActive) {
                        SearchField(
                            query = state.searchQuery,
                            onQueryChange = { onAction(GlobalBookmarksAction.OnSearchQueryChange(it)) },
                        )
                    } else {
                        Text(
                            text = "Bookmarks",
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimary,
                        )
                    }
                },
                navigationIcon = {
                    if (showMenuIcon) {
                        IconButton(onClick = { onAction(GlobalBookmarksAction.OnMenuClick) }) {
                            Icon(
                                imageVector = AppIcons.Menu,
                                contentDescription = "Open menu",
                                tint = IconColor,
                                modifier = Modifier.size(24.dp),
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { onAction(GlobalBookmarksAction.OnSearchToggle) }) {
                        Icon(
                            imageVector = if (state.isSearchActive) AppIcons.Close else AppIcons.Search,
                            contentDescription = if (state.isSearchActive) "Close search" else "Search",
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
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
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
                state.items.isEmpty() && !state.isSearchActive -> {
                    BookmarksEmptyState(useFilledCircleIcon = true)
                }
                state.items.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "No books match your search",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                        )
                    }
                }
                isWideScreen -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        items(
                            count = state.items.size,
                            key = { state.items[it].book.id },
                        ) { index ->
                            val item = state.items[index]
                            BookmarkBookCard(
                                book = item.book,
                                bookmarkCount = item.bookmarkCount,
                                onClick = { onAction(GlobalBookmarksAction.OnBookClick(item.book.id)) },
                                onViewBookmarksClick = {
                                    onAction(GlobalBookmarksAction.OnViewBookmarksClick(item.book.id))
                                },
                                onDeleteAllClick = {
                                    onAction(GlobalBookmarksAction.OnDeleteAllClick(item.book.id))
                                },
                            )
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(vertical = 4.dp),
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        items(
                            count = state.items.size,
                            key = { state.items[it].book.id },
                        ) { index ->
                            val item = state.items[index]
                            BookmarkBookCard(
                                book = item.book,
                                bookmarkCount = item.bookmarkCount,
                                onClick = { onAction(GlobalBookmarksAction.OnBookClick(item.book.id)) },
                                onViewBookmarksClick = {
                                    onAction(GlobalBookmarksAction.OnViewBookmarksClick(item.book.id))
                                },
                                onDeleteAllClick = {
                                    onAction(GlobalBookmarksAction.OnDeleteAllClick(item.book.id))
                                },
                            )
                        }
                    }
                }
            }
        }

        if (state.confirmDeleteAllBookId != null) {
            DeleteAllBookmarksDialog(onAction = onAction)
        }
    }
}

@Composable
private fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = {
            Text("Search books", color = TextSecondary)
        },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary,
        ),
    )
}
