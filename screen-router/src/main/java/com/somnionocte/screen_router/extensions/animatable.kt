package com.somnionocte.screen_router.extensions

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Animated state without calling recomposition. Only work with compose states.
 * */
@Composable
inline fun animatableAs(
    animatable: Animatable<Float, AnimationVector1D>,
    crossinline spec: () -> AnimationSpec<Float> = { spring() },
    crossinline onAnimationFinished: () -> Unit = {  },
    crossinline value: () -> Float
): Animatable<Float, AnimationVector1D> {
    val state by remember { derivedStateOf { value() } }
    val spec by remember { derivedStateOf { spec() } }

    LaunchedEffect(Unit) {
        snapshotFlow { state }.collectLatest { value ->
            launch {
                animatable.animateTo(value, spec)
                onAnimationFinished()
            }
        }
    }

    return animatable
}

/**
 * Animated state without calling recomposition. Only work with compose states.
 * */
@Composable
inline fun animatableAs(
    crossinline spec: () -> AnimationSpec<Float> = { spring() },
    initialValue: Float = 0f,
    crossinline onAnimationFinished: () -> Unit = {  },
    crossinline value: () -> Float
): Animatable<Float, AnimationVector1D> {
    val animatable = remember { Animatable(initialValue) }
    return animatableAs(animatable, spec, onAnimationFinished, value)
}