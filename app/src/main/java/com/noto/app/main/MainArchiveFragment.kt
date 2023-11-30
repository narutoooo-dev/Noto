package com.noto.app.main

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.noto.app.R
import com.noto.app.UiState
import com.noto.app.components.dialog.BaseDialogFragment
import com.noto.app.components.util.placeholderItem
import com.noto.app.databinding.MainArchiveFragmentBinding
import com.noto.app.domain.model.Folder
import com.noto.app.util.*
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainArchiveFragment : BaseDialogFragment(isCollapsable = true) {

    private val viewModel by viewModel<MainViewModel>()

    private val selectedDestinationId by lazy { navController?.lastDestinationIdOrNull }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = MainArchiveFragmentBinding.inflate(inflater, container, false).withBinding {
        setupState()
    }

    private fun MainArchiveFragmentBinding.setupState() {
//        rv.edgeEffectFactory = BounceEdgeEffectFactory()
        rv.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        rv.itemAnimator = VerticalListItemAnimator()
        tb.tvDialogTitle.text = context?.stringResource(R.string.folders_archive)

        combine(
            viewModel.archivedFolders,
            viewModel.isShowNotesCount,
        ) { folders, isShowNotesCount ->
            setupFolders(folders, isShowNotesCount)
        }.launchIn(lifecycleScope)

        rv.isScrollingAsFlow()
            .onEach { isScrolling -> tb.ll.isSelected = isScrolling }
            .launchIn(lifecycleScope)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun MainArchiveFragmentBinding.setupFolders(state: UiState<List<Folder>>, isShowNotesCount: Boolean) {
        if (state is UiState.Success) {
            val folders = state.value
            rv.withModels {
                context?.let { context ->
                    if (folders.isEmpty()) {
                        placeholderItem {
                            id("placeholder")
                            placeholder(context.stringResource(R.string.archive_is_empty))
                        }
                    } else {
                        buildFoldersModels(context, folders) { folders ->
                            folders.forEachRecursively { folder, depth ->
                                folderItem {
                                    id(folder.id)
                                    folder(folder)
                                    isManualSorting(false)
                                    isSelected(folder.id == selectedDestinationId)
                                    isShowNotesCount(isShowNotesCount)
                                    depth(depth)
                                    onClickListener { _ ->
                                        dismiss()
                                        if (folder.id != selectedDestinationId)
                                            navController?.navigateSafely(MainArchiveFragmentDirections.actionMainArchiveFragmentToFolderFragment(
                                                folder.id))
                                    }
                                    onLongClickListener { _ ->
                                        dismiss()
                                        navController?.navigateSafely(
                                            MainArchiveFragmentDirections.actionMainArchiveFragmentToFolderDialogFragment(folder.id)
                                        )
                                        true
                                    }
                                    onDragHandleTouchListener { _, _ -> false }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}