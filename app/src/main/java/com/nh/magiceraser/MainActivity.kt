package com.nh.magiceraser

import android.graphics.Color as AColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

enum class Screen { Splash, Home, Image, Video }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(AColor.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(AColor.TRANSPARENT)
        )
        setContent {
            NHTheme {
                NHBackground { App() }
            }
        }
    }
}

@Composable
fun App() {
    var screen by remember { mutableStateOf(Screen.Splash) }
    LaunchedEffect(Unit) {
        delay(1700)
        screen = Screen.Home
    }
    AnimatedContent(
        targetState = screen,
        transitionSpec = {
            (fadeIn(tween(380)) + scaleIn(tween(380), initialScale = 0.96f)) togetherWith fadeOut(tween(220))
        },
        label = "screens"
    ) { s ->
        when (s) {
            Screen.Splash -> SplashScreen()
            Screen.Home -> HomeScreen { screen = it }
            Screen.Image -> {
                BackHandler { screen = Screen.Home }
                ImageEraserScreen { screen = Screen.Home }
            }
            Screen.Video -> {
                BackHandler { screen = Screen.Home }
                VideoEraserScreen { screen = Screen.Home }
            }
        }
    }
}

@Composable
fun SplashScreen() {
    val t = rememberInfiniteTransition(label = "splash")
    val pulse by t.animateFloat(
        0.9f, 1.12f,
        infiniteRepeatable(tween(900, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulse"
    )
    val appear by animateFloatAsState(1f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow), label = "appear")
    val load by t.animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(1100, easing = LinearEasing), RepeatMode.Restart),
        label = "load"
    )
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(18.dp)) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.graphicsLayer { scaleX = appear; scaleY = appear }) {
                Box(
                    Modifier.size(150.dp).graphicsLayer { scaleX = pulse; scaleY = pulse }
                        .background(
                            Brush.radialGradient(listOf(NH.Violet.copy(alpha = 0.55f), Color.Transparent)), CircleShape
                        )
                )
                Box(
                    Modifier.size(96.dp).clip(RoundedCornerShape(30.dp)).background(
                        Brush.linearGradient(listOf(NH.Violet, NH.Pink))
                    ),
                    contentAlignment = Alignment.Center
                ) { Icon(NHIcons.Sparkle, null, tint = Color.White, modifier = Modifier.size(52.dp)) }
            }
            GradientText("NH Magic Eraser", 30)
            Text("by NH Developer", color = NH.TextLo, fontSize = 13.sp)
            Box(
                Modifier.padding(top = 16.dp).size(width = 120.dp, height = 4.dp)
                    .clip(RoundedCornerShape(50)).background(Color.White.copy(alpha = 0.1f))
                    .drawBehind {
                        val w = size.width * 0.4f
                        val x = (size.width + w) * load - w
                        drawRect(NH.Accent, Offset(x.coerceAtLeast(0f), 0f), androidx.compose.ui.geometry.Size(
                            (x + w).coerceAtMost(size.width) - x.coerceAtLeast(0f), size.height))
                    }
            )
        }
    }
}

@Composable
private fun Reveal(index: Int, content: @Composable () -> Unit) {
    var show by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { show = true }
    AnimatedVisibility(
        show,
        enter = fadeIn(tween(550, delayMillis = index * 110)) +
            slideInVertically(tween(550, delayMillis = index * 110)) { it / 4 }
    ) { content() }
}

