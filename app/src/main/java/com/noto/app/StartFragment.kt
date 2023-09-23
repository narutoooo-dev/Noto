package com.noto.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.fragment.app.Fragment
import com.noto.app.components.Button
import com.noto.app.components.Screen
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
                Screen(
                    title = "",
                    verticalArrangement = Arrangement.Bottom,
                    onNavigationIconClick = null,
                ) {
                    Button(
                        text = stringResource(id = R.string.get_started),
                        onClick = { navController?.navigateSafely(StartFragmentDirections.actionStartFragmentToGetStartedFragment()) },
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Spacer(Modifier.height(NotoTheme.dimensions.medium))

                    Button(
                        text = stringResource(id = R.string.login),
                        onClick = { navController?.navigateSafely(StartFragmentDirections.actionStartFragmentToLoginFragment()) },
                        modifier = Modifier.fillMaxWidth(),
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}