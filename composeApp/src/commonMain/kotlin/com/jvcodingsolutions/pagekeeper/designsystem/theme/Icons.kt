package com.jvcodingsolutions.pagekeeper.designsystem.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import org.jetbrains.compose.resources.vectorResource
import pagekeeper.composeapp.generated.resources.Res
import pagekeeper.composeapp.generated.resources.bookmark
import pagekeeper.composeapp.generated.resources.bookmark_add
import pagekeeper.composeapp.generated.resources.bookmark_color
import pagekeeper.composeapp.generated.resources.chevron_right
import pagekeeper.composeapp.generated.resources.close_menu
import pagekeeper.composeapp.generated.resources.continue_reading
import pagekeeper.composeapp.generated.resources.font_size
import pagekeeper.composeapp.generated.resources.ic_arrow_back
import pagekeeper.composeapp.generated.resources.ic_chapters
import pagekeeper.composeapp.generated.resources.ic_close
import pagekeeper.composeapp.generated.resources.ic_delete
import pagekeeper.composeapp.generated.resources.ic_edit
import pagekeeper.composeapp.generated.resources.ic_more_vert
import pagekeeper.composeapp.generated.resources.ic_visibility
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
import pagekeeper.composeapp.generated.resources.landscape
import pagekeeper.composeapp.generated.resources.menu_favorites_active
import pagekeeper.composeapp.generated.resources.menu_favorites_deactive
import pagekeeper.composeapp.generated.resources.menu_finished_active
import pagekeeper.composeapp.generated.resources.menu_finished_deactive
import pagekeeper.composeapp.generated.resources.menu_library_active
import pagekeeper.composeapp.generated.resources.menu_library_deactive
import pagekeeper.composeapp.generated.resources.minus
import pagekeeper.composeapp.generated.resources.plus
import pagekeeper.composeapp.generated.resources.portrait
import pagekeeper.composeapp.generated.resources.read

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

    val Portrait: ImageVector
        @Composable get() = vectorResource(Res.drawable.portrait)

    val Landscape: ImageVector
        @Composable get() = vectorResource(Res.drawable.landscape)

    val FontSizeIcon: ImageVector
        @Composable get() = vectorResource(Res.drawable.font_size)

    val PlusIcon: ImageVector
        @Composable get() = vectorResource(Res.drawable.plus)

    val MinusIcon: ImageVector
        @Composable get() = vectorResource(Res.drawable.minus)

    val Chapters: ImageVector
        @Composable get() = vectorResource(Res.drawable.ic_chapters)

    val Read: ImageVector
        @Composable get() = vectorResource(Res.drawable.read)

    val TabletContinueReading: ImageVector
        @Composable get() = vectorResource(Res.drawable.continue_reading)

    val BookmarkAdd: ImageVector
        @Composable get() = vectorResource(Res.drawable.bookmark_add)

    val Bookmark: ImageVector
        @Composable get() = vectorResource(Res.drawable.bookmark)

    val BookmarkFilled: ImageVector
        @Composable get() = vectorResource(Res.drawable.bookmark_color)

    val MoreVert: ImageVector
        @Composable get() = vectorResource(Res.drawable.ic_more_vert)

    val Edit: ImageVector
        @Composable get() = vectorResource(Res.drawable.ic_edit)

    val Visibility: ImageVector
        @Composable get() = vectorResource(Res.drawable.ic_visibility)

    val MenuLibraryDeActive: ImageVector
        @Composable get() = vectorResource(Res.drawable.menu_library_deactive)

    val MenuLibraryActive: ImageVector
        @Composable get() = vectorResource(Res.drawable.menu_library_active)

    val MenuFinishedDeActive: ImageVector
        @Composable get() = vectorResource(Res.drawable.menu_finished_deactive)

    val MenuFinishedActive: ImageVector
        @Composable get() = vectorResource(Res.drawable.menu_finished_active)

    val MenuFavoritesActive: ImageVector
        @Composable get() = vectorResource(Res.drawable.menu_favorites_active)

    val MenuFavoritesDeActive: ImageVector
        @Composable get() = vectorResource(Res.drawable.menu_favorites_deactive)

    val ChevronRight: ImageVector
        @Composable get() = vectorResource(Res.drawable.chevron_right)


}
