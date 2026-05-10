package com.jvcodingsolutions.pagekeeper.feature.reader.chapters

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jvcodingsolutions.pagekeeper.core.presentation.ObserveAsEvents
import com.jvcodingsolutions.pagekeeper.designsystem.theme.AppIcons
import com.jvcodingsolutions.pagekeeper.designsystem.theme.BgActive
import com.jvcodingsolutions.pagekeeper.designsystem.theme.BgBottomNav
import com.jvcodingsolutions.pagekeeper.designsystem.theme.BgMain
import com.jvcodingsolutions.pagekeeper.designsystem.theme.Divider
import com.jvcodingsolutions.pagekeeper.designsystem.theme.LoaderMain
import com.jvcodingsolutions.pagekeeper.designsystem.theme.LoaderSecondary
import com.jvcodingsolutions.pagekeeper.designsystem.theme.TextPrimary
import com.jvcodingsolutions.pagekeeper.designsystem.theme.Icons as IconColor
import com.jvcodingsolutions.pagekeeper.feature.reader.domain.BookStructure
import com.jvcodingsolutions.pagekeeper.feature.reader.domain.ChapterNode
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun ChaptersScreenRoot(
    bookId: String,
    onBackClick: () -> Unit,
    viewModel: ChaptersViewModel = koinViewModel { parametersOf(bookId) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is ChaptersEvent.NavigateBack -> onBackClick()
        }
    }

    ChaptersScreen(state = state, onAction = viewModel::onAction)
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ChaptersScreen(
    state: ChaptersState,
    onAction: (ChaptersAction) -> Unit,
) {
    val windowSizeClass = calculateWindowSizeClass()
    val isWideScreen = windowSizeClass.widthSizeClass >= WindowWidthSizeClass.Expanded
    val listState = rememberLazyListState()

    // Auto-scroll to the current chapter once content is loaded
    LaunchedEffect(state.isLoading, state.currentSectionIndex, state.expandedSectionIndex, state.expandedNodeIds) {
        if (!state.isLoading && state.structure != null) {
            val target = computeFlatIndexFor(
                structure = state.structure,
                expandedSectionIndex = state.expandedSectionIndex,
                expandedNodeIds = state.expandedNodeIds,
                currentSectionIndex = state.currentSectionIndex,
            )
            if (target >= 0) listState.scrollToItem(target)
        }
    }

    Scaffold(
        containerColor = BgMain,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Chapters",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onAction(ChaptersAction.OnBackClick) }) {
                        Icon(
                            imageVector = AppIcons.ArrowBack,
                            contentDescription = "Back",
                            tint = IconColor,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = BgBottomNav,
                ),
            )
        },
    ) { padding ->
        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
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
            state.structure == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "No chapters available",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary,
                    )
                }
            }
            else -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.TopCenter,
                ) {
                    val listModifier = if (isWideScreen) {
                        Modifier
                            .widthIn(max = 700.dp)
                            .fillMaxSize()
                    } else {
                        Modifier.fillMaxSize()
                    }
                    ChaptersList(
                        state = state,
                        onAction = onAction,
                        listState = listState,
                        modifier = listModifier,
                    )
                }
            }
        }
    }
}

@Composable
private fun ChaptersList(
    state: ChaptersState,
    onAction: (ChaptersAction) -> Unit,
    listState: LazyListState,
    modifier: Modifier = Modifier,
) {
    val structure = state.structure ?: return
    val rows = remember(structure, state.expandedSectionIndex, state.expandedNodeIds) {
        flattenRows(structure, state.expandedSectionIndex, state.expandedNodeIds)
    }

    LazyColumn(
        state = listState,
        modifier = modifier,
    ) {
        items(rows.size) { index ->
            when (val row = rows[index]) {
                is ChapterRow.SectionHeader -> SectionHeaderRow(
                    title = row.title,
                    isExpanded = row.isExpanded,
                    onClick = { onAction(ChaptersAction.OnSectionToggle(row.sectionListIndex)) },
                )
                is ChapterRow.Chapter -> ChapterItemRow(
                    chapter = row.node,
                    depth = row.depth,
                    isExpanded = row.isExpanded,
                    isCurrent = row.node.sectionIndex == state.currentSectionIndex,
                    onClick = {
                        onAction(ChaptersAction.OnChapterClick(row.node.sectionIndex, row.node.anchorId))
                    },
                    onToggle = { onAction(ChaptersAction.OnChapterToggle(row.node.id)) },
                )
            }
        }
    }
}

