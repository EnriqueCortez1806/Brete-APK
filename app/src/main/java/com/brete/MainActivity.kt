package com.brete

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.brete.navigation.BreteNavGraph
import com.brete.ui.theme.BreteTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Permitir que la app dibuje detrás de las barras del sistema
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // 2. Ocultar la barra de navegación inferior y la barra de estado
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        windowInsetsController.hide(WindowInsetsCompat.Type.navigationBars())

        setContent {
            // Estado global del modo oscuro para toda la app (inicia en false / modo claro)
            var isDarkModeEnabled by remember { mutableStateOf(false) }

            // Envolvemos toda la aplicación en BreteTheme pasándole el estado del botón
            BreteTheme(darkTheme = isDarkModeEnabled) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    BreteNavGraph(
                        isDarkMode = isDarkModeEnabled,
                        onDarkModeChanged = { isDarkModeEnabled = it }
                    )
                }
            }
        }
    }
}