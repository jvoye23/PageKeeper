package com.jvcodingsolutions.pagekeeper.feature.reader.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jvcodingsolutions.pagekeeper.core.presentation.ObserveAsEvents
import com.jvcodingsolutions.pagekeeper.core.presentation.OrientationEffect
import com.jvcodingsolutions.pagekeeper.core.presentation.UiText
import com.jvcodingsolutions.pagekeeper.designsystem.theme.AppIcons
import com.jvcodingsolutions.pagekeeper.designsystem.theme.BgBottomNav
import com.jvcodingsolutions.pagekeeper.designsystem.theme.BgMain
import com.jvcodingsolutions.pagekeeper.designsystem.theme.IconsColor
import com.jvcodingsolutions.pagekeeper.designsystem.theme.LoaderMain
import com.jvcodingsolutions.pagekeeper.designsystem.theme.LoaderSecondary
import com.jvcodingsolutions.pagekeeper.designsystem.theme.PageKeeperTheme
import com.jvcodingsolutions.pagekeeper.designsystem.theme.Primary
import com.jvcodingsolutions.pagekeeper.designsystem.theme.TextPrimary
import com.jvcodingsolutions.pagekeeper.designsystem.theme.chapterTitle
import com.jvcodingsolutions.pagekeeper.designsystem.theme.Icons as IconColor
import com.jvcodingsolutions.pagekeeper.feature.reader.domain.BookContentElement
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun ReaderScreenRoot(
    bookId: String,
    onBackClick: () -> Unit,
    onChaptersClick: (String) -> Unit,
    onBookmarksClick: (String) -> Unit,
    viewModel: ReaderViewModel = koinViewModel { parametersOf(bookId) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackBarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val pendingScroll = remember { mutableStateOf<Int?>(null) }
    val pendingBookmarkScroll = remember { mutableStateOf<Pair<Int, Int>?>(null) }

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is ReaderEvent.NavigateBack -> onBackClick()
            is ReaderEvent.NavigateToChapters -> onChaptersClick(event.bookId)
            is ReaderEvent.NavigateToBookmarks -> onBookmarksClick(event.bookId)
            is ReaderEvent.ScrollToItem -> {
                pendingScroll.value = event.itemIndex
            }
            is ReaderEvent.ScrollToBookmark -> {
                pendingBookmarkScroll.value = event.itemIndex to event.scrollOffset
            }
            is ReaderEvent.ShowSnackbar -> {
                val message = when (event.message) {
                    is UiText.DynamicString -> event.message.value
                    is UiText.StringResourceText -> event.message.resId.toString()
                }
                scope.launch {
                    snackBarHostState.showSnackbar(message)
                }
            }
        }
    }

    ReaderScreen(
        state = state,
        onAction = viewModel::onAction,
        snackBarHostState = snackBarHostState,
        pendingScrollItemIndex = pendingScroll.value,
        onPendingScrollConsumed = { pendingScroll.value = null },
        pendingBookmarkScroll = pendingBookmarkScroll.value,
        onPendingBookmarkScrollConsumed = { pendingBookmarkScroll.value = null },
    )
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    state: ReaderState,
    onAction: (ReaderAction) -> Unit,
    snackBarHostState: SnackbarHostState = remember { SnackbarHostState() },
    pendingScrollItemIndex: Int? = null,
    onPendingScrollConsumed: () -> Unit = {},
    pendingBookmarkScroll: Pair<Int, Int>? = null,
    onPendingBookmarkScrollConsumed: () -> Unit = {},
) {
    val windowSizeClass = calculateWindowSizeClass()
    val isWideScreen = windowSizeClass.widthSizeClass >= WindowWidthSizeClass.Expanded
    val showOrientationControl = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = state.initialItemIndex,
        initialFirstVisibleItemScrollOffset = state.initialScrollOffset,
    )

    // Restore saved reading position once loading finishes
    LaunchedEffect(state.isLoading) {
        if (!state.isLoading && (state.initialItemIndex > 0 || state.initialScrollOffset > 0)) {
            listState.scrollToItem(state.initialItemIndex, state.initialScrollOffset)
        }
    }

    // Programmatic scroll triggered by chapter selection
    LaunchedEffect(pendingScrollItemIndex, state.contentElements.size) {
        val target = pendingScrollItemIndex
        if (target != null && !state.isLoading && target < state.contentElements.size) {
            listState.scrollToItem(target)
            onPendingScrollConsumed()
        }
    }

    // Programmatic scroll triggered by tapping a bookmark — anchor stays slightly
    // below the top of the screen so the user can see the bookmarked fragment.
    LaunchedEffect(pendingBookmarkScroll, state.contentElements.size) {
        val target = pendingBookmarkScroll
        if (target != null && !state.isLoading && target.first < state.contentElements.size) {
            val anchorOffset = (target.second - 80).coerceAtLeast(0)
            listState.scrollToItem(target.first, anchorOffset)
            onPendingBookmarkScrollConsumed()
        }
    }

    // Hide the bookmark indicator once the anchored item leaves the visible viewport.
    LaunchedEffect(state.activeBookmarkAnchorItemIndex) {
        val anchor = state.activeBookmarkAnchorItemIndex ?: return@LaunchedEffect
        snapshotFlow {
            val visible = listState.layoutInfo.visibleItemsInfo
            if (visible.isEmpty()) false
            else anchor in visible.first().index..visible.last().index
        }.collect { stillVisible ->
            if (!stillVisible) onAction(ReaderAction.OnDismissBookmarkIndicator)
        }
    }

    // Track scroll position and report to ViewModel for persistence
    LaunchedEffect(listState) {
        snapshotFlow {
            listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset
        }.collect { (index, offset) ->
            onAction(ReaderAction.OnSaveReadingPosition(index, offset))
        }
    }

    // Apply orientation lock
    OrientationEffect(lockLandscape = state.isLandscapeLocked)

    // Hoist slider value so the bubble can track the thumb
    var sliderValue by remember(state.fontSize) { mutableFloatStateOf(state.fontSize.toFloat()) }
    val sliderFraction = ((sliderValue - 10f) / (40f - 10f)).coerceIn(0f, 1f)

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = BgMain,
            topBar = {
                AnimatedVisibility(
                    visible = state.isControlsVisible && !state.isLoading,
                    enter = slideInVertically { -it } + fadeIn(),
                    exit = slideOutVertically { -it } + fadeOut(),
                ) {
                    TopAppBar(
                        title = {
                            Text(
                                text = state.bookTitle,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontSize = 20.sp,
                                    lineHeight = 28.sp,
                                ),
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = { onAction(ReaderAction.OnBackClick) }) {
                                Icon(
                                    imageVector = AppIcons.ArrowBack,
                                    contentDescription = "Back",
                                    modifier = Modifier.size(24.dp),
                                    tint = IconColor,
                                )
                            }
                        },
                        actions = {
                            IconButton(onClick = { onAction(ReaderAction.OnToggleFavorite) }) {
                                Icon(
                                    imageVector = if (state.isFavorite) AppIcons.StarFilled else AppIcons.StarOutline,
                                    contentDescription = if (state.isFavorite) "Remove from favorites" else "Add to favorites",
                                    modifier = Modifier.size(24.dp),
                                    tint = IconColor,
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = BgBottomNav,
                        ),
                    )
                }
            },
            bottomBar = {
                if (!state.isLoading) {
                    if (state.isControlsVisible) {
                        AnimatedVisibility(
                            visible = true,
                            enter = slideInVertically { it } + fadeIn(),
                            exit = slideOutVertically { it } + fadeOut(),
                        ) {
                            if (state.isFontSizeMode) {
                                FontSizePanel(
                                    sliderValue = sliderValue,
                                    onSliderValueChange = { sliderValue = it },
                                    onFontSizeChanged = { onAction(ReaderAction.OnFontSizeChanged(it)) },
                                )
                            } else {
                                ReaderBottomToolbar(
                                    progressFraction = state.progressFraction,
                                    isLandscapeLocked = state.isLandscapeLocked,
                                    showChaptersButton = state.bookStructure != null,
                                    onChaptersClick = { onAction(ReaderAction.OnChaptersClick) },
                                    onBookmarksClick = { onAction(ReaderAction.OnBookmarksClick) },
                                    onToggleOrientation = { onAction(ReaderAction.OnToggleOrientation) },
                                ) { onAction(ReaderAction.OnFontSizeClick) }
                            }
                        }
                    } else {
                        ImmersiveProgressBar(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(start = 2.dp, end = 2.dp, bottom = 24.dp),
                            progressFraction = state.progressFraction
                        )
                    }
                }
            },
            snackbarHost = {
                SnackbarHost(hostState = snackBarHostState)
            },
        ) { innerPadding ->
            if (state.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = LoaderMain,
                        trackColor = LoaderSecondary,
                        strokeWidth = 5.dp,
                    )
                }
            } else {
                if (state.isPdf) {
                    PdfContent(
                        state = state,
                        listState = listState,
                        onLoadMore = { onAction(ReaderAction.OnLoadMoreSections) },
                        isWideScreen = isWideScreen,
                        onTap = { onAction(ReaderAction.OnScreenTap) },
                        contentPadding = innerPadding,
                    )
                } else {
                    BookContent(
                        modifier = Modifier
                            .padding(bottom = 16.dp),
                        state = state,
                        listState = listState,
                        onLoadMore = { onAction(ReaderAction.OnLoadMoreSections) },
                        isWideScreen = isWideScreen,
                        onTap = { onAction(ReaderAction.OnScreenTap) },
                        contentPadding = innerPadding,
                    )
                }
            }
        }

        // Floating font size bubble — drawn ON TOP of the Scaffold, anchored to thumb
        if (state.isControlsVisible && state.isFontSizeMode && !state.isLoading) {
            FontSizeBubble(
                fontSize = sliderValue.toInt(),
                sliderFraction = sliderFraction,
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }

        // Bookmark indicator — visible while the bookmarked fragment is on-screen
        AnimatedVisibility(
            visible = state.isBookmarkIndicatorVisible && !state.isLoading,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 16.dp, end = 16.dp),
        ) {
            Icon(
                imageVector = AppIcons.BookmarkFilled,
                contentDescription = "Bookmarked position",
                tint = Primary,
                modifier = Modifier.size(28.dp),
            )
        }
    }
}

