package jp.gr.aqua.adice.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import adicermp.composeapp.generated.resources.Res
import adicermp.composeapp.generated.resources.adddictionarytitle
import adicermp.composeapp.generated.resources.dictionaryimporttitle
import adicermp.composeapp.generated.resources.dictionarymanagementtitle
import adicermp.composeapp.generated.resources.dldictionarytitle
import adicermp.composeapp.generated.resources.download_process
import adicermp.composeapp.generated.resources.normalize_word
import adicermp.composeapp.generated.resources.searchsettingtitle
import adicermp.composeapp.generated.resources.setting_name
import adicermp.composeapp.generated.resources.toastadded
import adicermp.composeapp.generated.resources.toasterror
import jp.gr.aqua.adice.model.DictionaryRepository
import jp.gr.aqua.adice.model.PreferenceRepository
import jp.gr.aqua.adice.viewmodel.PreferencesGeneralViewModel
import jp.sblo.pandora.dice.IdicInfo
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    downloadNow: Boolean = false,
    viewModel: PreferencesGeneralViewModel = viewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToDictionarySettings: (String, Int) -> Unit,
    onNavigateToInstall: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val preferenceRepository = remember { PreferenceRepository() }
    val dictionaryRepository = remember { DictionaryRepository() }
    val snackbarHostState = remember { SnackbarHostState() }
    var dicList by remember { mutableStateOf<List<IdicInfo>>(emptyList()) }
    var normalizeSearch by remember { mutableStateOf(preferenceRepository.readGeneralSettings().normalize) }
    val completionResult = uiState.completionResult
    val completionMessage = completionResult?.let { result ->
        if (result.success) {
            stringResource(Res.string.toastadded, result.dicName)
        } else {
            stringResource(Res.string.toasterror, result.dicName)
        }
    }

    // Refresh dictionary list
    fun refreshDicList() {
        dicList = dictionaryRepository.getDicList()
    }

    LaunchedEffect(Unit) {
        refreshDicList()
    }

    val openDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { viewModel.openDictionary(it) }
    }

    // Handle download now
    LaunchedEffect(downloadNow) {
        if (downloadNow) {
            onNavigateToInstall()
        }
    }

    // Handle completion
    LaunchedEffect(completionResult) {
        completionResult?.let {
            refreshDicList()
            snackbarHostState.showSnackbar(
                message = completionMessage.orEmpty(),
                duration = SnackbarDuration.Long
            )
            viewModel.clearCompletionResult()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.setting_name)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search Settings Category
            item {
                SettingsCategoryHeader(title = stringResource(Res.string.searchsettingtitle))
            }

            item {
                SwitchSettingItem(
                    title = stringResource(Res.string.normalize_word),
                    checked = normalizeSearch,
                    onCheckedChange = { checked ->
                        normalizeSearch = checked
                        preferenceRepository.setNormalize(checked)
                    }
                )
            }

            // Import Dictionary Category
            item {
                SettingsCategoryHeader(title = stringResource(Res.string.dictionaryimporttitle))
            }

            if (!uiState.isDownloading) {
                item {
                    SettingsClickableItem(
                        title = stringResource(Res.string.adddictionarytitle),
                        onClick = { openDocumentLauncher.launch(arrayOf("*/*")) }
                    )
                }

                item {
                    SettingsClickableItem(
                        title = stringResource(Res.string.dldictionarytitle),
                        onClick = onNavigateToInstall
                    )
                }
            } else {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(stringResource(Res.string.download_process))
                        Spacer(modifier = Modifier.width(16.dp))
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    }
                }
            }

            // Dictionary Management Category
            item {
                SettingsCategoryHeader(title = stringResource(Res.string.dictionarymanagementtitle))
            }

            itemsIndexed(dicList) { index, dicInfo ->
                val name = dicInfo.GetFilename()
                if (name.isNotEmpty()) {
                    val dicName = preferenceRepository.getDicName(name) ?: name
                    SettingsClickableItem(
                        title = dicName,
                        onClick = { onNavigateToDictionarySettings(name, index) }
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsCategoryHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 8.dp)
    )
}

@Composable
fun SettingsClickableItem(
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, style = MaterialTheme.typography.bodyLarge)
    }
    subtitle?.let {
        Text(
            text = it,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
        )
    }
}

@Composable
fun SwitchSettingItem(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, style = MaterialTheme.typography.bodyLarge)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
