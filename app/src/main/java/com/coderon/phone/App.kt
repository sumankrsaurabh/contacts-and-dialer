package com.coderon.phone
import android.app.Application
import com.coderon.phone.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class App: Application() {
    override fun onCreate() {
        super.onCreate()

        // Start Koin with the appModule
        startKoin {
            androidContext(this@App)
            modules(appModule)
        }
    }
}
