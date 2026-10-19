package dev.nycocado.atlas.ui.theme

import androidx.compose.foundation.IndicationNodeFactory
import androidx.compose.foundation.interaction.FocusInteraction
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.node.DelegatableNode
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.invalidateDraw
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.drawscope.Stroke as StrokeStyle

// The app's default indication (see AtlasTheme): a ring only while focused by keyboard or switch access.
// Touch shows nothing, since every control has its own feedback.
class FocusRing(
    private val color: Color,
    private val shape: Shape = CircleShape,
) : IndicationNodeFactory {
    override fun create(interactionSource: InteractionSource): DelegatableNode =
        FocusRingNode(interactionSource, color, shape)

    override fun equals(other: Any?) = other is FocusRing && other.color == color && other.shape == shape

    override fun hashCode() = 31 * color.hashCode() + shape.hashCode()
}

private class FocusRingNode(
    private val interactionSource: InteractionSource,
    private val color: Color,
    private val shape: Shape,
) : Modifier.Node(),
    DrawModifierNode {
    private var focused = false

    override fun onAttach() {
        coroutineScope.launch {
            interactionSource.interactions.collect {
                when (it) {
                    is FocusInteraction.Focus -> focused = true
                    is FocusInteraction.Unfocus -> focused = false
                }
                invalidateDraw()
            }
        }
    }

    override fun ContentDrawScope.draw() {
        drawContent()
        if (!focused) return
        val stroke = 3.dp.toPx()
        // 3dp gap around the element, then the 3dp ring (docs/design.md).
        val inset = 3.dp.toPx() + stroke / 2
        translate(-inset, -inset) {
            val ring = Size(size.width + inset * 2, size.height + inset * 2)
            drawOutline(shape.createOutline(ring, layoutDirection, this), color, style = StrokeStyle(stroke))
        }
    }
}
