package com.misterio.core.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.misterio.core.ui.R
import com.misterio.core.ui.theme.CuraKidColors
import com.misterio.core.ui.theme.CurakidTheme

@Composable
fun CuraKidTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    error: String? = null,
    // password toggle support
    isPassword: Boolean = false
) {
    var passwordVisible by remember { mutableStateOf(false) }

    val transformation = when {
        isPassword && !passwordVisible -> PasswordVisualTransformation()
        else -> visualTransformation
    }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        visualTransformation = transformation,
        isError = error != null,
        supportingText = error?.let { { Text(it) } },
        singleLine = true,
        leadingIcon = leadingIcon?.let {
            {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = if (error != null) CurakidTheme.colors.errorContainer
                    else CurakidTheme.colors.onSurfaceVariant
                )
            }
        },
        trailingIcon = if (isPassword) {
            {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.Visibility
                        else Icons.Default.VisibilityOff,
                        contentDescription = stringResource(
                            if (passwordVisible) R.string.cd_hide_password
                            else R.string.cd_show_password
                        ),
                        modifier = Modifier.size(20.dp),
                        tint = CurakidTheme.colors.onSurfaceVariant
                    )
                }
            }
        } else null,
        shape = CurakidTheme.Radius.md,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = CurakidTheme.colors.primary,
            unfocusedBorderColor = CurakidTheme.colors.inversePrimary,
            errorBorderColor = CurakidTheme.colors.errorContainer,
            focusedLabelColor = CurakidTheme.colors.primary,
            unfocusedLabelColor = CurakidTheme.colors.onSurfaceVariant,
            errorLabelColor = CurakidTheme.colors.errorContainer,
            cursorColor = CurakidTheme.colors.primary,
            focusedTextColor = CurakidTheme.colors.onSurface,
            unfocusedTextColor = CurakidTheme.colors.onSurface,
            errorTextColor = CurakidTheme.colors.onSurface,
            errorSupportingTextColor = CurakidTheme.colors.errorContainer
        )
    )
}
