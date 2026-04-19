package com.jvcodingsolutions.pagekeeper

interface Platform {
    val name: String
    val showComposeSplash: Boolean
}

expect fun getPlatform(): Platform

expect fun currentTimeMillis(): Long