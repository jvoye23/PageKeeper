package com.jvcodingsolutions.pagekeeper.app

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Surface
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.WideNavigationRail
import androidx.compose.material3.WideNavigationRailDefaults
import androidx.compose.material3.WideNavigationRailItem
import androidx.compose.material3.WideNavigationRailItemDefaults
import androidx.compose.material3.WideNavigationRailState
import androidx.compose.material3.WideNavigationRailValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberWideNavigationRailState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.jvcodingsolutions.pagekeeper.app.navigation.FavoritesRoute
import com.jvcodingsolutions.pagekeeper.app.navigation.FinishedRoute
import com.jvcodingsolutions.pagekeeper.app.navigation.LibraryRoute
import com.jvcodingsolutions.pagekeeper.app.navigation.Navigator
import com.jvcodingsolutions.pagekeeper.app.navigation.SplashRoute
import com.jvcodingsolutions.pagekeeper.getPlatform
import com.jvcodingsolutions.pagekeeper.designsystem.theme.AppIcons
import com.jvcodingsolutions.pagekeeper.designsystem.theme.BgActive
import com.jvcodingsolutions.pagekeeper.designsystem.theme.Icons as IconColor
import com.jvcodingsolutions.pagekeeper.designsystem.theme.OnPrimary
import com.jvcodingsolutions.pagekeeper.designsystem.theme.PageKeeperTheme
import com.jvcodingsolutions.pagekeeper.designsystem.theme.TabletBlockBg
import com.jvcodingsolutions.pagekeeper.feature.library.presentation.LibraryAction
import com.jvcodingsolutions.pagekeeper.feature.library.presentation.LibraryScreenRoot
import com.jvcodingsolutions.pagekeeper.feature.library.presentation.LibraryViewModel
import com.jvcodingsolutions.pagekeeper.feature.splash.SplashScreen
import kotlinx.coroutines.launch
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.ui.tooling.preview.Devices.PIXEL_9_PRO
import androidx.compose.ui.tooling.preview.Devices.TABLET
import androidx.compose.ui.tooling.preview.Preview
import com.jvcodingsolutions.pagekeeper.feature.library.presentation.BookFilter
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun NavigationRoot() {
    PageKeeperTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
        val startRoute = if (getPlatform().showComposeSplash) SplashRoute else LibraryRoute
        val navigator = remember { Navigator(startRoute) }
        val windowSizeClass = calculateWindowSizeClass()
        val isExpandedScreen = windowSizeClass.widthSizeClass >= WindowWidthSizeClass.Expanded
        val libraryViewModel: LibraryViewModel = koinViewModel()
        val railState = rememberWideNavigationRailState()

        NavDisplay(
            backStack = navigator.backStack,
            onBack = { navigator.goBack() },
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator(),
            ),
            entryProvider = entryProvider {
                entry<SplashRoute> {
                    SplashScreen(
                        onSplashFinished = {
                            navigator.replaceWith(LibraryRoute)
                        },
                    )
                }

                entry<LibraryRoute> {
                    libraryViewModel.onAction(LibraryAction.OnFilterChanged(BookFilter.ALL))
                    if (isExpandedScreen) {
                        ExpandedLibraryLayout(
                            navigator = navigator,
                            selectedRoute = LibraryRoute,
                            viewModel = libraryViewModel,
                            railState = railState,
                        )
                    } else {
                        CompactLibraryLayout(
                            navigator = navigator,
                            selectedRoute = LibraryRoute,
                            viewModel = libraryViewModel,
                        )
                    }
                }

                entry<FavoritesRoute> {
                    libraryViewModel.onAction(LibraryAction.OnFilterChanged(BookFilter.FAVORITES))
                    if (isExpandedScreen) {
                        ExpandedLibraryLayout(
                            navigator = navigator,
                            selectedRoute = FavoritesRoute,
                            viewModel = libraryViewModel,
                            railState = railState,
                        )
                    } else {
                        CompactLibraryLayout(
                            navigator = navigator,
                            selectedRoute = FavoritesRoute,
                            viewModel = libraryViewModel,
                        )
                    }
                }

                entry<FinishedRoute> {
                    libraryViewModel.onAction(LibraryAction.OnFilterChanged(BookFilter.FINISHED))
                    if (isExpandedScreen) {
                        ExpandedLibraryLayout(
                            navigator = navigator,
                            selectedRoute = FinishedRoute,
                            viewModel = libraryViewModel,
                            railState = railState,
                        )
                    } else {
                        CompactLibraryLayout(
                            navigator = navigator,
                            selectedRoute = FinishedRoute,
                            viewModel = libraryViewModel,
                        )
                    }
                }
            },
        )
        }
    }
}

// ── Phone layout: ModalNavigationDrawer ──