@Composable
private fun BookContent(
    modifier: Modifier = Modifier,
    state: ReaderState,
    listState: LazyListState,
    onLoadMore: () -> Unit,
    isWideScreen: Boolean,
    onTap: () -> Unit,
    contentPadding: PaddingValues = PaddingValues(),
) {
    val contentModifier = if (isWideScreen) {
        Modifier.widthIn(max = 700.dp).fillMaxSize()
    } else {
        Modifier.fillMaxSize()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(contentPadding)
            .pointerInput(Unit) {
                detectTapGestures { onTap() }
            },
        contentAlignment = if (isWideScreen) Alignment.TopCenter else Alignment.TopStart,
    ) {
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(
                horizontal = if (isWideScreen) 0.dp else 24.dp,
                vertical = 24.dp,
            ),
            modifier = contentModifier,
        ) {
            items(
                count = state.contentElements.size,
                key = { it },
            ) { index ->
                when (val element = state.contentElements[index]) {
                    is BookContentElement.ChapterTitle -> ChapterTitleItem(
                        text = element.text,
                        fontSize = state.fontSize,
                    )
                    is BookContentElement.Paragraph -> ParagraphItem(
                        text = element.text,
                        fontSize = state.fontSize,
                    )
                    is BookContentElement.Quote -> QuoteItem(
                        text = element.text,
                        fontSize = state.fontSize,
                    )
                }
            }

            if (state.hasMoreSections) {
                item(key = "load_more") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(32.dp),
                            color = LoaderMain,
                            trackColor = LoaderSecondary,
                            strokeWidth = 3.dp,
                        )
                    }
                    LaunchedEffect(state.loadedSectionCount) {
                        onLoadMore()
                    }
                }
            }
        }
    }
}

