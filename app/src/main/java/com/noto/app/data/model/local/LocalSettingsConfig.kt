package com.noto.app.data.model.local

import com.noto.app.domain.model.*
import kotlinx.serialization.Serializable

@Serializable
data class LocalSettingsConfig(
    val theme: Theme,
    val font: Font,
    val language: Language,
    val icon: Icon,
    val vaultPasscode: String?,
    val sortingType: FolderListSortingType,
    val sortingOrder: SortingOrder,
    val isShowNotesCount: Boolean,
    val isDoNotDisturb: Boolean,
    val isScreenOn: Boolean,
    val isFullScreen: Boolean,
    val mainInterfaceId: Long,
    val quickNoteFolderId: Long,
    val isRememberScrollingPosition: Boolean,
    val screenBrightnessLevel: ScreenBrightnessLevel,
    val quickExist: Boolean,
    val continuousSearch: Boolean,
    val previewAutoScroll: Boolean,
)