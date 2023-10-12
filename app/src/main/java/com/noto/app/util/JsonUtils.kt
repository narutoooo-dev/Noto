package com.noto.app.util

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNamingStrategy

@OptIn(ExperimentalSerializationApi::class)
val NotoDefaultJson = Json {
    isLenient = true
    allowStructuredMapKeys = true
    coerceInputValues = true
    encodeDefaults = true
    ignoreUnknownKeys = true
    explicitNulls = false
    prettyPrint = true
}

val RemoteJson = Json(NotoDefaultJson) {
    namingStrategy = JsonNamingStrategy.SnakeCase
}