package com.somnionocte.screen_router

import androidx.activity.BackEventCompat
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.geometry.Offset
import kotlinx.coroutines.android.awaitFrame
import kotlinx.coroutines.launch

class PredictiveBackHandler(
    private val resetVelocity: (block: suspend () -> Unit) -> Unit,
    private val onClose: (PredictiveBackHandler) -> Unit
) {
    var velocityTransition by mutableFloatStateOf(0f)
        private set
    var transition by mutableFloatStateOf(0f)
        private set
    var touchOffset by mutableStateOf(Offset.Zero)
        private set
    var startOffset = Offset.Zero
        private set
    val isDragged by derivedStateOf { transition != 0f }

    internal val callback = object : OnBackPressedCallback(true) {
        @RequiresApi(34)
        override fun handleOnBackStarted(backEvent: BackEventCompat) {
            velocityTransition = 0f
            transition = 0f
            startOffset = Offset(backEvent.touchX, backEvent.touchY)
        }

        @RequiresApi(34)
        override fun handleOnBackProgressed(backEvent: BackEventCompat) {
            transition = backEvent.progress
            touchOffset = Offset(backEvent.touchX - startOffset.x, backEvent.touchY - startOffset.y)
        }

        override fun handleOnBackPressed() {
            velocityTransition = transition
            onClose(this@PredictiveBackHandler)
            resetVelocity {
                awaitFrame()
                velocityTransition = 0f
            }
            transition = 0f
            touchOffset = Offset.Zero
            startOffset = Offset.Zero
        }

        @RequiresApi(34)
        override fun handleOnBackCancelled() {
            velocityTransition = 0f
            transition = 0f
            touchOffset = Offset.Zero
            startOffset = Offset.Zero
        }
    }
}

@Composable
fun onPredictiveBackHandler(
    isEnabled: () -> Boolean = { true },
    onClose: (transform: PredictiveBackHandler) -> Unit
): PredictiveBackHandler {
    val scope = rememberCoroutineScope()
    val dispatcher = LocalOnBackPressedDispatcherOwner.current
    val backHandler = remember { PredictiveBackHandler({ scope.launch { it() } }, onClose) }

    if (dispatcher != null) {
        DisposableEffect(Unit) {
            scope.launch { snapshotFlow { isEnabled() }.collect { enabled ->
                backHandler.callback.isEnabled = enabled
            } }

            dispatcher.onBackPressedDispatcher.addCallback(backHandler.callback)

            onDispose { backHandler.callback.remove() }
        }
    }

    return backHandler

}