@Composable
private fun PdfContent(
    state: ReaderState,
    listState: LazyListState,
    onLoadMore: () -> Unit,
    isWideScreen: Boolean,
    onTap: () -> Unit,
    contentPadding: PaddingValues = PaddingValues(),
) {
    val contentModifier = if (isWideScreen) {
        Modifier.widthIn(max = 600.dp).fillMaxSize()
    } else {
        Modifier.fillMaxSize()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .pointerInput(Unit) {
                detectTapGestures { onTap() }
            },
        contentAlignment = if (isWideScreen) Alignment.TopCenter else Alignment.TopStart,
    ) {
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(
                horizontal = if (isWideScreen) 0.dp else 16.dp,
                vertical = 16.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = contentModifier,
        ) {
            items(
                count = state.pdfPages.size,
                key = { it },
            ) { index ->
                Image(
                    bitmap = state.pdfPages[index],
                    contentDescription = "Page ${index + 1}",
                    modifier = Modifier.fillMaxWidth(),
                    contentScale = ContentScale.FillWidth,
                )
            }

            if (state.hasMoreSections) {
                item(key = "pdf_load_more") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(32.dp),
                            color = LoaderMain,
                            trackColor = LoaderSecondary,
                            strokeWidth = 3.dp,
                        )
                    }
                    LaunchedEffect(state.loadedPdfPageCount) {
                        onLoadMore()
                    }
                }
            }
        }
    }
}

