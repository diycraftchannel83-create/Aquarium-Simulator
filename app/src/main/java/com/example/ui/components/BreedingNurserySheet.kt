package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AquariumCatalog
import com.example.data.GameState
import com.example.model.FishInstance
import com.example.viewmodel.AquariumViewModel

@Composable
fun BreedingNurserySheet(
    gameState: GameState,
    viewModel: AquariumViewModel,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedParent1 by remember { mutableStateOf<FishInstance?>(null) }
    var selectedParent2 by remember { mutableStateOf<FishInstance?>(null) }

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
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Breeding Nursery",
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Breeding & Genetic Nursery",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_breeding_button")
                ) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Check if any fish currently incubating eggs/baby
            val pregnantFishes = gameState.fishList.filter { it.isPregnant }
            if (pregnantFishes.isNotEmpty()) {
                Text(
                    text = "Incubation Pods (${pregnantFishes.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.tertiary
                )
                Spacer(modifier = Modifier.height(8.dp))
                for (pf in pregnantFishes) {
                    val pSpecies = AquariumCatalog.getSpecies(pf.speciesId)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(Color(pSpecies.primaryColorHex))
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(text = "${pf.customName} (${pSpecies.name})", fontWeight = FontWeight.Bold)
                                        Text(
                                            text = "Incubation: ${(pf.pregnancyProgress * 100).toInt()}%",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.tertiary
                                        )
                                    }
                                }

                                if (pf.pregnancyProgress >= 1.0f) {
                                    Button(
                                        onClick = { viewModel.claimBreedingBaby(pf) },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                        modifier = Modifier.testTag("claim_baby_${pf.id}")
                                    ) {
                                        Text("Hatch Baby!")
                                    }
                                } else {
                                    FilledTonalButton(
                                        onClick = { viewModel.applySpeedGrowthBooster() }
                                    ) {
                                        Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Speed Hatch", fontSize = 12.sp)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { pf.pregnancyProgress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(CircleShape),
                                color = MaterialTheme.colorScheme.tertiary,
                                trackColor = MaterialTheme.colorScheme.surface
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Pair selection box
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Select Two Adult Fish to Pair",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Parent 1 slot
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(72.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .clickable { selectedParent1 = null }
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (selectedParent1 != null) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = selectedParent1!!.customName,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = AquariumCatalog.getSpecies(selectedParent1!!.speciesId).name,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            } else {
                                Text("Parent 1 (Tap list)", style = MaterialTheme.typography.bodySmall)
                            }
                        }

                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        // Parent 2 slot
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(72.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .clickable { selectedParent2 = null }
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (selectedParent2 != null) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = selectedParent2!!.customName,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = AquariumCatalog.getSpecies(selectedParent2!!.speciesId).name,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            } else {
                                Text("Parent 2 (Tap list)", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    val canBreed = selectedParent1 != null && selectedParent2 != null && selectedParent1!!.id != selectedParent2!!.id
                    Button(
                        onClick = {
                            if (canBreed) {
                                viewModel.breedFish(selectedParent1!!, selectedParent2!!)
                                selectedParent1 = null
                                selectedParent2 = null
                            }
                        },
                        enabled = canBreed,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("start_breeding_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiary
                        )
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Begin Genetic Pairing")
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Available Mature Fish in Tank",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            val matureFish = gameState.fishList.filter { !it.isPregnant }

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(matureFish) { fish ->
                    val species = AquariumCatalog.getSpecies(fish.speciesId)
                    val isSelected1 = selectedParent1?.id == fish.id
                    val isSelected2 = selectedParent2?.id == fish.id

                    OutlinedCard(
                        onClick = {
                            if (isSelected1) selectedParent1 = null
                            else if (isSelected2) selectedParent2 = null
                            else if (selectedParent1 == null) selectedParent1 = fish
                            else if (selectedParent2 == null) selectedParent2 = fish
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.outlinedCardColors(
                            containerColor = if (isSelected1 || isSelected2)
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                            else Color.Transparent
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(Color(species.primaryColorHex))
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(text = fish.customName, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = "${species.name} • Growth: ${(fish.growthStage * 100).toInt()}%",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            if (isSelected1 || isSelected2) {
                                Badge(containerColor = MaterialTheme.colorScheme.primary) {
                                    Text(if (isSelected1) "Parent 1" else "Parent 2")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
