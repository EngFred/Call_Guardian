package com.engfred.callguardian.presentation

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.engfred.callguardian.presentation.theme.CallGuardianTheme
import com.engfred.callguardian.presentation.ui.BlockedContactsScreen
import com.engfred.callguardian.presentation.ui.CallForwardingScreen
import com.engfred.callguardian.presentation.ui.MainScreen
import com.engfred.callguardian.presentation.ui.SettingsScreen
import com.engfred.callguardian.presentation.ui.UnsupportedScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        setContent {
            CallGuardianTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        val navController = rememberNavController()
                        NavHost(navController = navController, startDestination = "main_screen") {
                            composable("main_screen") {
                                MainScreen(navController = navController)
                            }
                            composable("call_forwarding_screen") {
                                CallForwardingScreen(navController)
                            }
                            composable("blocked_contacts_screen") {
                                BlockedContactsScreen(navController = navController)
                            }
                            composable("settings_screen") {
                                SettingsScreen()
                            }
                        }
                    } else {
                        // The device's API level is too low, show a message to the user
                        UnsupportedScreen()
                    }
                }
            }
        }
    }
}