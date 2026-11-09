package com.nh.magiceraser

import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

data class EraseStroke(val points: List<Offset>, val radius: Float)

private fun buildMask(w: Int, h: Int, strokes: List<EraseStroke>): BooleanArray {
    val bm = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
    val c = android.graphics.Canvas(bm)
    val paint = android.graphics.Paint().apply {
        color = android.graphics.Color.WHITE
        style = android.graphics.Paint.Style.STROKE
        strokeCap = android.graphics.Paint.Cap.ROUND
        strokeJoin = android.graphics.Paint.Join.ROUND
        isAntiAlias = true
    }
    for (s in strokes) {
        paint.strokeWidth = s.radius * 2f
        val p = android.graphics.Path()
        s.points.forEachIndexed { i, pt -> if (i == 0) p.moveTo(pt.x, pt.y) else p.lineTo(pt.x, pt.y) }
        if (s.points.size == 1) p.lineTo(s.points[0].x, s.points[0].y)
        c.drawPath(p, paint)
    }
    val px = IntArray(w * h)
    bm.getPixels(px, 0, w, 0, 0, w, h)
    bm.recycle()
    return BooleanArray(w * h) { (px[it] ushr 24) > 64 }
}

@Composable
private fun Badge(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier.clip(RoundedCornerShape(50)).background(Color.Black.copy(alpha = 0.55f))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) { Text(text, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White) }
}

@Composable
private fun FadeBadge(visible: Boolean, text: String, modifier: Modifier = Modifier) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = fadeIn(),
        exit = fadeOut()
    ) { Badge(text) }
}

