package com.jvcodingsolutions.pagekeeper.app.di

import com.jvcodingsolutions.pagekeeper.app.MainViewModel
import com.jvcodingsolutions.pagekeeper.core.data.BookRepositoryImpl
import com.jvcodingsolutions.pagekeeper.core.data.EpubBookParser
import com.jvcodingsolutions.pagekeeper.core.data.Fb2BookParser
import com.jvcodingsolutions.pagekeeper.core.data.LocalBookDataSource
import com.jvcodingsolutions.pagekeeper.core.data.PdfBookParser
import com.jvcodingsolutions.pagekeeper.core.domain.BookRepository
import com.jvcodingsolutions.pagekeeper.feature.library.presentation.LibraryViewModel
import com.jvcodingsolutions.pagekeeper.feature.reader.data.EpubContentParser
import com.jvcodingsolutions.pagekeeper.feature.reader.data.Fb2ContentParser
import com.jvcodingsolutions.pagekeeper.feature.reader.data.ReaderSettingsStorage
import com.jvcodingsolutions.pagekeeper.feature.reader.presentation.ReaderViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

val dataModule = module {
    singleOf(::Fb2BookParser)
    singleOf(::EpubBookParser)
    singleOf(::PdfBookParser)
    singleOf(::LocalBookDataSource)
    single<BookRepository> { BookRepositoryImpl(get(), get(), get(), get(), get()) }
    singleOf(::Fb2ContentParser)
    singleOf(::EpubContentParser)
    singleOf(::ReaderSettingsStorage)
}

val appModule = module {

    single(named("AppScope")) {
        CoroutineScope(SupervisorJob() + Dispatchers.Default)
    }
    viewModelOf(::MainViewModel)
    viewModelOf(::LibraryViewModel)
    viewModel { params -> ReaderViewModel(params.get(), get(), get(), get(), get()) }
}
