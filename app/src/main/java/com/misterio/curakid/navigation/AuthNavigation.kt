package com.misterio.curakid.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.misterio.curakid.Route
import com.misterio.curakid.feature.signup.ForgotPasswordScreen
import com.misterio.curakid.feature.signup.LegalDocumentScreen
import com.misterio.curakid.feature.signup.LoginScreen
import com.misterio.curakid.feature.signup.SignUpScreen
import com.misterio.curakid.feature.signup.VerifyEmailScreen

@Composable
fun AuthNavHost() {
    val authStack = rememberNavBackStack(Route.Login)

    NavDisplay(
        backStack = authStack,
        entryProvider = entryProvider {
            entry<Route.Login> {
                LoginScreen(
                    onNavigateToSignUp = { authStack.add(Route.SignUp) },
                    onNavigateToForgotPassword = { authStack.add(Route.ForgotPassword) }
                )
            }
            entry<Route.ForgotPassword> {
                ForgotPasswordScreen(
                    onBack = { authStack.removeAt(authStack.lastIndex) }
                )
            }
            entry<Route.SignUp> {
                SignUpScreen(
                    onNavigateToLogIn = { authStack.removeAt(authStack.lastIndex) },
                    onNavigateToVerify = { email -> authStack.add(Route.VerifyEmail(email)) },
                    onShowDocument = { url, title -> authStack.add(Route.LegalDocument(url, title)) }
                )
            }
            entry<Route.LegalDocument> { key ->
                LegalDocumentScreen(
                    url = key.url,
                    title = key.title,
                    onBack = { authStack.removeAt(authStack.lastIndex) },
                    viewModel = hiltViewModel()
                )
            }
            entry<Route.VerifyEmail> {
                VerifyEmailScreen(
                    email = it.email,
                    onVerificationSuccess = {
                        while (authStack.size > 1) {
                            authStack.removeAt(authStack.lastIndex)
                        }
                    }
                )
            }
        }
    )
}