@Composable
private fun ChapterTitleItem(text: String, fontSize: Int) {
    val titleFontSize = (fontSize * 1.4f).sp
    val titleLineHeight = (fontSize * 1.4f * 1.2f).sp

    Spacer(modifier = Modifier.height(24.dp))
    Text(
        text = text,
        style = MaterialTheme.typography.chapterTitle,
        fontSize = titleFontSize,
        lineHeight = titleLineHeight,
        color = TextPrimary,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth(),
    )
    Spacer(modifier = Modifier.height(24.dp))
}

@Composable
private fun ParagraphItem(text: AnnotatedString, fontSize: Int) {
    val paragraphFontSize = fontSize.sp
    val paragraphLineHeight = (fontSize * 1.33f).sp

    Text(
        text = text,
        style = TextStyle(
            fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = paragraphFontSize,
            lineHeight = paragraphLineHeight,
            letterSpacing = 1.sp,
            color = TextPrimary,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
    )
}

@Composable
private fun QuoteItem(text: AnnotatedString, fontSize: Int) {
    val quoteFontSize = fontSize.sp
    val quoteLineHeight = (fontSize * 1.33f).sp
    val borderColor = Primary

    Spacer(modifier = Modifier.height(20.dp))
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                drawLine(
                    color = borderColor,
                    start = Offset(0f, 0f),
                    end = Offset(0f, size.height),
                    strokeWidth = 2.dp.toPx(),
                )
            }
            .padding(start = 20.dp),
    ) {
        Text(
            text = text,
            style = TextStyle(
                fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                fontWeight = FontWeight.Normal,
                fontStyle = FontStyle.Italic,
                fontSize = quoteFontSize,
                lineHeight = quoteLineHeight,
                color = TextPrimary,
            ),
        )
    }
    Spacer(modifier = Modifier.height(20.dp))
}

@Composable
private fun ImmersiveProgressBar(
    modifier: Modifier = Modifier,
    progressFraction: Float
) {
    LinearProgressIndicator(
        progress = { progressFraction },
        modifier = modifier
            .fillMaxWidth()
            .height(6.dp),
        color = Primary,
        trackColor = LoaderSecondary,
        gapSize = 6.dp,
    )
}

@Composable
private fun ReaderBottomToolbar(
    progressFraction: Float,
    isLandscapeLocked: Boolean,
    showChaptersButton: Boolean,
    onChaptersClick: () -> Unit,
    onBookmarksClick: () -> Unit,
    onToggleOrientation: () -> Unit,
    onFontSizeClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(BgBottomNav),
    ) {
        Text(
            text = "${(progressFraction * 100).toInt()}%",
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 4.dp),
        )
        LinearProgressIndicator(
            progress = { progressFraction },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(6.dp),
            color = Primary,
            trackColor = LoaderSecondary,
            gapSize = 6.dp,
        )
        BottomAppBar(
            containerColor = BgBottomNav,
            tonalElevation = 0.dp,
        ) {
            if (showChaptersButton) {
                NavigationBarItem(
                    selected = false,
                    onClick = { onChaptersClick() },
                    icon = {
                        Icon(
                            imageVector = AppIcons.Chapters,
                            contentDescription = "Chapters",
                            tint = IconsColor,
                            modifier = Modifier.size(24.dp),
                        )
                    },
                    label = {
                        Text(
                            text = "Chapters",
                            style = MaterialTheme.typography.bodyMedium,
                            color = IconsColor,
                        )
                    },
                )
            }
            NavigationBarItem(
                selected = false,
                onClick = { onBookmarksClick() },
                icon = {
                    Icon(
                        imageVector = AppIcons.Bookmark,
                        contentDescription = "Bookmarks",
                        tint = IconsColor,
                        modifier = Modifier.size(24.dp),
                    )
                },
                label = {
                    Text(
                        text = "Bookmarks",
                        style = MaterialTheme.typography.bodyMedium,
                        color = IconsColor,
                    )
                },
            )
            NavigationBarItem(
                selected = false,
                onClick = { onToggleOrientation() },
                icon = {
                    Icon(
                        imageVector = if(isLandscapeLocked) AppIcons.Landscape
                            else AppIcons.Portrait,
                        contentDescription = if(isLandscapeLocked) "Auto Rotate"
                            else "Landscape",
                        tint = IconsColor,
                        modifier = Modifier.size(24.dp),
                    )
                },
                label = {
                    Text(
                        text = if(isLandscapeLocked) "Landscape" else "Auto Rotate",
                        style = MaterialTheme.typography.bodyMedium,
                        color = IconsColor,
                    )
                }
            )
            NavigationBarItem(
                selected = false,
                onClick = { onFontSizeClick() },
                icon = {
                    Icon(
                        imageVector = AppIcons.FontSizeIcon,
                        contentDescription = "Font Size",
                        tint = IconsColor,
                        modifier = Modifier.size(24.dp),
                    )
                },
                label = {
                    Text(
                        text = "Font Size",
                        style = MaterialTheme.typography.bodyMedium,
                        color = IconsColor,
                    )
                }
            )
        }
    }
}

