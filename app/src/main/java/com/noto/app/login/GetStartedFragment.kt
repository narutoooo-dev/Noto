package com.noto.app.login

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.noto.app.R
import com.noto.app.components.*
import com.noto.app.domain.model.NotoColor
import com.noto.app.theme.NotoTheme
import com.noto.app.theme.toColor
import com.noto.app.util.navController
import com.noto.app.util.navigateSafely
import com.noto.app.util.setupMixedTransitions
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

private val IndicatorWidth = 10.dp

class GetStartedFragment : Fragment() {

    val viewModel by viewModel<LoginViewModel>()

    private val notificationPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->

    }

    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? = context?.let { context ->
        setupMixedTransitions()
        ComposeView(context).apply {
            isTransitionGroup = true
            setContent {
                val pagerState = rememberPagerState(initialPage = Page.Initial.index) { Page.Count }
                val page = remember(pagerState.currentPage) { Page.ofIndex(pagerState.currentPage) }
                val pageColor by animateColorAsState(targetValue = page.color)

                Screen(
                    title = "",
//                    color = pageColor,
                    onNavigationIconClick = { navController?.navigateUp() },
                    verticalArrangement = Arrangement.SpaceBetween,
                ) {
                    HorizontalPager(
                        modifier = Modifier.weight(1F),
                        state = pagerState,
                        userScrollEnabled = true,
                        verticalAlignment = Alignment.Top,
                    ) { page ->
                        when (page) {
                            Page.Start.index -> StartPage(pageColor)
                            Page.AdFree.index -> AdFreePage(pageColor)
                            Page.Design.index -> DesignPage(pageColor)
                            Page.Organization.index -> OrganizationPage(pageColor)
                            Page.Reminders.index -> RemindersPage(pageColor)
                            Page.ReadingMode.index -> ReadingModePage(pageColor)
                            Page.Vault.index -> VaultPage(pageColor)
                            Page.Notification.index -> NotificationPermissionPage(
                                pageColor,
                                onClick = ::requestNotificationsPermissionIfRequired
                            )

                            Page.Account.index -> AccountPage(
                                pageColor,
                                onRegister = { navController?.navigateSafely(GetStartedFragmentDirections.actionGetStartedFragmentToRegisterFragment()) },
                                onSkip = { viewModel.skipRegistration() }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(NotoTheme.dimensions.medium))

                    BottomNavigation(pagerState, pageColor)
                }
            }
        }
    }

    private fun requestNotificationsPermissionIfRequired() {
        context?.let { context ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val notificationPermissionStatus = ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                if (notificationPermissionStatus == PackageManager.PERMISSION_DENIED) {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }
}

@Composable
private fun StartPage(color: Color, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(NotoTheme.dimensions.extraLarge)) {
        PageTitle(text = stringResource(id = R.string.intro_start_title), color)
        PageDescription(text = stringResource(id = R.string.intro_start_page_description))
    }
}

@Composable
private fun AdFreePage(color: Color, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(NotoTheme.dimensions.extraLarge)) {
        PageTitle(text = stringResource(id = R.string.intro_ad_free_title), color)
        PageDescription(text = stringResource(id = R.string.intro_ad_free_description))
    }
}

@Composable
private fun DesignPage(color: Color, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(NotoTheme.dimensions.extraLarge)) {
        PageTitle(text = stringResource(id = R.string.intro_design_title), color)
        PageDescription(text = stringResource(id = R.string.intro_design_description))
    }
}

@Composable
private fun VaultPage(color: Color, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(NotoTheme.dimensions.extraLarge)) {
        PageTitle(text = stringResource(id = R.string.intro_vault_title), color)
        PageDescription(text = stringResource(id = R.string.intro_vault_description))
    }
}

@Composable
private fun ReadingModePage(color: Color, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(NotoTheme.dimensions.extraLarge)) {
        PageTitle(text = stringResource(id = R.string.intro_reading_mode_title), color)
        PageDescription(text = stringResource(id = R.string.intro_reading_mode_description))
    }
}

@Composable
private fun RemindersPage(color: Color, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(NotoTheme.dimensions.extraLarge)) {
        PageTitle(text = stringResource(id = R.string.intro_reminders_title), color)
        PageDescription(text = stringResource(id = R.string.intro_reminders_description))
    }
}

