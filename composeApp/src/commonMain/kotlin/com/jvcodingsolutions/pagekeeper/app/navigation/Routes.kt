package com.jvcodingsolutions.pagekeeper.app.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object SplashRoute : NavKey

@Serializable
data object LibraryRoute : NavKey

@Serializable
data object FavoritesRoute : NavKey

@Serializable
data object FinishedRoute : NavKey

@Serializable
data class ReaderRoute(val bookId: String) : NavKey
