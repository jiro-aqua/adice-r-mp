package jp.gr.aqua.adice.ui.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
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

sealed class Screen(val route: String) {
    data object Main : Screen("main")
    data object About : Screen("about")
    data object Install : Screen("install")
    data object Settings : Screen("settings/{downloadNow}") {
        fun createRoute(downloadNow: Boolean = false) = "settings/$downloadNow"
    }
    data object DictionarySettings : Screen("dictionary_settings/{filename}/{index}") {
        fun createRoute(filename: String, index: Int) =
            "dictionary_settings/${Uri.encode(filename)}/$index"
    }
}

@Composable
fun AdiceNavHost(
    navController: NavHostController,
    initialText: String = "",
    adiceViewModel: AdiceViewModel = viewModel(),
    settingsViewModel: PreferencesGeneralViewModel = viewModel()
) {
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

    NavHost(
        navController = navController,
        startDestination = Screen.Main.route
    ) {
        composable(Screen.Main.route) {
            MainScreen(
                initialText = initialText,
                viewModel = adiceViewModel,
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.createRoute(false))
                },
                onNavigateToAbout = {
                    navController.navigate(Screen.About.route)
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
                }
            )

            // Dialogs shown over MainScreen
            if (showWelcomeDialog) {
                WelcomeDialog(
                    onDismiss = { showWelcomeDialog = false },
                    onDownload = {
                        showWelcomeDialog = false
                        navController.navigate(Screen.Settings.createRoute(true))
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

        composable(Screen.About.route) {
            AboutScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Install.route) {
            InstallScreen(
                onNavigateBack = { navController.popBackStack() },
                onDownloadResult = { url ->
                    val uri = Uri.parse(url)
                    if (uri.scheme == "adicer" && uri.host == "install") {
                        val site = uri.getQueryParameter("site")
                        val english = uri.getQueryParameter("english") == "true"
                        val defname = uri.getQueryParameter("name") ?: ""
                        if (!site.isNullOrEmpty()) {
                            settingsViewModel.download(site, english, defname)
                        }
                    }
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.Settings.route,
            arguments = listOf(
                navArgument("downloadNow") {
                    type = NavType.BoolType
                    defaultValue = false
                }
            )
        ) { backStackEntry ->
            val downloadNow = backStackEntry.arguments?.getBoolean("downloadNow") ?: false
            SettingsScreen(
                downloadNow = downloadNow,
                viewModel = settingsViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDictionarySettings = { filename, index ->
                    navController.navigate(Screen.DictionarySettings.createRoute(filename, index))
                },
                onNavigateToInstall = {
                    navController.navigate(Screen.Install.route)
                }
            )
        }

        composable(
            route = Screen.DictionarySettings.route,
            arguments = listOf(
                navArgument("filename") { type = NavType.StringType },
                navArgument("index") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val filename = Uri.decode(backStackEntry.arguments?.getString("filename") ?: "")
            val index = backStackEntry.arguments?.getInt("index") ?: 0
            DictionarySettingsScreen(
                filename = filename,
                index = index,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
