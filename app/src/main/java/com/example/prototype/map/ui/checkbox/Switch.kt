package com.example.prototype.map.ui.checkbox

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun SliderSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    width: Dp = 44.dp,
    height: Dp = 24.dp,
    padding: Dp = 2.dp,
    checkedTrackColor: Color = Color(0xFF1976D2),
    uncheckedTrackColor: Color = Color(0xFFE0E0E0),
    thumbColor: Color = Color.White,
    animationDurationMillis: Int = 360
) {
    val thumbSize = height - padding * 2

    val maxThumbOffsetX = width - thumbSize - padding * 2

    val thumbOffsetX by animateDpAsState(
        targetValue = if (checked) maxThumbOffsetX else 0.dp,
        animationSpec = tween(
            durationMillis = animationDurationMillis,
            easing = FastOutSlowInEasing
        ),
        label = "switchThumbPosition"
    )

//    Track
    Box(
        modifier = modifier
            .size(width, height)
            .clip(RoundedCornerShape(percent = 50))
            .background(color = if (checked) checkedTrackColor else uncheckedTrackColor)
            .clickable { onCheckedChange(!checked) }
            .padding(padding)
    ) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.CenterStart
        ) {
            Box(
                modifier = modifier
                    .offset(x = thumbOffsetX)
                    .size(thumbSize)
                    .background(color = thumbColor, shape = CircleShape)
            )
        }
    }
}