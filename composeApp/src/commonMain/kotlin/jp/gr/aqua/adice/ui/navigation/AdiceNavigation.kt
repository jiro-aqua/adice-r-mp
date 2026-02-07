package jp.gr.aqua.adice.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import jp.gr.aqua.adice.ui.dialogs.ResultClickDialog
import jp.gr.aqua.adice.ui.dialogs.ResultLongClickDialog
import jp.gr.aqua.adice.ui.dialogs.WelcomeDialog
import jp.gr.aqua.adice.ui.screens.AboutScreen
import jp.gr.aqua.adice.ui.screens.DictionarySettingsScreen
import jp.gr.aqua.adice.ui.screens.InstallScreen
import jp.gr.aqua.adice.ui.screens.MainScreen
import jp.gr.aqua.adice.ui.screens.SettingsScreen
import jp.gr.aqua.adice.viewmodel.AdiceViewModel
import jp.gr.aqua.adice.viewmodel.PreferencesGeneralViewModel
import kotlinx.serialization.Serializable

@Serializable
sealed interface Screen : NavKey {
    @Serializable
    data object Main : Screen

    @Serializable
    data object About : Screen

    @Serializable
    data object Install : Screen

    @Serializable
    data class Settings(val downloadNow: Boolean = false) : Screen

    @Serializable
    data class DictionarySettings(val filename: String, val index: Int) : Screen
}

@Composable
fun AdiceNavHost(
    initialText: String = "",
    adiceViewModel: AdiceViewModel,
    settingsViewModel: PreferencesGeneralViewModel,
    onMoveTaskToBack: () -> Unit
) {
    val backStack = rememberNavBackStack(Screen.Main)

    fun popBackStack() {
        if (backStack.size > 1) {
            backStack.removeLastOrNull()
        }
    }

    // Dialog states
    var showWelcomeDialog by remember { mutableStateOf(false) }
    var showResultClickDialog by remember { mutableStateOf(false) }
    var showResultLongClickDialog by remember { mutableStateOf(false) }

    // Dialog data
    var resultClickTitle by remember { mutableStateOf("") }
    var resultClickDisps by remember { mutableStateOf<List<String>>(emptyList()) }
    var resultClickItems by remember { mutableStateOf<List<String>>(emptyList()) }

    var resultLongClickTitle by remember { mutableStateOf("") }
    var resultLongClickAllText by remember { mutableStateOf("") }

    NavDisplay(
        backStack = backStack,
        onBack = { popBackStack() },
        entryProvider = entryProvider {
            entry<Screen.Main> {
                MainScreen(
                    initialText = initialText,
                    viewModel = adiceViewModel,
                    onNavigateToSettings = {
                        backStack.add(Screen.Settings())
                    },
                    onNavigateToAbout = {
                        backStack.add(Screen.About)
                    },
                    onNavigateToWelcomeDialog = {
                        showWelcomeDialog = true
                    },
                    onNavigateToResultClickDialog = { title, disps, items ->
                        resultClickTitle = title
                        resultClickDisps = disps
                        resultClickItems = items
                        showResultClickDialog = true
                    },
                    onNavigateToResultLongClickDialog = { title, all ->
                        resultLongClickTitle = title
                        resultLongClickAllText = all
                        showResultLongClickDialog = true
                    },
                    onLinkClicked = { link ->
                        adiceViewModel.pushHistory()
                        adiceViewModel.updateSearchWord(link)
                        adiceViewModel.search(link)
                    },
                    onMoveTaskToBack = onMoveTaskToBack,
                )

                // Dialogs shown over MainScreen
                if (showWelcomeDialog) {
                    WelcomeDialog(
                        onDismiss = { showWelcomeDialog = false },
                        onDownload = {
                            showWelcomeDialog = false
                            backStack.add(Screen.Settings(downloadNow = true))
                        }
                    )
                }

                if (showResultClickDialog) {
                    ResultClickDialog(
                        title = resultClickTitle,
                        disps = resultClickDisps,
                        items = resultClickItems,
                        onDismiss = { showResultClickDialog = false },
                        onItemSelected = { link ->
                            adiceViewModel.pushHistory()
                            adiceViewModel.updateSearchWord(link)
                            adiceViewModel.search(link)
                        }
                    )
                }

                if (showResultLongClickDialog) {
                    ResultLongClickDialog(
                        title = resultLongClickTitle,
                        allText = resultLongClickAllText,
                        onDismiss = { showResultLongClickDialog = false }
                    )
                }
            }

            entry<Screen.About> {
                AboutScreen(
                    onNavigateBack = { popBackStack() }
                )
            }

            entry<Screen.Install> {
                InstallScreen(
                    onNavigateBack = { popBackStack() },
                    onDownloadResult = { name: String, english: Boolean, site: String ->
                        if (site.isNotEmpty()) {
                            settingsViewModel.download(site, english, name)
                        }
                        popBackStack()
                    }
                )
            }

            entry<Screen.Settings> { settings ->
                SettingsScreen(
                    downloadNow = settings.downloadNow,
                    viewModel = settingsViewModel,
                    onNavigateBack = { popBackStack() },
                    onNavigateToDictionarySettings = { filename, index ->
                        backStack.add(Screen.DictionarySettings(filename = filename, index = index))
                    },
                    onNavigateToInstall = {
                        backStack.add(Screen.Install)
                    }
                )
            }

            entry<Screen.DictionarySettings> { settings ->
                DictionarySettingsScreen(
                    filename = settings.filename,
                    index = settings.index,
                    onNavigateBack = { popBackStack() }
                )
            }
        }
    )
}
