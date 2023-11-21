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
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.noto.app.R
import com.noto.app.components.*
import com.noto.app.domain.model.Folder
import com.noto.app.domain.model.Layout
import com.noto.app.domain.model.NewNoteCursorPosition
import com.noto.app.domain.model.OpenNotesIn
import com.noto.app.settings.SettingsItem
import com.noto.app.settings.SettingsItemType
import com.noto.app.theme.NotoTheme
import com.noto.app.theme.toColor
import com.noto.app.util.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class NewFolderFragment : Fragment() {

    private val viewModel by viewModel<FolderViewModel> { parametersOf(args.folderId) }

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
                val folder by viewModel.folder.collectAsState()
                val notoColorsState = rememberLazyListState()
                val notoColorsItems by viewModel.notoColors.collectAsState()
                var title by remember(folder) { mutableStateOf(folder.getTitle(context)) }
                var layout by remember(folder.layout) { mutableStateOf(folder.layout) }
                var newNoteCursorPosition by remember(folder.newNoteCursorPosition) { mutableStateOf(folder.newNoteCursorPosition) }
                var openNotesIn by remember(folder.openNotesIn) { mutableStateOf(folder.openNotesIn) }
                var notePreviewSize by remember { mutableFloatStateOf(folder.notePreviewSize.toFloat()) }
                var isShowNoteCreationDate by remember(folder.isShowNoteCreationDate) { mutableStateOf(folder.isShowNoteCreationDate) }
                val parentFolder by viewModel.parentFolder.collectAsState()
                val parentFolderTitle = remember(parentFolder) { parentFolder?.getTitle(context) }
                val focusRequester = remember { FocusRequester() }
                val focusManager = LocalFocusManager.current
                var titleStatus by remember { mutableStateOf<TextFieldStatus>(TextFieldStatus.Empty) }

                Screen(
                    title = if (args.folderId == 0L) stringResource(id = R.string.new_folder) else stringResource(id = R.string.edit_folder),
                    color = if (args.folderId == 0L) MaterialTheme.colorScheme.onBackground else folder.color.toColor(),
                    bottomBar = {
                        Button(
                            text = if (args.folderId == 0L) stringResource(R.string.create_folder) else stringResource(R.string.update_folder),
                            onClick = {
                                focusManager.clearFocus()
                                if (title.isBlank() && args.folderId != Folder.GeneralFolderId) {
                                    titleStatus = TextFieldStatus.Error(R.string.empty_title)
                                    focusRequester.requestFocus()
                                } else {
                                    updatePinnedShortcut(folder.copy(title = title, color = notoColorsItems.first { it.isSelected }.notoColor))
                                    viewModel.createOrUpdateFolder(
                                        title = title,
                                        layout = layout,
                                        notePreviewSize = notePreviewSize.toInt(),
                                        newNoteCursorPosition = newNoteCursorPosition,
                                        openNotesIn = openNotesIn,
                                        isShowNoteCreationDate = isShowNoteCreationDate,
                                        onCreateFolder = { folderId ->
                                            navController?.navigateSafely(NewFolderFragmentDirections.actionNewFolderFragmentToFolderFragment(folderId))
                                        },
                                    ).invokeOnCompletion {
                                        context.updateAllWidgetsData()
                                        context.updateFolderListWidgets()
                                        context.updateNoteListWidgets()
                                        if (args.folderId != 0L) navController?.navigateUp()
                                    }
                                }
                            },
                            containerColor = if (args.folderId == 0L) MaterialTheme.colorScheme.primary else folder.color.toColor(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(NotoTheme.dimensions.medium)
                        )
                    },
                ) {
                    if (args.folderId != Folder.GeneralFolderId) {
                        NotoTextField(
                            value = title,
                            onValueChange = { title = it },
                            placeholder = stringResource(id = R.string.title),
                            status = titleStatus,
                            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
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
                        items(notoColorsItems) { item ->
                            NotoColorItem(
                                item = item,
                                onClick = { viewModel.selectNotoColor(item.notoColor) },
                            )
                        }
                    }

                    LaunchedEffect(folder.color.ordinal) {
                        notoColorsState.animateScrollToItem(folder.color.ordinal)
                    }

                    if (args.folderId != Folder.GeneralFolderId) {
                        SectionTitle(title = stringResource(id = R.string.parent_folder))
                        SettingsItem(
                            title = parentFolderTitle ?: stringResource(id = R.string.none),
                            type = SettingsItemType.None,
                            painter = if (parentFolder != null) painterResource(id = R.drawable.ic_round_folder_24) else painterResource(id = R.drawable.ic_round_none_24),
                            onClick = {
                                navController?.navigateSafely(
                                    NewFolderFragmentDirections.actionNewFolderFragmentToSelectFolderDialogFragment(
                                        filteredFolderIds = longArrayOf(Folder.GeneralFolderId, args.folderId),
                                        selectedFolderId = (viewModel.parentFolder.value?.id ?: 0L),
                                        isNoneEnabled = true,
                                        title = context.stringResource(R.string.parent_folder),
                                    )
                                )
                            }
                        )
                    }

                    SectionTitle(title = stringResource(id = R.string.layout))

                    NotoTabRow(selectedTabIndex = layout.ordinal) {
                        NotoLeadingIconTab(
                            selected = layout == Layout.Linear,
                            onClick = { layout = Layout.Linear },
                            text = stringResource(id = R.string.list),
                            painter = painterResource(id = R.drawable.ic_round_view_agenda_24),
                        )

                        NotoLeadingIconTab(
                            selected = layout == Layout.Grid,
                            onClick = { layout = Layout.Grid },
                            text = stringResource(id = R.string.grid),
                            painter = painterResource(id = R.drawable.ic_round_view_grid_24),
                        )
                    }

                    SectionTitle(title = stringResource(id = R.string.new_note_cursor_position))

                    NotoTabRow(selectedTabIndex = newNoteCursorPosition.ordinal) {
                        NotoLeadingIconTab(
                            selected = newNoteCursorPosition == NewNoteCursorPosition.Body,
                            onClick = { newNoteCursorPosition = NewNoteCursorPosition.Body },
                            text = stringResource(id = R.string.body),
                            painter = painterResource(id = R.drawable.ic_round_body_24),
                        )

                        NotoLeadingIconTab(
                            selected = newNoteCursorPosition == NewNoteCursorPosition.Title,
                            onClick = { newNoteCursorPosition = NewNoteCursorPosition.Title },
                            text = stringResource(id = R.string.title),
                            painter = painterResource(id = R.drawable.ic_round_title_24),
                        )
                    }

                    SectionTitle(title = stringResource(id = R.string.open_notes_in))

                    NotoTabRow(selectedTabIndex = openNotesIn.ordinal) {
                        NotoLeadingIconTab(
                            selected = openNotesIn == OpenNotesIn.Editor,
                            onClick = { openNotesIn = OpenNotesIn.Editor },
                            text = stringResource(id = R.string.editor),
                            painter = painterResource(id = R.drawable.ic_round_editor_24),
                        )

                        NotoLeadingIconTab(
                            selected = openNotesIn == OpenNotesIn.ReadingMode,
                            onClick = { openNotesIn = OpenNotesIn.ReadingMode },
                            text = stringResource(id = R.string.reading_mode),
                            painter = painterResource(id = R.drawable.ic_round_reading_mode_24),
                        )
                    }

                    SectionTitle(title = stringResource(id = R.string.note_preview_size))

                    AndroidViewSlider(
                        value = folder.notePreviewSize.toFloat(), // For some reason, the value doesn't update while state property directly.
                        onValueChange = { notePreviewSize = it },
                        contentDescription = stringResource(id = R.string.note_preview_size),
                        labelFormatter = { it.toInt().toString() },
                        valueRange = NotePreviewSizeRange,
                        stepSize = NotePreviewSizeStepSize,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    SettingsItem(
                        title = stringResource(id = R.string.show_note_creation_date),
                        type = SettingsItemType.Switch(isShowNoteCreationDate, MaterialTheme.colorScheme.primary),
                        onClick = { isShowNoteCreationDate = !isShowNoteCreationDate },
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