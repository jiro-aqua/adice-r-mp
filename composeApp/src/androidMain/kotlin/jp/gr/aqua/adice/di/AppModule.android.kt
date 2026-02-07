package jp.gr.aqua.adice.di

import jp.gr.aqua.adice.AdiceApplication
import jp.gr.aqua.adice.model.ContextModel
import jp.gr.aqua.adice.model.ContextModelAndroidImpl
import jp.gr.aqua.adice.model.PreferenceRepository
import jp.gr.aqua.adice.model.PreferenceRepositoryAndroidImpl
import org.koin.core.module.Module
import org.koin.dsl.module

actual val appModule : Module = module {
    single<PreferenceRepository> { PreferenceRepositoryAndroidImpl(AdiceApplication.appContext) }
    single<ContextModel> { ContextModelAndroidImpl(AdiceApplication.appContext) }
}
