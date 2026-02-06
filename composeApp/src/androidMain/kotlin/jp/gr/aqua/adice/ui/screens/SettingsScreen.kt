package jp.gr.aqua.adice.ui.screens

import android.net.Uri
import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import jp.gr.aqua.adice.R
import jp.gr.aqua.adice.model.ContextModel
import jp.gr.aqua.adice.model.DictionaryRepository
import jp.gr.aqua.adice.model.PreferenceRepository
import jp.gr.aqua.adice.viewmodel.PreferencesGeneralViewModel
import jp.sblo.pandora.dice.IdicInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    downloadNow: Boolean = false,
    viewModel: PreferencesGeneralViewModel = viewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToDictionarySettings: (String, Int) -> Unit,
    onNavigateToInstall: () -> Unit,
    onDownloadUrl: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val preferenceRepository = remember { PreferenceRepository() }
    val dictionaryRepository = remember { DictionaryRepository() }
    var dicList by remember { mutableStateOf<List<IdicInfo>>(emptyList()) }
    var normalizeSearch by remember { mutableStateOf(preferenceRepository.readGeneralSettings().normalize) }

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
    LaunchedEffect(uiState.completionResult) {
        uiState.completionResult?.let { result ->
            refreshDicList()
            val message = if (result.success) {
                ContextModel.resources.getString(R.string.toastadded, result.dicName)
            } else {
                ContextModel.resources.getString(R.string.toasterror, result.dicName)
            }
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            viewModel.clearCompletionResult()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.setting_name)) },
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
                SettingsCategoryHeader(title = stringResource(R.string.searchsettingtitle))
            }

            item {
                SwitchSettingItem(
                    title = stringResource(R.string.normalize),
                    checked = normalizeSearch,
                    onCheckedChange = { checked ->
                        normalizeSearch = checked
                        preferenceRepository.setNormalize(checked)
                    }
                )
            }

            // Import Dictionary Category
            item {
                SettingsCategoryHeader(title = stringResource(R.string.dictionaryimporttitle))
            }

            if (!uiState.isDownloading) {
                item {
                    SettingsClickableItem(
                        title = stringResource(R.string.adddictionarytitle),
                        onClick = { openDocumentLauncher.launch(arrayOf("*/*")) }
                    )
                }

                item {
                    SettingsClickableItem(
                        title = stringResource(R.string.dldictionarytitle),
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
                        Text(stringResource(R.string.download_process))
                        Spacer(modifier = Modifier.width(16.dp))
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    }
                }
            }

            // Dictionary Management Category
            item {
                SettingsCategoryHeader(title = stringResource(R.string.dictionarymanagementtitle))
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
