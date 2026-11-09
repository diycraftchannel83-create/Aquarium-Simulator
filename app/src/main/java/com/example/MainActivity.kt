package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.*
import com.example.ui.simulation.Aquarium3DCanvas
import com.example.ui.theme.AquaZenTheme
import com.example.viewmodel.ActiveScreen
import com.example.viewmodel.AquariumViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: AquariumViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val gameState by viewModel.gameState.collectAsStateWithLifecycle()
            var activeModalScreen by remember { mutableStateOf<ActiveScreen?>(null) }

            AquaZenTheme(darkTheme = gameState.isDarkMode) {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = MaterialTheme.colorScheme.background,
                    contentWindowInsets = WindowInsets.safeDrawing,
                    bottomBar = {
                        AquariumBottomNavigationBar(
                            activeScreen = activeModalScreen,
                            onSelectScreen = { screen ->
                                activeModalScreen = if (activeModalScreen == screen) null else screen
                            }
                        )
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        // 1. Full 3D Interactive Aquarium Simulation Canvas
                        Aquarium3DCanvas(
                            tank = gameState.tank,
                            fishList = gameState.fishList,
                            placedDecorList = gameState.placedDecorList,
                            isNightMode = gameState.isDarkMode,
                            onTapFish = { /* Can show fish info dialog */ },
                            onFeedAt = { x, y ->
                                viewModel.feedFishAt(x, y)
                            },
                            modifier = Modifier.fillMaxSize()
                        )

                        // 2. Top Game HUD (Level, Coins, Pearls, Ambient sound toggle, Dark mode, Cloud Sync)
                        AquariumTopHud(
                            gameState = gameState,
                            onToggleDarkMode = { viewModel.toggleDarkMode() },
                            onToggleMusic = { viewModel.toggleMusic() },
                            onOpenTutorial = { activeModalScreen = ActiveScreen.TUTORIAL_GUIDE },
                            onOpenStore = { activeModalScreen = ActiveScreen.AQUA_STORE_IAP },
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.TopCenter)
                                .padding(12.dp)
                        )

                        // 3. Floating Quick Feed & Clean Tool Pills at Bottom Right
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(end = 16.dp, bottom = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            horizontalAlignment = Alignment.End
                        ) {
                            // Active Growth Booster Indicator
                            if (System.currentTimeMillis() < gameState.speedGrowthActiveUntil) {
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = Color(0xFFFFD700),
                                    tonalElevation = 4.dp
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Bolt, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("4x Growth Active", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            // Water Filter Quick Status Button
                            FilledTonalButton(
                                onClick = { viewModel.cleanWaterFilter() },
                                modifier = Modifier.testTag("quick_clean_filter_button")
                            ) {
                                Icon(Icons.Default.CleaningServices, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Filter ${(gameState.tank.waterCleanliness * 100).toInt()}%")
                            }
                        }

                        // 4. Interactive Bottom Sheet Modals for Detailed Customization, Breeding, Catalog, Quests & Store
                        AnimatedVisibility(
                            visible = activeModalScreen != null,
                            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                            modifier = Modifier.align(Alignment.BottomCenter)
                        ) {
                            when (activeModalScreen) {
                                ActiveScreen.TANK_CUSTOMIZATION -> {
                                    TankCustomizationSheet(
                                        gameState = gameState,
                                        viewModel = viewModel,
                                        onDismiss = { activeModalScreen = null }
                                    )
                                }
                                ActiveScreen.FISH_NURSERY_BREEDING -> {
                                    BreedingNurserySheet(
                                        gameState = gameState,
                                        viewModel = viewModel,
                                        onDismiss = { activeModalScreen = null }
                                    )
                                }
                                ActiveScreen.SPECIES_CATALOG -> {
                                    SpeciesCatalogSheet(
                                        gameState = gameState,
                                        viewModel = viewModel,
                                        onDismiss = { activeModalScreen = null }
                                    )
                                }
                                ActiveScreen.DAILY_CHALLENGES_LEADERBOARD -> {
                                    DailyChallengesAndLeaderboardSheet(
                                        gameState = gameState,
                                        viewModel = viewModel,
                                        onDismiss = { activeModalScreen = null }
                                    )
                                }
                                ActiveScreen.AQUA_STORE_IAP -> {
                                    AquaStoreSheet(
                                        gameState = gameState,
                                        viewModel = viewModel,
                                        onDismiss = { activeModalScreen = null }
                                    )
                                }
                                ActiveScreen.TUTORIAL_GUIDE -> {
                                    TutorialAndCloudSyncDialog(
                                        gameState = gameState,
                                        viewModel = viewModel,
                                        onDismiss = { activeModalScreen = null }
                                    )
                                }
                                else -> Unit
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AquariumTopHud(
    gameState: com.example.data.GameState,
    onToggleDarkMode: () -> Unit,
    onToggleMusic: () -> Unit,
    onOpenTutorial: () -> Unit,
    onOpenStore: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
        tonalElevation = 6.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Player Level & Avatar
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🐠", fontSize = 20.sp)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Lv. ${gameState.playerLevel}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${gameState.fishList.size} Fish in Sanctuary",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Currency Indicators (Coins & Pearls)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.clickable { onOpenStore() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🪙", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${gameState.coins}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color(0xFFFFD700)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.clickable { onOpenStore() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🦪", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${gameState.pearls}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color(0xFF00E5FF)
                        )
                    }
                }
            }

            // Quick Settings Controls (Dark mode, Sound, Help)
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onToggleMusic,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("toggle_music_button")
                ) {
                    Icon(
                        imageVector = if (gameState.isMusicMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                        contentDescription = "Sound Soundtrack",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = onToggleDarkMode,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("toggle_dark_mode_button")
                ) {
                    Icon(
                        imageVector = if (gameState.isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                        contentDescription = "Theme View",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = onOpenTutorial,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("open_tutorial_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.HelpOutline,
                        contentDescription = "Help & Cloud Sync",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AquariumBottomNavigationBar(
    activeScreen: ActiveScreen?,
    onSelectScreen: (ActiveScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier
            .navigationBarsPadding()
            .testTag("bottom_nav_bar"),
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Tune, contentDescription = "Customize") },
            label = { Text("Tank", fontSize = 11.sp) },
            selected = activeScreen == ActiveScreen.TANK_CUSTOMIZATION,
            onClick = { onSelectScreen(ActiveScreen.TANK_CUSTOMIZATION) },
            modifier = Modifier.testTag("nav_item_tank")
        )

        NavigationBarItem(
            icon = { Icon(Icons.Default.Favorite, contentDescription = "Breeding Nursery") },
            label = { Text("Breeding", fontSize = 11.sp) },
            selected = activeScreen == ActiveScreen.FISH_NURSERY_BREEDING,
            onClick = { onSelectScreen(ActiveScreen.FISH_NURSERY_BREEDING) },
            modifier = Modifier.testTag("nav_item_breeding")
        )

        NavigationBarItem(
            icon = { Icon(Icons.Default.MenuBook, contentDescription = "Species Catalog") },
            label = { Text("Species", fontSize = 11.sp) },
            selected = activeScreen == ActiveScreen.SPECIES_CATALOG,
            onClick = { onSelectScreen(ActiveScreen.SPECIES_CATALOG) },
            modifier = Modifier.testTag("nav_item_species")
        )

        NavigationBarItem(
            icon = { Icon(Icons.Default.EmojiEvents, contentDescription = "Quests & Rank") },
            label = { Text("Quests", fontSize = 11.sp) },
            selected = activeScreen == ActiveScreen.DAILY_CHALLENGES_LEADERBOARD,
            onClick = { onSelectScreen(ActiveScreen.DAILY_CHALLENGES_LEADERBOARD) },
            modifier = Modifier.testTag("nav_item_quests")
        )

        NavigationBarItem(
            icon = { Icon(Icons.Default.Diamond, contentDescription = "Store & IAP") },
            label = { Text("Treasury", fontSize = 11.sp) },
            selected = activeScreen == ActiveScreen.AQUA_STORE_IAP,
            onClick = { onSelectScreen(ActiveScreen.AQUA_STORE_IAP) },
            modifier = Modifier.testTag("nav_item_treasury")
        )
    }
}
