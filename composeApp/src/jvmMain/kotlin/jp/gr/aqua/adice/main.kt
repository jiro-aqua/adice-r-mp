package jp.gr.aqua.adice

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import jp.gr.aqua.adice.di.appModule
import jp.gr.aqua.adice.di.commonModule
import jp.gr.aqua.adice.model.DictionaryRepository
import jp.gr.aqua.adice.model.DownloadRepository
import jp.gr.aqua.adice.model.SearchRepository
import jp.gr.aqua.adice.ui.navigation.AdiceNavHost
import jp.gr.aqua.adice.ui.theme.AdiceTheme
import jp.gr.aqua.adice.viewmodel.AdiceViewModel
import jp.gr.aqua.adice.viewmodel.PreferencesGeneralViewModel
import org.koin.compose.koinInject
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin

fun main() {
    startKoin {
        modules(appModule, commonModule)
    }

    application {
        Window(
            onCloseRequest = {
                stopKoin()
                exitApplication()
            },
            title = "aDice R MP",
        ) {
            DesktopApp(onMoveTaskToBack = ::exitApplication)
        }
    }
}

@Composable
private fun DesktopApp(
    onMoveTaskToBack: () -> Unit
) {
    val searchRepository = koinInject<SearchRepository>()
    val downloadRepository = koinInject<DownloadRepository>()
    val dictionaryRepository = koinInject<DictionaryRepository>()
    val adiceViewModel = remember(searchRepository) { AdiceViewModel(searchRepository) }
    val settingsViewModel = remember(downloadRepository, dictionaryRepository) {
        PreferencesGeneralViewModel(downloadRepository, dictionaryRepository)
    }

    AdiceTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            AdiceNavHost(
                adiceViewModel = adiceViewModel,
                settingsViewModel = settingsViewModel,
                onMoveTaskToBack = onMoveTaskToBack
            )
        }
    }
}
