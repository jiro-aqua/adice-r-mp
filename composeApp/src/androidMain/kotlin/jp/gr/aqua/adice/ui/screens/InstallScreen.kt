package jp.gr.aqua.adice.ui.screens

import android.content.res.Resources
import android.net.Uri
import android.os.Build
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
import androidx.compose.ui.unit.dp
import adicermp.composeapp.generated.resources.Res
import adicermp.composeapp.generated.resources.dicname_edict
import adicermp.composeapp.generated.resources.dicname_ichirofj
import adicermp.composeapp.generated.resources.dicname_pdej
import adicermp.composeapp.generated.resources.dicname_webster
import adicermp.composeapp.generated.resources.dldictionarytitle
import adicermp.composeapp.generated.resources.install_action
import adicermp.composeapp.generated.resources.install_disclaimer
import adicermp.composeapp.generated.resources.install_provider_edict
import adicermp.composeapp.generated.resources.install_provider_ichiro
import adicermp.composeapp.generated.resources.install_provider_pdej
import adicermp.composeapp.generated.resources.install_provider_webster
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
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
                title = { Text(stringResource(Res.string.dldictionarytitle)) },
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
                    text = stringResource(Res.string.install_disclaimer),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            items(installItems) { item ->
                val title = stringResource(item.titleRes)
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = stringResource(item.providerRes),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        onDownloadResult(
                            buildInstallUri(
                                name = title,
                                english = item.english,
                                site = item.site
                            )
                        )
                    },
                    modifier = Modifier
                        .height(40.dp)
                        .widthIn(min = 200.dp)
                ) {
                    Text(stringResource(Res.string.install_action))
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

private data class InstallDictionaryItem(
    val titleRes: StringResource,
    val providerRes: StringResource,
    val english: Boolean,
    val site: String
)

private fun buildInstallUri(name: String, english: Boolean, site: String): String {
    return Uri.Builder()
        .scheme("adicer")
        .authority("install")
        .appendQueryParameter("name", name)
        .appendQueryParameter("english", english.toString())
        .appendQueryParameter("site", site)
        .build()
        .toString()
}

private fun englishInstallItems(): List<InstallDictionaryItem> {
    return listOf(
        InstallDictionaryItem(
            titleRes = Res.string.dicname_webster,
            providerRes = Res.string.install_provider_webster,
            english = true,
            site = "aquamarine.sakura.ne.jp/sblo_files/pandora/image/PDWD1913U.zip"
        ),
        InstallDictionaryItem(
            titleRes = Res.string.dicname_pdej,
            providerRes = Res.string.install_provider_pdej,
            english = true,
            site = "aquamarine.sakura.ne.jp/sblo_files/pandora/image/PDEJ2005U.zip"
        ),
        InstallDictionaryItem(
            titleRes = Res.string.dicname_edict,
            providerRes = Res.string.install_provider_edict,
            english = false,
            site = "aquamarine.sakura.ne.jp/sblo_files/pandora/image/PDEDICTU.zip"
        ),
        InstallDictionaryItem(
            titleRes = Res.string.dicname_ichirofj,
            providerRes = Res.string.install_provider_ichiro,
            english = false,
            site = "aquamarine.sakura.ne.jp/sblo_files/pandora/image/f2jdic113.zip"
        )
    )
}

private fun japaneseInstallItems(): List<InstallDictionaryItem> {
    return listOf(
        InstallDictionaryItem(
            titleRes = Res.string.dicname_pdej,
            providerRes = Res.string.install_provider_pdej,
            english = true,
            site = "aquamarine.sakura.ne.jp/sblo_files/pandora/image/PDEJ2005U.zip"
        ),
        InstallDictionaryItem(
            titleRes = Res.string.dicname_edict,
            providerRes = Res.string.install_provider_edict,
            english = false,
            site = "aquamarine.sakura.ne.jp/sblo_files/pandora/image/PDEDICTU.zip"
        ),
        InstallDictionaryItem(
            titleRes = Res.string.dicname_webster,
            providerRes = Res.string.install_provider_webster,
            english = true,
            site = "aquamarine.sakura.ne.jp/sblo_files/pandora/image/PDWD1913U.zip"
        ),
        InstallDictionaryItem(
            titleRes = Res.string.dicname_ichirofj,
            providerRes = Res.string.install_provider_ichiro,
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
