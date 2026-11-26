package io.github.information89.motion.lab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import io.github.information89.motion.Dial
import io.github.information89.motion.DialLook
import io.github.information89.motion.DialScale
import io.github.information89.motion.EdgeBand
import io.github.information89.motion.Morph
import io.github.information89.motion.MorphHost
import io.github.information89.motion.Motion
import io.github.information89.motion.MotionSpring
import io.github.information89.motion.MotionTime
import io.github.information89.motion.PageOvershoot
import io.github.information89.motion.edgeBlur
import io.github.information89.motion.morphAnchor
import io.github.information89.motion.morphSource
import io.github.information89.motion.motionDrag
import io.github.information89.motion.pageOvershoot
import io.github.information89.motion.pageSpringFling
import io.github.information89.motion.rememberDialState
import io.github.information89.motion.rememberMotionValue
import io.github.information89.motion.reveal
import io.github.information89.motion.springToPage
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MotionLabTheme {
                MotionLabApp()
            }
        }
    }
}

private enum class LabPage(val title: String, val description: String) {
    SPRINGS("Springs and release", "Compare Motion spring tokens and try gesture-to-spring release."),
    MORPH("Morph", "Grow a surface from its source, then catch or drag it back."),
    PAGES("Page movement", "Swipe between pages or choose a page; both use the same spring motion."),
    DIAL("Dial", "Drag, flick, and snap a reusable dial with haptic feedback."),
    EDGE_BLUR("Scroll edge blur", "Explore progressive blur and fade at the edges of a horizontal list."),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MotionLabApp() {
    var page by rememberSaveable { mutableStateOf<LabPage?>(null) }
    var slowMotion by rememberSaveable { mutableFloatStateOf(1f) }
    BackHandler(enabled = page != null) { page = null }

    DisposableEffect(Unit) {
        onDispose { MotionTime.scale = 1f }
    }
    LaunchedEffect(slowMotion) {
        MotionTime.scale = slowMotion
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(page?.title ?: "Motion Lab") },
                navigationIcon = {
                    if (page != null) {
                        IconButton(onClick = { page = null }) {
                            Text("‹", style = MaterialTheme.typography.headlineMedium)
                        }
                    }
                }
            )
        }
    ) { insets ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(insets)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            if (page == null) {
                Text(
                    "Motion is the interaction layer beneath your UI. Pick a pattern to explore how it behaves.",
                    style = MaterialTheme.typography.bodyLarge
                )
                SlowMotionControl(slowMotion) { slowMotion = it }
                LabPage.entries.forEach { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { page = item },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(item.title, style = MaterialTheme.typography.titleLarge)
                            Text(item.description, style = MaterialTheme.typography.bodyMedium)
                            Text(
                                "Explore →",
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
                Text(
                    "Standalone sample app · Android / Jetpack Compose",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text(page!!.description, style = MaterialTheme.typography.bodyLarge)
                SlowMotionControl(slowMotion) { slowMotion = it }
                when (page) {
                    LabPage.SPRINGS -> SpringsDemo()
                    LabPage.MORPH -> MorphDemo()
                    LabPage.PAGES -> PageDemo()
                    LabPage.DIAL -> DialDemo()
                    LabPage.EDGE_BLUR -> EdgeBlurDemo()
                    null -> Unit
                }
            }
        }
    }
}

@Composable
private fun SlowMotionControl(value: Float, onChange: (Float) -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Slow motion · ${"%.1f".format(value)}×", style = MaterialTheme.typography.titleSmall)
            Slider(value = value, onValueChange = onChange, valueRange = 1f..5f, steps = 3)
            Text(
                "Applies to springs started after the setting changes. It resets when Motion Lab closes.",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

private val springTokens: List<Pair<String, MotionSpring>> = listOf(
    "smooth" to Motion.smooth,
    "container" to Motion.container,
    "dismiss" to Motion.dismiss,
    "playful" to Motion.playful,
    "momentum" to Motion.momentum
)

@Composable
private fun SpringsDemo() {
    var selected by rememberSaveable { mutableIntStateOf(0) }
    val position = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("Choose a spring token, then play it in both directions.", style = MaterialTheme.typography.bodyMedium)
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            springTokens.forEachIndexed { index, item ->
                FilterChip(
                    selected = selected == index,
                    onClick = { selected = index },
                    label = { Text(item.first) }
                )
            }
        }
        BoxWithConstraints(
            Modifier
                .fillMaxWidth()
                .height(64.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
        ) {
            val travel = with(androidx.compose.ui.platform.LocalDensity.current) { (maxWidth - 36.dp).toPx() }
            Box(
                Modifier
                    .align(Alignment.CenterStart)
                    .size(36.dp)
                    .graphicsLayer { translationX = travel * position.value }
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
            )
        }
        Text(
            "${springTokens[selected].first} · response ${"%.2f".format(
                springTokens[selected].second.response
            )} s · damping ${"%.2f".format(springTokens[selected].second.damping)}",
            style = MaterialTheme.typography.bodySmall
        )
        Button(onClick = {
            scope.launch {
                position.animateTo(
                    if (position.targetValue < 0.5f) 1f else 0f,
                    springTokens[selected].second.spec()
                )
            }
        }) { Text("Play spring") }

        Spacer(Modifier.height(4.dp))
        Text("Gesture release", style = MaterialTheme.typography.titleMedium)
        val value = rememberMotionValue(initial = 0f, overMin = 0.12f, overMax = 0.12f)
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val travelDp = maxWidth - 36.dp
            val travelPx = with(androidx.compose.ui.platform.LocalDensity.current) { travelDp.toPx() }
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                    .motionDrag(
                        value,
                        androidx.compose.foundation.gestures.Orientation.Horizontal,
                        travelDp
                    )
            ) {
                Box(
                    Modifier
                        .align(Alignment.CenterStart)
                        .size(36.dp)
                        .graphicsLayer { translationX = travelPx * value.value }
                        .background(MaterialTheme.colorScheme.tertiary, CircleShape)
                )
            }
        }
        Text(
            "position ${"%.2f".format(value.value)} · target ${"%.1f".format(value.target)}",
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun MorphDemo() {
    val motion = rememberMotionValue(initial = 0f, overMin = 0.06f, overMax = 0.08f)
    val visible = motion.value > 0.001f || motion.isRunning
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        MorphHost(
            Modifier
                .fillMaxWidth()
                .height(340.dp)
        ) {
            Box(Modifier.fillMaxSize()) {
                Spacer(
                    Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(230.dp)
                        .morphAnchor("lab-target", 28.dp)
                )
                Button(
                    onClick = { motion.animateTo(if (motion.target < 0.5f) 1f else 0f) },
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .morphAnchor("lab-source", 100.dp)
                        .morphSource("lab-source", 100.dp)
                ) {
                    Text("Open surface")
                }
                if (visible) {
                    Morph(
                        from = "lab-source",
                        to = "lab-target",
                        value = motion,
                        modifier = Modifier
                            .fillMaxSize()
                            .motionDrag(
                                motion,
                                androidx.compose.foundation.gestures.Orientation.Vertical,
                                230.dp,
                                reverseDirection = true
                            ),
                        surface = Modifier.background(MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Column(
                            Modifier
                                .fillMaxSize()
                                .padding(22.dp)
                                .reveal(motion, 0.35f, 0.7f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("The source became this surface", style = MaterialTheme.typography.titleMedium)
                            Text("Drag down to scrub the morph back to its source. Tap the action below to close.")
                            Button(onClick = { motion.animateTo(0f) }) { Text("Close") }
                        }
                    }
                }
            }
        }
        Text("The morph uses measured source and target anchors. Its content is revealed as the surface grows.")
    }
}

@Composable
private fun PageDemo() {
    val pager = rememberPagerState(initialPage = 0) { 3 }
    val overshoot = remember { PageOvershoot() }
    var springIndex by rememberSaveable { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()
    val springs = listOf("momentum" to Motion.momentum, "gentle" to Motion.gentle, "playful" to Motion.playful)

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            springs.forEachIndexed { index, item ->
                FilterChip(selected = springIndex == index, onClick = {
                    springIndex = index
                }, label = { Text(item.first) })
            }
        }
        HorizontalPager(
            state = pager,
            flingBehavior = remember(pager, springIndex) {
                pageSpringFling(pager, overshoot) { springs[springIndex].second }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .clip(RoundedCornerShape(24.dp))
                .pageOvershoot(overshoot)
        ) { index ->
            Surface(
                modifier = Modifier.fillMaxSize().padding(6.dp),
                shape = RoundedCornerShape(20.dp),
                color = listOf(
                    MaterialTheme.colorScheme.primaryContainer,
                    MaterialTheme.colorScheme.secondaryContainer,
                    MaterialTheme.colorScheme.tertiaryContainer
                )[index]
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("Page ${index + 1}", style = MaterialTheme.typography.headlineLarge)
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(3) { index ->
                Button(onClick = {
                    scope.launch { pager.springToPage(index, overshoot, springs[springIndex].second) }
                }) {
                    Text("${index + 1}")
                }
            }
        }
        Text(
            "Swipe, or tap a page number. The pages use the same spring-based movement.",
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun DialDemo() {
    val scale = remember { DialScale(min = -14f, max = 14f, step = 1f, majorEvery = 4, visibleSpan = 8f) }
    val state = rememberDialState(scale, initial = 0f)
    val ink = MaterialTheme.colorScheme.onSurface
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Value: ${state.value.target.toInt()}", style = MaterialTheme.typography.titleLarge)
        Dial(
            state = state,
            look = DialLook(
                ink = ink,
                marker = MaterialTheme.colorScheme.primary,
                labelStyle = MaterialTheme.typography.labelSmall
            ),
            label = { it.toInt().toString() },
            describe = { "Position ${it.toInt()}" },
            onDoubleTap = { it.value.animateTo(0f) },
            modifier = Modifier.fillMaxWidth().height(88.dp)
        )
        Text("Double-tap the dial to return to zero. Drag or flick to move between steps.")
    }
}

@Composable
private fun EdgeBlurDemo() {
    val items = remember { (1..18).map { "Item $it" } }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .edgeBlur(
                    start = EdgeBand(size = 54.dp, blur = 16.dp, fadeOut = true),
                    end = EdgeBand(size = 54.dp, blur = 16.dp, fadeOut = true)
                ),
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items) { item ->
                Card(
                    modifier = Modifier.width(112.dp).fillMaxSize(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(item, style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
        Text("Scroll horizontally. On Android 12 and later, the edge bands also blur nearby content.")
    }
}

@Composable
private fun MotionLabTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Color(0xFF465D72),
            secondary = Color(0xFF526B61),
            tertiary = Color(0xFF805B45)
        ),
        content = content
    )
}