@Composable
fun ImageEraserScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    var original by remember { mutableStateOf<Bitmap?>(null) }
    var current by remember { mutableStateOf<Bitmap?>(null) }
    val history = remember { mutableStateListOf<Bitmap>() }
    val redo = remember { mutableStateListOf<Bitmap>() }
    val strokes = remember { mutableStateListOf<EraseStroke>() }
    val live = remember { mutableStateListOf<Offset>() }
    var brushPct by remember { mutableFloatStateOf(4f) }
    var expand by remember { mutableFloatStateOf(4f) }
    var busy by remember { mutableStateOf(false) }
    var compare by remember { mutableStateOf(false) }
    var comparePos by remember { mutableFloatStateOf(0.5f) }
    var zoom by remember { mutableFloatStateOf(1f) }
    var pan by remember { mutableStateOf(Offset.Zero) }

    var showSave by remember { mutableStateOf(false) }
    var png by remember { mutableStateOf(false) }
    var quality by remember { mutableFloatStateOf(92f) }
    var saving by remember { mutableStateOf(false) }
    var savedUri by remember { mutableStateOf<Uri?>(null) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) scope.launch {
            busy = true
            val b = withContext(Dispatchers.IO) { MediaUtils.loadBitmap(ctx, uri) }
            if (b == null) {
                Toast.makeText(ctx, "Could not open this image", Toast.LENGTH_SHORT).show()
            } else {
                original = b; current = b
                history.clear(); redo.clear(); strokes.clear()
                compare = false; zoom = 1f; pan = Offset.Zero; savedUri = null
            }
            busy = false
        }
    }
    val pick = { picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }

    Column(Modifier.fillMaxSize().systemBarsPadding()) {
        ScreenHeader(
            "Image Eraser",
            if (current != null) "${current!!.width} x ${current!!.height}" else "Remove anything from a photo",
            onBack
        ) {
            IconAction(
                NHIcons.Download,
                { savedUri = null; showSave = true },
                enabled = current != null && !busy
            )
        }

        BoxWithConstraints(
            Modifier.weight(1f).fillMaxWidth().padding(horizontal = 14.dp)
                .glass(RoundedCornerShape(28.dp), 0.04f).clipToBounds()
        ) {
            val cur = current
            val orig = original
            if (cur == null || orig == null) {
                Column(
                    Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        Modifier.size(96.dp).clip(CircleShape).background(Brush.linearGradient(listOf(NH.Violet.copy(0.35f), NH.Pink.copy(0.25f)))),
                        contentAlignment = Alignment.Center
                    ) { Icon(NHIcons.Image, null, tint = Color.White, modifier = Modifier.size(46.dp)) }
                    Text("Choose a photo", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 18.dp))
                    Text(
                        "Paint over the area you want gone",
                        color = NH.TextLo, fontSize = 13.sp, modifier = Modifier.padding(top = 4.dp, bottom = 22.dp)
                    )
                    GradientButton("Select image", { pick() }, icon = NHIcons.Add)
                }
            } else {
                val wPx = constraints.maxWidth.toFloat()
                val hPx = constraints.maxHeight.toFloat()
                val s0 = minOf((wPx - 24f) / cur.width, (hPx - 24f) / cur.height)
                val ox = (wPx - cur.width * s0) / 2f
                val oy = (hPx - cur.height * s0) / 2f
                val center = Offset(wPx / 2f, hPx / 2f)
                val radiusImg = brushPct / 100f * minOf(cur.width, cur.height)
                val curImg = remember(cur) { cur.asImageBitmap() }
                val origImg = remember(orig) { orig.asImageBitmap() }

                val toImg: (Offset) -> Offset = { p ->
                    val base = center + (p - center - pan) / zoom
                    Offset((base.x - ox) / s0, (base.y - oy) / s0)
                }

                Canvas(
                    Modifier.fillMaxSize().pointerInput(cur, s0, wPx, hPx, compare, radiusImg) {
                        awaitEachGesture {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            if (compare) {
                                comparePos = (down.position.x / wPx).coerceIn(0f, 1f)
                                do {
                                    val ev = awaitPointerEvent()
                                    val c = ev.changes.firstOrNull()
                                    if (c != null && c.pressed) {
                                        comparePos = (c.position.x / wPx).coerceIn(0f, 1f)
                                        c.consume()
                                    }
                                } while (ev.changes.any { it.pressed })
                                return@awaitEachGesture
                            }
                            var multi = false
                            live.clear()
                            live.add(toImg(down.position))
                            do {
                                val ev = awaitPointerEvent()
                                val pressed = ev.changes.filter { it.pressed }
                                if (pressed.size >= 2) {
                                    if (!multi) { multi = true; live.clear() }
                                    val z = ev.calculateZoom()
                                    val pn = ev.calculatePan()
                                    val cen = ev.calculateCentroid()
                                    val newZ = (zoom * z).coerceIn(1f, 8f)
                                    val rel = cen - center
                                    var np = rel - (rel - pan) * (newZ / zoom)
                                    np += pn
                                    zoom = newZ
                                    pan = if (newZ <= 1.001f) Offset.Zero else np
                                    ev.changes.forEach { it.consume() }
                                } else if (pressed.size == 1 && !multi) {
                                    live.add(toImg(pressed[0].position))
                                    pressed[0].consume()
                                }
                            } while (ev.changes.any { it.pressed })
                            if (!multi && live.isNotEmpty()) strokes.add(EraseStroke(live.toList(), radiusImg))
                            live.clear()
                        }
                    }
                ) {
                    val dw = (cur.width * s0).roundToInt()
                    val dh = (cur.height * s0).roundToInt()
                    withTransform({
                        translate(pan.x, pan.y)
                        scale(zoom, zoom, center)
                    }) {
                        drawImage(curImg, dstOffset = IntOffset(ox.roundToInt(), oy.roundToInt()), dstSize = IntSize(dw, dh))
                        if (compare) {
                            val bx = center.x + (comparePos * size.width - center.x - pan.x) / zoom
                            clipRect(-100000f, -100000f, bx, 100000f) {
                                drawImage(origImg, dstOffset = IntOffset(ox.roundToInt(), oy.roundToInt()), dstSize = IntSize(dw, dh))
                            }
                        } else {
                            val all = strokes.toList() +
                                (if (live.isNotEmpty()) listOf(EraseStroke(live.toList(), radiusImg)) else emptyList())
                            for (s in all) {
                                val p = Path()
                                s.points.forEachIndexed { i, pt ->
                                    val x = ox + pt.x * s0
                                    val y = oy + pt.y * s0
                                    if (i == 0) p.moveTo(x, y) else p.lineTo(x, y)
                                }
                                if (s.points.size == 1) p.lineTo(ox + s.points[0].x * s0, oy + s.points[0].y * s0)
                                drawPath(
                                    p, NH.Pink.copy(alpha = 0.55f),
                                    style = Stroke(s.radius * 2f * s0, cap = StrokeCap.Round, join = StrokeJoin.Round)
                                )
                            }
                        }
                    }
                    if (compare) {
                        val x = comparePos * size.width
                        drawLine(Color.White, Offset(x, 0f), Offset(x, size.height), 4f)
                        drawCircle(Color.Black.copy(alpha = 0.35f), 30f, Offset(x, size.height / 2f))
                        drawCircle(Color.White, 26f, Offset(x, size.height / 2f))
                        val tri = Path().apply {
                            moveTo(x - 6f, size.height / 2f); lineTo(x - 15f, size.height / 2f - 9f); lineTo(x - 15f, size.height / 2f + 9f); close()
                            moveTo(x + 6f, size.height / 2f); lineTo(x + 15f, size.height / 2f - 9f); lineTo(x + 15f, size.height / 2f + 9f); close()
                        }
                        drawPath(tri, NH.Violet)
                    }
                }

                if (compare) {
                    Badge("BEFORE", Modifier.align(Alignment.TopStart).padding(12.dp))
                    Badge("AFTER", Modifier.align(Alignment.TopEnd).padding(12.dp))
                }
                if (zoom > 1.01f) {
                    Badge("${(zoom * 100).roundToInt()}%", Modifier.align(Alignment.TopStart).padding(12.dp))
                }
                FadeBadge(
                    strokes.isEmpty() && !busy && !compare && history.isEmpty(),
                    "Paint over the object  -  pinch to zoom",
                    Modifier.align(Alignment.BottomCenter).padding(14.dp)
                )

                if (busy) {
                    ScanOverlay(Modifier.fillMaxSize())
                    Row(
                        Modifier.align(Alignment.Center).glass(RoundedCornerShape(50), 0.16f).padding(horizontal = 18.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CircularProgressIndicator(Modifier.size(18.dp), color = NH.Cyan, strokeWidth = 2.5.dp)
                        Text("Erasing magic...", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    }
                }
            }
        }

        if (current != null) {
            Column(
                Modifier.fillMaxWidth().padding(top = 12.dp)
                    .glass(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp), 0.07f)
                    .padding(horizontal = 18.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    ToolButton(NHIcons.Undo, "Undo", {
                        if (strokes.isNotEmpty()) strokes.removeAt(strokes.lastIndex)
                        else if (history.isNotEmpty()) {
                            current?.let { redo.add(it) }
                            current = history.removeAt(history.lastIndex)
                            if (history.isEmpty()) compare = false
                        }
                    }, enabled = !busy && (strokes.isNotEmpty() || history.isNotEmpty()))
                    ToolButton(NHIcons.Redo, "Redo", {
                        if (redo.isNotEmpty()) {
                            current?.let { history.add(it) }
                            current = redo.removeAt(redo.lastIndex)
                        }
                    }, enabled = !busy && redo.isNotEmpty())
                    ToolButton(NHIcons.Compare, "Compare", { compare = !compare }, enabled = !busy && history.isNotEmpty(), selected = compare)
                    ToolButton(NHIcons.Fit, "Reset view", { zoom = 1f; pan = Offset.Zero }, enabled = zoom > 1.01f)
                    ToolButton(NHIcons.Image, "New", { pick() }, enabled = !busy)
                }
                LabeledSlider(
                    "Brush size", "${brushPct.roundToInt()}%", brushPct, { brushPct = it }, 1f..15f,
                    enabled = !busy && !compare
                )
                LabeledSlider(
                    "Expand edges", "${expand.roundToInt()} px", expand, { expand = it }, 0f..20f,
                    enabled = !busy && !compare
                )
                GradientButton(
                    if (strokes.isEmpty()) "Paint an area to erase" else "Erase  (${strokes.size})",
                    {
                        val src = current ?: return@GradientButton
                        val list = strokes.toList()
                        val exp = expand.roundToInt()
                        scope.launch {
                            busy = true
                            val res = withContext(Dispatchers.Default) {
                                val mask = buildMask(src.width, src.height, list)
                                Inpainter.inpaint(src, mask, exp, LamaModel.get(ctx))
                            }
                            history.add(src)
                            redo.clear()
                            current = res
                            strokes.clear()
                            savedUri = null
                            busy = false
                        }
                    },
                    Modifier.fillMaxWidth(),
                    enabled = !busy && strokes.isNotEmpty() && !compare,
                    icon = NHIcons.Sparkle
                )
            }
        }
    }

    if (showSave) {
        Dialog(
            onDismissRequest = { if (!saving) showSave = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Column(
                Modifier.fillMaxWidth(0.9f).clip(RoundedCornerShape(30.dp)).background(NH.Surface2)
                    .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(30.dp))
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val saved = savedUri
                if (saved == null) {
                    Text("Save image", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Pill("JPG", !png, { png = false })
                        Pill("PNG", png, { png = true })
                    }
                    if (!png) {
                        LabeledSlider(
                            "Quality", "${quality.roundToInt()}", quality, { quality = it }, 60f..100f,
                            Modifier.fillMaxWidth()
                        )
                    }
                    GradientButton(
                        if (saving) "Saving..." else "Save to gallery",
                        {
                            val src = current ?: return@GradientButton
                            scope.launch {
                                saving = true
                                val uri = withContext(Dispatchers.IO) {
                                    MediaUtils.saveBitmap(ctx, src, png, quality.roundToInt())
                                }
                                saving = false
                                if (uri != null) savedUri = uri
                                else Toast.makeText(ctx, "Save failed", Toast.LENGTH_SHORT).show()
                            }
                        },
                        Modifier.fillMaxWidth(), enabled = !saving, icon = NHIcons.Download
                    )
                } else {
                    AnimatedCheck(Modifier.size(104.dp))
                    Text("Saved!", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                    Text("Pictures/NH Magic Eraser", color = NH.TextLo, fontSize = 13.sp)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        GradientButton(
                            "Share",
                            { MediaUtils.share(ctx, saved, if (png) "image/png" else "image/jpeg") },
                            Modifier.weight(1f), icon = NHIcons.Share
                        )
                        GradientButton(
                            "Done", { showSave = false }, Modifier.weight(1f),
                            brush = Brush.horizontalGradient(listOf(Color.White.copy(0.14f), Color.White.copy(0.14f)))
                        )
                    }
                }
            }
        }
    }
}
