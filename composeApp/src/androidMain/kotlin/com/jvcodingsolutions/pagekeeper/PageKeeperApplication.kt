package com.jvcodingsolutions.pagekeeper

import android.app.Application
import com.jvcodingsolutions.pagekeeper.app.di.initKoin
import org.koin.android.ext.koin.androidContext

class PageKeeperApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidContext(this@PageKeeperApplication)
        }
    }
}
