package com.noto.app.intro

import com.noto.app.R
import com.noto.app.domain.model.NotoColor

enum class IntroPage(
    val titleStringId: Int,
    val descriptionStringId: Int,
    val imageDrawableId: Int,
    val notoColor: NotoColor,
    val extrasStringIds: List<Int> = emptyList(),
) {
    Start(
        R.string.intro_discover_features_title,
        R.string.intro_discover_features_description,
        R.drawable.illustration_features,
        NotoColor.General,
    ),
    AdFree(
        R.string.intro_ad_free_title,
        R.string.intro_ad_free_description,
        R.drawable.illustration_ads,
        NotoColor.Pink,
    ),
    OpenSource(
        R.string.intro_open_source_title,
        R.string.intro_open_source_description,
        R.drawable.illustration_open_source,
        NotoColor.Green,
    ),
    Organization(
        R.string.intro_organization_title,
        R.string.intro_organization_description,
        R.drawable.illustration_organization,
        NotoColor.Blue,
        listOf(
            R.string.intro_labels,
            R.string.intro_archiving,
            R.string.intro_colorful_folders,
            R.string.intro_label_filtering,
            R.string.intro_pinning,
            R.string.intro_grouping,
            R.string.intro_sorting,
            R.string.intro_manual_ordering,
            R.string.intro_layouts,
            R.string.intro_actions,
        ),
    ),
    MultiSelection(
        R.string.intro_multi_selection_title,
        R.string.intro_multi_selection_description,
        R.drawable.illustration_multi_selection,
        NotoColor.Yellow,
    ),
    Search(
        R.string.intro_search_title,
        R.string.intro_search_description,
        R.drawable.illustration_search,
        NotoColor.BlueGray,
    ),
    ReadingMode(
        R.string.intro_reading_mode_title,
        R.string.intro_reading_mode_description,
        R.drawable.illustration_reading_mode,
        NotoColor.ReadingMode,
    ),
    UndoRedo(
        R.string.intro_undo_redo_title,
        R.string.intro_undo_redo_description,
        R.drawable.illustration_undo_redo,
        NotoColor.Cyan,
    ),
    Reminders(
        R.string.intro_reminders_title,
        R.string.intro_reminders_description,
        R.drawable.illustration_reminders,
        NotoColor.Red,
    ),
    Vault(
        R.string.intro_vault_title,
        R.string.intro_vault_description,
        R.drawable.illustration_vault,
        NotoColor.Vault,
    ),
    Other(
        R.string.intro_other_title,
        R.string.intro_other_description,
        R.drawable.illustration_other,
        NotoColor.DeepGreen,
        listOf(
            R.string.intro_widgets,
            R.string.intro_auto_save,
            R.string.intro_quick_note,
            R.string.intro_custom_app_icons,
            R.string.intro_nested_folders,
            R.string.intro_shortcuts,
            R.string.intro_design,
            R.string.intro_private_secure,
            R.string.intro_telegram_community,
            R.string.intro_auto_backup,
        ),
    ),
    Cloud(
        R.string.intro_cloud_title,
        R.string.intro_cloud_description,
        R.drawable.illustration_cloud,
        NotoColor.Account,
    ),
    Setup(
        R.string.intro_setup,
        R.string.intro_setup_description,
        R.drawable.illustration_setup,
        NotoColor.General,
    );

    companion object {
        val Initial = Start
        val Count = entries.count()
        fun ofOrdinal(ordinal: Int) = entries.first { it.ordinal == ordinal }
    }
}