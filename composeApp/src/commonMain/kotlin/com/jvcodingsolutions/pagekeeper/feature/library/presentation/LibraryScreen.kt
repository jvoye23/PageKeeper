package com.jvcodingsolutions.pagekeeper.feature.library.presentation

import androidx.compose.foundation.Image
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices.PIXEL_9_PRO
import androidx.compose.ui.tooling.preview.Devices.PIXEL_TABLET
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jvcodingsolutions.pagekeeper.core.presentation.ObserveAsEvents
import com.jvcodingsolutions.pagekeeper.core.presentation.UiText
import com.jvcodingsolutions.pagekeeper.core.presentation.ShareFile
import com.jvcodingsolutions.pagekeeper.core.presentation.rememberBookSharerLauncher
import com.jvcodingsolutions.pagekeeper.core.presentation.rememberFilePickerLauncher
import com.jvcodingsolutions.pagekeeper.designsystem.components.BookCard
import com.jvcodingsolutions.pagekeeper.designsystem.components.SearchResultBookCard
import com.jvcodingsolutions.pagekeeper.designsystem.theme.AppIcons
import com.jvcodingsolutions.pagekeeper.designsystem.theme.Icons as IconColor
import com.jvcodingsolutions.pagekeeper.designsystem.theme.BgMain
import com.jvcodingsolutions.pagekeeper.designsystem.theme.Divider as DividerColor
import com.jvcodingsolutions.pagekeeper.designsystem.theme.OnPrimary
import com.jvcodingsolutions.pagekeeper.designsystem.theme.StateAlert
import com.jvcodingsolutions.pagekeeper.designsystem.theme.TabletBlockBg
import com.jvcodingsolutions.pagekeeper.designsystem.theme.PageKeeperTheme
import com.jvcodingsolutions.pagekeeper.designsystem.theme.TextPrimary
import com.jvcodingsolutions.pagekeeper.designsystem.theme.TextSecondary
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import pagekeeper.composeapp.generated.resources.Res
import pagekeeper.composeapp.generated.resources.book_open_logo

