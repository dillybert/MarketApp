package kz.market.presentation.navigation

import androidx.annotation.Keep
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable
import kz.market.presentation.screens.dashboard.DashboardDetailsScreen
import kz.market.presentation.screens.dashboard.DashboardScreen
import kz.market.presentation.screens.product.ProductSalesDetailsScreen
import kz.market.presentation.screens.product.ProductSalesScreen
import kz.market.presentation.screens.reports.ReportsDetailsScreen
import kz.market.presentation.screens.reports.ReportsScreen
import kz.market.presentation.screens.storage.StorageAddProductScreen
import kz.market.presentation.screens.storage.StorageDetailsContent
import kz.market.presentation.screens.storage.StorageDetailsScreen
import kz.market.presentation.screens.storage.StorageScreen

interface ApplicationDestination

@Serializable @Keep object MainRoot : ApplicationDestination
@Serializable @Keep object DetailsRoot : ApplicationDestination
@Serializable @Keep object InnerRoot : ApplicationDestination

@Serializable @Keep object DashboardMain : ApplicationDestination
@Serializable @Keep object DashboardDetails : ApplicationDestination

@Serializable @Keep object ProductSalesMain : ApplicationDestination
@Serializable @Keep data class ProductSalesDetails(val id: Int) : ApplicationDestination

@Serializable @Keep object ReportsMain : ApplicationDestination
@Serializable @Keep object ReportsDetails : ApplicationDestination

@Serializable @Keep object StorageMain : ApplicationDestination
@Serializable @Keep data class StorageDetails(val barcode: String) : ApplicationDestination
@Serializable @Keep object StorageAddProduct : ApplicationDestination


@Composable
fun ApplicationNavGraph(
    modifier: Modifier,
    navController: NavHostController
) {
    NavHost(
        modifier = modifier,
        navController = navController,
        route = MainRoot::class,
        startDestination = StorageMain::class,

        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None  }
    ) {
        composable<DashboardMain> {
            DashboardScreen(
                onDetailsClick = {
                    navController.navigate(DashboardDetails)
                }
            )
        }
        composable<ProductSalesMain> {
            ProductSalesScreen(
                onDetailsClick = { id ->
                    navController.navigate(ProductSalesDetails(id))
                }
            )
        }
        composable<ReportsMain> {
            ReportsScreen(
                onDetailsClick = {
                    navController.navigate(ReportsDetails)
                }
            )
        }
        composable<StorageMain> {
            StorageScreen(
                onProductDetailsClick = { barcode ->
                    navController.navigate(StorageDetails(barcode))
                },
                onProductAddButtonClick = {
                    navController.navigate(StorageAddProduct)
                }
            )
        }

        detailsNavGraph(navController)
        innerNavGraph(navController)
    }
}


fun NavGraphBuilder.detailsNavGraph(
    navController: NavHostController
) {
    navigation<DetailsRoot>(
        startDestination = DashboardDetails
    ) {
        composable<DashboardDetails> {
            DashboardDetailsScreen()
        }
        composable<ProductSalesDetails> { entry ->
            val productSalesDetailsId = entry.toRoute<ProductSalesDetails>()
            ProductSalesDetailsScreen(productSalesDetailsId.id)
        }
        composable<ReportsDetails> {
            ReportsDetailsScreen()
        }
        composable<StorageDetails> { entry ->
            val storageDetails = entry.toRoute<StorageDetails>()

            StorageDetailsScreen(barcode = storageDetails.barcode)
        }
    }
}

fun NavGraphBuilder.innerNavGraph(
    navController: NavHostController
) {
    navigation<InnerRoot>(
        startDestination = StorageAddProduct
    ) {
        composable<StorageAddProduct> {
            StorageAddProductScreen(
                onDetailsClick = { barcode ->
                    navController.navigate(StorageDetails(barcode = barcode))
                }
            )
        }
    }
}

