package com.somnionocte.screen_router.extensions

import android.os.Build
import android.view.RoundedCorner
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp

@Composable
fun getScreenRadiusCornerInPx(): Float {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        LocalView.current.display.getRoundedCorner(RoundedCorner.POSITION_TOP_LEFT)
            ?.radius?.coerceAtLeast(0)?.toFloat() ?: 0f
    } else {
        0f
    }
}

@Composable
fun getScreenRadiusCorner(): Dp = with(LocalDensity.current) { getScreenRadiusCornerInPx().toDp() }