package com.somnionocte.screen_router

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.util.fastLastOrNull
import androidx.compose.ui.util.lerp
import androidx.lifecycle.ViewModel
import com.somnionocte.compose_extensions.PredictiveBackState
import com.somnionocte.compose_extensions.animatableAs
import com.somnionocte.compose_extensions.getScreenCornerRadius
import com.somnionocte.compose_extensions.scale

/**
 * A marker interface representing a distinct screen or UI state in your application.
 * All your application's navigatable destinations should implement this interface.
 *
 * @see RouterState
 * @see NullScreen
 */
interface Screen

/**
 * A special [Screen] object used internally by the [RouterState] to represent an
 * initial or empty screen state.
 */
object NullScreen : Screen

/**
 * The central state holder for navigation within the Compose application.
 * [RouterState] is a [androidx.lifecycle.ViewModel] that manages a mutable stack
 * of [Screen] objects, representing the navigation history.
 *
 * <p>It provides properties to access the [currentScreen], [previousScreen],
 * and check if the stack is [isEmpty] or [isNotEmpty]. It also offers core
 * functions for navigating forward ([go]) and backward ([pop]).</p>
 *
 * <p>Internally, [RouterState] also tracks `composedScreens` to help manage the
 * lifecycle of screen Composables and includes a reference for potential
 * [PredictiveBackState] integration for predictive back gestures.</p>
 */
class RouterState : ViewModel() {
    /**
     * Internal reference to the [PredictiveBackState] if predictive back gestures are active.
     */
    internal var predictiveGesture: PredictiveBackState? = null

    /**
     * The internal mutable list representing the stack of screens.
     * The last element in this list is the [currentScreen].
     */
    internal val screenStack = mutableStateListOf<Screen>(NullScreen)

    /**
     * A list of [ScreenInstance]s that have been composed. This is used internally
     * to manage the lifecycle and state of individual screen Composables.
     */
    internal val composedScreens = mutableStateListOf(ScreenInstance(NullScreen))

    /**
     * True if the screen stack contains only the [NullScreen], indicating no
     * application-specific screens are currently on the stack.
     */
    val isEmpty: Boolean get() = screenStack.size == 1

    /**
     * True if there are application-specific screens on the stack (i.e., more
     * than just the [NullScreen]).
     */
    val isNotEmpty: Boolean get() = screenStack.size > 1

    /**
     * The currently active screen at the top of the navigation stack.
     */
    val currentScreen: Screen get() = screenStack.last()

    /**
     * The screen immediately preceding the [currentScreen] in the stack, or `null`
     * if there is no previous screen.
     */
    val previousScreen: Screen? get() = screenStack.getOrNull(screenStack.lastIndex - 1)

    /**
     * Navigates to a new [screen] by adding it to the top of the navigation stack.
     * If the provided [screen] is already the [currentScreen], no action is taken.
     *
     * <p>This also ensures that a [ScreenInstance] for the new screen is added
     * to `composedScreens` if it's not already present, helping with screen lifecycle management.</p>
     *
     * @param screen The [Screen] destination to navigate to.
     */
    fun go(screen: Screen) {
        if(screen != currentScreen) {
            screenStack.add(screen)

            if(composedScreens.fastLastOrNull { it.screen == screen } == null)
                composedScreens.add(ScreenInstance(screen))
        }
    }

    /**
     * Removes the [currentScreen] from the top of the navigation stack, effectively
     * navigating back to the previously active screen.
     *
     * <p>This operation is ignored if the stack is already considered [isEmpty]
     * (i.e., contains only the [NullScreen]).</p>
     */
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
            val screenCornerRadius = getScreenCornerRadius()

            val backTransition = animatableAs(
                spec = { spring(1f, if(predictiveGesture?.isDragged == true) 2500f else 225f) },
                value = { predictiveGesture?.progress ?: 0f }
            )

            val opacity = animatableAs(
                transitionOpacity,
                spec = {
                    val defaultSpeed = 225f + (predictiveGesture?.progress ?: 0f) * 1250f

                    if(isOpened || !isInStack || isPrevious)
                        spring(1f, if(predictiveGesture?.isDragged == true) 2500f else defaultSpeed)
                    else
                        tween(250, 0, EaseIn)
                },
                value = {
                    if(isOpened || !isInStack) 1f
                    else if(isPrevious) (predictiveGesture?.progress ?: 0f) * .75f
                    else 0f
                }
            )

            val offsetX = animatableAs(
                transitionOffsetX,
                spec = {
                    val defaultSpeed = 250f + (predictiveGesture?.progress ?: 0f) * 1250f
                    spring(1f, if(predictiveGesture?.isDragged == true) 2500f else defaultSpeed)
                },
                onAnimationFinished = { if(!isInStack && !isOpened) dispose() },
                value = {
                    if(isOpened) (predictiveGesture?.progress ?: 0f) * 350f
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

/**
 * A [androidx.compose.runtime.CompositionLocalProvider] that provides access to the current [RouterState] within
 * the Compose tree.
 *
 * <p>This allows any descendant Composable to retrieve the [RouterState] without
 * needing to pass it down explicitly through multiple function parameters.
 * To provide a [RouterState] to the local, wrap your Composables with
 * `CompositionLocalProvider(LocalScreenRouter provides routerState) { ... }`.</p>
 *
 * @throws IllegalStateException if `LocalScreenRouter` is accessed before a
 * [RouterState] has been provided in the CompositionLocalProvider.
 * @see RouterState
 * @see androidx.compose.runtime.CompositionLocalProvider
 */
val LocalScreenRouter = compositionLocalOf<RouterState> { error("RouterState isn't initialized") }