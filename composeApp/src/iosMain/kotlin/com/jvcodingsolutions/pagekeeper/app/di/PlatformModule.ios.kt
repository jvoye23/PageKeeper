package com.jvcodingsolutions.pagekeeper.app.di

import com.jvcodingsolutions.pagekeeper.core.data.FileStorage
import org.koin.dsl.module

actual val platformModule = module {
    single { FileStorage() }
}