@Composable
private fun OrganizationPage(color: Color, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(NotoTheme.dimensions.extraLarge)) {
        PageTitle(text = stringResource(id = R.string.intro_organization_title), color)
        PageDescription(text = stringResource(id = R.string.intro_organization_description))
    }
}

@Composable
private fun NotificationPermissionPage(
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
        Column(verticalArrangement = Arrangement.spacedBy(NotoTheme.dimensions.extraLarge)) {
            PageTitle(text = stringResource(id = R.string.intro_notifications_title), color)
            PageDescription(text = stringResource(id = R.string.intro_notifications_description))
        }

        Button(
            text = stringResource(id = R.string.grant_permission),
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
            containerColor = color,
            contentColor = Color.White,
        )
    }
}

@Composable
private fun AccountPage(
    color: Color,
    onRegister: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxSize(), Arrangement.SpaceBetween) {
        Column(verticalArrangement = Arrangement.spacedBy(NotoTheme.dimensions.medium)) {
            PageTitle(text = stringResource(id = R.string.account), color)
            PageDescription(text = stringResource(id = R.string.intro_account_description))
            PageDescription(text = stringResource(id = R.string.intro_private_and_secure_description))
        }


        Column(verticalArrangement = Arrangement.spacedBy(NotoTheme.dimensions.medium)) {
            Button(
                text = stringResource(id = R.string.register_for_free),
                onClick = onRegister,
                modifier = Modifier.fillMaxWidth(),
                containerColor = color,
                contentColor = Color.White,
            )

            OutlinedButton(
                text = stringResource(id = R.string.skip_for_now),
                onClick = onSkip,
                modifier = Modifier.fillMaxWidth(),
                contentColor = color,
                borderColor = color,
            )
        }
    }
}

@Composable
private fun SettingsPage() {
    // After registration/skipping.
}

@Composable
private fun PageTitle(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Text(
        text,
        modifier,
        color = color,
        fontWeight = FontWeight.Bold,
        style = MaterialTheme.typography.headlineLarge,
    )
}

@Composable
private fun PageDescription(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        modifier,
        color = MaterialTheme.colorScheme.secondary,
        fontWeight = FontWeight.SemiBold,
        style = MaterialTheme.typography.titleMedium
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun BottomNavigation(
    pagerState: PagerState,
    pageColor: Color,
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()
    val progress = remember(pagerState.currentPage) { (pagerState.currentPage.plus(1).toFloat() / Page.Count) }
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AnimatedVisibility(visible = pagerState.currentPage != 0) {
            FilledIconButton(
                painter = painterResource(id = R.drawable.ic_round_previous_page_24),
                contentDescription = stringResource(id = R.string.previous),
                onClick = {
                    if (pagerState.currentPage != 0) {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage - 1)
                        }
                    }
                },
                enabled = pagerState.currentPage != 0,
            )
        }

        Spacer(modifier = Modifier.width(NotoTheme.dimensions.medium))

        AnimatedVisibility(visible = pagerState.currentPage != Page.Count.minus(1)) {
            LinearProgressIndicator(
                progress = progress,
                color = pageColor,
                trackColor = MaterialTheme.colorScheme.surface,
                strokeCap = StrokeCap.Round,
            )
        }

        Spacer(modifier = Modifier.width(NotoTheme.dimensions.medium))
        AnimatedVisibility(visible = pagerState.currentPage != Page.Count.minus(1)) {
            FilledIconButton(
                painter = painterResource(id = R.drawable.ic_round_next_page_24),
                contentDescription = stringResource(id = R.string.next),
                onClick = {
                    if (pagerState.currentPage != Page.Count - 1) {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    }
                },
                enabled = pagerState.currentPage != Page.Count - 1,
                containerColor = pageColor,
                contentColor = Color.White,
            )
        }
    }
}

private enum class Page(val index: Int, val notoColor: NotoColor) {
    Start(0, NotoColor.Blue),
    AdFree(1, NotoColor.Pink),
    Design(2, NotoColor.Purple),
    Organization(3, NotoColor.Red),
    Reminders(4, NotoColor.Yellow),
    ReadingMode(5, NotoColor.Orange),
    Vault(6, NotoColor.Green),
    Notification(7, NotoColor.Brown),
    Account(8, NotoColor.Indigo);

    companion object {
        val Initial = Start
        val Count = entries.count()
        fun ofIndex(index: Int) = Page.values().first { it.index == index }
    }
}


private val Page.color: Color
    @Composable
    get() = notoColor.toColor()