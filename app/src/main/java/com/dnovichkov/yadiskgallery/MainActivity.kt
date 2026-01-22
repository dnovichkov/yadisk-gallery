package com.dnovichkov.yadiskgallery

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.dnovichkov.yadiskgallery.domain.repository.IAuthRepository
import com.dnovichkov.yadiskgallery.presentation.auth.AuthResult
import com.dnovichkov.yadiskgallery.presentation.auth.YandexAuthManager
import com.dnovichkov.yadiskgallery.presentation.navigation.NavGraph
import com.dnovichkov.yadiskgallery.presentation.navigation.Screen
import com.dnovichkov.yadiskgallery.presentation.theme.YaDiskGalleryTheme
import com.yandex.authsdk.YandexAuthLoginOptions
import com.yandex.authsdk.YandexAuthResult
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Main Activity that hosts the Compose UI with Navigation.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var yandexAuthManager: YandexAuthManager

    @Inject
    lateinit var authRepository: IAuthRepository

    private lateinit var yandexAuthLauncher: ActivityResultLauncher<YandexAuthLoginOptions>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Register for Yandex auth result
        yandexAuthLauncher =
            registerForActivityResult(
                yandexAuthManager.getContract(),
            ) { result ->
                handleYandexAuthResult(result)
            }

        enableEdgeToEdge()
        setContent {
            YaDiskGalleryTheme {
                MainScreen(
                    onStartYandexLogin = { startYandexLogin() },
                )
            }
        }
    }

    /**
     * Starts the Yandex OAuth login flow.
     */
    private fun startYandexLogin() {
        yandexAuthLauncher.launch(YandexAuthLoginOptions())
    }

    /**
     * Handles the result from Yandex OAuth flow.
     */
    private fun handleYandexAuthResult(result: YandexAuthResult) {
        val authResult = AuthResult.fromYandexResult(result)
        lifecycleScope.launch {
            when (authResult) {
                is AuthResult.Success -> {
                    // Save the token with default expiration (1 year)
                    authRepository.saveToken(authResult.token, DEFAULT_TOKEN_EXPIRATION_SECONDS)
                }
                is AuthResult.Error -> {
                    authRepository.setAuthError(authResult.message)
                }
                is AuthResult.Cancelled -> {
                    // User cancelled, no action needed
                }
            }
        }
    }

    companion object {
        private const val DEFAULT_TOKEN_EXPIRATION_SECONDS = 31536000L // 1 year
    }
}

/**
 * Main screen composable with navigation.
 *
 * @param onStartYandexLogin Callback to start Yandex OAuth login flow (handled by Activity)
 */
@Composable
fun MainScreen(onStartYandexLogin: () -> Unit = {}) {
    val navController = rememberNavController()

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        NavGraph(
            navController = navController,
            onStartYandexLogin = onStartYandexLogin,
            startDestination = Screen.Settings.route,
            modifier = Modifier.padding(innerPadding),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MainScreenPreview() {
    YaDiskGalleryTheme {
        MainScreen()
    }
}
