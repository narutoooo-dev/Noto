package com.noto.app.util

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNamingStrategy

@OptIn(ExperimentalSerializationApi::class)
val NotoDefaultJson = Json {
    isLenient = true
    allowStructuredMapKeys = true
    coerceInputValues = true
    ignoreUnknownKeys = true
    explicitNulls = false
    prettyPrint = true
}

val RemoteJson = Json(NotoDefaultJson) {
    namingStrategy = JsonNamingStrategy.SnakeCase
    encodeDefaults = false
}

val ExportImportDataJson = Json {
    isLenient = true
    allowStructuredMapKeys = true
    coerceInputValues = true
    ignoreUnknownKeys = true
    explicitNulls = false
    prettyPrint = true
    encodeDefaults = true
}