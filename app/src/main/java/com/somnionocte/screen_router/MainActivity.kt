package com.somnionocte.screen_router

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.somnionocte.screen_router.ui.theme.Screen_routerTheme

object Screens {
    object Screen1 : Screen
    object Screen2 : Screen
    object Screen3 : Screen
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            Screen_routerTheme {
                ScreenRouter { screen ->
                    when (screen) {
                        is NullScreen -> Screent()
                        is Screens.Screen1 -> ScreenQ()
                        is Screens.Screen2 -> ScreenW()
                        is Screens.Screen3 -> ScreenE()
                    }
                }
            }
        }
    }

    @Composable
    fun Screent() {
        val router = LocalScreenRouter.current

        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            Column(
                Modifier.fillMaxSize().padding(innerPadding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Main screen")

                Spacer(Modifier.height(16.dp))

                FilledTonalButton(
                    onClick = { router.go(Screens.Screen1) }
                ) {
                    Text("Go to screen 1")
                }
            }
        }
    }

    @Composable
    fun ScreenQ() {
        val router = LocalScreenRouter.current

        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            Column(
                Modifier.fillMaxSize().padding(innerPadding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("First screen")

                Spacer(Modifier.height(16.dp))

                FilledTonalButton(
                    onClick = { router.go(Screens.Screen2) }
                ) {
                    Text("Go to screen 2")
                }
            }
        }
    }

    @Composable
    fun ScreenW() {
        val router = LocalScreenRouter.current

        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            Column(
                Modifier.fillMaxSize().padding(innerPadding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Second screen")

                Spacer(Modifier.height(16.dp))

                FilledTonalButton(
                    onClick = { router.go(Screens.Screen3) }
                ) {
                    Text("Go to screen 3")
                }
            }
        }
    }

    @Composable
    fun ScreenE() {
        val router = LocalScreenRouter.current

        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            Column(
                Modifier.fillMaxSize().padding(innerPadding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Last screen")
            }
        }
    }
}