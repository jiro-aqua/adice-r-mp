package jp.gr.aqua.adice

import android.app.Application
import android.content.Context

class AdiceApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        appContext = this
    }

    companion object {
        lateinit var appContext: Context
    }
}
