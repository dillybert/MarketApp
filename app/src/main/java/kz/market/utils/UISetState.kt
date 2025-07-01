package kz.market.utils

sealed interface UISetState {
    object Idle : UISetState
    object Loading : UISetState
    object Success : UISetState
    data class Error(val message: String) : UISetState
}