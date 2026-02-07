package jp.gr.aqua.adice.ui.components

import adicermp.composeapp.generated.resources.Res
import adicermp.composeapp.generated.resources.morebtn
import adicermp.composeapp.generated.resources.noresulthtml
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import jp.gr.aqua.adice.model.ResultModel
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WordResultItem(
    result: ResultModel,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(horizontal = 8.dp)
    ) {
        // Index
        result.index?.let { index ->
            if (index.isNotEmpty()) {
                Text(
                    text = index.toString(),
                    color = Color.Black,
                    fontSize = result.indexSize.sp
                )
            }
        }

        // Phone
        result.phone?.let { phone ->
            if (phone.isNotEmpty()) {
                Text(
                    text = phone.toString(),
                    color = Color.Black,
                    fontSize = result.phoneSize.sp
                )
            }
        }

        // Trans
        result.trans?.let { trans ->
            if (trans.isNotEmpty()) {
                Text(
                    text = trans.toString(),
                    color = Color.Black,
                    fontSize = result.transSize.sp
                )
            }
        }

        // Sample
        result.sample?.let { sample ->
            if (sample.isNotEmpty()) {
                Text(
                    text = sample.toString(),
                    color = Color.Black,
                    fontSize = result.sampleSize.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun MoreResultItem(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(Color.LightGray)
            .clickable(onClick = onClick)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(Res.string.morebtn),
            color = Color.Black,
            fontSize = 16.sp
        )
    }
}

@Composable
fun NoneResultItem(result: ResultModel) {
    Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 16.dp)) {
        result.index?.let { title ->
            if (title.isNotEmpty()) {
                Text(
                    text = title.toString(),
                    color = Color.Black,
                    fontSize = result.indexSize.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
        }
        result.phone?.let { version ->
            if (version.isNotEmpty()) {
                Text(
                    text = version.toString(),
                    color = Color.Black,
                    fontSize = result.phoneSize.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
        }
        result.trans?.let { description ->
            if (description.isNotEmpty()) {
                Text(
                    text = description.toString(),
                    color = Color.Black,
                    fontSize = result.transSize.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
        }
        result.sample?.let { footer ->
            if (footer.isNotEmpty()) {
                Text(
                    text = footer.toString(),
                    color = Color.Black,
                    fontSize = result.sampleSize.sp
                )
            }
        }
    }
}

@Composable
fun NoResultItem() {
    Text(
        text = stringResource(Res.string.noresulthtml),
        color = Color.Black,
        fontSize = 16.sp,
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 16.dp)
    )
}

@Composable
fun FooterResultItem(result: ResultModel) {
    Column(modifier = Modifier.fillMaxWidth()) {
        result.index?.let { index ->
            if (index.isNotEmpty()) {
                Text(
                    text = index.toString(),
                    color = Color.Black,
                    fontSize = result.indexSize.sp,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        }
        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            thickness = 1.dp,
            color = Color.Black
        )
    }
}
