package com.noto.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import com.noto.app.components.*
import com.noto.app.theme.NotoTheme
import com.noto.app.util.navController
import com.noto.app.util.navigateSafely
import com.noto.app.util.setupMixedTransitions

class StartFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? = context?.let { context ->
        setupMixedTransitions()
        ComposeView(context).apply {
            isTransitionGroup = true
            setContent {
                val scrollState = rememberScrollState()
                val isFullyScrolled by remember { derivedStateOf { scrollState.value == scrollState.maxValue } }
                val elevation by animateDpAsState(
                    targetValue = if (!isFullyScrolled) NotoTheme.dimensions.extraSmall else 0.dp,
                    animationSpec = tween(ElevationAnimationDuration)
                )

                Screen(
                    title = "",
                    onNavigationIconClick = null,
                    verticalArrangement = Arrangement.spacedBy(NotoTheme.dimensions.medium),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    scrollState = scrollState,
                    bottomBar = {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(elevation)
                                .background(MaterialTheme.colorScheme.background)
                                .padding(NotoTheme.dimensions.medium),
                            verticalArrangement = Arrangement.spacedBy(NotoTheme.dimensions.medium),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            NotoButton(
                                text = stringResource(id = R.string.get_started),
                                onClick = { navController?.navigateSafely(StartFragmentDirections.actionStartFragmentToIntroFragment()) },
                                modifier = Modifier.fillMaxWidth(),
                            )

                            NotoOutlinedButton(
                                text = stringResource(id = R.string.log_in),
                                onClick = { navController?.navigateSafely(StartFragmentDirections.actionStartFragmentToLoginFragment()) },
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                ) {
                    IntroPageImage(
                        painter = painterResource(id = R.drawable.illustration_welcome),
                        contentDescription = stringResource(id = R.string.start_title)
                    )
                    Spacer(modifier = Modifier.height(NotoTheme.dimensions.extraLarge))
                    IntroPageTitle(text = stringResource(id = R.string.start_title))
                    IntroPageDescription(text = stringResource(id = R.string.start_page_description))
                }
            }
        }
    }
}