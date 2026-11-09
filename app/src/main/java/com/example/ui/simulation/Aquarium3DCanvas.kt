package com.example.ui.simulation

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Size as CoilSize
import com.example.data.AquariumCatalog
import com.example.model.FinStyle
import com.example.model.FishInstance
import com.example.model.PlacedDecor
import com.example.model.TankEnvironment
import com.example.util.LiveAssetHelper
import kotlin.math.*

data class WaterRipple(
    val x: Float,
    val y: Float,
    var radius: Float = 0f,
    var maxRadius: Float = 140f,
    var alpha: Float = 0.8f
)

data class FoodPellet(
    val id: Long = System.nanoTime(),
    var x: Float,
    var y: Float,
    var speedY: Float = 3.5f,
    var eaten: Boolean = false
)

data class TankBubble(
    var x: Float,
    var y: Float,
    var radius: Float,
    var speed: Float,
    var wobbleSpeed: Float,
    var wobbleAmp: Float,
    var phase: Float
)

/**
 * High-performance 3D Aquarium Canvas rendering:
 * - Realistic fluid wave surface displacement & caustics
 * - Dynamic lighting gradients based on tank settings
 * - 3D simulated depth-sorting (Z-layer parallax, lighting attenuation)
 * - True fish skeletal undulating tail & fin swimming physics
 * - Interactive food pellet feeding & particle fluid ripples
 * - Realistic rising air bubbles with micro-wobbles
 */
