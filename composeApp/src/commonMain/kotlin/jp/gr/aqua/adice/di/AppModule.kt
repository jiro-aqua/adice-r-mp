package jp.gr.aqua.adice.di

import jp.gr.aqua.adice.model.DictionaryRepository
import jp.gr.aqua.adice.model.DownloadRepository
import jp.gr.aqua.adice.model.SearchRepository
import jp.gr.aqua.adice.viewmodel.AdiceViewModel
import jp.gr.aqua.adice.viewmodel.PreferencesDictionaryViewModel
import jp.gr.aqua.adice.viewmodel.PreferencesGeneralViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

expect val appModule: Module

val commonModule : Module = module {
    single { SearchRepository() }
    single { DownloadRepository() }
    single { DictionaryRepository() }
    viewModel { AdiceViewModel(get()) }
    viewModel { PreferencesGeneralViewModel(get(), get()) }
    viewModel { PreferencesDictionaryViewModel(get()) }
}
