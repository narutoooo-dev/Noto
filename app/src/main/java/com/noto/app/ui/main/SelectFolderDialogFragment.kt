package com.noto.app.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.fragment.navArgs
import com.noto.app.R
import com.noto.app.domain.FilteredItem
import com.noto.app.domain.isGeneral
import com.noto.app.ui.component.dialog.BaseDialogFragment
import com.noto.app.ui.component.dialog.LazyBottomSheetDialog
import com.noto.app.ui.component.material.ScreenCircularProgressIndicator
import com.noto.app.ui.component.model.FilteredItem
import com.noto.app.ui.component.model.FolderItem
import com.noto.app.ui.component.util.HeaderItem
import com.noto.app.ui.component.util.NoneItem
import com.noto.app.ui.component.util.NoneItemId
import com.noto.app.ui.component.util.PlaceholderItem
import com.noto.app.ui.fold
import com.noto.app.ui.theme.NotoTheme
import com.noto.app.ui.util.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class SelectFolderDialogFragment() : BaseDialogFragment(isCollapsable = true) {

    private var onClick: (Long, String) -> Unit = { _, _ -> }

    constructor(onClick: (Long, String) -> Unit = { _, _ -> }) : this() {
        this.onClick = onClick
    }

    private val viewModel by viewModel<MainViewModel>()

    private val args by navArgs<SelectFolderDialogFragmentArgs>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? = context?.let { context ->
        if (!args.isDismissible) {
            dialog?.setCanceledOnTouchOutside(false)
            dialog?.setOnCancelListener { activity?.finish() }
        }

        ComposeView(context).apply {
            setContent {
                val state by viewModel.allFolders.collectAsState()
                val isShowNotesCount by viewModel.isShowNotesCount.collectAsState()
                val notesCount by viewModel.notesCount.collectAsState()
                val isVaultOpen by viewModel.isVaultOpen.collectAsState()

                LazyBottomSheetDialog(
                    title = args.title,
                    painter = if (args.isFilteredEnabled) painterResource(id = R.drawable.ic_round_reset_24) else null,
                    iconContentDescription = if (args.isFilteredEnabled) stringResource(id = R.string.reset) else args.title,
                    onIconClick = if (args.isFilteredEnabled) {
                        {
                            returnResult(FilteredItem.AllFoldersId, context.stringResource(R.string.default_main_interface))
                        }
                    } else {
                        null
                    },
                ) {
                    state.fold(
                        onLoading = { item { ScreenCircularProgressIndicator() } },
                        onSuccess = { allFolders ->
                            val allFoldersItems = allFolders.mapRecursivelyToFolderItem { folder, depth, childItems ->
                                val isEnabled = if (args.filteredFolderIds.isEmpty()) true else folder.id !in args.filteredFolderIds
                                FolderItem(
                                    folder = folder,
                                    isSelected = folder.id == args.selectedFolderId,
                                    isEnabled = isEnabled,
                                    depth = depth,
                                    childItems = childItems.mapRecursively {
                                        it.copy(isEnabled = if (isEnabled) it.isEnabled else args.isChildFoldersEnabled)
                                    },
                                )
                            }
                            val nonGeneralFoldersItem = allFoldersItems.filterNot { it.folder.isGeneral }
                            val mainFoldersItems = nonGeneralFoldersItem.filter { !it.folder.isVaulted && !it.folder.isArchived }
                            val vaultedFoldersItems = nonGeneralFoldersItem.filter { it.folder.isVaulted }
                            val archivedFoldersItems = nonGeneralFoldersItem.filter { it.folder.isArchived }
                            val generalFolderItem = allFoldersItems.firstOrNull { it.folder.isGeneral }

                            if (args.isNoneEnabled) {
                                item {
                                    NoneItem(
                                        isSelected = args.selectedFolderId == NoneItemId,
                                        onClick = { returnResult(NoneItemId, context.stringResource(R.string.none)) },
                                    )
                                }
                            }

                            if (args.isFilteredEnabled) {
                                item {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(NotoTheme.dimensions.medium),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(NotoTheme.dimensions.medium),
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            FilteredItem(
                                                item = FilteredItem.All,
                                                notesCount = notesCount.all,
                                                isSelected = FilteredItem.All.id == args.selectedFolderId,
                                                isShowNotesCount = isShowNotesCount,
                                                onClick = { returnResult(FilteredItem.All.id, context.stringResource(R.string.all)) },
                                                modifier = Modifier.weight(1F),
                                            )

                                            FilteredItem(
                                                item = FilteredItem.Recent,
                                                notesCount = notesCount.recent,
                                                isSelected = FilteredItem.Recent.id == args.selectedFolderId,
                                                isShowNotesCount = isShowNotesCount,
                                                onClick = { returnResult(FilteredItem.Recent.id, context.stringResource(R.string.recent)) },
                                                modifier = Modifier.weight(1F),
                                            )
                                        }

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(NotoTheme.dimensions.medium),
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            FilteredItem(
                                                item = FilteredItem.Scheduled,
                                                notesCount = notesCount.scheduled,
                                                isSelected = FilteredItem.Scheduled.id == args.selectedFolderId,
                                                isShowNotesCount = isShowNotesCount,
                                                onClick = {
                                                    returnResult(
                                                        FilteredItem.Scheduled.id,
                                                        context.stringResource(R.string.scheduled)
                                                    )
                                                },
                                                modifier = Modifier.weight(1F),
                                            )

                                            FilteredItem(
                                                item = FilteredItem.Archived,
                                                notesCount = notesCount.archived,
                                                isSelected = FilteredItem.Archived.id == args.selectedFolderId,
                                                isShowNotesCount = isShowNotesCount,
                                                onClick = { returnResult(FilteredItem.Archived.id, context.stringResource(R.string.archived)) },
                                                modifier = Modifier.weight(1F),
                                            )
                                        }
                                    }
                                }

                                item { Spacer(modifier = Modifier.height(NotoTheme.dimensions.medium)) }
                            }

                            generalFolderItem?.let { item ->
                                item {
                                    FolderItem(
                                        item = item,
                                        isShowNotesCount = isShowNotesCount,
                                        onClick = { returnResult(it.folder.id, it.folder.getTitle(context)) }
                                    )
                                }
                            }

                            if (allFoldersItems.isEmpty() && generalFolderItem == null && !args.isNoneEnabled) {
                                item {
                                    PlaceholderItem(
                                        placeholder = if (allFoldersItems.isEmpty())
                                            stringResource(id = R.string.no_folders_found)
                                        else
                                            stringResource(id = R.string.no_relevant_folders_found)
                                    )
                                }
                            } else {
                                if (args.isMainFoldersEnabled) {
                                    val (pinnedMainFoldersItems, notPinnedFolderItems) = mainFoldersItems.partition { it.folder.isPinned }

                                    if (pinnedMainFoldersItems.isNotEmpty()) {
                                        item {
                                            HeaderItem(title = stringResource(id = R.string.pinned))
                                        }

                                        items(pinnedMainFoldersItems) { item ->
                                            FolderItem(
                                                item = item,
                                                isShowNotesCount = isShowNotesCount,
                                                onClick = { returnResult(it.folder.id, it.folder.getTitle(context)) },
                                            )
                                        }

                                        if (notPinnedFolderItems.isNotEmpty()) {
                                            item {
                                                HeaderItem(title = stringResource(id = R.string.folders))
                                            }
                                        }
                                    }

                                    items(notPinnedFolderItems) { item ->
                                        FolderItem(
                                            item = item,
                                            isShowNotesCount = isShowNotesCount,
                                            onClick = { returnResult(it.folder.id, it.folder.getTitle(context)) },
                                        )
                                    }
                                }

                                if (args.isVaultedFoldersEnabled && vaultedFoldersItems.isNotEmpty() && isVaultOpen) {
                                    item {
                                        HeaderItem(title = stringResource(id = R.string.vaulted))
                                    }

                                    items(vaultedFoldersItems) { item ->
                                        FolderItem(
                                            item = item,
                                            isShowNotesCount = isShowNotesCount,
                                            onClick = { returnResult(it.folder.id, it.folder.getTitle(context)) },
                                        )
                                    }
                                }

                                if (args.isArchivedFoldersEnabled && archivedFoldersItems.isNotEmpty()) {
                                    item {
                                        HeaderItem(title = stringResource(id = R.string.archived))
                                    }

                                    items(archivedFoldersItems) { item ->
                                        FolderItem(
                                            item = item,
                                            isShowNotesCount = isShowNotesCount,
                                            onClick = { returnResult(it.folder.id, it.folder.getTitle(context)) },
                                        )
                                    }
                                }
                            }
                        }
                    )
                }
            }
        }
    }

    private fun returnResult(id: Long, title: String) {
        try {
            navController?.previousBackStackEntry?.savedStateHandle?.apply {
                set(Constants.FolderTitle, title)
                set(args.key ?: Constants.FolderId, id)
            }
            onClick(id, title)
        } catch (exception: IllegalStateException) {
            onClick(id, title)
        }
        dismiss()
    }
}