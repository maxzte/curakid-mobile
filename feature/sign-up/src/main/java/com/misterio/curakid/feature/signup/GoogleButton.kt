package com.misterio.curakid.feature.signup

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.misterio.core.domain.util.suspendRunCatching
import com.misterio.core.ui.theme.CurakidTheme
import kotlinx.coroutines.launch


@Composable
internal fun GoogleButton(
    onTokenReceived: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val credentialManager = remember(context) { CredentialManager.create(context) }
    val coroutineScope = rememberCoroutineScope()

    OutlinedButton(
        onClick = {
            if (BuildConfig.GOOGLE_WEB_CLIENT_ID.isEmpty()) {
                println("Error: GOOGLE_WEB_CLIENT_ID is not set in local.properties")
                return@OutlinedButton
            }

            val googleIdOption = GetGoogleIdOption
                .Builder()
                .setServerClientId(BuildConfig.GOOGLE_WEB_CLIENT_ID)
                .setFilterByAuthorizedAccounts(false)
                .build()

            val request = GetCredentialRequest
                .Builder()
                .addCredentialOption(googleIdOption)
                .build()

            coroutineScope.launch {
                suspendRunCatching {
                    val result = credentialManager.getCredential(
                        request = request,
                        context = context
                    )
                    val credential = result.credential

                    if (
                        credential is CustomCredential &&
                        credential.type ==
                        GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                    ) {
                        val googleCredential = GoogleIdTokenCredential.createFrom(credential.data)
                        val idToken = googleCredential.idToken

                        onTokenReceived(idToken)
                    }
                }.onFailure { e ->
                    e.printStackTrace()
                }
            }
        },
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = CurakidTheme.Radius.md,
        border = BorderStroke(1.dp, CurakidTheme.colors.outline),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = CurakidTheme.colors.surface
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(CurakidTheme.paddings.spaceSm)
        ) {
            Text(
                text = "G",
                style = CurakidTheme.typography.bodyMedium.copy(
                    fontSize = 17.sp,
                    color = Color(0xFF4285F4)
                )
            )
            Text(
                text = stringResource(R.string.continue_with_google),
                style = CurakidTheme.typography.bodyMedium.copy(
                    color = CurakidTheme.colors.primary,
                    fontSize = 15.sp
                )
            )
        }
    }
}