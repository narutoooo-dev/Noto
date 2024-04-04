package com.noto.app.data.util

import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import io.ktor.http.isSuccess

suspend inline fun <reified T> HttpResponse.getOrElse(callback: (HttpResponse) -> T): T {
    return if (status.isSuccess()) {
        body<T>()
    } else {
        callback(this)
    }
}

fun unhandledError(message: String): Nothing = error("Unhandled error: $message")