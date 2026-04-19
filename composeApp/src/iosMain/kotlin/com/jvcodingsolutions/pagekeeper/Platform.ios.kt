package com.jvcodingsolutions.pagekeeper

import platform.Foundation.timeIntervalSince1970
import platform.UIKit.UIDevice

class IOSPlatform: Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
    override val showComposeSplash: Boolean = true
}

actual fun getPlatform(): Platform = IOSPlatform()

actual fun currentTimeMillis(): Long =
    (platform.Foundation.NSDate().timeIntervalSince1970 * 1000).toLong()