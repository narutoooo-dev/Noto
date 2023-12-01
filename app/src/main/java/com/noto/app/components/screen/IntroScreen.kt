package com.noto.app.components.screen


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerScope
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import com.noto.app.R
import com.noto.app.components.material.BasicTopAppBar
import com.noto.app.components.material.DotProgressIndicator
import com.noto.app.components.material.ElevationAnimationDuration
import com.noto.app.components.material.NotoFilledIconButton
import com.noto.app.settings.SettingsViewModel
import com.noto.app.theme.NotoTheme
import com.noto.app.util.navController
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Fragment.IntroScreen(
    pagerState: PagerState,
    modifier: Modifier = Modifier,
    color: Color = LocalContentColor.current,
    onNavigationIconClick: (() -> Unit)? = { navController?.navigateUp() },
    actions: @Composable RowScope.() -> Unit = {},
    snackbarHost: @Composable () -> Unit = {},
    scrollState: ScrollState = rememberScrollState(),
    bottomAppBarContent: @Composable (() -> Unit)? = null,
    content: @Composable PagerScope.(page: Int) -> Unit,
) {
    val viewModel by remember { viewModel<SettingsViewModel>() }
    val theme by viewModel.theme.collectAsState()
    val scope = rememberCoroutineScope()
    val isScrolling by remember(scrollState) { derivedStateOf { scrollState.value > 0 } }
    val isFullyScrolled by remember(scrollState) { derivedStateOf { scrollState.value == scrollState.maxValue } }

    NotoTheme(theme = theme) {
        Scaffold(
            topBar = {
                BasicTopAppBar(
                    onClick = {
                        scope.launch {
                            scrollState.animateScrollTo(0)
                        }
                    },
                    isScrolling = isScrolling,
                    onNavigationIconClick = onNavigationIconClick,
                    actions = actions,
                    color = color,
                )
            },
            snackbarHost = snackbarHost,
            bottomBar = { BottomAppBar(pagerState, color, isFullyScrolled, bottomAppBarContent) }
        ) { contentPadding ->
            HorizontalPager(
                state = pagerState,
                modifier = modifier.fillMaxSize(),
                contentPadding = contentPadding,
                pageSpacing = NotoTheme.dimensions.medium,
                verticalAlignment = Alignment.Top,
                pageContent = content,
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun BottomAppBar(
    pagerState: PagerState,
    color: Color,
    isFullyScrolled: Boolean,
    bottomAppBarContent: @Composable (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()
    val isPreviousEnabled by remember(pagerState) { derivedStateOf { pagerState.currentPage != 0 } }
    val isNextEnabled by remember(pagerState) { derivedStateOf { pagerState.currentPage != pagerState.pageCount - 1 } }
    val nextAlpha by animateFloatAsState(targetValue = if (isNextEnabled) 1F else 0F)
    val previousAlpha by animateFloatAsState(targetValue = if (isPreviousEnabled) 1F else 0F)
    val elevation by animateDpAsState(
        targetValue = if (!isFullyScrolled) NotoTheme.dimensions.extraSmall else 0.dp,
        animationSpec = tween(ElevationAnimationDuration)
    )
    val currentPageIndex by remember(pagerState) { derivedStateOf { pagerState.currentPage } }
    val totalPageCount by remember(pagerState) { derivedStateOf { pagerState.pageCount } }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation)
            .background(MaterialTheme.colorScheme.background)
            .padding(NotoTheme.dimensions.medium),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AnimatedVisibility(visible = bottomAppBarContent != null) {
            if (bottomAppBarContent != null) bottomAppBarContent()
        }

        AnimatedVisibility(visible = bottomAppBarContent != null) {
            Spacer(modifier = Modifier.height(NotoTheme.dimensions.medium))
        }

        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            NotoFilledIconButton(
                painter = painterResource(id = R.drawable.ic_round_previous_page_24),
                contentDescription = stringResource(id = R.string.previous),
                onClick = {
                    if (isPreviousEnabled) {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage - 1)
                        }
                    }
                },
                enabled = isPreviousEnabled,
                modifier = Modifier.alpha(previousAlpha),
            )

            Spacer(modifier = Modifier.weight(1F))

            DotProgressIndicator(
                currentItemIndex = currentPageIndex,
                totalItemCount = totalPageCount,
                selectedColor = color,
            )

            Spacer(modifier = Modifier.weight(1F))

            NotoFilledIconButton(
                painter = painterResource(id = R.drawable.ic_round_next_page_24),
                contentDescription = stringResource(id = R.string.next),
                onClick = {
                    if (isNextEnabled) {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    }
                },
                enabled = isNextEnabled,
                containerColor = color,
                contentColor = Color.White,
                modifier = Modifier.alpha(nextAlpha),
            )
        }
    }
}