@Composable
fun LibraryScreenRoot(
    onMenuClick: () -> Unit,
    viewModel: LibraryViewModel = koinViewModel(),
    containerColor: Color = MaterialTheme.colorScheme.background,
    isTablet: Boolean = false,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val filePickerLauncher = rememberFilePickerLauncher { fileName, fileBytes ->
        if (fileName != null && fileBytes != null) {
            viewModel.onAction(LibraryAction.OnBookFileSelected(fileName, fileBytes))
        }
    }

    val bookSharer = rememberBookSharerLauncher()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is LibraryEvent.OpenFilePicker -> {
                filePickerLauncher.launch()
            }
            is LibraryEvent.ShareBook -> {
                bookSharer.share(ShareFile(event.book.filePath, event.book.title))
            }
            is LibraryEvent.ShareBooks -> {
                bookSharer.share(event.books.map { ShareFile(it.filePath, it.title) })
            }
        }
    }

    LibraryScreen(
        state = state,
        onAction = viewModel::onAction,
        onMenuClick = onMenuClick,
        containerColor = containerColor,
        isTablet = isTablet,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    state: LibraryState,
    onAction: (LibraryAction) -> Unit,
    onMenuClick: () -> Unit,
    containerColor: Color = MaterialTheme.colorScheme.background,
    isTablet: Boolean = false,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                if (isTablet && state.isSelectionMode) {
                    TabletSelectionToolbar(
                        state = state,
                        onAction = onAction,
                        containerColor = containerColor,
                    )
                } else if (isTablet) {
                    TabletSearchBar(
                        state = state,
                        onAction = onAction,
                        containerColor = containerColor,
                    )
                } else if (state.isSelectionMode) {
                    TopAppBar(
                        title = {
                            Text(
                                text = "${state.selectedBookIds.size} selected",
                                style = MaterialTheme.typography.titleLarge,
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = { onAction(LibraryAction.OnExitSelectionMode) }) {
                                Icon(
                                    imageVector = AppIcons.ArrowBack,
                                    contentDescription = "Exit selection mode",
                                    tint = IconColor,
                                )
                            }
                        },
                        actions = {
                            IconButton(onClick = { onAction(LibraryAction.OnFavoriteSelected) }) {
                                Icon(
                                    imageVector = AppIcons.StarOutline,
                                    contentDescription = "Add selected to favorites",
                                    tint = IconColor,
                                )
                            }
                            IconButton(onClick = { onAction(LibraryAction.OnShareSelected) }) {
                                Icon(
                                    imageVector = AppIcons.Share,
                                    contentDescription = "Share selected",
                                    tint = IconColor,
                                )
                            }
                            IconButton(onClick = { onAction(LibraryAction.OnDeleteSelected) }) {
                                Icon(
                                    imageVector = AppIcons.Delete,
                                    contentDescription = "Delete selected",
                                    tint = IconColor,
                                )
                            }
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = containerColor,
                            titleContentColor = MaterialTheme.colorScheme.onBackground,
                        ),
                    )
                } else if (state.isSearchActive) {
                    val focusRequester = remember { FocusRequester() }
                    LaunchedEffect(Unit) {
                        focusRequester.requestFocus()
                    }
                    Column {
                        TopAppBar(
                            title = {
                                BasicTextField(
                                    value = state.searchQuery,
                                    onValueChange = { onAction(LibraryAction.OnSearchQueryChanged(it)) },
                                    singleLine = true,
                                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                                        color = TextPrimary,
                                    ),
                                    cursorBrush = SolidColor(TextPrimary),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .focusRequester(focusRequester),
                                    decorationBox = { innerTextField ->
                                        Box {
                                            if (state.searchQuery.isEmpty()) {
                                                Text(
                                                    text = "Search books",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = TextSecondary,
                                                )
                                            }
                                            innerTextField()
                                        }
                                    },
                                )
                            },
                            navigationIcon = {
                                IconButton(onClick = { onAction(LibraryAction.OnCloseSearch) }) {
                                    Icon(
                                        imageVector = AppIcons.ArrowBack,
                                        contentDescription = "Close search",
                                        tint = IconColor,
                                    )
                                }
                            },
                            actions = {
                                if (state.searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { onAction(LibraryAction.OnSearchQueryChanged("")) }) {
                                        Icon(
                                            imageVector = AppIcons.Close,
                                            contentDescription = "Clear search",
                                            tint = IconColor,
                                        )
                                    }
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = containerColor,
                            ),
                        )
                        HorizontalDivider(
                            thickness = 1.dp,
                            color = DividerColor,
                        )
                    }
                } else {
                    CenterAlignedTopAppBar(
                        title = {
                            Text(
                                text = state.screenTitle,
                                style = MaterialTheme.typography.titleLarge,
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = onMenuClick) {
                                Icon(
                                    imageVector = AppIcons.Menu,
                                    contentDescription = "Menu",
                                    tint = IconColor,
                                )
                            }
                        },
                        actions = {
                            IconButton(onClick = { onAction(LibraryAction.OnSearchClick) }) {
                                Icon(
                                    imageVector = AppIcons.Search,
                                    contentDescription = "Search",
                                    tint = IconColor,
                                )
                            }
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = containerColor,
                            titleContentColor = MaterialTheme.colorScheme.onBackground,
                        ),
                    )
                }
            },
            containerColor = containerColor,
        ) { paddingValues ->
            if (state.hasNoSearchResults) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(top = 40.dp),
                    contentAlignment = Alignment.TopCenter,
                ) {
                    Text(
                        text = "No results found",
                        style = MaterialTheme.typography.bodyMedium,
                        fontSize = 22.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else if (state.isEmpty) {
                val emptyState = when (state.activeFilter) {
                    BookFilter.ALL -> EmptyStateConfig(
                        icon = null,
                        title = "Your library is empty",
                        description = "Import your first book to start\nbuilding your library",
                        showImportButton = true,
                    )
                    BookFilter.FAVORITES -> EmptyStateConfig(
                        icon = AppIcons.Favorites,
                        title = "Your favorites is empty",
                        description = "Books you add to Favorites will appear here.",
                        showImportButton = false,
                    )
                    BookFilter.FINISHED -> EmptyStateConfig(
                        icon = AppIcons.Finished,
                        title = "Your finished is empty",
                        description = "Books you mark as Finished will appear here.",
                        showImportButton = false,
                    )
                }
                LibraryEmptyContent(
                    config = emptyState,
                    onImportBookClick = { onAction(LibraryAction.OnImportBookClick) },
                    isTablet = isTablet,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                )
            } else if (isTablet && state.isSearchActive) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                ) {
                    items(
                        items = state.displayedBooks,
                        key = { it.id },
                    ) { book ->
                        SearchResultBookCard(
                            book = book,
                            onClick = { onAction(LibraryAction.OnBookClick(book.id)) },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            } else if (isTablet) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(start = 8.dp, end = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(
                        items = state.displayedBooks,
                        key = { it.id },
                    ) { book ->
                        BookCard(
                            book = book,
                            onFavoriteClick = { onAction(LibraryAction.OnToggleFavorite(book.id)) },
                            onFinishedClick = { onAction(LibraryAction.OnToggleFinished(book.id)) },
                            onShareClick = { onAction(LibraryAction.OnShareBook(book.id)) },
                            onDeleteClick = { onAction(LibraryAction.OnDeleteBook(book.id)) },
                            onClick = { onAction(LibraryAction.OnBookClick(book.id)) },
                            onLongClick = { onAction(LibraryAction.OnBookLongClick(book.id)) },
                            modifier = Modifier.fillMaxWidth(),
                            isSelectionMode = state.isSelectionMode,
                            isSelected = book.id in state.selectedBookIds,
                            isTablet = true,
                        )
                    }
                }
            } else if (!isTablet && state.isSearchActive) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                ) {
                    items(
                        items = state.displayedBooks,
                        key = { it.id },
                    ) { book ->
                        SearchResultBookCard(
                            book = book,
                            onClick = { onAction(LibraryAction.OnBookClick(book.id)) },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(8.dp),
                ) {
                    items(
                        items = state.displayedBooks,
                        key = { it.id },
                    ) { book ->
                        BookCard(
                            book = book,
                            onFavoriteClick = { onAction(LibraryAction.OnToggleFavorite(book.id)) },
                            onFinishedClick = { onAction(LibraryAction.OnToggleFinished(book.id)) },
                            onShareClick = { onAction(LibraryAction.OnShareBook(book.id)) },
                            onDeleteClick = { onAction(LibraryAction.OnDeleteBook(book.id)) },
                            onClick = { onAction(LibraryAction.OnBookClick(book.id)) },
                            onLongClick = { onAction(LibraryAction.OnBookLongClick(book.id)) },
                            modifier = Modifier.fillMaxWidth(),
                            isSelectionMode = state.isSelectionMode,
                            isSelected = book.id in state.selectedBookIds,
                        )
                    }
                }
            }
        }

        // Loading overlay
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }

        // Delete confirmation dialog
        state.deleteConfirmation?.let { confirmation ->
            val title = when (confirmation) {
                is DeleteConfirmation.SingleBook -> "Delete \"${confirmation.title}\"?"
                is DeleteConfirmation.MultipleBooks -> "Delete ${confirmation.count} books?"
            }
            val description = when (confirmation) {
                is DeleteConfirmation.SingleBook -> "This action will remove the book from your library."
                is DeleteConfirmation.MultipleBooks -> "This action will remove the selected books from your library."
            }
            AlertDialog(
                onDismissRequest = { onAction(LibraryAction.OnDismissDeleteDialog) },
                shape = RoundedCornerShape(28.dp),
                containerColor = BgMain,
                title = {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        color = TextPrimary,
                    )
                },
                text = {
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                    )
                },
                dismissButton = {
                    TextButton(onClick = { onAction(LibraryAction.OnDismissDeleteDialog) }) {
                        Text(
                            text = "Cancel",
                            color = TextPrimary,
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = { onAction(LibraryAction.OnConfirmDelete) }) {
                        Text(
                            text = "Delete",
                            color = StateAlert,
                        )
                    }
                },
            )
        }

        // Error dialog
        state.errorMessage?.let { message ->
            val text = when (message) {
                is UiText.DynamicString -> message.value
                is UiText.StringResourceText -> message.resId.toString()
            }
            AlertDialog(
                containerColor = BgMain,
                onDismissRequest = { onAction(LibraryAction.OnDismissErrorDialog) },
                text = {
                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                },
                confirmButton = {
                    TextButton(onClick = { onAction(LibraryAction.OnDismissErrorDialog) }) {
                        Text("OK")
                    }
                },
            )
        }
    }
}