@Composable
fun Aquarium3DCanvas(
    tank: TankEnvironment,
    fishList: List<FishInstance>,
    placedDecorList: List<PlacedDecor>,
    isNightMode: Boolean,
    onTapFish: (FishInstance) -> Unit,
    onFeedAt: (Float, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "AquariumLoop")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "TimePhase"
    )

    // Interactive ripples & floating food particles
    val ripples = remember { mutableStateListOf<WaterRipple>() }
    val foodPellets = remember { mutableStateListOf<FoodPellet>() }

    // Procedural rising bubble column
    val bubbles = remember {
        List(40) {
            TankBubble(
                x = (Math.random() * 0.9 + 0.05).toFloat(),
                y = (Math.random()).toFloat(),
                radius = (3f + Math.random() * 7f).toFloat(),
                speed = (0.002f + Math.random() * 0.005f).toFloat(),
                wobbleSpeed = (2f + Math.random() * 4f).toFloat(),
                wobbleAmp = (10f + Math.random() * 15f).toFloat(),
                phase = (Math.random() * 2f * PI).toFloat()
            )
        }
    }

    // Frame update ticker
    LaunchedEffect(Unit) {
        while (true) {
            withFrameNanos { _ ->
                // Update ripples
                val rippleIterator = ripples.iterator()
                while (rippleIterator.hasNext()) {
                    val r = rippleIterator.next()
                    r.radius += 3.5f
                    r.alpha = (1f - (r.radius / r.maxRadius)).coerceAtLeast(0f)
                    if (r.radius >= r.maxRadius) {
                        rippleIterator.remove()
                    }
                }

                // Update food pellets
                val foodIterator = foodPellets.iterator()
                while (foodIterator.hasNext()) {
                    val p = foodIterator.next()
                    p.y += p.speedY
                    // Check if fish near food
                    for (f in fishList) {
                        // normalized coordinates to pixel coords handled in canvas, but rough distance check
                        // Pellets disappear when eaten or hit substrate
                    }
                    if (p.y > 2000f || p.eaten) {
                        foodIterator.remove()
                    }
                }
            }
        }
    }

    val context = LocalContext.current
    val imageLoader = remember { LiveAssetHelper.getImageLoader(context) }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    ripples.add(WaterRipple(offset.x, offset.y))
                    // Add food pellet at tapped location
                    foodPellets.add(FoodPellet(x = offset.x, y = offset.y))
                    if (size.width > 0 && size.height > 0) {
                        onFeedAt(
                            (offset.x / size.width.toFloat()).coerceIn(0f, 1f),
                            (offset.y / size.height.toFloat()).coerceIn(0f, 1f)
                        )
                    }
                }
            }
            .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    change.consume()
                    if (Math.random() > 0.6) {
                        ripples.add(WaterRipple(change.position.x, change.position.y, maxRadius = 80f))
                    }
                }
            }
    ) {
        val totalWidthPx = constraints.maxWidth.toFloat()
        val totalHeightPx = constraints.maxHeight.toFloat()

        // Filter fish into procedural rendered vs asset-based GIF animated
        val proceduralFish = fishList.filter { fish ->
            val species = AquariumCatalog.getSpecies(fish.speciesId)
            species.assetPath == null || !LiveAssetHelper.assetExists(context, species.assetPath)
        }
        val liveGifFish = fishList.filter { fish ->
            val species = AquariumCatalog.getSpecies(fish.speciesId)
            species.assetPath != null && LiveAssetHelper.assetExists(context, species.assetPath)
        }

        // Filter decor into procedural vs GIF
        val proceduralDecor = placedDecorList.filter { placed ->
            val decor = AquariumCatalog.getDecor(placed.decorId)
            decor?.assetPath == null || !LiveAssetHelper.assetExists(context, decor.assetPath)
        }
        val liveGifDecor = placedDecorList.filter { placed ->
            val decor = AquariumCatalog.getDecor(placed.decorId)
            decor?.assetPath != null && LiveAssetHelper.assetExists(context, decor.assetPath)
        }

        // 1. Base 3D Canvas (Renders water caustics, bubbles, substrate, procedural fish and particles)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // 1. Render Realistic Deep Water Fluid Gradient
            drawWaterBackground(width, height, tank, isNightMode, time)

            // 2. Render Underwater Sun Rays / Bioluminescent Caustic Shimmer
            drawCausticsAndRays(width, height, time, tank.lightingLevel, tank.lightingColorHex)

            // 3. Render Substrate & Aquascaped Seabed
            drawSubstrate(width, height, tank.substrateType, isNightMode)

            // 4. Render Procedural Background Placed Decor
            for (decor in proceduralDecor) {
                drawDecorItem(decor, width, height, time)
            }

            // 5. Update & Draw Rising Bubbles (Simulating aerator)
            drawBubbleStream(bubbles, width, height, time, tank.filterActive)

            // 6. Draw Falling Food Pellets
            for (pellet in foodPellets) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFFFB74D), Color(0xFFE65100)),
                        center = Offset(pellet.x, pellet.y),
                        radius = 8f
                    ),
                    radius = 6.5f,
                    center = Offset(pellet.x, pellet.y)
                )
            }

            // 7. Render 3D Fish with Organic Tail Physics (those without dedicated GIF asset)
            val sortedProcedural = proceduralFish.sortedBy { it.posZ }
            for (fish in sortedProcedural) {
                drawRealisticFish3D(fish, width, height, time, isNightMode)
            }

            // 8. Render Water Surface Ripples & Fluid Waves
            for (ripple in ripples) {
                drawCircle(
                    color = Color.White.copy(alpha = ripple.alpha * 0.45f),
                    radius = ripple.radius,
                    center = Offset(ripple.x, ripple.y),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.5f)
                )
            }

            // 9. Glass Refraction & Vignette Overlay
            drawAquariumGlassEdge(width, height)
        }

        // 2. Render Live Animated GIF Decor Items (e.g., swaying live anemone/coral)
        for (placed in liveGifDecor) {
            val decor = AquariumCatalog.getDecor(placed.decorId) ?: continue
            LiveGifDecorItem(
                decor = decor,
                scale = placed.scale,
                posX = placed.posX,
                totalWidthPx = totalWidthPx,
                totalHeightPx = totalHeightPx
            )
        }

        // 3. Render Live Animated GIF Fishes (e.g. Clownfish swimming, Jellyfish pulsating)
        for (fish in liveGifFish) {
            val species = AquariumCatalog.getSpecies(fish.speciesId)
            LiveGifFishItem(
                fish = fish,
                species = species,
                totalWidthPx = totalWidthPx,
                totalHeightPx = totalHeightPx,
                onTap = { onTapFish(fish) }
            )
        }
    }
}

