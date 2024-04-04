package com.noto.app.ui.util

import android.app.PendingIntent
import android.os.Build

val PendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
else
    PendingIntent.FLAG_UPDATE_CURRENT

val MutablePendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
else
    PendingIntent.FLAG_UPDATE_CURRENT

object IntentConstants {
    const val ActionCreateFolder = "com.noto.intent.action.CREATE_FOLDER"
    const val ActionCreateNote = "com.noto.intent.action.CREATE_NOTE"
    const val ActionQuickNote = "com.noto.intent.action.QUICK_NOTE"
    const val ActionOpenFolder = "com.noto.intent.action.OPEN_FOLDER"
    const val ActionOpenNote = "com.noto.intent.action.OPEN_NOTE"
    const val ActionOpenVault = "com.noto.intent.action.OPEN_VAULT"
    const val ActionSettings = "com.noto.intent.action.SETTINGS"
}