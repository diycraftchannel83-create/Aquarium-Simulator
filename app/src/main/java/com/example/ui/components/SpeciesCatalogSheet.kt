package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.remember
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Size as CoilSize
import com.example.data.AquariumCatalog
import com.example.data.GameState
import com.example.model.FishRarity
import com.example.model.FishSpecies
import com.example.util.LiveAssetHelper
import com.example.viewmodel.AquariumViewModel

@Composable
fun SpeciesCatalogSheet(
    gameState: GameState,
    viewModel: AquariumViewModel,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.MenuBook,
                        contentDescription = "Encyclopedia",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Marine Species Encyclopedia",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_catalog_button")
                ) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Discovered ${gameState.unlockedSpeciesIds.size} of ${AquariumCatalog.allSpecies.size} Unique Ocean Species",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(14.dp))

            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 160.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(AquariumCatalog.allSpecies) { species ->
                    val isUnlocked = gameState.unlockedSpeciesIds.contains(species.id)
                    val canAfford = (gameState.coins >= species.coinPrice && gameState.pearls >= species.pearlPrice)

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isUnlocked)
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val context = LocalContext.current
                                val hasLiveGif = species.assetPath != null && LiveAssetHelper.assetExists(context, species.assetPath)

                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(Color(species.primaryColorHex).copy(alpha = 0.25f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (hasLiveGif) {
                                        val imageLoader = remember { LiveAssetHelper.getImageLoader(context) }
                                        val painter = rememberAsyncImagePainter(
                                            model = ImageRequest.Builder(context)
                                                .data(LiveAssetHelper.getAssetUri(species.assetPath!!))
                                                .size(CoilSize.ORIGINAL)
                                                .build(),
                                            imageLoader = imageLoader
                                        )
                                        Image(
                                            painter = painter,
                                            contentDescription = species.name,
                                            modifier = Modifier.size(38.dp),
                                            contentScale = ContentScale.Fit
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(CircleShape)
                                                .background(Color(species.primaryColorHex))
                                        )
                                    }

                                    if (!isUnlocked) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(Color.Black.copy(alpha = 0.45f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.Lock, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }

                                AssistChip(
                                    onClick = {},
                                    label = {
                                        Text(
                                            species.rarity.name,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    },
                                    colors = AssistChipDefaults.assistChipColors(
                                        labelColor = when (species.rarity) {
                                            FishRarity.LEGENDARY -> Color(0xFFFFD700)
                                            FishRarity.MYTHIC -> Color(0xFFE040FB)
                                            FishRarity.RARE -> Color(0xFF00E5FF)
                                            FishRarity.UNCOMMON -> Color(0xFF69F0AE)
                                            FishRarity.COMMON -> Color(0xFFB0BEC5)
                                        }
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = species.name,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = species.scientificName,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = species.description,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 3
                            )

                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = { viewModel.buySpecies(species) },
                                enabled = canAfford,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("buy_species_${species.id}"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Text(
                                    text = if (species.pearlPrice > 0) "${species.pearlPrice} 🦪" else "${species.coinPrice} 🪙",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