@Composable
private fun TabletSelectionToolbar(
    state: LibraryState,
    onAction: (LibraryAction) -> Unit,
    containerColor: Color,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(containerColor),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = { onAction(LibraryAction.OnExitSelectionMode) },
                modifier = Modifier.size(48.dp),
            ) {
                Icon(
                    imageVector = AppIcons.ArrowBack,
                    contentDescription = "Exit selection mode",
                    tint = IconColor,
                    modifier = Modifier.size(24.dp),
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "${state.selectedBookIds.size} selected",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f),
            )
            IconButton(
                onClick = { onAction(LibraryAction.OnFavoriteSelected) },
                modifier = Modifier.size(48.dp),
            ) {
                Icon(
                    imageVector = AppIcons.StarOutline,
                    contentDescription = "Add selected to favorites",
                    tint = IconColor,
                    modifier = Modifier.size(24.dp),
                )
            }
            IconButton(
                onClick = { onAction(LibraryAction.OnShareSelected) },
                modifier = Modifier.size(48.dp),
            ) {
                Icon(
                    imageVector = AppIcons.Share,
                    contentDescription = "Share selected",
                    tint = IconColor,
                    modifier = Modifier.size(24.dp),
                )
            }
            IconButton(
                onClick = { onAction(LibraryAction.OnDeleteSelected) },
                modifier = Modifier.size(48.dp),
            ) {
                Icon(
                    imageVector = AppIcons.Delete,
                    contentDescription = "Delete selected",
                    tint = IconColor,
                    modifier = Modifier.size(24.dp),
                )
            }
        }
    }
}