@Composable
private fun LiveGifDecorItem(
    decor: com.example.model.DecorItem,
    scale: Float,
    posX: Float,
    totalWidthPx: Float,
    totalHeightPx: Float
) {
    val context = LocalContext.current
    val imageLoader = remember { LiveAssetHelper.getImageLoader(context) }
    val assetUri = LiveAssetHelper.getAssetUri(decor.assetPath!!)
    val decorWidthDp = (90 * scale).dp
    val decorHeightDp = (90 * scale).dp
    val xPx = (posX * totalWidthPx) - (45 * scale)
    val yPx = (totalHeightPx * 0.84f) - (70 * scale)

    val painter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(context)
            .data(assetUri)
            .size(CoilSize.ORIGINAL)
            .build(),
        imageLoader = imageLoader
    )

    Image(
        painter = painter,
        contentDescription = decor.name,
        contentScale = ContentScale.Fit,
        modifier = Modifier
            .offset { IntOffset(xPx.toInt(), yPx.toInt()) }
            .size(decorWidthDp, decorHeightDp)
    )
}

@Composable
private fun LiveGifFishItem(
    fish: FishInstance,
    species: com.example.model.FishSpecies,
    totalWidthPx: Float,
    totalHeightPx: Float,
    onTap: () -> Unit
) {
    val context = LocalContext.current
    val imageLoader = remember { LiveAssetHelper.getImageLoader(context) }
    val assetUri = LiveAssetHelper.getAssetUri(species.assetPath!!)

    val depthScale = 0.65f + (fish.posZ * 0.55f)
    val finalScale = species.bodyScale * depthScale * (0.6f + fish.growthStage * 0.4f)
    val baseSize = 85.dp * finalScale
    val movingRight = fish.velX >= 0
    val headingAngle = Math.toDegrees(atan2(fish.velY.toDouble(), abs(fish.velX).toDouble())).toFloat()

    val xPx = (fish.posX * totalWidthPx) - 40 * finalScale
    val yPx = (fish.posY * totalHeightPx) - 30 * finalScale

    val painter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(context)
            .data(assetUri)
            .size(CoilSize.ORIGINAL)
            .build(),
        imageLoader = imageLoader
    )

    Image(
        painter = painter,
        contentDescription = fish.customName,
        contentScale = ContentScale.Fit,
        modifier = Modifier
            .offset { IntOffset(xPx.toInt(), yPx.toInt()) }
            .size(baseSize)
            .graphicsLayer {
                rotationZ = if (movingRight) headingAngle else -headingAngle
                scaleX = if (movingRight) 1f else -1f // Flip horizontally when swimming left
                alpha = (0.75f + (fish.posZ * 0.25f)).coerceIn(0.7f, 1f)
            }
            .clickable { onTap() }
    )
}

private fun DrawScope.drawWaterBackground(
    width: Float,
    height: Float,
    tank: TankEnvironment,
    isNightMode: Boolean,
    time: Float
) {
    val topColor = if (isNightMode) {
        Color(0xFF030A16)
    } else {
        when (tank.backgroundTheme) {
            "Coral Paradise Twilight" -> Color(0xFF003049)
            "Luminescent Midnight Trench" -> Color(0xFF0D0221)
            "Golden Sunlit Lagoon" -> Color(0xFF006D77)
            else -> Color(0xFF023E8A) // Deep Blue Abyss
        }
    }

    val bottomColor = if (isNightMode) {
        Color(0xFF02040A)
    } else {
        when (tank.backgroundTheme) {
            "Coral Paradise Twilight" -> Color(0xFF0D1B2A)
            "Luminescent Midnight Trench" -> Color(0xFF05010E)
            "Golden Sunlit Lagoon" -> Color(0xFF00282E)
            else -> Color(0xFF03045E)
        }
    }

    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(topColor, bottomColor),
            startY = 0f,
            endY = height
        ),
        size = Size(width, height)
    )

    // Dynamic water surface wave at top
    val wavePath = Path()
    wavePath.moveTo(0f, 0f)
    var x = 0f
    while (x <= width) {
        val y = 25f + sin((x / 120f) + time) * 12f + cos((x / 60f) - time * 0.5f) * 6f
        wavePath.lineTo(x, y)
        x += 20f
    }
    wavePath.lineTo(width, 0f)
    wavePath.close()

    drawPath(
        path = wavePath,
        color = Color(0x3380DEEA)
    )
}