@Composable
private fun FontSizePanel(
    sliderValue: Float,
    onSliderValueChange: (Float) -> Unit,
    onFontSizeChanged: (Int) -> Unit,
) {
    BottomAppBar(
        containerColor = BgBottomNav,
        tonalElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Minus button
            IconButton(
                onClick = {
                    val newSize = (sliderValue.toInt() - 1).coerceAtLeast(10)
                    onSliderValueChange(newSize.toFloat())
                    onFontSizeChanged(newSize)
                },
                modifier = Modifier
                    .width(40.dp)
                    .height(32.dp)
                    .background(Primary, CircleShape),
            ) {
                Icon(
                    modifier = Modifier
                        .size(20.dp),
                    imageVector = AppIcons.MinusIcon,
                    contentDescription = "Minus",
                    tint = BgMain,
                )
            }

            Slider(
                value = sliderValue,
                onValueChange = onSliderValueChange,
                onValueChangeFinished = {
                    onFontSizeChanged(sliderValue.toInt())
                },
                valueRange = 10f..40f,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp),
                colors = SliderDefaults.colors(
                    thumbColor = Primary,
                    activeTrackColor = LoaderSecondary,
                    inactiveTrackColor = LoaderSecondary,
                ),
            )

            // Plus button
            IconButton(
                onClick = {
                    val newSize = (sliderValue.toInt() + 1).coerceAtMost(40)
                    onSliderValueChange(newSize.toFloat())
                    onFontSizeChanged(newSize)
                },
                modifier = Modifier
                    .width(40.dp)
                    .height(32.dp)
                    .background(Primary, CircleShape),
            ) {
                Icon(
                    modifier = Modifier
                        .size(20.dp),
                    imageVector = AppIcons.PlusIcon,
                    contentDescription = "Plus",
                    tint = BgMain,
                )
            }
        }
    }
}

@Composable
private fun FontSizeBubble(
    fontSize: Int,
    sliderFraction: Float,
    modifier: Modifier = Modifier,
) {
    val bubbleWidth = 64.dp
    val bubbleHeight = 54.dp
    // Position bubble so its bottom edge is 4dp above the top of the bottom bar (80dp)
    val bottomOffset =  38.dp + bubbleHeight

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = bottomOffset),
        contentAlignment = Alignment.BottomStart,
    ) {
        // Match the slider's horizontal layout: 16dp padding + 48dp button + 12dp slider padding
        val sliderStart = 16.dp + 48.dp + 12.dp
        val sliderEnd = 16.dp + 48.dp + 12.dp
        val trackWidth = maxWidth - sliderStart - sliderEnd
        val thumbX = sliderStart + trackWidth * sliderFraction - bubbleWidth / 2

        Box(
            modifier = Modifier
                .offset(x = thumbX)
                .width(bubbleWidth)
                .height(bubbleHeight)
                .shadow(8.dp, CircleShape)
                .background(Color.White, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = fontSize.toString(),
                style = MaterialTheme.typography.chapterTitle,
                color = TextPrimary,
            )
        }
    }
}

@Preview (showSystemUi = true)
@Composable
private fun ReaderScreenPreview() {
    PageKeeperTheme {
        ReaderScreen(
            state = ReaderState(
                bookTitle = "Sample Book",
                contentElements = listOf(
                    BookContentElement.ChapterTitle("Chapter 1"),
                    BookContentElement.Paragraph(androidx.compose.ui.text.AnnotatedString("" +
                            "This is a sample paragraph.")),
                    BookContentElement.Quote(AnnotatedString("This is a sample quote.")),
                ),
                isLoading = false,
                isControlsVisible = true
            ),
            onAction = {},
        )
    }
}
