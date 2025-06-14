package com.somnionocte.screen_router

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.util.fastFilterNotNull
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastMap
import androidx.compose.ui.util.fastMapIndexed
import androidx.lifecycle.viewmodel.compose.viewModel
import com.somnionocte.compose_extensions.onPredictiveBack
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * A Composable function that remembers and provides an instance of [RouterState].
 * This function is typically used within a Composable tree to obtain the current
 * navigation state, often in conjunction with [ScreenRouter]. It leverages
 * [androidx.lifecycle.viewmodel.compose.viewModel] to ensure the state persists
 * across configuration changes and is properly scoped within the ViewModel's lifecycle.
 *
 * @return An instance of [RouterState] that manages the navigation stack.
 * @see RouterState
 * @see ScreenRouter
 */
@Composable
fun rememberRouterState() = viewModel<RouterState>()

/**
 * The main Composable entry point for handling screen navigation within your application.
 * [ScreenRouter] observes the provided [RouterState] and renders the UI for the
 * [RouterState.currentScreen] by invoking the [content] lambda.
 *
 * <p>By default, if no [RouterState] is explicitly passed, it will obtain one
 * using [rememberRouterState()]. You should place this Composable at a high
 * level in your UI hierarchy to manage the navigation flow for a specific part
 * of your application.</p>
 *
 * @param state The [RouterState] instance that manages the screen stack.
 * Defaults to one provided by [rememberRouterState()].
 * @param content A Composable lambda that takes a [Screen] as a parameter. This
 * lambda is responsible for rendering the UI corresponding to the current screen.
 * You typically use a `when` expression inside this lambda to branch based on
 * the specific [Screen] type.
 * @see RouterState
 * @see rememberRouterState
 */
@Composable
fun ScreenRouter(
    state: RouterState = rememberRouterState(),
    content: @Composable (screen: Screen) -> Unit
) {
    val focusManager = LocalFocusManager.current

    LaunchedEffect(Unit) {
        focusManager.clearFocus()
        launch { snapshotFlow { state.screenStack.size }.onEach { focusManager.clearFocus() } }
    }

    state.predictiveGesture = onPredictiveBack(state.isNotEmpty) { state.pop() }
    DisposableEffect(Unit) { onDispose { state.predictiveGesture = null } }

    CompositionLocalProvider(LocalScreenRouter provides state) {
        SubcomposeLayout(Modifier.fillMaxSize().background(Color.Black)) { constraints ->
            val placeables = state.composedScreens.fastMap {
                subcompose(it.hashCode()) { remember { movableContentOf { it.Render { content(it.screen) } } }() }
                    .first()
                    .measure(constraints)
            }

            layout(constraints.maxWidth, constraints.maxHeight) {
                val indexesToPlace = state.composedScreens
                    .fastMapIndexed { index, screen ->
                        if(screen.isRendered) placeables.getOrNull(index)
                        else null
                    }
                    .fastFilterNotNull()

                indexesToPlace.fastForEach { it.place(IntOffset.Zero) }
            }
        }
    }
}