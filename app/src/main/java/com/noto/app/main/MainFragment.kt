package com.noto.app.main

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.view.forEach
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.airbnb.epoxy.EpoxyController
import com.airbnb.epoxy.EpoxyViewHolder
import com.airbnb.epoxy.group
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.noto.app.R
import com.noto.app.UiState
import com.noto.app.components.BaseDialogFragment
import com.noto.app.databinding.MainFragmentBinding
import com.noto.app.domain.model.*
import com.noto.app.filtered.FilteredItemModel
import com.noto.app.filtered.filteredItem
import com.noto.app.getOrDefault
import com.noto.app.util.*
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class MainFragment : BaseDialogFragment(isCollapsable = true) {

    private val viewModel by sharedViewModel<MainViewModel>()

    private val args by navArgs<MainFragmentArgs>()

    private lateinit var epoxyController: EpoxyController

    private lateinit var itemTouchHelper: ItemTouchHelper

    private val selectedDestinationId by lazy { navController?.lastDestinationIdOrNull }

    private val popUpToDestinationId by lazy {
        when (selectedDestinationId) {
            null -> R.id.filteredFragment
            else -> R.id.folderFragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = MainFragmentBinding.inflate(inflater, container, false).withBinding {
        setupMixedTransitions()
        setupListeners()
        setupState()
    }

    private fun MainFragmentBinding.setupListeners() {
        fab.setOnClickListener {
            dismiss()
            navController?.navigateSafely(MainFragmentDirections.actionMainFragmentToNewFolderFragment())
        }

        ibMore.setOnClickListener {
            dismiss()
            navController?.navigateSafely(MainFragmentDirections.actionMainFragmentToMainDialogFragment())
        }

        ibSettings.setOnClickListener {
            dismiss()
            navController?.navigateSafely(MainFragmentDirections.actionMainFragmentToSettingsFragment())
        }

        ibSorting.setOnClickListener {
            navController?.navigateSafely(MainFragmentDirections.actionMainFragmentToFolderListViewDialogFragment())
        }
    }

    private fun MainFragmentBinding.setupState() {
//        rv.edgeEffectFactory = BounceEdgeEffectFactory()
        rv.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        tvFoldersCount.typeface = context?.tryLoadingFontResource(R.font.nunito_semibold)
        tvFoldersCount.animationInterpolator = DefaultInterpolator()

        combine(
            viewModel.folders,
            viewModel.sortingType,
            viewModel.isShowNotesCount,
            viewModel.notesCount,
        ) { folders, sortingType, isShowNotesCount, notesCount ->
            setupFolders(folders, sortingType, isShowNotesCount, notesCount)
            setupItemTouchHelper(sortingType == FolderListSortingType.Manual)
        }.launchIn(lifecycleScope)

        viewModel.sortingType
            .onEach { sortingType ->
                rv.itemAnimator = when (sortingType) {
                    FolderListSortingType.Manual -> DefaultItemAnimator().apply {
                        addDuration = DefaultAnimationDuration
                        changeDuration = DefaultAnimationDuration
                        moveDuration = DefaultAnimationDuration
                        removeDuration = DefaultAnimationDuration
                    }

                    else -> VerticalListItemAnimator()
                }
            }
            .launchIn(lifecycleScope)

        rv.isScrollingAsFlow()
            .onEach { isScrolling -> tb.isSelected = isScrolling }
            .launchIn(lifecycleScope)

        if (isCurrentLocaleArabic()) {
            tvFoldersCount.isVisible = false
            tvFoldersCountRtl.isVisible = true
        } else {
            tvFoldersCount.isVisible = true
            tvFoldersCountRtl.isVisible = false
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun MainFragmentBinding.setupFolders(
        state: UiState<List<Folder>>,
        sortingType: FolderListSortingType,
        isShowNotesCount: Boolean,
        notesCount: FilteredItemModel.NotesCount,
    ) {
        if (state is UiState.Success) {

            rv.withModels {
                epoxyController = this
                val folders = state.value.filterNot { it.isGeneral }
                val generalFolder = state.value.firstOrNull { it.isGeneral }
                val isManualSorting = sortingType == FolderListSortingType.Manual
                val foldersCount = folders.countRecursively()

                tvFoldersCount.text = context?.quantityStringResource(R.plurals.folders_count, foldersCount, foldersCount)
                tvFoldersCountRtl.text = context?.quantityStringResource(R.plurals.folders_count, foldersCount, foldersCount)

                context?.let { context ->

                    group(R.layout.vertical_linear_layout_group) {
                        id("header")

                        group(R.layout.horizontal_linear_layout_group) {
                            id("sub_header_1")
                            spanSizeOverride { _, _, _ -> 2 }

                            filteredItem {
                                id("all")
                                model(FilteredItemModel.All)
                                notesCount(notesCount.all)
                                isShowNotesCount(isShowNotesCount)
                                isSelected(FilteredItemModel.All.id == selectedDestinationId)
                                onClickListener { _ ->
                                    dismiss()
                                    if (selectedDestinationId != FilteredItemModel.All.id)
                                        navController?.navigateSafely(MainFragmentDirections.actionMainFragmentToFilteredFragment(FilteredItemModel.All)) {
                                            popUpTo(popUpToDestinationId) {
                                                inclusive = true
                                            }
                                        }
                                }
                            }

                            filteredItem {
                                id("recent")
                                model(FilteredItemModel.Recent)
                                notesCount(notesCount.recent)
                                isShowNotesCount(isShowNotesCount)
                                isSelected(FilteredItemModel.Recent.id == selectedDestinationId)
                                onClickListener { _ ->
                                    dismiss()
                                    if (selectedDestinationId != FilteredItemModel.Recent.id)
                                        navController?.navigateSafely(MainFragmentDirections.actionMainFragmentToFilteredFragment(FilteredItemModel.Recent)) {
                                            popUpTo(popUpToDestinationId) {
                                                inclusive = true
                                            }
                                        }
                                }
                            }
                        }

                        group(R.layout.horizontal_linear_layout_group) {
                            id("sub_header_2")
                            spanSizeOverride { _, _, _ -> 2 }

                            filteredItem {
                                id("scheduled")
                                model(FilteredItemModel.Scheduled)
                                notesCount(notesCount.scheduled)
                                isShowNotesCount(isShowNotesCount)
                                isSelected(FilteredItemModel.Scheduled.id == selectedDestinationId)
                                onClickListener { _ ->
                                    dismiss()
                                    if (selectedDestinationId != FilteredItemModel.Scheduled.id)
                                        navController?.navigateSafely(MainFragmentDirections.actionMainFragmentToFilteredFragment(FilteredItemModel.Scheduled)) {
                                            popUpTo(popUpToDestinationId) {
                                                inclusive = true
                                            }
                                        }
                                }
                            }

                            filteredItem {
                                id("archived")
                                model(FilteredItemModel.Archived)
                                notesCount(notesCount.archived)
                                isShowNotesCount(isShowNotesCount)
                                isSelected(FilteredItemModel.Archived.id == selectedDestinationId)
                                onClickListener { _ ->
                                    dismiss()
                                    if (selectedDestinationId != FilteredItemModel.Archived.id)
                                        navController?.navigateSafely(MainFragmentDirections.actionMainFragmentToFilteredFragment(FilteredItemModel.Archived)) {
                                            popUpTo(popUpToDestinationId) {
                                                inclusive = true
                                            }
                                        }
                                }
                            }
                        }
                    }


                    generalFolder?.let {
                        folderItem {
                            id(generalFolder.id)
                            folder(generalFolder)
                            isManualSorting(isManualSorting)
                            isShowNotesCount(isShowNotesCount)
                            isSelected(generalFolder.id == selectedDestinationId)
                            onClickListener { _ ->
                                dismiss()
                                if (generalFolder.id != selectedDestinationId)
                                    navController?.navigateSafely(MainFragmentDirections.actionMainFragmentToFolderFragment(generalFolder.id)) {
                                        popUpTo(popUpToDestinationId) {
                                            inclusive = true
                                        }
                                    }
                            }
                            onLongClickListener { _ ->
                                dismiss()
                                navController?.navigateSafely(
                                    MainFragmentDirections.actionMainFragmentToFolderDialogFragment(
                                        generalFolder.id
                                    )
                                )
                                true
                            }
                            onDragHandleTouchListener { _, _ -> false }
                        }
                    }

                    buildFoldersModels(context, folders) { folders ->
                        folders.forEachRecursively { folder, depth ->
                            folderItem {
                                id(folder.id)
                                folder(folder)
                                isManualSorting(isManualSorting)
                                isShowNotesCount(isShowNotesCount)
                                isSelected(folder.id == selectedDestinationId)
                                depth(depth)
                                onClickListener { _ ->
                                    dismiss()
                                    if (folder.id != selectedDestinationId)
                                        navController?.navigateSafely(MainFragmentDirections.actionMainFragmentToFolderFragment(folder.id)) {
                                            popUpTo(popUpToDestinationId) {
                                                inclusive = true
                                            }
                                        }
                                }
                                onLongClickListener { _ ->
                                    dismiss()
                                    navController?.navigateSafely(MainFragmentDirections.actionMainFragmentToFolderDialogFragment(folder.id))
                                    true
                                }
                                onDragHandleTouchListener { view, event ->
                                    if (event.action == MotionEvent.ACTION_DOWN)
                                        rv.findContainingViewHolder(view)?.let { viewHolder ->
                                            if (this@MainFragment::itemTouchHelper.isInitialized)
                                                itemTouchHelper.startDrag(viewHolder)
                                        }
                                    view.performClick()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun MainFragmentBinding.setupItemTouchHelper(isManualSorting: Boolean) {
        if (isManualSorting) {
            if (this@MainFragment::epoxyController.isInitialized) {
                val itemTouchHelperCallback = FolderItemTouchHelperCallback(
                    epoxyController,
                    onSwipe = { viewHolder, direction -> onSwipe(viewHolder, direction) },
                    onDrag = { onDrag() }
                )
                itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
                    .apply { attachToRecyclerView(rv) }
            }
        } else {
            if (this@MainFragment::itemTouchHelper.isInitialized) {
                itemTouchHelper.attachToRecyclerView(null)
            }
        }
    }

    private fun MainFragmentBinding.onSwipe(viewHolder: EpoxyViewHolder, direction: Int) {
        val folders = viewModel.folders.value.getOrDefault(emptyList()).filterNot { it.isGeneral }
        val model = viewHolder.model as? FolderItem
        if (model != null) {
            if (direction == ItemTouchHelper.START) {
                val parentFolder = folders.findRecursively { it.id == model.folder.parentFolder?.id }?.parentFolder
                viewModel.updateFolderParentId(model.folder, parentFolder)
            } else {
                val previousViewHolder = rv.findViewHolderForAdapterPosition(viewHolder.bindingAdapterPosition - 1) as EpoxyViewHolder?
                val previousModel = previousViewHolder?.model as? FolderItem?
                val parentFolder = folders.findRecursively {
                    val isSameParent = it.parentFolder?.id == model.folder.parentFolder?.id
                    val isPreviousSelf = it.id == previousModel?.folder?.id
                    val isWithinPreviousFolders = it.childFolders.findRecursively { it.id == previousModel?.folder?.id } != null
                    isSameParent && (isPreviousSelf || isWithinPreviousFolders)
                }
                if (parentFolder != null)
                    viewModel.updateFolderParentId(model.folder, parentFolder)
            }
            epoxyController.notifyModelChanged(viewHolder.bindingAdapterPosition)
        }
    }

    private fun MainFragmentBinding.onDrag() {
        rv.forEach { view ->
            val viewHolder = rv.findContainingViewHolder(view) as EpoxyViewHolder
            val model = viewHolder.model as? FolderItem
            if (model != null) viewModel.updateFolderPosition(model.folder, viewHolder.bindingAdapterPosition)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return object : BottomSheetDialog(requireContext(), theme) {
            @Deprecated("Deprecated in Java")
            override fun onBackPressed() {
                if (args.exit)
                    activity?.finish()
                else
                    super.onBackPressed()
            }
        }
    }

}