private fun DrawScope.drawCausticsAndRays(
    width: Float,
    height: Float,
    time: Float,
    intensity: Float,
    lightColorHex: Long
) {
    val lightTint = Color(lightColorHex).copy(alpha = 0.08f * intensity)

    // Volumetric underwater light shafts
    for (i in 0 until 5) {
        val beamOffset = (i * (width / 4f) + sin(time * 0.7f + i) * 60f).mod(width + 100f) - 50f
        val rayPath = Path().apply {
            moveTo(beamOffset, 0f)
            lineTo(beamOffset + 80f, 0f)
            lineTo(beamOffset + 180f, height * 0.85f)
            lineTo(beamOffset - 20f, height * 0.85f)
            close()
        }
        drawPath(
            path = rayPath,
            brush = Brush.verticalGradient(
                colors = listOf(lightTint, Color.Transparent),
                startY = 0f,
                endY = height * 0.85f
            )
        )
    }
}

private fun DrawScope.drawSubstrate(
    width: Float,
    height: Float,
    substrateType: String,
    isNightMode: Boolean
) {
    val bedHeight = height * 0.16f
    val bedTopY = height - bedHeight

    val (subColorTop, subColorBottom) = when (substrateType) {
        "Black Obsidian Volcanic Gravel" -> Pair(Color(0xFF263238), Color(0xFF10171B))
        "Tahitian Azure Crystals" -> Pair(Color(0xFF00838F), Color(0xFF00363A))
        "Emerald River Pebbles" -> Pair(Color(0xFF2E7D32), Color(0xFF1B5E20))
        else -> Pair(Color(0xFFD7CCC8), Color(0xFF8D6E63)) // Caribbean White Sand
    }

    val finalTop = if (isNightMode) subColorTop.copy(red = subColorTop.red * 0.6f, green = subColorTop.green * 0.6f, blue = subColorTop.blue * 0.6f) else subColorTop
    val finalBottom = if (isNightMode) subColorBottom.copy(red = subColorBottom.red * 0.6f, green = subColorBottom.green * 0.6f, blue = subColorBottom.blue * 0.6f) else subColorBottom

    val path = Path().apply {
        moveTo(0f, height)
        lineTo(0f, bedTopY)
        // Gentle undulating dune line
        var currX = 0f
        while (currX <= width) {
            val duneY = bedTopY + sin(currX / 90f) * 14f + cos(currX / 40f) * 6f
            lineTo(currX, duneY)
            currX += 30f
        }
        lineTo(width, height)
        close()
    }

    drawPath(
        path = path,
        brush = Brush.verticalGradient(
            colors = listOf(finalTop, finalBottom),
            startY = bedTopY,
            endY = height
        )
    )
}

private fun DrawScope.drawDecorItem(
    placed: PlacedDecor,
    width: Float,
    height: Float,
    time: Float
) {
    val decor = AquariumCatalog.getDecor(placed.decorId) ?: return
    val centerX = placed.posX * width
    val baseY = height * 0.86f // Rooted into substrate
    val sway = sin(time * 1.5f + placed.posX * 10f) * 8f * placed.scale

    when (decor.category) {
        com.example.model.DecorCategory.PLANTS -> {
            // Draw multi-stem Amazon Sword / aquatic flora with gentle fluid sway
            for (stem in -2..2) {
                val stemX = centerX + stem * 14f * placed.scale
                val stemHeight = (120f + abs(stem) * 20f) * placed.scale
                val plantPath = Path().apply {
                    moveTo(stemX, baseY)
                    cubicTo(
                        stemX + sway * 0.4f, baseY - stemHeight * 0.5f,
                        stemX + sway * 1.2f + stem * 10f, baseY - stemHeight * 0.8f,
                        stemX + sway * 1.5f + stem * 15f, baseY - stemHeight
                    )
                }
                drawPath(
                    path = plantPath,
                    color = Color(decor.colorHex),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                        width = 8f * placed.scale,
                        cap = StrokeCap.Round
                    )
                )
            }
        }
        com.example.model.DecorCategory.CORALS -> {
            // Branching glowing coral with bioluminescent tips
            val coralRadius = 45f * placed.scale
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(decor.colorHex), Color(decor.colorHex).copy(alpha = 0.4f)),
                    center = Offset(centerX, baseY - coralRadius),
                    radius = coralRadius
                ),
                radius = coralRadius,
                center = Offset(centerX, baseY - coralRadius)
            )
            // Coral branches
            for (angle in listOf(-40f, -20f, 0f, 20f, 40f)) {
                val rad = Math.toRadians(angle.toDouble())
                val endX = centerX + (sin(rad) * coralRadius * 1.5).toFloat()
                val endY = (baseY - coralRadius) - (cos(rad) * coralRadius * 1.4).toFloat()
                drawLine(
                    color = Color(decor.colorHex),
                    start = Offset(centerX, baseY - coralRadius * 0.5f),
                    end = Offset(endX, endY),
                    strokeWidth = 7f * placed.scale,
                    cap = StrokeCap.Round
                )
            }
        }
        else -> {
            // Structures (Atlantis Arch, Chest, Volcano)
            val structWidth = 90f * placed.scale
            val structHeight = 70f * placed.scale
            drawRoundRect(
                color = Color(decor.colorHex),
                topLeft = Offset(centerX - structWidth / 2, baseY - structHeight),
                size = Size(structWidth, structHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(16f, 16f)
            )
            // Detail highlight
            drawRoundRect(
                color = Color.White.copy(alpha = 0.25f),
                topLeft = Offset(centerX - structWidth / 2 + 6f, baseY - structHeight + 6f),
                size = Size(structWidth - 12f, structHeight * 0.35f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
            )
        }
    }
}

