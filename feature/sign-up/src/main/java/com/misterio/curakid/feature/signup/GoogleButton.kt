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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.misterio.core.ui.theme.CuraKidColors
import com.misterio.core.ui.theme.CurakidTheme
import com.misterio.curakid.feature.signup.BuildConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@Composable
internal fun GoogleButton(onTokenReceived: (String) -> Unit) {
    val context = LocalContext.current
    val credentialManager = CredentialManager.create(context)

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

            CoroutineScope(Dispatchers.Main).launch {
                try {
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
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = CurakidTheme.Radius.md,
        border = BorderStroke(1.dp, CurakidTheme.colors.outline),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = CuraKidColors.White
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Google G icon via text — replace with painterResource(R.drawable.ic_google)
            Text(
                text = "G",
                style = CurakidTheme.typography.bodyMedium.copy(
                    fontSize = 17.sp,
                    color = Color(0xFF4285F4)
                )
            )
            Text(
                text = "Продовжити з Google",
                style = CurakidTheme.typography.bodyMedium.copy(
                    color = CuraKidColors.Blue30,
                    fontSize = 15.sp
                )
            )
        }
    }
}