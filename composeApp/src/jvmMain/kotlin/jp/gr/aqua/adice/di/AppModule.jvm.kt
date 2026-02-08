package jp.gr.aqua.adice.di

import jp.gr.aqua.adice.model.ContextModel
import jp.gr.aqua.adice.model.ContextModelJvmImpl
import jp.gr.aqua.adice.model.PreferenceRepository
import jp.gr.aqua.adice.model.PreferenceRepositoryJvmImpl
import org.koin.core.module.Module
import org.koin.dsl.module

actual val appModule: Module = module {
    single<PreferenceRepository> { PreferenceRepositoryJvmImpl() }
    single<ContextModel> { ContextModelJvmImpl() }
}
