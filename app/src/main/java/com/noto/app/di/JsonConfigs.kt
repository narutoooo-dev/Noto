package com.noto.app.di

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNamingStrategy

@OptIn(ExperimentalSerializationApi::class)
data object JsonConfigs {

    val Remote = Json {
        namingStrategy = JsonNamingStrategy.SnakeCase
        isLenient = true
        allowStructuredMapKeys = true
        coerceInputValues = true
        ignoreUnknownKeys = true
        explicitNulls = false
        prettyPrint = true
    }

    val ExportImportData = Json {
        isLenient = true
        allowStructuredMapKeys = true
        coerceInputValues = true
        ignoreUnknownKeys = true
        explicitNulls = false
        prettyPrint = true
        encodeDefaults = true
    }

    val Crypto = Json {
        namingStrategy = JsonNamingStrategy.SnakeCase
        isLenient = true
        prettyPrint = true
    }

}