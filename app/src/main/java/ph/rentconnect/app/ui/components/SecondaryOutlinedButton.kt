package ph.rentconnect.app.ui.components

import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ph.rentconnect.app.ui.theme.Orange500

@Composable
fun SecondaryOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isCompact: Boolean = true,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val alpha by animateFloatAsState(
        targetValue = if (isPressed) 0.5f else 1.0f,
        animationSpec = tween(durationMillis = 80, easing = EaseOut),
        label = "press-alpha",
    )

    val fontSize = if (isCompact) 12.sp else 14.sp
    val hPad = if (isCompact) 12.dp else 20.dp
    val vPad = if (isCompact) 6.dp else 10.dp
    val corner = if (isCompact) 8.dp else 12.dp
    val weight = if (isCompact) FontWeight.Medium else FontWeight.SemiBold

    val shape = RoundedCornerShape(corner)

    Box(
        modifier = modifier
            .clip(shape)
            .border(1.dp, Orange500, shape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = hPad, vertical = vPad)
            .alpha(alpha),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = Orange500,
            fontSize = fontSize,
            fontWeight = weight,
        )
    }
}
