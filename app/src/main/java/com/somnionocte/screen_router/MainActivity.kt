package com.somnionocte.screen_router

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.somnionocte.screen_router.ui.theme.MyAppTheme

object Screens {
    data class User(val id: String) : Screen
    object Settings : Screen {
        object General : Screen
        object Theme : Screen
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyAppTheme {
                ScreenRouter { screen ->
                    when (screen) {
                        is NullScreen -> MainScreen()
                        is Screens.User -> UserScreen(screen.id)
                        is Screens.Settings -> SettingsScreen()
                        is Screens.Settings.General -> GeneralSettingsScreen()
                        is Screens.Settings.Theme -> ThemeSettingsScreen()
                    }
                }
            }
        }
    }
}

@Composable
fun UserScreen(id: String) {
    val router = LocalScreenRouter.current

    Scaffold { padding ->
        Column(
            Modifier.padding(padding).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Profile", fontSize = 28.sp)

            Text("id: $id", fontSize = 22.sp)

            FilledTonalButton(
                onClick = { router.pop() }
            ) {
                Text("Go back")
            }
        }
    }
}

@Composable
fun MainScreen() {
    val router = LocalScreenRouter.current

    Scaffold { padding ->
        Column(
            Modifier.padding(padding).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Main screen", fontSize = 28.sp)

            FilledTonalIconButton(
                onClick = { router.go(Screens.Settings) }
            ) {
                Icon(Icons.Rounded.Settings, "Settings")
            }

            FilledTonalButton(
                onClick = { router.go(Screens.User(id = "123")) }
            ) {
                Text("Go to some profile")
            }
        }
    }
}

@Composable
fun SettingsScreen() {
    val router = LocalScreenRouter.current

    Scaffold { padding ->
        Column(
            Modifier.padding(padding).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Settings", fontSize = 28.sp)

            FilledTonalButton(
                onClick = { router.go(Screens.Settings.General) }
            ) {
                Text("General settings")
            }

            FilledTonalButton(
                onClick = { router.go(Screens.Settings.Theme) }
            ) {
                Text("Theme settings")
            }
        }
    }
}

@Composable
fun ThemeSettingsScreen() {
    val router = LocalScreenRouter.current

    Scaffold { padding ->
        Column(
            Modifier.padding(padding).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Theme settings", fontSize = 28.sp)

            FilledTonalButton(
                onClick = { router.pop() }
            ) {
                Text("Go back")
            }
        }
    }
}

@Composable
fun GeneralSettingsScreen() {
    val router = LocalScreenRouter.current

    Scaffold { padding ->
        Column(
            Modifier.padding(padding).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("General settings", fontSize = 28.sp)

            FilledTonalButton(
                onClick = { router.pop() }
            ) {
                Text("Go back")
            }
        }
    }
}