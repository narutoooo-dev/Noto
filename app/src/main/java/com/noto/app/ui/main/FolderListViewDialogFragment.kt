package com.noto.app.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.noto.app.R
import com.noto.app.domain.FolderListSortingType
import com.noto.app.domain.SortingOrder
import com.noto.app.ui.component.dialog.BaseDialogFragment
import com.noto.app.ui.component.dialog.BottomSheetDialog
import com.noto.app.ui.component.dialog.BottomSheetDialogItem
import com.noto.app.ui.component.material.NotoButton
import com.noto.app.ui.component.util.Group
import com.noto.app.ui.theme.NotoTheme
import com.noto.app.ui.util.Constants
import com.noto.app.ui.util.navController
import com.noto.app.ui.util.navigateSafely
import com.noto.app.ui.util.toStringResourceId
import org.koin.androidx.viewmodel.ext.android.viewModel

class FolderListViewDialogFragment : BaseDialogFragment() {

    private val viewModel by viewModel<MainViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? = context?.let { context ->
        val navController = navController
        val savedStateHandle = navController?.currentBackStackEntry?.savedStateHandle

        ComposeView(context).apply {
            if (navController == null || savedStateHandle == null) return@apply

            setContent {
                val sortingType by viewModel.sortingType.collectAsState()
                val sortingOrder by viewModel.sortingOrder.collectAsState()
                val updatedSortingType by savedStateHandle.getStateFlow<FolderListSortingType?>(key = Constants.SortingType, initialValue = null)
                    .collectAsState()
                val updatedSortingOrder by savedStateHandle.getStateFlow<SortingOrder?>(key = Constants.SortingOrder, initialValue = null)
                    .collectAsState()

                BottomSheetDialog(title = stringResource(R.string.folders_view)) {
                    Group {
                        BottomSheetDialogItem(
                            text = stringResource(id = R.string.sorting),
                            onClick = {
                                navController.navigateSafely(FolderListViewDialogFragmentDirections.actionFolderListViewDialogFragmentToFolderListSortingDialogFragment())
                            },
                            painter = painterResource(id = R.drawable.ic_round_sorting_24),
                            value = stringResource(id = updatedSortingType?.toStringResourceId() ?: sortingType.toStringResourceId()),
                        )

                        BottomSheetDialogItem(
                            text = stringResource(id = R.string.ordering),
                            onClick = {
                                navController.navigateSafely(FolderListViewDialogFragmentDirections.actionFolderListViewDialogFragmentToFolderListOrderingDialogFragment())
                            },
                            painter = painterResource(id = R.drawable.ic_round_ordering_24),
                            value = stringResource(id = updatedSortingOrder?.toStringResourceId() ?: sortingOrder.toStringResourceId()),
                            enabled = (updatedSortingType ?: sortingType) != FolderListSortingType.Manual,
                        )
                    }

                    Spacer(modifier = Modifier.height(NotoTheme.dimensions.extraLarge))

                    NotoButton(
                        text = stringResource(id = R.string.apply),
                        onClick = {
                            viewModel.updateFoldersView(
                                sortingType = updatedSortingType ?: sortingType,
                                sortingOrder = updatedSortingOrder ?: sortingOrder,
                            ).invokeOnCompletion { dismiss() }
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}