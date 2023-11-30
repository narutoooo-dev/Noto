package com.noto.app.components

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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.fragment.app.Fragment
import com.noto.app.R
import com.noto.app.settings.SettingsViewModel
import com.noto.app.theme.NotoTheme
import com.noto.app.util.navController
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.math.absoluteValue

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Fragment.PagerScreen(
    title: String,
    pagerState: PagerState,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    color: Color = LocalContentColor.current,
    onNavigationIconClick: (() -> Unit)? = { navController?.navigateUp() },
    actions: @Composable RowScope.() -> Unit = {},
    snackbarHost: @Composable () -> Unit = {},
    scrollState: ScrollState = rememberScrollState(),
    primaryButton: @Composable (() -> Unit)? = null,
    content: @Composable PagerScope.(page: Int) -> Unit,
) {
    val viewModel by viewModel<SettingsViewModel>()
    val theme by viewModel.theme.collectAsState()
    val scope = rememberCoroutineScope()
    val isScrolling by remember(scrollState) { derivedStateOf { scrollState.value > 0 } }
    val isFullyScrolled by remember(scrollState) { derivedStateOf { scrollState.value == scrollState.maxValue } }
    val currentPageIndex by remember(pagerState) { derivedStateOf { pagerState.currentPage } }
    val totalPagesCount by remember(pagerState) { derivedStateOf { pagerState.pageCount } }
    val progress by animateFloatAsState(targetValue = currentPageIndex.plus(1).toFloat() / totalPagesCount)

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
                    actions = {
                        ActionCircularProgressIndicator(progress = progress, color = color)
                        actions()
                    },
                    subtitle = subtitle,
                    color = color,
                )
            },
            snackbarHost = snackbarHost,
            bottomBar = { BottomAppBar(pagerState, color, isFullyScrolled, primaryButton = primaryButton) }
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
    modifier: Modifier = Modifier,
    primaryButton: @Composable (() -> Unit)? = null,
) {
    val coroutineScope = rememberCoroutineScope()
    val isPreviousEnabled by remember(pagerState) { derivedStateOf { pagerState.currentPage != 0 } }
    val isNextEnabled by remember(pagerState) { derivedStateOf { pagerState.currentPage != pagerState.pageCount - 1 } }
    val elevation by animateDpAsState(
        targetValue = if (!isFullyScrolled) NotoTheme.dimensions.extraSmall else 0.dp,
        animationSpec = tween(ElevationAnimationDuration)
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation, spotColor = Color.Unspecified)
            .background(MaterialTheme.colorScheme.background)
            .padding(NotoTheme.dimensions.medium),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AnimatedVisibility(visible = primaryButton != null) {
            if (primaryButton != null) primaryButton()
        }

        AnimatedVisibility(visible = primaryButton != null) {
            Spacer(modifier = Modifier.height(NotoTheme.dimensions.medium))
        }

        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AnimatedVisibility(visible = isPreviousEnabled) {
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
                )
            }

            Spacer(modifier = Modifier.weight(1F))

            AnimatedVisibility(visible = isNextEnabled) {
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
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
fun Modifier.animatePageAlpha(pagerState: PagerState, currentPage: Int) = this.graphicsLayer {
    // Calculate the absolute offset for the current page from the
    // scroll position. We use the absolute value which allows us to mirror
    // any effects for both directions
    val pageOffset = ((pagerState.currentPage - currentPage) + pagerState.currentPageOffsetFraction).absoluteValue
    // We animate the alpha, between 50% and 100%
    alpha = lerp(start = 0.25F, stop = 1F, fraction = 1F - pageOffset.coerceIn(0F, 1F))
}