@Composable
private fun CompactLibraryLayout(
    navigator: Navigator,
    selectedRoute: Any,
    viewModel: LibraryViewModel,
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            PageKeeperDrawerContent(
                selectedRoute = selectedRoute,
                onNavigate = { route ->
                    scope.launch { drawerState.close() }
                    if (route != selectedRoute) {
                        navigator.replaceWith(route)
                    }
                },
                onImportBookClick = {
                    scope.launch { drawerState.close() }
                    viewModel.onAction(LibraryAction.OnImportBookClick)
                },
                onCloseDrawer = {
                    scope.launch { drawerState.close() }
                },
            )
        },
    ) {
        LibraryScreenRoot(
            onMenuClick = {
                scope.launch { drawerState.open() }
            },
            viewModel = viewModel,
        )
    }
}

// ── Expanded layout: NavigationRail ──

@Composable
private fun ExpandedLibraryLayout(
    navigator: Navigator,
    selectedRoute: Any,
    viewModel: LibraryViewModel,
    railState: WideNavigationRailState,
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        PageKeeperNavigationRail(
            selectedRoute = selectedRoute,
            onNavigate = { route ->
                if (route != selectedRoute) {
                    navigator.replaceWith(route)
                }
            },
            onImportBookClick = { viewModel.onAction(LibraryAction.OnImportBookClick) },
            railState = railState,
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(top = 52.dp, end = 16.dp, bottom = 16.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(TabletBlockBg),
        ) {
            LibraryScreenRoot(
                onMenuClick = { },
                viewModel = viewModel,
                containerColor = TabletBlockBg,
                isTablet = true,
            )
        }
    }
}

// ── Shared Navigation Drawer Content ──