@Composable
private fun TabletSearchBar(
    state: LibraryState,
    onAction: (LibraryAction) -> Unit,
    containerColor: Color,
) {
    val focusRequester = remember { FocusRequester() }

    if (state.isSearchActive) {
        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(containerColor),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(if (state.isSearchActive) 1f else 0.4f)
                    .animateContentSize()
                    .height(40.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(BgMain)
                    .then(
                        if (!state.isSearchActive) {
                            Modifier.clickable { onAction(LibraryAction.OnSearchClick) }
                        } else {
                            Modifier
                        }
                    )
                    .padding(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (state.isSearchActive) {
                    IconButton(
                        onClick = { onAction(LibraryAction.OnCloseSearch) },
                        modifier = Modifier.size(24.dp),
                    ) {
                        Icon(
                            imageVector = AppIcons.ArrowBack,
                            contentDescription = "Close search",
                            tint = IconColor,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                    Spacer(modifier = Modifier.size(8.dp))
                }

                Box(modifier = Modifier.weight(1f)) {
                    if (state.isSearchActive) {
                        BasicTextField(
                            value = state.searchQuery,
                            onValueChange = { onAction(LibraryAction.OnSearchQueryChanged(it)) },
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                color = TextPrimary,
                            ),
                            cursorBrush = SolidColor(TextPrimary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequester),
                            decorationBox = { innerTextField ->
                                Box {
                                    if (state.searchQuery.isEmpty()) {
                                        Text(
                                            text = "Search books",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = TextSecondary,
                                        )
                                    }
                                    innerTextField()
                                }
                            },
                        )
                    } else {
                        Text(
                            text = "Search books",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                        )
                    }
                }

                if (state.isSearchActive && state.searchQuery.isNotEmpty()) {
                    IconButton(
                        onClick = { onAction(LibraryAction.OnSearchQueryChanged("")) },
                        modifier = Modifier.size(24.dp),
                    ) {
                        Icon(
                            imageVector = AppIcons.Close,
                            contentDescription = "Clear search",
                            tint = IconColor,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                } else if (!state.isSearchActive) {
                    Icon(
                        imageVector = AppIcons.Search,
                        contentDescription = "Search",
                        tint = IconColor,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
        HorizontalDivider(
            thickness = 1.dp,
            color = DividerColor,
            modifier = Modifier.padding(horizontal = 12.dp),
        )
    }
}

private data class EmptyStateConfig(
    val icon: ImageVector?,
    val title: String,
    val description: String,
    val showImportButton: Boolean,
)

@Composable
private fun LibraryEmptyContent(
    config: EmptyStateConfig,
    onImportBookClick: () -> Unit,
    isTablet: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val circleColor = if (isTablet) BgMain else MaterialTheme.colorScheme.secondaryContainer
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(circleColor),
            contentAlignment = Alignment.Center,
        ) {
            if (config.icon != null) {
                Icon(
                    imageVector = config.icon,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = IconColor,
                )
            } else {
                Image(
                    painter = painterResource(Res.drawable.book_open_logo),
                    contentDescription = null,
                    modifier = Modifier
                        .size(72.dp)
                        .alpha(0.5f),
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = config.title,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = config.description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        if (config.showImportButton) {
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onImportBookClick,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = OnPrimary,
                ),
                contentPadding = PaddingValues(all = 16.dp),
            ) {
                Icon(
                    imageVector = AppIcons.ImportBook,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = "Import Book",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Preview(device = PIXEL_9_PRO)
@Composable
private fun LibraryScreenPhonePreview() {
    PageKeeperTheme {
        LibraryScreen(
            state = LibraryState(
                books = testBooks,
            ),
            onAction = {},
            onMenuClick = {},
        )
    }
}

@Preview(device = PIXEL_TABLET)
@Composable
private fun LibraryScreenTabletPreview() {
    PageKeeperTheme {
        LibraryScreen(
            state = LibraryState(
                books = testBooks,
            ),
            onAction = {},
            onMenuClick = {},
            isTablet = true,
            containerColor = TabletBlockBg,
        )
    }
}
