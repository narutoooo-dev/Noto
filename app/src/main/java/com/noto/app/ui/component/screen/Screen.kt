package com.noto.app.ui.component.screen

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.fragment.app.Fragment
import com.noto.app.ui.component.material.NotoTopAppbar
import com.noto.app.ui.settings.SettingsViewModel
import com.noto.app.ui.theme.NotoTheme
import com.noto.app.ui.util.navController
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

@Composable
fun Fragment.Screen(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    color: Color = LocalContentColor.current,
    onNavigationIconClick: (() -> Unit)? = { navController?.navigateUp() },
    actions: @Composable RowScope.() -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    snackbarHost: @Composable () -> Unit = {},
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(NotoTheme.dimensions.medium),
    horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    scrollState: ScrollState = rememberScrollState(),
    content: @Composable ColumnScope.() -> Unit,
) {
    val viewModel by remember { viewModel<SettingsViewModel>() }
    val theme by viewModel.theme.collectAsState()
    val scope = rememberCoroutineScope()
    val isScrolling by remember { derivedStateOf { scrollState.value > 0 } }
    NotoTheme(theme = theme) {
        Scaffold(
            topBar = {
                NotoTopAppbar(
                    title = title,
                    onClick = {
                        scope.launch {
                            scrollState.animateScrollTo(0)
                        }
                    },
                    isScrolling = isScrolling,
                    onNavigationIconClick = onNavigationIconClick,
                    actions = actions,
                    subtitle = subtitle,
                    color = color,
                )
            },
            snackbarHost = snackbarHost,
            bottomBar = bottomBar,
        ) { contentPadding ->
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(NotoTheme.dimensions.medium)
                    .padding(contentPadding),
                verticalArrangement = verticalArrangement,
                horizontalAlignment = horizontalAlignment,
                content = content,
            )
        }
    }
}