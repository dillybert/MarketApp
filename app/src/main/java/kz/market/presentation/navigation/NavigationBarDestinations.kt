package kz.market.presentation.navigation

import kz.market.R

sealed class NavigationBarDestinations(
    val route: ApplicationDestination,
    val icon: Int,
    val label: String
) {
    object Dashboard : NavigationBarDestinations(DashboardMain, R.drawable.ic_dashboard, "Панель")
    object ProductSales : NavigationBarDestinations(ProductSalesMain, R.drawable.ic_bag, "Продажи")
    object Reports : NavigationBarDestinations(ReportsMain, R.drawable.ic_chart, "Отчёты")
    object Storage : NavigationBarDestinations(StorageMain, R.drawable.ic_package_add, "Склад")

    companion object {
        val tabs = listOf(Dashboard, ProductSales, Reports, Storage)
    }
}