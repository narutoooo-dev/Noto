package com.noto.app.intro

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.noto.app.*
import com.noto.app.R
import com.noto.app.components.material.IntroPageDescription
import com.noto.app.components.material.IntroPageImage
import com.noto.app.components.material.IntroPageTitle
import com.noto.app.components.material.NotoButton
import com.noto.app.components.screen.IntroScreen
import com.noto.app.components.screen.animatePageAlpha
import com.noto.app.components.util.EmptyPainter
import com.noto.app.components.util.Group
import com.noto.app.domain.model.UserStatus
import com.noto.app.settings.SettingsItem
import com.noto.app.settings.SettingsItemType
import com.noto.app.theme.NotoTheme
import com.noto.app.theme.toColor
import com.noto.app.util.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class IntroFragment : Fragment() {

    private val viewModel by viewModel<IntroViewModel>()

    private val notificationPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        viewModel.setNotificationPermissionResult(isGranted)
    }

    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? = context?.let { context ->
        setupMixedTransitions()
        activity?.onBackPressedDispatcher?.addCallback { navController?.navigateUp() }
        ComposeView(context).apply {
            isTransitionGroup = true
            setContent {
                val pagerState = rememberPagerState(initialPage = IntroPage.Initial.ordinal) { IntroPage.Count }
                val currentPage by remember { derivedStateOf { IntroPage.ofOrdinal(pagerState.currentPage) } }
                val currentPageColor by animateColorAsState(targetValue = currentPage.color)
                var currentPageScrollState by remember { mutableStateOf(ScrollState(initial = 0)) }

                IntroScreen(
                    pagerState = pagerState,
                    color = currentPageColor,
                    onNavigationIconClick = { navController?.navigateUp() },
                    scrollState = currentPageScrollState,
                    bottomAppBarContent = bottomAppBarContent(page = currentPage, color = currentPageColor),
                ) { pageOrdinal ->
                    val page = remember(pageOrdinal) { IntroPage.ofOrdinal(pageOrdinal) }
                    val scrollState = rememberScrollState()
                    val scrollStateValue by remember { derivedStateOf { scrollState.value } }

                    PageItem(
                        page = page,
                        modifier = Modifier
                            .verticalScroll(scrollState)
                            .animatePageAlpha(pagerState, pageOrdinal),
                    )

                    LaunchedEffect(key1 = scrollStateValue) { currentPageScrollState = scrollState }
                }
            }
        }
    }

    private fun bottomAppBarContent(page: IntroPage, color: Color): @Composable (() -> Unit)? {
        return when (page) {
            IntroPage.OpenSource -> {
                {
                    BottomAppBarSourceCodeContent(color = color)
                }
            }

            IntroPage.Reminders -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    {
                        BottomAppBarRemindersContent(color = color)
                    }
                } else {
                    null
                }
            }

            IntroPage.Vault -> {
                {
                    BottomAppBarVaultContent(color = color)
                }
            }

            IntroPage.Cloud -> {
                {
                    BottomAppBarAccountContent(color = color)
                }
            }

            IntroPage.Setup -> {
                {
                    BottomAppBarSetupContent(color = color)
                }
            }

            else -> null
        }
    }

    @Composable
    private fun BottomAppBarRemindersContent(color: Color) {
        context?.let { context ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val notificationPermissionStatus = ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                if (notificationPermissionStatus == PackageManager.PERMISSION_DENIED) {
                    NotoButton(
                        text = stringResource(id = R.string.grant_permission),
                        onClick = { notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) },
                        modifier = Modifier.fillMaxWidth(),
                        containerColor = color,
                        contentColor = Color.White,
                    )
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_round_check_24),
                            contentDescription = stringResource(id = R.string.permission_is_granted),
                            tint = color,
                        )
                        Spacer(modifier = Modifier.width(NotoTheme.dimensions.medium))
                        Text(
                            text = stringResource(id = R.string.permission_is_granted),
                            color = color,
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun BottomAppBarVaultContent(color: Color) {
        val vaultPasscode by viewModel.vaultPasscode.collectAsState()
        if (vaultPasscode == null) {
            NotoButton(
                text = stringResource(id = R.string.enable_vault),
                onClick = { navController?.navigateSafely(IntroFragmentDirections.actionIntroFragmentToVaultPasscodeDialogFragment()) },
                modifier = Modifier.fillMaxWidth(),
                containerColor = color,
                contentColor = Color.White,
            )
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_round_check_24),
                    contentDescription = stringResource(id = R.string.vault_is_enabled),
                    tint = color,
                )
                Spacer(modifier = Modifier.width(NotoTheme.dimensions.medium))
                Text(
                    text = stringResource(id = R.string.vault_is_enabled),
                    color = color
                )
            }
        }
    }

    @Composable
    private fun BottomAppBarAccountContent(color: Color) {
        val userStatus by viewModel.userStatus.collectAsState(UserStatus.New)
        if (userStatus == UserStatus.NotLoggedIn || userStatus == UserStatus.New) {
            NotoButton(
                text = stringResource(id = R.string.create_account),
                onClick = { navController?.navigateSafely(IntroFragmentDirections.actionIntroFragmentToCreateAccountFragment()) },
                modifier = Modifier.fillMaxWidth(),
                containerColor = color,
                contentColor = Color.White,
            )
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_round_check_24),
                    contentDescription = stringResource(id = R.string.account_is_created),
                    tint = color,
                )
                Spacer(modifier = Modifier.width(NotoTheme.dimensions.medium))
                Text(
                    text = stringResource(id = R.string.account_is_created),
                    color = color
                )
            }
        }
    }

    @Composable
    private fun BottomAppBarSetupContent(color: Color) {
        NotoButton(
            text = stringResource(id = R.string.intro_finish),
            onClick = { viewModel.finishIntro() },
            modifier = Modifier.fillMaxWidth(),
            containerColor = color,
            contentColor = Color.White,
        )
    }

    @Composable
    private fun BottomAppBarSourceCodeContent(color: Color) {
        NotoButton(
            text = stringResource(id = R.string.source_code),
            onClick = {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(Constants.Noto.GithubUrl))
                startActivity(intent)
            },
            modifier = Modifier.fillMaxWidth(),
            containerColor = color,
            contentColor = Color.White,
        )
    }


    @Composable
    private fun PageContent(page: IntroPage, modifier: Modifier = Modifier) {
        Group(modifier) {
            when (page) {
                IntroPage.Setup -> {
                    val theme by viewModel.theme.collectAsState()
                    val themeId = remember(theme) { theme.toStringResourceId() }
                    val themeText = stringResource(id = themeId)
                    val language = remember { AppCompatDelegate.getApplicationLocales().toLanguages().first() }
                    val languageId = remember(language) { language.toStringResourceId() }
                    val languageText = stringResource(id = languageId)
                    val rememberScrollingPositionEnabled by viewModel.isRememberScrollingPosition.collectAsState()
                    val notesCountEnabled by viewModel.isShowNotesCount.collectAsState()
                    val quickExit by viewModel.quickExit.collectAsState()
                    SettingsItem(
                        title = stringResource(id = R.string.theme),
                        type = SettingsItemType.Text(themeText),
                        onClick = { navController?.navigateSafely(IntroFragmentDirections.actionIntroFragmentToThemeDialogFragment()) },
                        painter = painterResource(id = R.drawable.ic_round_theme_24),
                    )

                    SettingsItem(
                        title = stringResource(id = R.string.language),
                        type = SettingsItemType.Text(languageText),
                        onClick = { navController?.navigateSafely(IntroFragmentDirections.actionIntroFragmentToLanguageDialogFragment()) },
                        painter = painterResource(id = R.drawable.ic_round_language_24),
                    )

                    SettingsItem(
                        title = stringResource(id = R.string.show_notes_count),
                        type = SettingsItemType.Switch(notesCountEnabled),
                        onClick = { viewModel.toggleShowNotesCount() },
                        description = stringResource(id = R.string.show_notes_count_description),
                        painter = painterResource(id = R.drawable.ic_round_tag_24),
                    )

                    SettingsItem(
                        title = stringResource(id = R.string.remember_scrolling_position),
                        type = SettingsItemType.Switch(rememberScrollingPositionEnabled),
                        onClick = { viewModel.toggleRememberScrollingPosition() },
                        description = stringResource(id = R.string.remember_scrolling_position_description),
                        painter = EmptyPainter,
                    )

                    SettingsItem(
                        title = stringResource(id = R.string.quick_exit),
                        type = SettingsItemType.Switch(quickExit),
                        onClick = { viewModel.toggleQuickExit() },
                        description = stringResource(id = R.string.quick_exit_description),
                        painter = painterResource(id = R.drawable.ic_round_quick_exit_24),
                    )
                }

                else -> {
                    page.extrasStringIds.forEach { extraStringId ->
                        SettingsItem(
                            title = stringResource(id = extraStringId),
                            type = SettingsItemType.None,
                            painter = painterResource(id = R.drawable.ic_round_check_24),
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun PageItem(
        page: IntroPage,
        modifier: Modifier = Modifier,
    ) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(NotoTheme.dimensions.medium),
            verticalArrangement = Arrangement.spacedBy(NotoTheme.dimensions.medium),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            IntroPageImage(painter = painterResource(id = page.imageDrawableId), contentDescription = stringResource(id = page.titleStringId))
            Spacer(modifier = Modifier.height(NotoTheme.dimensions.extraLarge))
            IntroPageTitle(text = stringResource(id = page.titleStringId), color = page.color)
            IntroPageDescription(text = stringResource(id = page.descriptionStringId))
            PageContent(page = page)
        }
    }
}

private val IntroPage.color: Color
    @Composable
    get() = notoColor.toColor()