@Composable
private fun PageKeeperDrawerContent(
    selectedRoute: Any,
    onNavigate: (Any) -> Unit,
    onImportBookClick: () -> Unit,
    onCloseDrawer: () -> Unit,
) {
    ModalDrawerSheet(
        modifier = Modifier.fillMaxWidth(0.7f),
        drawerContainerColor = MaterialTheme.colorScheme.background,

    ) {
        IconButton(
            onClick = onCloseDrawer,
            modifier = Modifier
                .padding(start = 8.dp, top = 8.dp)
                .size(48.dp),
        ) {
            Icon(
                modifier = Modifier.size(24.dp),
                imageVector = AppIcons.CloseMenu,
                contentDescription = "Close menu",
                tint = IconColor,
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Column(
            modifier = Modifier

        ) {
            Button(
                onClick = onImportBookClick,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.padding(start = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = OnPrimary,
                ),
                contentPadding = PaddingValues(all = 16.dp)
            ) {
                Icon(
                    imageVector = AppIcons.ImportBook,
                    contentDescription = null,
                    modifier = Modifier
                        .size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Import Book",
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            Spacer(modifier = Modifier.height(60.dp))

            NavigationDrawerItem(
                icon = {
                    Icon(
                        imageVector = AppIcons.Library,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = IconColor,
                    )
                },
                label = {
                    Text("Library", style = MaterialTheme.typography.labelMedium, color = IconColor)
                },
                selected = selectedRoute is LibraryRoute,
                onClick = { onNavigate(LibraryRoute) },
                colors = NavigationDrawerItemDefaults.colors(
                    selectedContainerColor = BgActive,
                    unselectedContainerColor = Color.Transparent,
                ),
                modifier = Modifier
                    .padding(start = 16.dp)
                    .fillMaxWidth(0.55f),
            )

            NavigationDrawerItem(
                icon = {
                    Icon(
                        imageVector = AppIcons.Favorites,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = IconColor,
                    )
                },
                label = {
                    Text("Favorites", style = MaterialTheme.typography.labelMedium, color = IconColor)
                },
                selected = selectedRoute is FavoritesRoute,
                onClick = { onNavigate(FavoritesRoute) },
                colors = NavigationDrawerItemDefaults.colors(
                    selectedContainerColor = BgActive,
                    unselectedContainerColor = Color.Transparent,
                ),
                modifier = Modifier
                    .padding(start = 16.dp)
                    .fillMaxWidth(0.55f)
            )

            NavigationDrawerItem(
                icon = {
                    Icon(
                        imageVector = AppIcons.Finished,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = IconColor,
                    )
                },
                label = {
                    Text("Finished", style = MaterialTheme.typography.labelMedium, color = IconColor)
                },
                selected = selectedRoute is FinishedRoute,
                onClick = { onNavigate(FinishedRoute) },
                colors = NavigationDrawerItemDefaults.colors(
                    selectedContainerColor = BgActive,
                    unselectedContainerColor = Color.Transparent,
                ),
                modifier = Modifier
                    .padding(start = 16.dp)
                    .fillMaxWidth(0.55f),
            )

        }
    }
}

// ── Navigation Rail (Expanded screens) ──
// Uses M3 Expressive WideNavigationRail which animates the active indicator
// from a small icon-only pill (collapsed) to a wide icon+label pill (expanded).

@Composable
private fun PageKeeperNavigationRail(
    selectedRoute: Any,
    onNavigate: (Any) -> Unit,
    onImportBookClick: () -> Unit,
    railState: WideNavigationRailState,
) {
    val scope = rememberCoroutineScope()
    val expanded = railState.targetValue == WideNavigationRailValue.Expanded

    val itemColors = WideNavigationRailItemDefaults.colors(
        selectedIndicatorColor = BgActive,
        selectedIconColor = IconColor,
        unselectedIconColor = IconColor,
        selectedTextColor = IconColor,
        unselectedTextColor = IconColor,
    )

    WideNavigationRail(
        state = railState,
        colors = WideNavigationRailDefaults.colors(
            containerColor = MaterialTheme.colorScheme.background,
        ),
        header = {
            // Re-read state inside the header lambda so the header recomposes
            // when the rail toggles (the header is hoisted by WideNavigationRail
            // into its own slot and won't recompose via the outer scope alone).
            val isExpanded = railState.targetValue == WideNavigationRailValue.Expanded
            RailHeader(
                expanded = isExpanded,
                onToggle = { scope.launch { railState.toggle() } },
                onImportBookClick = onImportBookClick,
            )
        },
    ) {
        WideNavigationRailItem(
            selected = selectedRoute is LibraryRoute,
            onClick = { onNavigate(LibraryRoute) },
            icon = {
                Icon(
                    imageVector = AppIcons.Library,
                    contentDescription = null,
                )
            },
            label = { Text("Library") },
            railExpanded = expanded,
            colors = itemColors,
        )
        WideNavigationRailItem(
            selected = selectedRoute is FavoritesRoute,
            onClick = { onNavigate(FavoritesRoute) },
            icon = {
                Icon(
                    imageVector = AppIcons.Favorites,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                )
            },
            label = { Text("Favorites") },
            railExpanded = expanded,
            colors = itemColors,
        )
        WideNavigationRailItem(
            selected = selectedRoute is FinishedRoute,
            onClick = { onNavigate(FinishedRoute) },
            icon = {
                Icon(
                    imageVector = AppIcons.Finished,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                )
            },
            label = { Text("Finished") },
            railExpanded = expanded,
            colors = itemColors,
        )
    }
}

@Composable
private fun RailHeader(
    expanded: Boolean,
    onToggle: () -> Unit,
    onImportBookClick: () -> Unit,
) {
    // NOTE: do NOT use fillMaxWidth here. WideNavigationRail measures the
    // header with looseConstraints (max = parent's maxWidth), so fillMaxWidth
    // would make this Column as wide as the whole screen and push centered
    // content completely out of the rail's clipped 96 dp area.
    Column(
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .padding(top = 0.dp),
        horizontalAlignment = Alignment.Start,
    ) {
        IconButton(
            onClick = onToggle,
            modifier = Modifier.size(48.dp),
        ) {
            Icon(
                modifier = Modifier.size(24.dp),
                imageVector = if (expanded) AppIcons.CloseMenu else AppIcons.Menu,
                contentDescription = if (expanded) "Collapse menu" else "Expand menu",
                tint = IconColor,
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        AnimatedContent(
            targetState = expanded,
            transitionSpec = {
                (fadeIn() togetherWith fadeOut()).using(SizeTransform(clip = false))
            },
            label = "railImportButton",
        ) { isExpanded ->
            if (isExpanded) {
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
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Import Book",
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.primary)
                        .clickable(onClick = onImportBookClick),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = AppIcons.ImportBook,
                        contentDescription = "Import Book",
                        modifier = Modifier.size(24.dp),
                        tint = OnPrimary,
                    )
                }
            }
        }
    }
}



@Preview(device = PIXEL_9_PRO, showSystemUi = true)
@Composable
fun PageKeeperDrawerContentPreview() {
    PageKeeperTheme {
        PageKeeperDrawerContent(
            selectedRoute = LibraryRoute,
            onNavigate = {},
            onImportBookClick = {},
            onCloseDrawer = {},
        )
    }
}
@Preview(device = TABLET, showSystemUi = true)
@Composable
fun PageKeeperDrawerContentTabletPreview() {
    PageKeeperTheme {
        PageKeeperNavigationRail(
            selectedRoute = LibraryRoute,
            onNavigate = {},
            onImportBookClick = {},
            railState = rememberWideNavigationRailState(),
        )
    }
}
