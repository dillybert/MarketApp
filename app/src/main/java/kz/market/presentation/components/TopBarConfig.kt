package kz.market.presentation.components

import androidx.compose.runtime.Composable

data class TopBarConfig(
    val title: String,
    val iconSet: @Composable () -> Unit = {}
)