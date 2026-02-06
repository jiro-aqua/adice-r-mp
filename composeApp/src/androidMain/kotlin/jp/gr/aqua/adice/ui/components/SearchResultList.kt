package jp.gr.aqua.adice.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import jp.gr.aqua.adice.model.ResultModel

@Composable
fun SearchResultList(
    results: List<ResultModel>,
    onItemClick: (Int, ResultModel) -> Unit,
    onItemLongClick: (Int, ResultModel) -> Unit,
    onMoreClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState()
) {
    LazyColumn(
        state = listState,
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        itemsIndexed(
            items = results,
            key = { index, item -> "${item.mode}_${index}_${item.dic}" }
        ) { index, result ->
            when (result.mode) {
                ResultModel.Mode.WORD -> WordResultItem(
                    result = result,
                    onClick = { onItemClick(index, result) },
                    onLongClick = { onItemLongClick(index, result) }
                )
                ResultModel.Mode.MORE -> MoreResultItem(
                    onClick = { onMoreClick(index) }
                )
                ResultModel.Mode.NONE -> NoneResultItem(result = result)
                ResultModel.Mode.NORESULT -> NoResultItem()
                ResultModel.Mode.FOOTER -> FooterResultItem(result = result)
            }
        }
    }
}
