package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.data.AquariumCatalog
import com.example.data.GameState
import com.example.viewmodel.AquariumViewModel

data class IAPProduct(
    val id: String,
    val title: String,
    val description: String,
    val priceStr: String,
    val coins: Int,
    val pearls: Int,
    val isBestValue: Boolean = false,
    val badgeIcon: String = "💎"
)

@Composable
fun AquaStoreSheet(
    gameState: GameState,
    viewModel: AquariumViewModel,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val iapProducts = listOf(
        IAPProduct("p_starter", "Coral Novice Pack", "500 Gold Coins + 25 Rare Pearls", "$1.99", 500, 25),
        IAPProduct("p_growth", "Speed Growth Elixir", "Instantly quadruples growth speed for 30 minutes", "$0.99", 200, 10, badgeIcon = "⚡"),
        IAPProduct("p_pro", "Reef Master Vault", "3,500 Gold Coins + 150 Pearls + Instant Rare Egg", "$4.99", 3500, 150, isBestValue = true, badgeIcon = "👑"),
        IAPProduct("p_titan", "Poseidon Treasury", "10,000 Gold Coins + 500 Pearls + Mythic Jelly", "$9.99", 10000, 500, badgeIcon = "🔱")
    )

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
                        imageVector = Icons.Default.Diamond,
                        contentDescription = "Aqua Store",
                        tint = Color(0xFF00E5FF),
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Treasury & Premium Decor",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_store_button")
                ) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Text(
                        text = "Virtual Currency & Growth Boosters",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(iapProducts) { product ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (product.isBestValue)
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = product.badgeIcon, fontSize = 28.sp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = product.title, fontWeight = FontWeight.Bold)
                                        if (product.isBestValue) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Badge(containerColor = Color(0xFFFFD700)) {
                                                Text("BEST VALUE", color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                    Text(
                                        text = product.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    viewModel.purchaseIAPPackage(product.title, product.coins, product.pearls)
                                    if (product.id == "p_growth") {
                                        viewModel.applySpeedGrowthBooster()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                modifier = Modifier.testTag("buy_iap_${product.id}")
                            ) {
                                Text(product.priceStr, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Premium Decor Items Section
                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Exclusive Aquatic Decor Items",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                val premiumDecors = AquariumCatalog.allDecor.filter { it.isPremium || it.pearlPrice > 0 }
                items(premiumDecors) { decor ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = decor.iconEmoji, fontSize = 26.sp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(text = decor.name, fontWeight = FontWeight.SemiBold)
                                    Text(
                                        text = decor.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Button(
                                onClick = { viewModel.purchaseAndPlaceDecor(decor) },
                                enabled = (gameState.coins >= decor.coinPrice && gameState.pearls >= decor.pearlPrice),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                modifier = Modifier.testTag("buy_decor_${decor.id}")
                            ) {
                                Text(
                                    text = if (decor.pearlPrice > 0) "${decor.pearlPrice} 🦪" else "${decor.coinPrice} 🪙",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
