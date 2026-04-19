package com.jvcodingsolutions.pagekeeper.designsystem.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import org.jetbrains.compose.resources.vectorResource
import pagekeeper.composeapp.generated.resources.Res
import pagekeeper.composeapp.generated.resources.close_menu
import pagekeeper.composeapp.generated.resources.ic_arrow_back
import pagekeeper.composeapp.generated.resources.ic_close
import pagekeeper.composeapp.generated.resources.ic_delete
import pagekeeper.composeapp.generated.resources.ic_favorites
import pagekeeper.composeapp.generated.resources.ic_finished
import pagekeeper.composeapp.generated.resources.ic_flag_filled
import pagekeeper.composeapp.generated.resources.ic_flag_outline
import pagekeeper.composeapp.generated.resources.ic_import_book
import pagekeeper.composeapp.generated.resources.ic_library
import pagekeeper.composeapp.generated.resources.ic_menu
import pagekeeper.composeapp.generated.resources.ic_search
import pagekeeper.composeapp.generated.resources.ic_share
import pagekeeper.composeapp.generated.resources.ic_star_filled
import pagekeeper.composeapp.generated.resources.ic_star_outline

object AppIcons {
    val ArrowBack: ImageVector
        @Composable get() = vectorResource(Res.drawable.ic_arrow_back)

    val Close: ImageVector
        @Composable get() = vectorResource(Res.drawable.ic_close)

    val Delete: ImageVector
        @Composable get() = vectorResource(Res.drawable.ic_delete)

    val Favorites: ImageVector
        @Composable get() = vectorResource(Res.drawable.ic_favorites)

    val Finished: ImageVector
        @Composable get() = vectorResource(Res.drawable.ic_finished)

    val FlagFilled: ImageVector
        @Composable get() = vectorResource(Res.drawable.ic_flag_filled)

    val FlagOutline: ImageVector
        @Composable get() = vectorResource(Res.drawable.ic_flag_outline)

    val ImportBook: ImageVector
        @Composable get() = vectorResource(Res.drawable.ic_import_book)

    val Library: ImageVector
        @Composable get() = vectorResource(Res.drawable.ic_library)

    val Menu: ImageVector
        @Composable get() = vectorResource(Res.drawable.ic_menu)

    val Search: ImageVector
        @Composable get() = vectorResource(Res.drawable.ic_search)

    val Share: ImageVector
        @Composable get() = vectorResource(Res.drawable.ic_share)

    val StarFilled: ImageVector
        @Composable get() = vectorResource(Res.drawable.ic_star_filled)

    val StarOutline: ImageVector
        @Composable get() = vectorResource(Res.drawable.ic_star_outline)

    val CloseMenu: ImageVector
        @Composable get() = vectorResource(Res.drawable.close_menu)
}
