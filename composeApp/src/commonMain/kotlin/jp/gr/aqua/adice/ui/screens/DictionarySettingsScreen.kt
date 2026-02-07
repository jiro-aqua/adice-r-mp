package jp.gr.aqua.adice.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import adicermp.composeapp.generated.resources.Res
import adicermp.composeapp.generated.resources.dictionarymanagementtitle
import adicermp.composeapp.generated.resources.dictionarynametitle
import adicermp.composeapp.generated.resources.dictionarypathtitle
import adicermp.composeapp.generated.resources.enabledictionarysummaryoff
import adicermp.composeapp.generated.resources.enabledictionarysummaryon
import adicermp.composeapp.generated.resources.enabledictionarytitle
import adicermp.composeapp.generated.resources.englishsummaryoff
import adicermp.composeapp.generated.resources.englishsummaryon
import adicermp.composeapp.generated.resources.englishtitle
import adicermp.composeapp.generated.resources.label_close
import adicermp.composeapp.generated.resources.movedowntitle
import adicermp.composeapp.generated.resources.moveuptitle
import adicermp.composeapp.generated.resources.numberofresulttitle
import adicermp.composeapp.generated.resources.removedictionarytitle
import adicermp.composeapp.generated.resources.toastremoved
import jp.gr.aqua.adice.model.DictionaryRepository
import jp.gr.aqua.adice.model.DictionarySettings
import jp.gr.aqua.adice.model.PreferenceRepository
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DictionarySettingsScreen(
    filename: String,
    index: Int,
    onNavigateBack: () -> Unit
) {
    val preferenceRepository = koinInject<PreferenceRepository>()
    val dictionaryRepository = remember { DictionaryRepository() }
    val snackbarHostState = remember { SnackbarHostState() }
    val dicListSize = remember { dictionaryRepository.getDicList().size }
    val toastRemovedMessage = stringResource(Res.string.toastremoved, filename)

    var dicName by remember { mutableStateOf("") }
    var isEnglish by remember { mutableStateOf(false) }
    var isEnabled by remember { mutableStateOf(true) }
    var resultNum by remember { mutableIntStateOf(30) }
    var showResultNumDialog by remember { mutableStateOf(false) }
    var showRemoveDialog by remember { mutableStateOf(false) }
    var removedSnackbarMessage by remember { mutableStateOf<String?>(null) }

    // Load settings
    LaunchedEffect(filename) {
        val settings = preferenceRepository.readDictionarySettings(filename)
        dicName = settings.dicname
        isEnglish = settings.english
        isEnabled = settings.use
        resultNum = settings.resultNum
    }

    // Save settings when changed
    fun saveSettings() {
        preferenceRepository.updateDictionarySettings(
            filename,
            DictionarySettings(
                dicname = dicName,
                english = isEnglish,
                use = isEnabled,
                resultNum = resultNum
            )
        )
    }

    LaunchedEffect(removedSnackbarMessage) {
        removedSnackbarMessage?.let { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Long
            )
            removedSnackbarMessage = null
            onNavigateBack()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(dicName.ifEmpty { filename }) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Category Header
            SettingsCategoryHeader(title = stringResource(Res.string.dictionarymanagementtitle))

            // Path
            SettingsInfoItem(
                title = stringResource(Res.string.dictionarypathtitle),
                value = filename
            )

            HorizontalDivider()

            // Dictionary Name
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = stringResource(Res.string.dictionarynametitle),
                    style = MaterialTheme.typography.bodyLarge
                )
                OutlinedTextField(
                    value = dicName,
                    onValueChange = {
                        dicName = it
                        saveSettings()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    singleLine = true
                )
            }

            HorizontalDivider()

            // Number of results
            SettingsClickableItem(
                title = stringResource(Res.string.numberofresulttitle),
                subtitle = resultNum.toString(),
                onClick = { showResultNumDialog = true }
            )

            HorizontalDivider()

            // English dictionary
            SwitchSettingItem(
                title = stringResource(Res.string.englishtitle),
                subtitle = if (isEnglish)
                    stringResource(Res.string.englishsummaryon)
                else
                    stringResource(Res.string.englishsummaryoff),
                checked = isEnglish,
                onCheckedChange = {
                    isEnglish = it
                    saveSettings()
                }
            )

            HorizontalDivider()

            // Enable dictionary
            SwitchSettingItem(
                title = stringResource(Res.string.enabledictionarytitle),
                subtitle = if (isEnabled)
                    stringResource(Res.string.enabledictionarysummaryon)
                else
                    stringResource(Res.string.enabledictionarysummaryoff),
                checked = isEnabled,
                onCheckedChange = {
                    isEnabled = it
                    saveSettings()
                }
            )

            HorizontalDivider()

            // Move Up
            if (index > 0) {
                SettingsClickableItem(
                    title = stringResource(Res.string.moveuptitle),
                    onClick = {
                        dictionaryRepository.swap(filename, up = true)
                        onNavigateBack()
                    }
                )
                HorizontalDivider()
            }

            // Move Down
            if (index < dicListSize - 1) {
                SettingsClickableItem(
                    title = stringResource(Res.string.movedowntitle),
                    onClick = {
                        dictionaryRepository.swap(filename, up = false)
                        onNavigateBack()
                    }
                )
                HorizontalDivider()
            }

            // Remove
            SettingsClickableItem(
                title = stringResource(Res.string.removedictionarytitle),
                onClick = { showRemoveDialog = true }
            )
        }
    }

    // Result num dialog
    if (showResultNumDialog) {
        ResultNumDialog(
            currentValue = resultNum,
            onDismiss = { showResultNumDialog = false },
            onSelect = { selected ->
                resultNum = selected
                saveSettings()
                showResultNumDialog = false
            }
        )
    }

    // Remove confirmation dialog
    if (showRemoveDialog) {
        AlertDialog(
            onDismissRequest = { showRemoveDialog = false },
            title = { Text(stringResource(Res.string.removedictionarytitle)) },
            text = { Text(filename) },
            confirmButton = {
                TextButton(onClick = {
                    dictionaryRepository.remove(filename)
                    showRemoveDialog = false
                    removedSnackbarMessage = toastRemovedMessage
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRemoveDialog = false }) {
                    Text(stringResource(Res.string.label_close))
                }
            }
        )
    }
}

@Composable
private fun SettingsInfoItem(
    title: String,
    value: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(text = title, style = MaterialTheme.typography.bodyLarge)
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
private fun SwitchSettingItem(
    title: String,
    subtitle: String,
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
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun ResultNumDialog(
    currentValue: Int,
    onDismiss: () -> Unit,
    onSelect: (Int) -> Unit
) {
    val options = listOf(10, 20, 30, 50, 100)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.numberofresulttitle)) },
        text = {
            Column {
                options.forEach { num ->
                    TextButton(
                        onClick = { onSelect(num) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = num.toString(),
                            color = if (num == currentValue)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        },
        confirmButton = {}
    )
}
