package com.noto.app.data.model.local

import kotlinx.serialization.Serializable

@Serializable
data class LocalNotoData(
    val folders: List<LocalFolder>,
    val notes: List<LocalNote>,
    val labels: List<LocalLabel>,
    val noteLabels: List<LocalNoteLabel>,
    val settings: LocalSettingsConfig,
)