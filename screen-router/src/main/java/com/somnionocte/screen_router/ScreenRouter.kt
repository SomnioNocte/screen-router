package com.somnionocte.screen_router

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.util.fastFilterNotNull
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastLastOrNull
import androidx.compose.ui.util.fastMap
import androidx.compose.ui.util.fastMapIndexed
import androidx.compose.ui.util.lerp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.somnionocte.screen_router.extensions.animatableAs
import com.somnionocte.screen_router.extensions.getScreenRadiusCorner
import com.somnionocte.screen_router.extensions.scale
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

interface Screen
object NullScreen : Screen

class RouterState : ViewModel() {
    internal var predictiveGesture: PredictiveBackHandler? = null
    internal val screenStack = mutableStateListOf<Screen>(NullScreen)
    internal val composedScreens = mutableStateListOf(ScreenInstance(NullScreen))

    val isEmpty: Boolean get() = screenStack.size == 1
    val isNotEmpty: Boolean get() = screenStack.size > 1
    val currentScreen: Screen get() = screenStack.last()
    val previousScreen: Screen? get() = screenStack.getOrNull(screenStack.lastIndex - 1)

    fun go(screen: Screen) {
        if(screen != currentScreen) {
            screenStack.add(screen)

            if(composedScreens.fastLastOrNull { it.screen == screen } == null)
                composedScreens.add(ScreenInstance(screen))
        }
    }

    fun pop() { if(isNotEmpty) screenStack.removeAt(screenStack.lastIndex) }

    internal inner class ScreenInstance(val screen: Screen) {
        val isInStack by derivedStateOf { screenStack.contains(screen) }
        val isOpened by derivedStateOf { currentScreen == screen }
        val isPrevious by derivedStateOf { previousScreen == screen }

        private val transitionOpacity = Animatable(1f)
        private val transitionOffsetX = Animatable(if(isEmpty) 0f else 1000f)
        val isRunning by derivedStateOf { transitionOpacity.isRunning || transitionOffsetX.isRunning }
        val isRendered by derivedStateOf { isRunning || isOpened || (isPrevious && predictiveGesture?.isDragged == true) }

        fun dispose() { composedScreens.remove(this) }

        @Composable
        fun Render(content: @Composable () -> Unit) {
            val screenCornerRadius = getScreenRadiusCorner()

            val backTransition = animatableAs(
                spec = { spring(1f, if(predictiveGesture?.isDragged == true) 2500f else 225f) },
                value = { predictiveGesture?.transition ?: 0f }
            )

            val opacity = animatableAs(
                transitionOpacity,
                spec = {
                    val defaultSpeed = 225f + (predictiveGesture?.transition ?: 0f) * 1250f

                    if(isOpened || !isInStack || isPrevious)
                        spring(1f, if(predictiveGesture?.isDragged == true) 2500f else defaultSpeed)
                    else
                        tween(250, 0, EaseIn)
                },
                value = {
                    if(isOpened || !isInStack) 1f
                    else if(isPrevious) (predictiveGesture?.transition ?: 0f) * .75f
                    else 0f
                }
            )

            val offsetX = animatableAs(
                transitionOffsetX,
                spec = {
                    val defaultSpeed = 250f + (predictiveGesture?.transition ?: 0f) * 1250f
                    spring(1f, if(predictiveGesture?.isDragged == true) 2500f else defaultSpeed)
                },
                onAnimationFinished = { if(!isInStack && !isOpened) dispose() },
                value = {
                    if(isOpened) (predictiveGesture?.transition ?: 0f) * 350f
                    else if(isInStack) 0f
                    else 1000f
                }
            )

            Box(Modifier
                .graphicsLayer {
                    alpha = opacity.value
                    scale = lerp(.85f, 1f - backTransition.value * .05f, alpha)
                    translationX = offsetX.value * .001f * size.width
                    if(scale != 1f || translationX != 0f) {
                        shape = RoundedCornerShape(screenCornerRadius * (2f - scale))
                        clip = true
                    }
                }
            ) {
                content()
            }
        }
    }
}

val LocalScreenRouter = compositionLocalOf<RouterState> { error("RouterState isn't initialized") }

@Composable
fun rememberRouterState() = viewModel<RouterState>()

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

    state.predictiveGesture = onPredictiveBackHandler({ state.isNotEmpty }) { state.pop() }
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