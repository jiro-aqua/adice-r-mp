package jp.gr.aqua.adice.ui.screens

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import adicermp.composeapp.generated.resources.Res
import adicermp.composeapp.generated.resources.description
import adicermp.composeapp.generated.resources.help
import adicermp.composeapp.generated.resources.manual
import adicermp.composeapp.generated.resources.manual_url
import jp.gr.aqua.adice.BuildConfig
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.help)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        val context = LocalContext.current
        val uriHandler = LocalUriHandler.current
        val iconBitmap = rememberAssetBitmap("icon.png")

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    iconBitmap?.let { bitmap ->
                        Image(
                            bitmap = bitmap,
                            contentDescription = "aDice icon",
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.size(12.dp))
                    }
                    Text(
                        text = "aDice",
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
            }
            item {
                Text(
                    text = "Ver. ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            item {
                Text(
                    text = stringResource(Res.string.description),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            item {
                ExternalLinkText(
                    text = stringResource(Res.string.manual),
                    url = stringResource(Res.string.manual_url),
                    onClick = { openExternalUrl(context, uriHandler, it) }
                )
            }
            item {
                Spacer(modifier = Modifier.height(4.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Copyright (C) 2020, Jiro and Aquamarine Networks.",
                    style = MaterialTheme.typography.bodyMedium
                )
                ExternalLinkText(
                    text = "http://pandora.sblo.jp/",
                    url = "http://pandora.sblo.jp",
                    onClick = { openExternalUrl(context, uriHandler, it) }
                )
            }
            item {
                HorizontalDivider()
            }
            item {
                Text(
                    text = "This software is under license of NYSL.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "See:",
                    style = MaterialTheme.typography.bodyMedium
                )
                ExternalLinkText(
                    text = "http://www.kmonos.net/nysl/NYSL.TXT",
                    url = "http://www.kmonos.net/nysl/NYSL.TXT",
                    onClick = { openExternalUrl(context, uriHandler, it) }
                )
                Text(
                    text = NYSL_LICENSE_TEXT,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            item {
                HorizontalDivider()
            }
            item {
                Text(
                    text = "This software includes the parts of below projects.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "CAUTION: These are NOT licensed under NYSL.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(4.dp))

                Text("PDIC/Unicode by TaN.", style = MaterialTheme.typography.bodyMedium)
                ExternalLinkText(
                    text = "http://homepage3.nifty.com/TaN/unicode/",
                    url = "http://homepage3.nifty.com/TaN/unicode",
                    onClick = { openExternalUrl(context, uriHandler, it) }
                )
                Spacer(modifier = Modifier.height(4.dp))

                Text("Doulos SIL Font", style = MaterialTheme.typography.bodyMedium)
                ExternalLinkText(
                    text = "http://scripts.sil.org/cms/scripts/page.php?site_id=nrsi&item_id=DoulosSILfont",
                    url = "http://scripts.sil.org/cms/scripts/page.php?site_id=nrsi&item_id=DoulosSILfont",
                    onClick = { openExternalUrl(context, uriHandler, it) }
                )
                ExternalLinkText(
                    text = "http://scripts.sil.org/",
                    url = "http://scripts.sil.org/",
                    onClick = { openExternalUrl(context, uriHandler, it) }
                )
                ExternalLinkText(
                    text = "http://scripts.sil.org/OFL",
                    url = "http://scripts.sil.org/OFL",
                    onClick = { openExternalUrl(context, uriHandler, it) }
                )
                Spacer(modifier = Modifier.height(4.dp))

                Text("okhttp by square", style = MaterialTheme.typography.bodyMedium)
                ExternalLinkText(
                    text = "https://square.github.io/okhttp/",
                    url = "https://square.github.io/okhttp/",
                    onClick = { openExternalUrl(context, uriHandler, it) }
                )
                ExternalLinkText(
                    text = "https://github.com/square/okhttp/blob/master/LICENSE.txt",
                    url = "https://github.com/square/okhttp/blob/master/LICENSE.txt",
                    onClick = { openExternalUrl(context, uriHandler, it) }
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text("Thanks to the authors!", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun rememberAssetBitmap(assetName: String): ImageBitmap? {
    val context = LocalContext.current
    return remember(context, assetName) {
        runCatching {
            context.assets.open(assetName).use { stream ->
                BitmapFactory.decodeStream(stream)?.asImageBitmap()
            }
        }.getOrNull()
    }
}

@Composable
private fun ExternalLinkText(
    text: String,
    url: String,
    onClick: (String) -> Unit
) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.primary,
        textDecoration = TextDecoration.Underline,
        modifier = Modifier.clickable { onClick(url) }
    )
}

private fun openExternalUrl(
    context: Context,
    uriHandler: androidx.compose.ui.platform.UriHandler,
    url: String
) {
    val opened = runCatching {
        uriHandler.openUri(url)
    }.isSuccess
    if (opened) return

    runCatching {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}

private const val NYSL_LICENSE_TEXT = """
NYSL Version 0.9982 (en)
----------------------------------------
A. This software is "Everyone'sWare".
It means: Anybody who has this software can use it as if you're the author.
A-1. Freeware. No fee is required. (But donation is welcomed.)
A-2. You can freely redistribute this software.
A-3. You can freely modify this software.
A-4. When you release a modified version to public, you MUST publish it with your name.

B. The author is not responsible for any kind of damages or loss while using or misusing this software, which is distributed "AS IS".
No warranty of any kind is expressed or implied. You use AT YOUR OWN RISK.

C. Copyright by Jiro and Aquamarine Networks.

D. Above three clauses are applied both to source and binary form of this software.
"""
