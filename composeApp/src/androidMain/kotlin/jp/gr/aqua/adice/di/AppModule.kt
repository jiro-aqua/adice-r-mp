package jp.gr.aqua.adice.di

import jp.gr.aqua.adice.AdiceApplication
import jp.gr.aqua.adice.model.DictionaryRepository
import jp.gr.aqua.adice.model.DownloadRepository
import jp.gr.aqua.adice.model.PreferenceRepository
import jp.gr.aqua.adice.model.PreferenceRepositoryAndroidImpl
import jp.gr.aqua.adice.model.SearchRepository
import jp.gr.aqua.adice.viewmodel.AdiceViewModel
import jp.gr.aqua.adice.viewmodel.PreferencesDictionaryViewModel
import jp.gr.aqua.adice.viewmodel.PreferencesGeneralViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { SearchRepository() }
    single { DownloadRepository() }
    single { DictionaryRepository() }
    single<PreferenceRepository> { PreferenceRepositoryAndroidImpl(AdiceApplication.appContext) }

    viewModel { AdiceViewModel(get()) }
    viewModel { PreferencesGeneralViewModel(get(), get()) }
    viewModel { PreferencesDictionaryViewModel(get()) }
}
