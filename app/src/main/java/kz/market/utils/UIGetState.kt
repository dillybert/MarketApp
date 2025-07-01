package kz.market.utils

sealed interface UIGetState<out T> {
    object Loading : UIGetState<Nothing>
    data class Success<T>(val data: T) : UIGetState<T>
    data class Error(val message: String) : UIGetState<Nothing>
    object Empty : UIGetState<Nothing>
}

