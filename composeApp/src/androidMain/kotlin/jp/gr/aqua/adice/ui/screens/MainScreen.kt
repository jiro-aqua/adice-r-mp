package jp.gr.aqua.adice.ui.screens

import android.app.Activity
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import adicermp.composeapp.generated.resources.Res
import adicermp.composeapp.generated.resources.app_name
import adicermp.composeapp.generated.resources.help
import adicermp.composeapp.generated.resources.settings
import jp.gr.aqua.adice.model.PreferenceRepository
import jp.gr.aqua.adice.model.ResultModel
import jp.gr.aqua.adice.ui.components.SearchResultList
import jp.gr.aqua.adice.ui.components.SearchTextField
import jp.gr.aqua.adice.viewmodel.AdiceViewModel
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    initialText: String = "",
    viewModel: AdiceViewModel = viewModel(),
    onNavigateToSettings: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onNavigateToWelcomeDialog: () -> Unit,
    onNavigateToResultClickDialog: (String, List<String>, List<String>) -> Unit,
    onNavigateToResultLongClickDialog: (String, String) -> Unit,
    onLinkClicked: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val focusRequester = remember { FocusRequester() }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    var showMenu by remember { mutableStateOf(false) }

    // Handle initial text from intent
    LaunchedEffect(initialText) {
        if (initialText.isNotEmpty() && uiState.searchWord != initialText) {
            viewModel.updateSearchWord(initialText)
            viewModel.search(initialText, loseFocus = true)
        }
    }

    // Check for version update on first launch
    LaunchedEffect(Unit) {
        if (PreferenceRepository().isVersionUp()) {
            onNavigateToWelcomeDialog()
        }
        if (uiState.searchWord.isEmpty() && uiState.results.isEmpty()) {
            viewModel.startPage()
            try {
                focusRequester.requestFocus()
            } catch (_: Exception) {}
        }
    }

    // Resume behavior
    LaunchedEffect(Unit) {
        viewModel.onResume()
    }

    // Handle scroll reset
    LaunchedEffect(uiState.resetScroll) {
        if (uiState.resetScroll && uiState.results.isNotEmpty()) {
            coroutineScope.launch {
                listState.scrollToItem(0)
            }
            viewModel.clearScrollFlag()
        }
    }

    // Handle focus loss
    LaunchedEffect(uiState.loseFocus) {
        if (uiState.loseFocus && uiState.results.isNotEmpty() &&
            uiState.results[0].mode != ResultModel.Mode.NONE) {
            viewModel.clearFocusFlag()
        }
    }

    // Back handler for history
    BackHandler {
        var cs: CharSequence?
        do {
            cs = viewModel.popHistory()
        } while (cs != null && uiState.searchWord == cs.toString())

        if (cs != null) {
            viewModel.updateSearchWord(cs.toString())
            viewModel.search(cs.toString())
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                (context as? Activity)?.moveTaskToBack(false)
            } else {
                (context as? Activity)?.finish()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.app_name)) },
                actions = {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(Res.string.settings)) },
                            onClick = {
                                showMenu = false
                                onNavigateToSettings()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(Res.string.help)) },
                            onClick = {
                                showMenu = false
                                onNavigateToAbout()
                            }
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            SearchTextField(
                value = uiState.searchWord,
                onValueChange = { viewModel.updateSearchWord(it) },
                onSearch = { viewModel.search(it) },
                onClear = {
                    viewModel.pushHistory()
                    viewModel.updateSearchWord("")
                    viewModel.search("")
                },
                focusRequester = focusRequester
            )

            SearchResultList(
                results = uiState.results,
                listState = listState,
                onItemClick = { position, data ->
                    when (data.mode) {
                        ResultModel.Mode.WORD -> {
                            val (disps, items) = data.links()
                            when {
                                disps.size == 1 -> {
                                    viewModel.pushHistory()
                                    viewModel.updateSearchWord(items[0])
                                    viewModel.search(items[0])
                                }
                                disps.size > 1 -> {
                                    val title = data.index?.toString() ?: ""
                                    onNavigateToResultClickDialog(title, disps.toList(), items.toList())
                                }
                            }
                        }
                        ResultModel.Mode.MORE -> {
                            viewModel.more(position)
                        }
                        else -> {}
                    }
                },
                onItemLongClick = { _, data ->
                    if (data.mode == ResultModel.Mode.WORD) {
                        val all = data.allText()
                        val title = data.index?.toString() ?: ""
                        onNavigateToResultLongClickDialog(title, all)
                    }
                },
                onMoreClick = { position -> viewModel.more(position) }
            )
        }
    }
}