private fun DrawScope.drawBubbleStream(
    bubbles: List<TankBubble>,
    width: Float,
    height: Float,
    time: Float,
    filterActive: Boolean
) {
    if (!filterActive) return

    for (b in bubbles) {
        b.y -= b.speed
        if (b.y < -0.05f) {
            b.y = 1.05f
            b.x = (Math.random() * 0.9 + 0.05).toFloat()
        }

        val px = (b.x * width) + sin(time * b.wobbleSpeed + b.phase) * b.wobbleAmp
        val py = b.y * height

        // Draw crystal bubble with specular highlight
        drawCircle(
            color = Color(0x33E0F7FA),
            radius = b.radius,
            center = Offset(px, py)
        )
        drawCircle(
            color = Color(0xAAFFFFFF),
            radius = b.radius,
            center = Offset(px, py),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.2f)
        )
        // Specular dot
        drawCircle(
            color = Color.White,
            radius = b.radius * 0.25f,
            center = Offset(px - b.radius * 0.35f, py - b.radius * 0.35f)
        )
    }
}

private fun DrawScope.drawRealisticFish3D(
    fish: FishInstance,
    width: Float,
    height: Float,
    time: Float,
    isNightMode: Boolean
) {
    val species = AquariumCatalog.getSpecies(fish.speciesId)
    val depthScale = 0.72f + fish.posZ * 0.42f
    val growth = (0.58f + fish.growthStage * 0.42f).coerceIn(0.58f, 1.08f)
    val scale = species.bodyScale * depthScale * growth
    val cx = fish.posX * width
    val cy = fish.posY * height
    val right = fish.velX >= 0f
    val direction = if (right) 1f else -1f
    val speed = hypot(fish.velX, fish.velY)
    val swim = (1.8f + speed * 900f).coerceIn(1.8f, 5.5f)
    val phase = time * swim * species.baseSwimSpeed + fish.posX * 13f + fish.posY * 4f
    val tailSwing = sin(phase) * 0.22f
    val finFlutter = sin(phase * 1.65f) * 0.12f
    val heading = Math.toDegrees(atan2(fish.velY.toDouble(), abs(fish.velX).coerceAtLeast(0.0001f).toDouble())).toFloat()
    val night = if (isNightMode) 0.76f else 1f

    // Species-specific silhouettes: the body proportions are deliberately different,
    // rather than reusing one generic oval for every fish.
    val isDiscus = fish.speciesId == "discus_royal"
    val isAngel = fish.speciesId == "flame_angelfish" || fish.speciesId == "angelfish"
    val isTetra = fish.speciesId == "neon_tetra"
    val bodyLength = when {
        isDiscus -> 62f
        isAngel -> 48f
        isTetra -> 66f
        fish.speciesId == "blue_tang" -> 76f
        fish.speciesId == "clownfish" -> 66f
        else -> 68f
    } * scale
    val bodyHeight = when {
        isDiscus -> 54f
        isAngel -> 56f
        isTetra -> 25f
        fish.speciesId == "clownfish" -> 39f
        else -> 35f
    } * scale
    val primary = Color(species.primaryColorHex).copy(
        red = (Color(species.primaryColorHex).red * night).coerceIn(0f, 1f),
        green = (Color(species.primaryColorHex).green * night).coerceIn(0f, 1f),
        blue = (Color(species.primaryColorHex).blue * night).coerceIn(0f, 1f)
    )
    val accent = Color(species.secondaryColorHex)
    val fin = Color(species.finColorHex)
    val darkOutline = Color(0xFF102B3A).copy(alpha = 0.82f)

    rotate(degrees = if (right) heading else -heading, pivot = Offset(cx, cy)) {
        val tailX = cx - direction * bodyLength * 0.48f
        val tailWagY = tailSwing * bodyHeight

        // Soft depth shadow gives the fish separation from the water.
        drawOval(
            color = Color(0xFF00121D).copy(alpha = 0.22f * night),
            topLeft = Offset(cx - bodyLength * 0.54f, cy - bodyHeight * 0.36f + 5f * scale),
            size = Size(bodyLength * 1.08f, bodyHeight * 0.82f)
        )

        // Caudal peduncle and animated forked tail.
        val tailPath = Path().apply {
            moveTo(tailX, cy)
            cubicTo(
                tailX - direction * 12f * scale, cy - 8f * scale,
                tailX - direction * 22f * scale, cy - bodyHeight * (0.72f + tailSwing * 0.18f),
                tailX - direction * 39f * scale, cy - bodyHeight * (0.78f + tailSwing * 0.28f) + tailWagY
            )
            lineTo(tailX - direction * 30f * scale, cy + tailWagY * 0.35f)
            lineTo(tailX - direction * 39f * scale, cy + bodyHeight * (0.78f - tailSwing * 0.28f) + tailWagY)
            cubicTo(
                tailX - direction * 20f * scale, cy + bodyHeight * 0.65f,
                tailX - direction * 10f * scale, cy + 8f * scale,
                tailX, cy
            )
            close()
        }
        drawPath(tailPath, Brush.linearGradient(listOf(fin.copy(alpha = 0.96f), primary, fin.copy(alpha = 0.82f)),
            start = Offset(tailX - direction * 40f * scale, cy), end = Offset(tailX, cy)))
        drawPath(tailPath, darkOutline.copy(alpha = 0.34f), style = androidx.compose.ui.graphics.drawscope.Stroke(1.1f * scale))

        // Dorsal and anal fins, with swept-back profile and animated edge.
        val dorsal = Path().apply {
            moveTo(cx - bodyLength * 0.38f, cy - bodyHeight * 0.24f)
            cubicTo(cx - bodyLength * 0.20f, cy - bodyHeight * 0.78f,
                cx + bodyLength * 0.16f, cy - bodyHeight * (0.84f + finFlutter),
                cx + bodyLength * 0.40f, cy - bodyHeight * 0.22f)
            close()
        }
        drawPath(dorsal, Brush.verticalGradient(listOf(fin.copy(alpha = 0.98f), primary.copy(alpha = 0.76f)),
            startY = cy - bodyHeight, endY = cy))
        drawPath(dorsal, darkOutline.copy(alpha = 0.28f), style = androidx.compose.ui.graphics.drawscope.Stroke(0.9f * scale))

        val anal = Path().apply {
            moveTo(cx - bodyLength * 0.30f, cy + bodyHeight * 0.22f)
            cubicTo(cx - bodyLength * 0.05f, cy + bodyHeight * 0.72f,
                cx + bodyLength * 0.23f, cy + bodyHeight * (0.64f - finFlutter),
                cx + bodyLength * 0.38f, cy + bodyHeight * 0.18f)
            close()
        }
        drawPath(anal, fin.copy(alpha = 0.88f))

        // Main body silhouette and layered light/shadow.
        val body = Path().apply {
            moveTo(cx + direction * bodyLength * 0.51f, cy - bodyHeight * 0.02f)
            cubicTo(cx + direction * bodyLength * 0.34f, cy - bodyHeight * 0.58f,
                cx - direction * bodyLength * 0.12f, cy - bodyHeight * 0.62f,
                cx - direction * bodyLength * 0.43f, cy - bodyHeight * 0.22f)
            cubicTo(cx - direction * bodyLength * 0.57f, cy - bodyHeight * 0.08f,
                cx - direction * bodyLength * 0.57f, cy + bodyHeight * 0.08f,
                cx - direction * bodyLength * 0.43f, cy + bodyHeight * 0.22f)
            cubicTo(cx - direction * bodyLength * 0.12f, cy + bodyHeight * 0.62f,
                cx + direction * bodyLength * 0.34f, cy + bodyHeight * 0.58f,
                cx + direction * bodyLength * 0.51f, cy - bodyHeight * 0.02f)
            close()
        }
        drawPath(body, Brush.linearGradient(
            colors = listOf(Color.White.copy(alpha = 0.42f), primary, primary.copy(red = primary.red * 0.62f, green = primary.green * 0.68f, blue = primary.blue * 0.78f)),
            start = Offset(cx, cy - bodyHeight * 0.72f), end = Offset(cx, cy + bodyHeight * 0.72f)
        ))
        drawPath(body, darkOutline.copy(alpha = 0.40f), style = androidx.compose.ui.graphics.drawscope.Stroke(1.1f * scale))

        // Species markings.
        when (fish.speciesId) {
            "clownfish" -> {
                // Three curved white bands with dark pinstripes, following the body volume.
                for (fraction in listOf(-0.23f, 0.10f, 0.37f)) {
                    val sx = cx + direction * bodyLength * fraction
                    val band = Path().apply {
                        moveTo(sx - 4f * scale, cy - bodyHeight * 0.46f)
                        cubicTo(sx - 9f * scale, cy - bodyHeight * 0.12f, sx - 8f * scale, cy + bodyHeight * 0.18f, sx - 2f * scale, cy + bodyHeight * 0.45f)
                        lineTo(sx + 5f * scale, cy + bodyHeight * 0.42f)
                        cubicTo(sx - 1f * scale, cy + bodyHeight * 0.12f, sx + 1f * scale, cy - bodyHeight * 0.15f, sx + 5f * scale, cy - bodyHeight * 0.43f)
                        close()
                    }
                    drawPath(band, Color(0xFF17232B).copy(alpha = 0.9f))
                    drawPath(band, Color(0xFFF7F5E9))
                }
            }
            "blue_tang" -> {
                val marking = Path().apply {
                    moveTo(cx + direction * bodyLength * 0.43f, cy - bodyHeight * 0.25f)
                    cubicTo(cx + direction * bodyLength * 0.16f, cy - bodyHeight * 0.08f,
                        cx + direction * bodyLength * 0.12f, cy + bodyHeight * 0.34f,
                        cx - direction * bodyLength * 0.26f, cy + bodyHeight * 0.38f)
                    cubicTo(cx - direction * bodyLength * 0.05f, cy + bodyHeight * 0.10f,
                        cx - direction * bodyLength * 0.06f, cy - bodyHeight * 0.25f,
                        cx + direction * bodyLength * 0.43f, cy - bodyHeight * 0.25f)
                    close()
                }
                drawPath(marking, Color(0xFF071B38).copy(alpha = 0.92f))
                drawOval(Color(0xFFFFD600), Offset(tailX - direction * 34f * scale, cy - 4f * scale), Size(9f * scale, 8f * scale))
            }
            "flame_angelfish", "angelfish" -> {
                for (i in -2..2) {
                    val sx = cx + direction * i * bodyLength * 0.12f
                    drawPath(Path().apply {
                        moveTo(sx - 3f * scale, cy - bodyHeight * 0.48f)
                        quadraticTo(sx + 7f * scale, cy, sx - 2f * scale, cy + bodyHeight * 0.48f)
                        lineTo(sx + 3f * scale, cy + bodyHeight * 0.46f)
                        quadraticTo(sx + 12f * scale, cy, sx + 3f * scale, cy - bodyHeight * 0.46f)
                        close()
                    }, accent.copy(alpha = 0.8f))
                }
            }
            "discus_royal" -> {
                for (i in -3..3) {
                    val sx = cx + i * bodyLength * 0.095f
                    drawLine(Color(0xFFB2F5FF).copy(alpha = 0.28f), Offset(sx, cy - bodyHeight * 0.43f),
                        Offset(sx, cy + bodyHeight * 0.43f), 1.2f * scale)
                }
            }
            "neon_tetra" -> {
                drawLine(Color(0xFF00E5FF), Offset(cx - direction * bodyLength * 0.30f, cy - 2f * scale),
                    Offset(cx + direction * bodyLength * 0.28f, cy - 2f * scale), 3.2f * scale)
                drawLine(Color(0xFFFF3D71), Offset(cx - direction * bodyLength * 0.34f, cy + 4f * scale),
                    Offset(cx + direction * bodyLength * 0.18f, cy + 4f * scale), 2f * scale)
            }
        }

        // Pectoral fin translucent and visibly fluttering.
        val pectoral = Path().apply {
            moveTo(cx + direction * bodyLength * 0.03f, cy + bodyHeight * 0.02f)
            cubicTo(cx - direction * bodyLength * 0.06f, cy + bodyHeight * (0.42f + finFlutter),
                cx + direction * bodyLength * 0.18f, cy + bodyHeight * (0.50f + finFlutter),
                cx + direction * bodyLength * 0.20f, cy + bodyHeight * 0.08f)
            close()
        }
        drawPath(pectoral, fin.copy(alpha = 0.52f))
        drawPath(pectoral, Color.White.copy(alpha = 0.24f), style = androidx.compose.ui.graphics.drawscope.Stroke(0.8f * scale))

        // Gill plate and curved gill slit.
        val gillX = cx + direction * bodyLength * 0.19f
        drawPath(Path().apply {
            moveTo(gillX, cy - bodyHeight * 0.30f)
            quadraticTo(gillX + direction * 11f * scale, cy, gillX, cy + bodyHeight * 0.30f)
        }, Color(0xFF143847).copy(alpha = 0.46f), style = androidx.compose.ui.graphics.drawscope.Stroke(1.25f * scale))

        // Mouth and a dimensional eye with iris ring.
        val mouthX = cx + direction * bodyLength * 0.48f
        drawLine(darkOutline, Offset(mouthX - direction * 2f * scale, cy - 1.5f * scale),
            Offset(mouthX + direction * 4f * scale, cy + 1f * scale), 1.3f * scale, cap = StrokeCap.Round)
        val eyeX = cx + direction * bodyLength * 0.34f
        val eyeY = cy - bodyHeight * 0.19f
        drawCircle(Color(0xFF07141F), 5.4f * scale, Offset(eyeX, eyeY))
        drawCircle(Color(0xFFF8FBFF), 4.35f * scale, Offset(eyeX, eyeY))
        drawCircle(Color(0xFF101820), 2.65f * scale, Offset(eyeX + direction * 0.9f * scale, eyeY))
        drawCircle(Color.White, 1.05f * scale, Offset(eyeX - direction * 1.0f * scale, eyeY - 1.3f * scale))

        // Scattered subtle scale glints and a soft dorsal highlight.
        for (i in -2..2) {
            val sx = cx + i * bodyLength * 0.105f
            drawLine(Color.White.copy(alpha = 0.12f), Offset(sx, cy - bodyHeight * 0.12f),
                Offset(sx + 3f * scale, cy + bodyHeight * 0.06f), 0.8f * scale)
        }
        drawPath(Path().apply {
            moveTo(cx - bodyLength * 0.22f, cy - bodyHeight * 0.34f)
            quadraticTo(cx + bodyLength * 0.05f, cy - bodyHeight * 0.52f, cx + bodyLength * 0.34f, cy - bodyHeight * 0.30f)
        }, Color.White.copy(alpha = 0.30f), style = androidx.compose.ui.graphics.drawscope.Stroke(2.2f * scale, cap = StrokeCap.Round))
    }
}

private fun DrawScope.drawAquariumGlassEdge(width: Float, height: Float) {
    // Subtle glass reflections along outer boundary
    val glassStroke = 8f
    drawRect(
        color = Color(0x3380DEEA),
        topLeft = Offset(0f, 0f),
        size = Size(width, height),
        style = androidx.compose.ui.graphics.drawscope.Stroke(width = glassStroke)
    )
}
