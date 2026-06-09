package io.github.kmpstore.util

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import io.github.oikvpqya.compose.fastscroller.ScrollbarStyle
import io.github.oikvpqya.compose.fastscroller.ThumbStyle
import io.github.oikvpqya.compose.fastscroller.TrackStyle


fun scrollbarStyle(color: Color) = ScrollbarStyle(
    minimalHeight = 16.dp,
    thickness = 8.dp,
    hoverDurationMillis = 300,
    thumbStyle = ThumbStyle(
        shape = RoundedCornerShape(4.dp),
        unhoverColor = color.copy(alpha = 0.12f),
        hoverColor = color.copy(alpha = 0.50f),
    ),
    trackStyle = TrackStyle(
        shape = RectangleShape,
        unhoverColor = Color.Transparent,
        hoverColor = Color.Transparent,
    ),
)