@Composable
fun FeatureCard(
    title: String, desc: String, icon: ImageVector, c1: Color, c2: Color, tag: String, onClick: () -> Unit
) {
    val src = remember { MutableInteractionSource() }
    val pressed by src.collectIsPressedAsState()
    val sc by animateFloatAsState(if (pressed) 0.97f else 1f, spring(Spring.DampingRatioMediumBouncy), label = "fc")
    val t = rememberInfiniteTransition(label = "shine")
    val shimmer by t.animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(3400, easing = LinearEasing), RepeatMode.Restart),
        label = "shimmer"
    )
    Box(
        Modifier.fillMaxWidth().height(172.dp)
            .graphicsLayer { scaleX = sc; scaleY = sc }
            .clip(RoundedCornerShape(32.dp))
            .background(Brush.linearGradient(listOf(c1, c2)))
            .drawBehind {
                drawCircle(Color.White.copy(alpha = 0.10f), size.height * 0.9f, Offset(size.width * 0.95f, size.height * 0.1f))
                drawCircle(Color.White.copy(alpha = 0.07f), size.height * 0.55f, Offset(size.width * 0.72f, size.height))
                val x = -size.width * 0.4f + shimmer * size.width * 1.9f
                drawRect(
                    Brush.linearGradient(
                        listOf(Color.Transparent, Color.White.copy(alpha = 0.20f), Color.Transparent),
                        Offset(x, 0f), Offset(x + size.width * 0.35f, size.height)
                    )
                )
            }
            .clickable(interactionSource = src, indication = null, onClick = onClick)
            .padding(22.dp)
    ) {
        Column(Modifier.align(Alignment.CenterStart).fillMaxWidth(0.66f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(Modifier.clip(RoundedCornerShape(50)).background(Color.Black.copy(alpha = 0.25f)).padding(horizontal = 10.dp, vertical = 4.dp)) {
                Text(tag, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
            Text(title, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
            Text(desc, fontSize = 13.sp, color = Color.White.copy(alpha = 0.88f), lineHeight = 17.sp)
        }
        Box(
            Modifier.align(Alignment.CenterEnd).size(88.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.22f)),
            contentAlignment = Alignment.Center
        ) { Icon(icon, null, tint = Color.White, modifier = Modifier.size(46.dp)) }
    }
}

@Composable
private fun StatTile(icon: ImageVector, title: String, sub: String, tint: Color, modifier: Modifier) {
    Column(
        modifier.glass(RoundedCornerShape(22.dp)).padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(22.dp))
        Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Text(sub, fontSize = 11.sp, color = NH.TextLo)
    }
}

@Composable
fun HomeScreen(onOpen: (Screen) -> Unit) {
    val ctx = LocalContext.current
    val prefs = remember { ctx.getSharedPreferences("nh", 0) }
    var showNotice by remember { mutableStateOf(!prefs.getBoolean("notice_ok", false)) }

    if (showNotice) {
        AlertDialog(
            onDismissRequest = {},
            containerColor = NH.Surface2,
            titleContentColor = NH.TextHi,
            textContentColor = NH.TextLo,
            shape = RoundedCornerShape(28.dp),
            title = { Text("Before you start", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Use this tool only on content you own or have permission to edit. " +
                        "Do not remove ownership, copyright or AI-origin marks to mislead other people."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    prefs.edit().putBoolean("notice_ok", true).apply()
                    showNotice = false
                }) { Text("I understand", color = NH.Cyan, fontWeight = FontWeight.Bold) }
            }
        )
    }

    Column(
        Modifier.fillMaxSize().systemBarsPadding().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Reveal(0) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    Modifier.size(38.dp).clip(RoundedCornerShape(12.dp)).background(Brush.linearGradient(listOf(NH.Violet, NH.Pink))),
                    contentAlignment = Alignment.Center
                ) { Icon(NHIcons.Sparkle, null, tint = Color.White, modifier = Modifier.size(22.dp)) }
                Column {
                    Text("NH Magic Eraser", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("by NH Developer", fontSize = 11.sp, color = NH.TextLo)
                }
            }
        }
        Reveal(1) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Erase the", fontSize = 40.sp, fontWeight = FontWeight.ExtraBold, lineHeight = 42.sp)
                GradientText("unwanted.", 44)
                Text(
                    "Remove objects, logos and text from your photos and videos. Private, offline, and fast.",
                    color = NH.TextLo, fontSize = 14.sp, lineHeight = 20.sp
                )
            }
        }
        Reveal(2) {
            FeatureCard(
                "Image Eraser", "Paint over anything. Zoom, compare before and after, export in HD.",
                NHIcons.Image, Color(0xFF7C5CFF), Color(0xFFB04DFF), "PHOTOS"
            ) { onOpen(Screen.Image) }
        }
        Reveal(3) {
            FeatureCard(
                "Video Eraser", "Box a static logo and export a clean video. Original audio is kept.",
                NHIcons.Movie, Color(0xFF0EA5E9), Color(0xFF7C5CFF), "VIDEOS"
            ) { onOpen(Screen.Video) }
        }
        Reveal(4) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatTile(NHIcons.Bolt, "Offline", "No internet", NH.Amber, Modifier.weight(1f))
                StatTile(NHIcons.Check, "No ads", "Clean and private", NH.Green, Modifier.weight(1f))
                StatTile(NHIcons.Download, "HD export", "Full quality", NH.Cyan, Modifier.weight(1f))
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}
