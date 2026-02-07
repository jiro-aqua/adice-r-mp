package jp.gr.aqua.adice

import android.app.Application
import android.content.Context
import jp.gr.aqua.adice.di.appModule
import jp.gr.aqua.adice.di.commonModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class AdiceApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        appContext = this
        startKoin {
            androidContext(this@AdiceApplication)
            modules(appModule, commonModule)
        }
    }

    companion object {
        lateinit var appContext: Context
    }
}
