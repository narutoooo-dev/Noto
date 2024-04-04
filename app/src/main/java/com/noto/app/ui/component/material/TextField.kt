package com.noto.app.ui.component.material

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActionScope
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.noto.app.R

private val ErrorTextFieldBorderWidth = 1.dp

sealed interface TextFieldStatus {
    data object Empty : TextFieldStatus

    @JvmInline
    value class Info(val messageStringResourceId: Int) : TextFieldStatus

    @JvmInline
    value class Error(val errorStringResourceId: Int) : TextFieldStatus

    val isError: Boolean
        get() = this is Error
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotoTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    status: TextFieldStatus = TextFieldStatus.Empty,
    onDone: KeyboardActionScope.() -> Unit = { defaultKeyboardAction(keyboardOptions.imeAction) },
) {
    val interactionSource = remember { MutableInteractionSource() }
    val supportingText: @Composable (() -> Unit)? = when (status) {
        is TextFieldStatus.Empty -> {
            null
        }

        is TextFieldStatus.Info -> @Composable {
            { InfoTextFieldStatus(status = status) }
        }

        is TextFieldStatus.Error -> @Composable {
            { ErrorTextFieldStatus(status = status) }
        }
    }

    val borderColor by animateColorAsState(
        targetValue = if (status is TextFieldStatus.Error) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.surface,
        label = "TextFieldBorderColor",
    )

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.animateContentSize(),
        enabled = enabled,
        textStyle = MaterialTheme.typography.bodyLarge,
        singleLine = true,
        visualTransformation = visualTransformation,
        cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
        keyboardOptions = keyboardOptions,
        keyboardActions = KeyboardActions(onDone),
        interactionSource = interactionSource,
    ) { innerTextField ->
        TextFieldDefaults.DecorationBox(
            value = value,
            innerTextField = innerTextField,
            enabled = enabled,
            singleLine = true,
            visualTransformation = visualTransformation,
            interactionSource = interactionSource,
            label = { Text(text = placeholder) },
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            shape = MaterialTheme.shapes.small,
            supportingText = supportingText,
            container = {
                Box(
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.small)
                        .border(ErrorTextFieldBorderWidth, borderColor, MaterialTheme.shapes.small)
                        .background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.small)
                )
            }
        )
    }
}

@Composable
fun NotoPasswordTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = stringResource(id = R.string.password),
    enabled: Boolean = true,
    status: TextFieldStatus = TextFieldStatus.Empty,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Password,
    onDone: KeyboardActionScope.() -> Unit = { defaultKeyboardAction(keyboardOptions.imeAction) },
    isPasswordVisible: MutableState<Boolean> = rememberSaveable { mutableStateOf(false) },
) {
    var isPasswordVisible by isPasswordVisible
    NotoTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = placeholder,
        modifier = modifier,
        enabled = enabled,
        leadingIcon = {
            Icon(
                painter = painterResource(id = R.drawable.ic_round_lock_24),
                contentDescription = stringResource(id = R.string.password),
            )
        },
        trailingIcon = {
            Icon(
                painter = painterResource(id = if (isPasswordVisible) R.drawable.ic_round_visibility_24 else R.drawable.ic_round_visibility_off_24),
                contentDescription = stringResource(id = if (isPasswordVisible) R.string.hide else R.string.show),
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable { isPasswordVisible = !isPasswordVisible }
            )
        },
        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        status = status,
        keyboardOptions = keyboardOptions,
        onDone = onDone,
    )
}

val KeyboardOptions.Companion.Password
    get() = KeyboardOptions(
        keyboardType = KeyboardType.Password,
        imeAction = ImeAction.Next,
    )

@Composable
private fun InfoTextFieldStatus(status: TextFieldStatus.Info) {
    Crossfade(targetState = status, label = "InfoTextFieldStatus") { status ->
        Text(
            text = stringResource(id = status.messageStringResourceId),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.secondary,
        )
    }
}

@Composable
private fun ErrorTextFieldStatus(status: TextFieldStatus.Error) {
    Crossfade(targetState = status, label = "ErrorTextFieldStatus") { status ->
        Text(
            text = stringResource(id = status.errorStringResourceId),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.error,
        )
    }
}