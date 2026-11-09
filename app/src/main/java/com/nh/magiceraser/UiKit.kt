package com.nh.magiceraser

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.statusBarsPadding
import kotlin.math.min
import kotlin.random.Random

@Composable
fun NHBackground(content: @Composable BoxScope.() -> Unit) {
    val t = rememberInfiniteTransition(label = "bg")
    val a by t.animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(9000, easing = LinearEasing), RepeatMode.Reverse),
        label = "a"
    )
    Box(
        Modifier.fillMaxSize().background(NH.Bg).drawBehind {
            val r = size.width * 0.9f
            val c1 = Offset(size.width * (0.1f + 0.3f * a), size.height * 0.08f)
            drawCircle(Brush.radialGradient(listOf(NH.Violet.copy(alpha = 0.38f), Color.Transparent), c1, r), r, c1)
            val c2 = Offset(size.width * (0.9f - 0.3f * a), size.height * 0.95f)
            drawCircle(Brush.radialGradient(listOf(NH.Pink.copy(alpha = 0.22f), Color.Transparent), c2, r), r, c2)
            val c3 = Offset(size.width * 0.9f, size.height * (0.45f + 0.1f * a))
            drawCircle(Brush.radialGradient(listOf(NH.Cyan.copy(alpha = 0.12f), Color.Transparent), c3, r * 0.7f), r * 0.7f, c3)
        },
        content = content
    )
}

fun Modifier.glass(shape: Shape = RoundedCornerShape(24.dp), alpha: Float = 0.07f): Modifier =
    this.clip(shape)
        .background(Color.White.copy(alpha = alpha))
        .border(
            1.dp,
            Brush.linearGradient(listOf(Color.White.copy(alpha = 0.24f), Color.White.copy(alpha = 0.04f))),
            shape
        )

@Composable
fun GradientText(text: String, size: Int, brush: Brush = NH.Accent, modifier: Modifier = Modifier) {
    Text(
        text,
        modifier,
        style = androidx.compose.ui.text.TextStyle(
            brush = brush, fontSize = size.sp, fontWeight = FontWeight.ExtraBold, lineHeight = (size * 1.1f).sp
        )
    )
}

