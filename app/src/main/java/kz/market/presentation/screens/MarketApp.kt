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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
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

@Composable
fun MarketApp() {
    MarketTheme {
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
        }
    }
}