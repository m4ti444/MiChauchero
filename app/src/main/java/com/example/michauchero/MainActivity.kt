package com.example.michauchero

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.michauchero.reminders.NotificationUtils
import com.example.michauchero.ui.theme.MiChaucheroTheme
import com.example.michauchero.navigation.AppNavHost

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        AppGraph.init(this)
        NotificationUtils.ensureChannel(this)
        setContent {
            MiChaucheroTheme {
                AppNavHost()
            }
        }
    }
}
