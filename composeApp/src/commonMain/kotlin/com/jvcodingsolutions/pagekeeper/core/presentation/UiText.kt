package com.jvcodingsolutions.pagekeeper.core.presentation

import org.jetbrains.compose.resources.StringResource

sealed interface UiText {
    data class DynamicString(val value: String) : UiText
    class StringResourceText(
        val resId: StringResource,
        val args: Array<Any> = emptyArray(),
    ) : UiText
}
