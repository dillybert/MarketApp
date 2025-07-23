package kz.market.presentation.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kz.market.presentation.navigation.ApplicationNavGraph
import kz.market.presentation.navigation.DashboardMain
import kz.market.presentation.navigation.NavigationBarDestinations
import kz.market.presentation.navigation.ProductSalesMain
import kz.market.presentation.navigation.ReportsMain
import kz.market.presentation.navigation.StorageMain
import kz.market.presentation.theme.MarketTheme
import kz.market.utils.update.UpdateDialog
import kz.market.utils.update.UpdateViewModel

@Composable
fun MarketApp() {
    MarketTheme {
        val updateViewModel: UpdateViewModel = hiltViewModel()
        val info by updateViewModel.updateInfo.collectAsState()
        var showUpdateDialog by remember { mutableStateOf(true) }

        val rootNavController = rememberNavController()
        val currentBackStack by rootNavController.currentBackStackEntryAsState()
        val currentDestination = currentBackStack?.destination

        val bottomNavScreens = listOf(
            DashboardMain,
            ProductSalesMain,
            ReportsMain,
            StorageMain
        )

        val showBottomBar = bottomNavScreens.any { it::class.qualifiedName == currentDestination?.route }

        Scaffold(
            bottomBar = {
                AnimatedVisibility(
                    visible = showBottomBar,
                    enter = slideInVertically { it },
                    exit = slideOutVertically { it },
                    content = {
                        NavigationBar {
                            NavigationBarDestinations.tabs.forEach { destination ->
                                NavigationBarItem(
                                    icon = {
                                        Icon(
                                            painter = painterResource(destination.icon),
                                            tint = MaterialTheme.colorScheme.secondary,
                                            contentDescription = destination.label
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = destination.label,
                                            textAlign = TextAlign.Center
                                        )
                                    },
                                    selected = currentDestination?.hierarchy?.any {
                                        it.hasRoute(destination.route::class)
                                    } == true,
                                    onClick = {
                                        rootNavController.navigate(destination.route) {
                                            popUpTo(rootNavController.graph.startDestinationId) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                )
                            }
                        }
                    }
                )
            }
        ) { innerPadding ->
            ApplicationNavGraph(
                modifier = Modifier
                    .consumeWindowInsets(innerPadding)
                    .padding(innerPadding),
                navController = rootNavController
            )

            if (info != null && showUpdateDialog) {
                UpdateDialog(
                    info = info!!,
                    onConfirm = {
                        updateViewModel.confirmUpdate()
                        showUpdateDialog = false
                    },
                    onDismiss = {
                        updateViewModel.dismissDialog()
                        showUpdateDialog = false
                    }
                )
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
private fun MarketAppPreview() {
    MarketApp()
}