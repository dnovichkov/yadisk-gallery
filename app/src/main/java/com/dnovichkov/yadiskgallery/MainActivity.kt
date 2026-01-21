package com.dnovichkov.yadiskgallery

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.dnovichkov.yadiskgallery.presentation.navigation.NavGraph
import com.dnovichkov.yadiskgallery.presentation.navigation.Screen
import com.dnovichkov.yadiskgallery.presentation.theme.YaDiskGalleryTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main Activity that hosts the Compose UI with Navigation.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            YaDiskGalleryTheme {
                MainScreen()
            }
        }
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