@Composable
private fun SectionHeaderRow(
    title: String,
    isExpanded: Boolean,
    onClick: () -> Unit,
) {
    val rotation by animateFloatAsState(
        targetValue = if (isExpanded) 0f else 180f,
        label = "section-chevron",
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(BgBottomNav)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = AppIcons.ArrowBack,
            contentDescription = if (isExpanded) "Collapse" else "Expand",
            tint = IconColor,
            modifier = Modifier
                .size(18.dp)
                .rotate(rotation + 90f), // Reuse arrow_back: 90° = up arrow (expanded), 270° = down
        )
        Spacer(modifier = Modifier.size(12.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary,
        )
    }
    HorizontalDivider(thickness = 1.dp, color = Divider)
}

@Composable
private fun ChapterItemRow(
    chapter: ChapterNode,
    depth: Int,
    isExpanded: Boolean,
    isCurrent: Boolean,
    onClick: () -> Unit,
    onToggle: () -> Unit,
) {
    val bg = if (isCurrent) BgActive else BgMain
    val weight = if (isCurrent) FontWeight.SemiBold else FontWeight.Normal
    val hasChildren = chapter.children.isNotEmpty()
    val startPad: Dp = (16 + depth * 16).dp

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bg)
            .clickable(onClick = onClick)
            .padding(start = startPad, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(top = 14.dp, bottom = 14.dp, end = 8.dp),
        ) {
            Text(
                text = chapter.title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = weight),
                color = TextPrimary,
            )
        }
        if (hasChildren) {
            val rotation by animateFloatAsState(
                targetValue = if (isExpanded) 0f else 180f,
                label = "chapter-chevron",
            )
            IconButton(
                onClick = onToggle,
                modifier = Modifier.size(40.dp),
            ) {
                Icon(
                    imageVector = AppIcons.ArrowBack,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = IconColor,
                    modifier = Modifier
                        .size(18.dp)
                        .rotate(rotation + 90f),
                )
            }
        } else {
            Spacer(modifier = Modifier.size(40.dp))
        }
    }
    HorizontalDivider(thickness = 1.dp, color = Divider)
}

private sealed interface ChapterRow {
    data class SectionHeader(
        val title: String,
        val isExpanded: Boolean,
        val sectionListIndex: Int,
    ) : ChapterRow

    data class Chapter(
        val node: ChapterNode,
        val depth: Int,
        val isExpanded: Boolean,
    ) : ChapterRow
}

private fun flattenRows(
    structure: BookStructure,
    expandedSectionIndex: Int?,
    expandedNodeIds: Set<String>,
): List<ChapterRow> {
    val rows = mutableListOf<ChapterRow>()
    structure.sections.forEachIndexed { idx, section ->
        rows.add(
            ChapterRow.SectionHeader(
                title = section.title,
                isExpanded = idx == expandedSectionIndex,
                sectionListIndex = idx,
            )
        )
        if (idx == expandedSectionIndex) {
            section.chapters.forEach { node ->
                appendChapterRows(rows, node, depth = 0, expandedNodeIds)
            }
        }
    }
    return rows
}

private fun appendChapterRows(
    rows: MutableList<ChapterRow>,
    node: ChapterNode,
    depth: Int,
    expandedNodeIds: Set<String>,
) {
    val isExpanded = node.id in expandedNodeIds
    rows.add(ChapterRow.Chapter(node = node, depth = depth, isExpanded = isExpanded))
    if (isExpanded) {
        node.children.forEach { child ->
            appendChapterRows(rows, child, depth + 1, expandedNodeIds)
        }
    }
}

private fun computeFlatIndexFor(
    structure: BookStructure,
    expandedSectionIndex: Int?,
    expandedNodeIds: Set<String>,
    currentSectionIndex: Int,
): Int {
    var counter = 0
    structure.sections.forEachIndexed { idx, section ->
        // section header itself
        if (idx == expandedSectionIndex) {
            counter++
            section.chapters.forEach { node ->
                val found = walkForCurrent(node, expandedNodeIds, currentSectionIndex, counter)
                if (found.first) return found.second
                counter = found.second
            }
        } else {
            counter++
        }
    }
    return -1
}

/**
 * Walks [node] (and its visible descendants) starting at flat index [startCounter].
 * Returns (true, indexOfMatch) when [currentSectionIndex] matches; otherwise
 * (false, counterAfterWalk).
 */
private fun walkForCurrent(
    node: ChapterNode,
    expandedNodeIds: Set<String>,
    currentSectionIndex: Int,
    startCounter: Int,
): Pair<Boolean, Int> {
    if (node.sectionIndex == currentSectionIndex) return true to startCounter
    var counter = startCounter + 1
    if (node.id in expandedNodeIds) {
        node.children.forEach { child ->
            val found = walkForCurrent(child, expandedNodeIds, currentSectionIndex, counter)
            if (found.first) return found
            counter = found.second
        }
    }
    return false to counter
}
