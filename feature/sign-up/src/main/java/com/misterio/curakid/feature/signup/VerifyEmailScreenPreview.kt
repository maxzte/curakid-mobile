package com.misterio.curakid.feature.signup

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.misterio.core.domain.UiState

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun VerifyEmailScreenPreview() {
    MaterialTheme {
        VerifyEmailScreen(
            email = "test@example.com",
            onVerificationSuccess = {}
        )
    }
}
