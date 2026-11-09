package com.example.model

enum class FishRarity {
    COMMON, UNCOMMON, RARE, MYTHIC, LEGENDARY
}

enum class FishDiet {
    HERBIVORE, CARNIVORE, OMNIVORE
}

data class FishSpecies(
    val id: String,
    val name: String,
    val scientificName: String,
    val rarity: FishRarity,
    val diet: FishDiet,
    val primaryColorHex: Long,
    val secondaryColorHex: Long,
    val finColorHex: Long,
    val bodyScale: Float = 1.0f,
    val baseSwimSpeed: Float = 1.0f,
    val unlockLevel: Int = 1,
    val coinPrice: Int = 50,
    val pearlPrice: Int = 0,
    val description: String,
    val glowBioluminescent: Boolean = false,
    val finStyle: FinStyle = FinStyle.FLOWING,
    val assetPath: String? = null
)

enum class FinStyle {
    FLOWING, ANGEL, ROUND, SHARK, JELLY
}

data class FishInstance(
    val id: String,
    val speciesId: String,
    val customName: String,
    val bornTimestamp: Long = System.currentTimeMillis(),
    val hungerPercent: Float = 0.85f,     // 0.0 to 1.0
    val happinessPercent: Float = 0.95f,  // 0.0 to 1.0
    val growthStage: Float = 0.5f,        // 0.0 to 1.0 (adult)
    val isPregnant: Boolean = false,
    val mateSpeciesId: String? = null,
    val pregnancyProgress: Float = 0.0f,  // 0.0 to 1.0
    // 3D Simulation state
    var posX: Float = 0.5f,   // 0.0 to 1.0 normalized in tank
    var posY: Float = 0.5f,   // 0.0 to 1.0 normalized
    var posZ: Float = 0.5f,   // 0.0 to 1.0 depth
    var velX: Float = 0.002f,
    var velY: Float = 0.001f,
    var velZ: Float = 0.001f,
    var targetX: Float = 0.5f,
    var targetY: Float = 0.5f,
    var targetZ: Float = 0.5f,
    var swimCycle: Float = 0.0f
)

enum class DecorCategory {
    PLANTS, CORALS, STRUCTURES, SUBSTRATE, LIGHTING
}

data class DecorItem(
    val id: String,
    val name: String,
    val category: DecorCategory,
    val coinPrice: Int,
    val pearlPrice: Int,
    val waterClarityBoost: Float,
    val happinessBonus: Float,
    val isPremium: Boolean = false,
    val description: String,
    val iconEmoji: String,
    val colorHex: Long,
    val assetPath: String? = null
)

data class PlacedDecor(
    val id: String,
    val decorId: String,
    val posX: Float,
    val posY: Float,
    val scale: Float = 1.0f
)

data class TankEnvironment(
    val waterCleanliness: Float = 0.92f, // 0.0 to 1.0
    val waterTemperatureC: Float = 24.5f, // 22 to 28 optimal
    val oxygenLevel: Float = 0.95f,
    val lightingLevel: Float = 0.85f,
    val lightingColorHex: Long = 0xFF00E5FF,
    val substrateType: String = "Caribbean White Sand",
    val backgroundTheme: String = "Deep Blue Abyss",
    val filterActive: Boolean = true,
    val heaterActive: Boolean = true
)

data class DailyChallenge(
    val id: String,
    val title: String,
    val description: String,
    val targetCount: Int,
    var currentCount: Int,
    val rewardCoins: Int,
    val rewardPearls: Int,
    val rewardSpeciesId: String? = null,
    val isClaimed: Boolean = false
)

data class LeaderboardPlayer(
    val rank: Int,
    val name: String,
    val avatarEmoji: String,
    val aquariumLevel: Int,
    val uniqueSpeciesCount: Int,
    val aestheticScore: Int,
    val isCurrentUser: Boolean = false
)
