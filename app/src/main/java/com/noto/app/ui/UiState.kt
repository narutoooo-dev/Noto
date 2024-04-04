package com.noto.app.ui

import com.noto.app.domain.NotoException
import com.noto.app.ui.UiState.*

sealed interface UiState<out T> {
    data object Empty : UiState<Nothing>
    data object Loading : UiState<Nothing>
    data class Success<T>(val value: T) : UiState<T>
    data class Failure(val exception: NotoException) : UiState<Nothing>

    val isEmpty: Boolean
        get() = this is Empty

    val isLoading: Boolean
        get() = this is Loading

    val isSuccess: Boolean
        get() = this is Success

    val isFailure: Boolean
        get() = this is Failure
}

inline fun <T, R> UiState<T>.map(transform: (value: T) -> R): UiState<R> = when (this) {
    is Empty -> Empty
    is Loading -> Loading
    is Success -> Success(transform(value))
    is Failure -> Failure(exception)
}

fun <T> UiState<T>.getOrDefault(defaultValue: T) = when (this) {
    is Empty -> defaultValue
    is Loading -> defaultValue
    is Success -> value
    is Failure -> defaultValue
}

inline fun <T> UiState<T>.fold(
    onEmpty: () -> Unit = {},
    onLoading: () -> Unit = {},
    onSuccess: (T) -> Unit = {},
    onFailure: (NotoException) -> Unit = {},
): UiState<T> {
    when (this) {
        is Empty -> onEmpty()
        is Loading -> onLoading()
        is Success -> onSuccess(value)
        is Failure -> onFailure(exception)
    }
    return this
}

fun <T> Result<T>.toUiState() = fold(
    onSuccess = { Success(it) },
    onFailure = { Failure(it as? NotoException ?: NotoException.Unknown(it.message)) },
)