@Composable
fun GradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    brush: Brush = NH.Accent
) {
    val src = remember { MutableInteractionSource() }
    val pressed by src.collectIsPressedAsState()
    val sc by animateFloatAsState(if (pressed) 0.95f else 1f, spring(Spring.DampingRatioMediumBouncy), label = "gb")
    Box(
        modifier
            .graphicsLayer { scaleX = sc; scaleY = sc; alpha = if (enabled) 1f else 0.35f }
            .clip(RoundedCornerShape(20.dp))
            .background(brush)
            .clickable(interactionSource = src, indication = null, enabled = enabled, onClick = onClick)
            .padding(horizontal = 22.dp, vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (icon != null) Icon(icon, null, tint = Color.White, modifier = Modifier.size(20.dp))
            Text(text, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

@Composable
fun IconAction(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    tint: Color = Color.White,
    size: Dp = 44.dp
) {
    val src = remember { MutableInteractionSource() }
    val pressed by src.collectIsPressedAsState()
    val sc by animateFloatAsState(if (pressed) 0.88f else 1f, spring(Spring.DampingRatioMediumBouncy), label = "ia")
    Box(
        modifier
            .size(size)
            .graphicsLayer { scaleX = sc; scaleY = sc; alpha = if (enabled) 1f else 0.35f }
            .glass(CircleShape, 0.09f)
            .clickable(interactionSource = src, indication = null, enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) { Icon(icon, null, tint = tint, modifier = Modifier.size(size * 0.5f)) }
}

@Composable
fun ToolButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    selected: Boolean = false
) {
    val src = remember { MutableInteractionSource() }
    val pressed by src.collectIsPressedAsState()
    val sc by animateFloatAsState(if (pressed) 0.9f else 1f, spring(Spring.DampingRatioMediumBouncy), label = "tb")
    Column(
        modifier
            .graphicsLayer { scaleX = sc; scaleY = sc; alpha = if (enabled) 1f else 0.35f }
            .clickable(interactionSource = src, indication = null, enabled = enabled, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Box(
            Modifier.size(48.dp).clip(RoundedCornerShape(16.dp))
                .background(if (selected) NH.Accent else Brush.linearGradient(listOf(Color.White.copy(0.09f), Color.White.copy(0.09f)))),
            contentAlignment = Alignment.Center
        ) { Icon(icon, null, tint = Color.White, modifier = Modifier.size(24.dp)) }
        Text(label, fontSize = 11.sp, color = if (selected) Color.White else NH.TextLo, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun Pill(text: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier.clip(RoundedCornerShape(50))
            .background(if (selected) NH.Accent else Brush.linearGradient(listOf(Color.White.copy(0.08f), Color.White.copy(0.08f))))
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 9.dp),
        contentAlignment = Alignment.Center
    ) { Text(text, color = if (selected) Color.White else NH.TextLo, fontWeight = FontWeight.SemiBold, fontSize = 14.sp) }
}

@Composable
fun LabeledSlider(
    label: String,
    valueText: String,
    value: Float,
    onChange: (Float) -> Unit,
    range: ClosedFloatingPointRange<Float>,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onFinished: (() -> Unit)? = null
) {
    Column(modifier) {
        Row(Modifier.fillMaxWidth()) {
            Text(label, fontSize = 13.sp, color = NH.TextLo, fontWeight = FontWeight.Medium)
            androidx.compose.foundation.layout.Spacer(Modifier.weight(1f))
            Text(valueText, fontSize = 13.sp, color = NH.Cyan, fontWeight = FontWeight.Bold)
        }
        Slider(
            value, onChange,
            valueRange = range,
            enabled = enabled,
            onValueChangeFinished = onFinished,
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = NH.Violet,
                inactiveTrackColor = Color.White.copy(alpha = 0.14f),
                disabledThumbColor = Color.White.copy(alpha = 0.4f),
                disabledActiveTrackColor = Color.White.copy(alpha = 0.2f),
                disabledInactiveTrackColor = Color.White.copy(alpha = 0.1f)
            )
        )
    }
}

@Composable
fun ScreenHeader(
    title: String,
    subtitle: String? = null,
    onBack: () -> Unit,
    trailing: @Composable RowScope.() -> Unit = {}
) {
    Row(
        Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        IconAction(NHIcons.Back, onBack)
        Column(Modifier.weight(1f)) {
            Text(title, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            if (subtitle != null) Text(subtitle, fontSize = 12.sp, color = NH.TextLo)
        }
        trailing()
    }
}

@Composable
fun ProgressRing(progress: Float, modifier: Modifier = Modifier, stroke: Float = 18f) {
    val spin = rememberInfiniteTransition(label = "ring")
    val glow by spin.animateFloat(
        0.25f, 0.6f,
        infiniteRepeatable(tween(1400, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "glow"
    )
    Canvas(modifier) {
        val d = min(size.width, size.height) - stroke * 2
        val tl = Offset((size.width - d) / 2f, (size.height - d) / 2f)
        val sz = Size(d, d)
        drawArc(Color.White.copy(alpha = 0.08f), 0f, 360f, false, tl, sz, style = Stroke(stroke))
        val sweep = 360f * progress.coerceIn(0f, 1f)
        val brush = Brush.sweepGradient(listOf(NH.Cyan, NH.Violet, NH.Pink, NH.Cyan))
        rotate(-90f) {
            drawArc(brush, 0f, sweep, false, tl, sz, alpha = glow, style = Stroke(stroke * 1.9f, cap = StrokeCap.Round))
            drawArc(brush, 0f, sweep, false, tl, sz, style = Stroke(stroke, cap = StrokeCap.Round))
        }
    }
}

@Composable
fun AnimatedCheck(modifier: Modifier = Modifier, color: Color = NH.Green) {
    val p = remember { Animatable(0f) }
    val pop = remember { Animatable(0.6f) }
    LaunchedEffect(Unit) {
        pop.animateTo(1f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow))
    }
    LaunchedEffect(Unit) { p.animateTo(1f, tween(1000, easing = FastOutSlowInEasing)) }
    Canvas(modifier.graphicsLayer { scaleX = pop.value; scaleY = pop.value }) {
        val w = size.width
        val stroke = w * 0.07f
        val v = p.value
        drawCircle(color.copy(alpha = 0.14f * v), radius = w / 2f)
        val arcSize = Size(w - stroke, w - stroke)
        rotate(-90f) {
            drawArc(
                color, 0f, 360f * min(1f, v / 0.6f), false,
                Offset(stroke / 2, stroke / 2), arcSize, style = Stroke(stroke, cap = StrokeCap.Round)
            )
        }
        val t = ((v - 0.55f) / 0.45f).coerceIn(0f, 1f)
        if (t > 0f) {
            val a = Offset(w * 0.30f, w * 0.52f)
            val b = Offset(w * 0.45f, w * 0.67f)
            val c = Offset(w * 0.72f, w * 0.38f)
            val st = Stroke(stroke, cap = StrokeCap.Round)
            if (t <= 0.4f) {
                val f = t / 0.4f
                drawLine(color, a, Offset(a.x + (b.x - a.x) * f, a.y + (b.y - a.y) * f), stroke, StrokeCap.Round)
            } else {
                val f = (t - 0.4f) / 0.6f
                drawLine(color, a, b, stroke, StrokeCap.Round)
                drawLine(color, b, Offset(b.x + (c.x - b.x) * f, b.y + (c.y - b.y) * f), stroke, StrokeCap.Round)
            }
        }
    }
}

private data class Particle(val vx: Float, val vy: Float, val color: Int, val size: Float, val rot: Float)

@Composable
fun Confetti(modifier: Modifier = Modifier) {
    val parts = remember {
        val r = Random(7)
        List(56) {
            Particle(
                (r.nextFloat() - 0.5f) * 1.3f, 0.35f + r.nextFloat() * 0.6f,
                r.nextInt(4), 8f + r.nextFloat() * 10f, r.nextFloat() * 360f
            )
        }
    }
    val t = remember { Animatable(0f) }
    LaunchedEffect(Unit) { t.animateTo(1f, tween(3000, easing = LinearEasing)) }
    val cols = listOf(NH.Violet, NH.Pink, NH.Cyan, NH.Amber)
    Canvas(modifier.fillMaxSize()) {
        val tt = t.value
        parts.forEach { p ->
            val x = size.width * (0.5f + p.vx * tt)
            val y = size.height * 0.30f - size.height * p.vy * tt * 0.8f + size.height * 1.1f * tt * tt
            val a = (1f - tt * tt).coerceIn(0f, 1f)
            rotate(p.rot + tt * 540f, Offset(x, y)) {
                drawRect(cols[p.color].copy(alpha = a), Offset(x - p.size / 2, y - p.size / 4), Size(p.size, p.size / 2))
            }
        }
    }
}

@Composable
fun ScanOverlay(modifier: Modifier = Modifier, color: Color = NH.Cyan) {
    val t = rememberInfiniteTransition(label = "scan")
    val p by t.animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(1900, easing = LinearEasing), RepeatMode.Restart),
        label = "p"
    )
    Canvas(modifier) {
        val y = size.height * p
        val band = 140f
        drawRect(
            Brush.verticalGradient(
                listOf(Color.Transparent, color.copy(alpha = 0.32f), Color.Transparent),
                startY = y - band, endY = y + band
            ),
            Offset(0f, y - band), Size(size.width, band * 2)
        )
        drawLine(color.copy(alpha = 0.9f), Offset(0f, y), Offset(size.width, y), 3f)
    }
}
