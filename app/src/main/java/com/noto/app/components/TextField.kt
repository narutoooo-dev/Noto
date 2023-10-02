package com.noto.app.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActionScope
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.noto.app.R
import com.noto.app.theme.NotoTheme

sealed interface TextFieldStatus {
    data object Empty : TextFieldStatus

    @JvmInline
    value class Info(val messageStringResourceId: Int) : TextFieldStatus

    @JvmInline
    value class Error(val errorStringResourceId: Int) : TextFieldStatus

    val isError: Boolean
        get() = this is Error
}

@Composable
fun TextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    trailingIcon: (@Composable () -> Unit)? = null,
    leadingIcon: (@Composable () -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    status: TextFieldStatus = TextFieldStatus.Empty,
    onDone: KeyboardActionScope.() -> Unit = { defaultKeyboardAction(keyboardOptions.imeAction) },
) {
    var isPlaceholderVisible by rememberSaveable(value) { mutableStateOf(value.isEmpty()) }
    val textStyle = MaterialTheme.typography.bodyMedium
    BasicTextField(
        value = value,
        onValueChange = {
            onValueChange(it)
            isPlaceholderVisible = it.isEmpty()
        },
        modifier = modifier,
        enabled = enabled,
        textStyle = textStyle,
        singleLine = true,
        visualTransformation = visualTransformation,
        cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
        keyboardOptions = keyboardOptions,
        keyboardActions = KeyboardActions(onDone)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(NotoTheme.dimensions.small)
        ) {
            Text(text = placeholder, style = MaterialTheme.typography.bodyLarge)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.small)
                    .padding(NotoTheme.dimensions.medium),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                leadingIcon?.let { leadingIcon ->
                    leadingIcon()
                    Spacer(Modifier.width(NotoTheme.dimensions.medium))
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1F),
                    contentAlignment = Alignment.CenterStart
                ) {
                    this@Row.AnimatedVisibility(visible = isPlaceholderVisible) {
                        Text(text = placeholder, style = textStyle)
                    }
                    it()
                }
                trailingIcon?.let { trailingIcon ->
                    Spacer(Modifier.width(NotoTheme.dimensions.medium))
                    trailingIcon()
                }
            }
            TextFieldStatus(status)
        }
    }
}

@Composable
fun PasswordTextField(
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
    TextField(
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
private fun ColumnScope.TextFieldStatus(status: TextFieldStatus, modifier: Modifier = Modifier) {
    AnimatedVisibility(visible = status != TextFieldStatus.Empty, modifier) {
        when (status) {
            is TextFieldStatus.Empty -> {}
            is TextFieldStatus.Info -> {
                Text(
                    text = stringResource(id = status.messageStringResourceId),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary,
                )
            }

            is TextFieldStatus.Error -> {
                Text(
                    text = stringResource(id = status.errorStringResourceId),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}