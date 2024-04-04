package com.noto.app.data.settings.model

import com.noto.app.domain.*
import kotlinx.serialization.Serializable

@Serializable
data class LocalSettingsConfig(
    val theme: Theme? = null,
    val font: Font? = null,
    val language: Language? = null,
    val icon: Icon? = null,
    val vaultPasscode: String? = null,
    val sortingType: FolderListSortingType? = null,
    val sortingOrder: SortingOrder? = null,
    val isShowNotesCount: Boolean? = null,
    val isDoNotDisturb: Boolean? = null,
    val isScreenOn: Boolean? = null,
    val isFullScreen: Boolean? = null,
    val mainInterfaceId: Long? = null,
    val quickNoteFolderId: Long? = null,
    val isRememberScrollingPosition: Boolean? = null,
    val screenBrightnessLevel: ScreenBrightnessLevel? = null,
    val quickExist: Boolean? = null,
    val continuousSearch: Boolean? = null,
    val previewAutoScroll: Boolean? = null,
)