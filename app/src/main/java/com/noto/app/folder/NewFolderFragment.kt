package com.noto.app.folder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.noto.app.R
import com.noto.app.components.android.AndroidViewSlider
import com.noto.app.components.material.*
import com.noto.app.components.model.NotoColorItem
import com.noto.app.components.screen.Screen
import com.noto.app.domain.model.*
import com.noto.app.fold
import com.noto.app.settings.SettingsItem
import com.noto.app.settings.SettingsItemType
import com.noto.app.theme.NotoTheme
import com.noto.app.theme.toColor
import com.noto.app.util.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class NewFolderFragment : Fragment() {

    private val viewModel by viewModel<NewFolderViewModel> { parametersOf(args.folderId) }

    private val args by navArgs<NewFolderFragmentArgs>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = context?.let { context ->
        setupMixedTransitions()
        activity?.onBackPressedDispatcher?.addCallback { navController?.navigateUp() }
        navController?.currentBackStackEntry?.savedStateHandle
            ?.getLiveData<Long>(Constants.FolderId)
            ?.observe(viewLifecycleOwner) { folderId -> viewModel.setParentFolder(folderId) }

        ComposeView(context).apply {
            isTransitionGroup = true
            setContent {
                val state by viewModel.state.collectAsState()
                val folder by viewModel.folder.collectAsState()
                val folderTitle = remember(folder.title) { folder.getTitle(context) }
                val titleStatus by viewModel.titleStatus.collectAsState()
                val keyboardOptions = remember { KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Done) }
                val notoColorsState = rememberLazyListState()
                val notoColors = remember { NotoColor.entries.toList() }
                val parentFolderTitle = remember(folder.parentFolder) { folder.parentFolder?.getTitle(context) }
                val focusRequester = remember { FocusRequester() }
                val focusManager = LocalFocusManager.current

                Screen(
                    title = if (args.folderId == 0L) stringResource(id = R.string.new_folder) else stringResource(id = R.string.edit_folder),
                    color = if (args.folderId == 0L) MaterialTheme.colorScheme.onBackground else folder.color.toColor(),
                    bottomBar = {
                        NotoButton(
                            text = if (args.folderId == 0L) stringResource(R.string.create_folder) else stringResource(R.string.update_folder),
                            onClick = viewModel::createOrUpdateFolder,
                            containerColor = if (args.folderId == 0L) MaterialTheme.colorScheme.primary else folder.color.toColor(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(NotoTheme.dimensions.medium)
                        )
                    },
                ) {
                    if (args.folderId != Folder.GeneralFolderId) {
                        NotoTextField(
                            value = folderTitle,
                            onValueChange = viewModel::setTitle,
                            placeholder = stringResource(id = R.string.title),
                            status = titleStatus,
                            keyboardOptions = keyboardOptions,
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequester),
                        )
                    }

                    DisposableEffect(Unit) {
                        if (args.folderId == 0L) focusRequester.requestFocus()
                        onDispose { focusManager.clearFocus() }
                    }

                    SectionTitle(title = stringResource(id = R.string.color))

                    LazyRow(
                        state = notoColorsState,
                        contentPadding = PaddingValues(NotoTheme.dimensions.small),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(NotoTheme.dimensions.medium),
                        modifier = Modifier.background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.small),
                    ) {
                        items(notoColors) { notoColor ->
                            NotoColorItem(
                                notoColor = notoColor,
                                isSelected = notoColor == folder.color,
                                onClick = viewModel::setNotoColor,
                            )
                        }
                    }

                    LaunchedEffect(Unit) {
                        notoColorsState.animateScrollToItem(folder.color.ordinal)
                    }

                    if (args.folderId != Folder.GeneralFolderId) {
                        SectionTitle(title = stringResource(id = R.string.parent_folder))
                        SettingsItem(
                            title = parentFolderTitle ?: stringResource(id = R.string.none),
                            type = SettingsItemType.None,
                            painter = if (folder.parentFolder != null) painterResource(id = R.drawable.ic_round_folder_24) else painterResource(id = R.drawable.ic_round_none_24),
                            onClick = {
                                navController?.navigateSafely(
                                    NewFolderFragmentDirections.actionNewFolderFragmentToSelectFolderDialogFragment(
                                        filteredFolderIds = longArrayOf(Folder.GeneralFolderId, args.folderId),
                                        selectedFolderId = (folder.parentFolder?.id ?: 0L),
                                        isNoneEnabled = true,
                                        title = context.stringResource(R.string.parent_folder),
                                        isMainFoldersEnabled = !folder.isVaulted && !folder.isArchived,
                                        isVaultedFoldersEnabled = folder.isVaulted,
                                        isArchivedFoldersEnabled = folder.isArchived,
                                        isChildFoldersEnabled = false,
                                    )
                                )
                            }
                        )
                    }

                    SectionTitle(title = stringResource(id = R.string.layout))

                    NotoTabRow(selectedTabIndex = folder.layout.ordinal) {
                        NotoLeadingIconTab(
                            selected = folder.layout == Layout.Linear,
                            onClick = { viewModel.setLayout(Layout.Linear) },
                            text = stringResource(id = R.string.list),
                            painter = painterResource(id = R.drawable.ic_round_view_agenda_24),
                        )

                        NotoLeadingIconTab(
                            selected = folder.layout == Layout.Grid,
                            onClick = { viewModel.setLayout(Layout.Grid) },
                            text = stringResource(id = R.string.grid),
                            painter = painterResource(id = R.drawable.ic_round_view_grid_24),
                        )
                    }

                    SectionTitle(title = stringResource(id = R.string.new_note_cursor_position))

                    NotoTabRow(selectedTabIndex = folder.newNoteCursorPosition.ordinal) {
                        NotoLeadingIconTab(
                            selected = folder.newNoteCursorPosition == NewNoteCursorPosition.Body,
                            onClick = { viewModel.setNewNoteCursorPosition(NewNoteCursorPosition.Body) },
                            text = stringResource(id = R.string.body),
                            painter = painterResource(id = R.drawable.ic_round_body_24),
                        )

                        NotoLeadingIconTab(
                            selected = folder.newNoteCursorPosition == NewNoteCursorPosition.Title,
                            onClick = { viewModel.setNewNoteCursorPosition(NewNoteCursorPosition.Title) },
                            text = stringResource(id = R.string.title),
                            painter = painterResource(id = R.drawable.ic_round_title_24),
                        )
                    }

                    SectionTitle(title = stringResource(id = R.string.open_notes_in))

                    NotoTabRow(selectedTabIndex = folder.openNotesIn.ordinal) {
                        NotoLeadingIconTab(
                            selected = folder.openNotesIn == OpenNotesIn.Editor,
                            onClick = { viewModel.setOpenNotesIn(OpenNotesIn.Editor) },
                            text = stringResource(id = R.string.editor),
                            painter = painterResource(id = R.drawable.ic_round_editor_24),
                        )

                        NotoLeadingIconTab(
                            selected = folder.openNotesIn == OpenNotesIn.ReadingMode,
                            onClick = { viewModel.setOpenNotesIn(OpenNotesIn.ReadingMode) },
                            text = stringResource(id = R.string.reading_mode),
                            painter = painterResource(id = R.drawable.ic_round_reading_mode_24),
                        )
                    }

                    SectionTitle(title = stringResource(id = R.string.note_preview_size))

                    AndroidViewSlider(
                        value = folder.notePreviewSize.toFloat(), // For some reason, the value doesn't update while state property directly.
                        onValueChange = viewModel::setNotePreviewSize,
                        contentDescription = stringResource(id = R.string.note_preview_size),
                        labelFormatter = { it.toInt().toString() },
                        valueRange = NotePreviewSizeRange,
                        stepSize = NotePreviewSizeStepSize,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    SettingsItem(
                        title = stringResource(id = R.string.show_note_creation_date),
                        type = SettingsItemType.Switch(folder.isShowNoteCreationDate, MaterialTheme.colorScheme.primary),
                        onClick = viewModel::toggleIsShowNoteCreationDate,
                    )
                }

                LaunchedEffect(state) {
                    state.fold(
                        onFailure = { exception ->
                            when (exception) {
                                NotoException.Model.TitleIsRequired -> {
                                    focusManager.clearFocus()
                                    focusRequester.requestFocus()
                                    viewModel.setTitleStatus(TextFieldStatus.Error(R.string.title_is_required))
                                }

                                else -> {}
                            }
                        },
                        onSuccess = { folderId ->
                            updatePinnedShortcut(folder)
                            context.updateAllWidgetsData()
                            context.updateFolderListWidgets()
                            context.updateNoteListWidgets()
                            if (args.folderId == 0L) {
                                navController?.navigateSafely(NewFolderFragmentDirections.actionNewFolderFragmentToFolderFragment(folderId))
                            } else {
                                navController?.navigateUp()
                            }
                        }
                    )
                }
            }
        }
    }

    companion object {
        private val NotePreviewSizeRange = 0F..30F
        private const val NotePreviewSizeStepSize = 1F
    }

    private fun updatePinnedShortcut(folder: Folder) {
        context?.let { context ->
            ShortcutManagerCompat.updateShortcuts(context, listOf(context.createPinnedShortcut(folder)))
        }
    }
}