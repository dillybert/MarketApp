package kz.market.presentation.screens.storage

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import kz.market.domain.models.Product
import kz.market.utils.UIGetState


sealed class StorageTab(
    val title: String,
    val icon: ImageVector? = null,
    val content: @Composable () -> Unit
) {
    object QuickAddProduct : StorageTab(
        title = "Быстрое добавление",
        icon = null,
        content = { QuickAddScreen() }
    )

    object RegisterArrival : StorageTab(
        title = "Оформить приход",
        icon = null,
        content = { RegisterArrivalScreen() }
    )
}


@Composable
fun QuickAddScreen(modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier
    ) {
        items(100) {
            Text(text = "Item $it")
        }
    }
}


@Composable
fun RegisterArrivalScreen(modifier: Modifier = Modifier) {
    Text(
        text = "Register Arrival Screen",
        modifier = modifier
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StorageAddProductScreen(
    viewModel: StorageViewModel = hiltViewModel()
) {
    val tabNames = listOf(
        StorageTab.QuickAddProduct,
        StorageTab.RegisterArrival
    )

    val horizontalPagerState = rememberPagerState(
        initialPage = 0,
        initialPageOffsetFraction = 0f,
        pageCount = { tabNames.size }
    )
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Добавление товара")
                },
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            PrimaryTabRow (
                selectedTabIndex = horizontalPagerState.currentPage
            ) {
                tabNames.forEachIndexed { index, tab ->
                    Tab(
                        selected = horizontalPagerState.currentPage == index,
                        onClick = {
                            scope.launch {
                                horizontalPagerState.animateScrollToPage(index)
                            }
                        },
                        text = { Text(text = tab.title) },
                        icon = tab.icon?.let { { Icon(it, contentDescription = tab.title) } },
                        unselectedContentColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalPager(
                state = horizontalPagerState,
                modifier = Modifier
                    .fillMaxSize()
            ) { page ->
                tabNames[page].content()
            }
        }
    }
}


@Preview(
    showBackground = true,
    showSystemUi = true,
    device = "spec:width=411dp,height=891dp"
)
@Composable
private fun StorageAddScreenPreview() {
    StorageAddProductScreen()
}