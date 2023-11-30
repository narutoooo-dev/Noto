package com.noto.app

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
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.rememberPagerState
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
import com.noto.app.components.*
import com.noto.app.domain.model.NotoColor
import com.noto.app.domain.model.UserStatus
import com.noto.app.settings.SettingsItem
import com.noto.app.settings.SettingsItemType
import com.noto.app.settings.SettingsViewModel
import com.noto.app.theme.NotoTheme
import com.noto.app.theme.toColor
import com.noto.app.util.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class IntroFragment : Fragment() {

    private val viewModel by viewModel<AppViewModel>()

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
                val pagerState = rememberPagerState(initialPage = Page.Initial.ordinal) { Page.Count }
                val currentPage by remember { derivedStateOf { Page.ofOrdinal(pagerState.currentPage) } }
                val currentPageColor by animateColorAsState(targetValue = currentPage.color)
                var currentPageScrollState by remember { mutableStateOf(ScrollState(initial = 0)) }

                IntroScreen(
                    pagerState = pagerState,
                    color = currentPageColor,
                    onNavigationIconClick = { navController?.navigateUp() },
                    scrollState = currentPageScrollState,
                    bottomAppBarContent = bottomAppBarContent(page = currentPage, color = currentPageColor),
                ) { pageOrdinal ->
                    val page = remember(pageOrdinal) { Page.ofOrdinal(pageOrdinal) }
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

    private fun bottomAppBarContent(page: Page, color: Color): @Composable (() -> Unit)? {
        return when (page) {
            Page.OpenSource -> {
                {
                    BottomAppBarSourceCodeContent(color = color)
                }
            }

            Page.Reminders -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    {
                        BottomAppBarRemindersContent(color = color)
                    }
                } else {
                    null
                }
            }

            Page.Vault -> {
                {
                    BottomAppBarVaultContent(color = color)
                }
            }

            Page.Cloud -> {
                {
                    BottomAppBarAccountContent(color = color)
                }
            }

            Page.Setup -> {
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
}

@Composable
private fun Fragment.PageItem(
    page: Page,
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

@Composable
private fun Fragment.PageContent(page: Page, modifier: Modifier = Modifier) {
    Group(modifier) {
        when (page) {
            Page.Setup -> {
                val settingsViewModel by viewModel<SettingsViewModel>()
                val theme by settingsViewModel.theme.collectAsState()
                val themeId = remember(theme) { theme.toStringResourceId() }
                val themeText = stringResource(id = themeId)
                val language = remember { AppCompatDelegate.getApplicationLocales().toLanguages().first() }
                val languageId = remember(language) { language.toStringResourceId() }
                val languageText = stringResource(id = languageId)
                val rememberScrollingPositionEnabled by settingsViewModel.isRememberScrollingPosition.collectAsState()
                val notesCountEnabled by settingsViewModel.isShowNotesCount.collectAsState()
                val quickExit by settingsViewModel.quickExit.collectAsState()
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
                    onClick = { settingsViewModel.toggleShowNotesCount() },
                    description = stringResource(id = R.string.show_notes_count_description),
                    painter = painterResource(id = R.drawable.ic_round_tag_24),
                )

                SettingsItem(
                    title = stringResource(id = R.string.remember_scrolling_position),
                    type = SettingsItemType.Switch(rememberScrollingPositionEnabled),
                    onClick = { settingsViewModel.toggleRememberScrollingPosition() },
                    description = stringResource(id = R.string.remember_scrolling_position_description),
                    painter = EmptyPainter,
                )

                SettingsItem(
                    title = stringResource(id = R.string.quick_exit),
                    type = SettingsItemType.Switch(quickExit),
                    onClick = { settingsViewModel.toggleQuickExit() },
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

private enum class Page(
    val titleStringId: Int,
    val descriptionStringId: Int,
    val imageDrawableId: Int,
    val notoColor: NotoColor,
    val extrasStringIds: List<Int> = emptyList(),
) {
    Start(
        R.string.intro_discover_features_title,
        R.string.intro_discover_features_description,
        R.drawable.illustration_features,
        NotoColor.General,
    ),
    AdFree(
        R.string.intro_ad_free_title,
        R.string.intro_ad_free_description,
        R.drawable.illustration_ads,
        NotoColor.Pink,
    ),
    OpenSource(
        R.string.intro_open_source_title,
        R.string.intro_open_source_description,
        R.drawable.illustration_open_source,
        NotoColor.Green,
    ),
    Organization(
        R.string.intro_organization_title,
        R.string.intro_organization_description,
        R.drawable.illustration_organization,
        NotoColor.Blue,
        listOf(
            R.string.intro_labels,
            R.string.intro_archiving,
            R.string.intro_colorful_folders,
            R.string.intro_label_filtering,
            R.string.intro_pinning,
            R.string.intro_grouping,
            R.string.intro_sorting,
            R.string.intro_manual_ordering,
            R.string.intro_layouts,
            R.string.intro_actions,
        ),
    ),
    MultiSelection(
        R.string.intro_multi_selection_title,
        R.string.intro_multi_selection_description,
        R.drawable.illustration_multi_selection,
        NotoColor.Yellow,
    ),
    Search(
        R.string.intro_search_title,
        R.string.intro_search_description,
        R.drawable.illustration_search,
        NotoColor.BlueGray,
    ),
    ReadingMode(
        R.string.intro_reading_mode_title,
        R.string.intro_reading_mode_description,
        R.drawable.illustration_reading_mode,
        NotoColor.ReadingMode,
    ),
    UndoRedo(
        R.string.intro_undo_redo_title,
        R.string.intro_undo_redo_description,
        R.drawable.illustration_undo_redo,
        NotoColor.Cyan,
    ),
    Reminders(
        R.string.intro_reminders_title,
        R.string.intro_reminders_description,
        R.drawable.illustration_reminders,
        NotoColor.Red,
    ),
    Vault(
        R.string.intro_vault_title,
        R.string.intro_vault_description,
        R.drawable.illustration_vault,
        NotoColor.Vault,
    ),
    Other(
        R.string.intro_other_title,
        R.string.intro_other_description,
        R.drawable.illustration_other,
        NotoColor.DeepGreen,
        listOf(
            R.string.intro_widgets,
            R.string.intro_auto_save,
            R.string.intro_quick_note,
            R.string.intro_custom_app_icons,
            R.string.intro_nested_folders,
            R.string.intro_shortcuts,
            R.string.intro_design,
            R.string.intro_private_secure,
            R.string.intro_telegram_community,
            R.string.intro_auto_backup,
        ),
    ),
    Cloud(
        R.string.intro_cloud_title,
        R.string.intro_cloud_description,
        R.drawable.illustration_cloud,
        NotoColor.Account,
    ),
    Setup(
        R.string.intro_setup,
        R.string.intro_setup_description,
        R.drawable.illustration_setup,
        NotoColor.General,
    );

    companion object {
        val Initial = Start
        val Count = entries.count()
        fun ofOrdinal(ordinal: Int) = entries.first { it.ordinal == ordinal }
    }
}

private val Page.color: Color
    @Composable
    get() = notoColor.toColor()