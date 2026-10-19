package dev.nycocado.atlas.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

// Spacing, radius, stroke and shadow tokens from design/tokens.json.
object Space {
    val s1 = 4.dp
    val s2 = 8.dp
    val s3 = 12.dp
    val s4 = 16.dp
    val s5 = 20.dp
    val s6 = 24.dp
}

// radius-full is CircleShape.
object Radius {
    val cardSm = RoundedCornerShape(10.dp)
    val cardLg = RoundedCornerShape(16.dp)
    val md = RoundedCornerShape(22.dp)
    val lg = RoundedCornerShape(32.dp)
}

object Stroke {
    val thin = 2.dp
    val sticker = 2.5.dp
    val strong = 3.dp
}

// Hard offset down and right, in the outline color, never blurred.
object Shadow {
    val sticker = 3.dp
    val stickerLg = 5.dp
}
