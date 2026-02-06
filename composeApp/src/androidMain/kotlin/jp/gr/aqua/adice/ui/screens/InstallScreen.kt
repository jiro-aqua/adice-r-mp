package jp.gr.aqua.adice.ui.screens

import android.content.res.Resources
import android.net.Uri
import android.os.Build
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import jp.gr.aqua.adice.R
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstallScreen(
    onNavigateBack: () -> Unit,
    onDownloadResult: (String) -> Unit
) {
    val context = LocalContext.current
    val resources = context.resources
    val installItems = remember(resources.configuration) {
        if (isJapanese(resources)) {
            japaneseInstallItems()
        } else {
            englishInstallItems()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.dldictionarytitle)) },
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
                .padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Text(
                    text = stringResource(R.string.install_disclaimer),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            items(installItems) { item ->
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(item.titleRes),
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = stringResource(item.providerRes),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { onDownloadResult(item.buildInstallUri(resources)) },
                    modifier = Modifier
                        .height(40.dp)
                        .widthIn(min = 200.dp)
                ) {
                    Text(stringResource(R.string.install_action))
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

private data class InstallDictionaryItem(
    @StringRes val titleRes: Int,
    @StringRes val providerRes: Int,
    val english: Boolean,
    val site: String
)

private fun InstallDictionaryItem.buildInstallUri(resources: Resources): String {
    return Uri.Builder()
        .scheme("adicer")
        .authority("install")
        .appendQueryParameter("name", resources.getString(titleRes))
        .appendQueryParameter("english", english.toString())
        .appendQueryParameter("site", site)
        .build()
        .toString()
}

private fun englishInstallItems(): List<InstallDictionaryItem> {
    return listOf(
        InstallDictionaryItem(
            titleRes = R.string.dicname_webster,
            providerRes = R.string.install_provider_webster,
            english = true,
            site = "aquamarine.sakura.ne.jp/sblo_files/pandora/image/PDWD1913U.zip"
        ),
        InstallDictionaryItem(
            titleRes = R.string.dicname_pdej,
            providerRes = R.string.install_provider_pdej,
            english = true,
            site = "aquamarine.sakura.ne.jp/sblo_files/pandora/image/PDEJ2005U.zip"
        ),
        InstallDictionaryItem(
            titleRes = R.string.dicname_edict,
            providerRes = R.string.install_provider_edict,
            english = false,
            site = "aquamarine.sakura.ne.jp/sblo_files/pandora/image/PDEDICTU.zip"
        ),
        InstallDictionaryItem(
            titleRes = R.string.dicname_ichirofj,
            providerRes = R.string.install_provider_ichiro,
            english = false,
            site = "aquamarine.sakura.ne.jp/sblo_files/pandora/image/f2jdic113.zip"
        )
    )
}

private fun japaneseInstallItems(): List<InstallDictionaryItem> {
    return listOf(
        InstallDictionaryItem(
            titleRes = R.string.dicname_pdej,
            providerRes = R.string.install_provider_pdej,
            english = true,
            site = "aquamarine.sakura.ne.jp/sblo_files/pandora/image/PDEJ2005U.zip"
        ),
        InstallDictionaryItem(
            titleRes = R.string.dicname_edict,
            providerRes = R.string.install_provider_edict,
            english = false,
            site = "aquamarine.sakura.ne.jp/sblo_files/pandora/image/PDEDICTU.zip"
        ),
        InstallDictionaryItem(
            titleRes = R.string.dicname_webster,
            providerRes = R.string.install_provider_webster,
            english = true,
            site = "aquamarine.sakura.ne.jp/sblo_files/pandora/image/PDWD1913U.zip"
        ),
        InstallDictionaryItem(
            titleRes = R.string.dicname_ichirofj,
            providerRes = R.string.install_provider_ichiro,
            english = false,
            site = "aquamarine.sakura.ne.jp/sblo_files/pandora/image/f2jdic113.zip"
        )
    )
}

private fun isJapanese(resources: Resources): Boolean {
    val locale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        resources.configuration.locales[0]
    } else {
        @Suppress("DEPRECATION")
        resources.configuration.locale
    }
    return locale.language.equals(Locale.JAPANESE.language, ignoreCase = true)
}
