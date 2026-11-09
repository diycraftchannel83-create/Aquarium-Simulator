package com.example.data

import com.example.model.*

object AquariumCatalog {

    val allSpecies = listOf(
        FishSpecies(
            id = "clownfish",
            name = "Ocellaris Clownfish",
            scientificName = "Amphiprion ocellaris",
            rarity = FishRarity.COMMON,
            diet = FishDiet.OMNIVORE,
            primaryColorHex = 0xFFFF7043,
            secondaryColorHex = 0xFFFFFFFF,
            finColorHex = 0xFF212121,
            bodyScale = 0.9f,
            baseSwimSpeed = 1.0f,
            unlockLevel = 1,
            coinPrice = 80,
            description = "Iconic orange fish with crisp white stripes, loves hosting in gentle sea anemones.",
            finStyle = FinStyle.FLOWING
        ),
        FishSpecies(
            id = "blue_tang",
            name = "Royal Blue Tang",
            scientificName = "Paracanthurus hepatus",
            rarity = FishRarity.COMMON,
            diet = FishDiet.HERBIVORE,
            primaryColorHex = 0xFF1E88E5,
            secondaryColorHex = 0xFFFFEB3B,
            finColorHex = 0xFF0D47A1,
            bodyScale = 1.1f,
            baseSwimSpeed = 1.2f,
            unlockLevel = 1,
            coinPrice = 120,
            description = "Vibrant electric blue surgeonfish with a bright yellow tail and gentle gliding motion.",
            finStyle = FinStyle.FLOWING
        ),
        FishSpecies(
            id = "mandarin",
            name = "Psychedelic Mandarinfish",
            scientificName = "Synchiropus splendidus",
            rarity = FishRarity.UNCOMMON,
            diet = FishDiet.CARNIVORE,
            primaryColorHex = 0xFF00E5FF,
            secondaryColorHex = 0xFFFF6D00,
            finColorHex = 0xFF7C4DFF,
            bodyScale = 0.85f,
            baseSwimSpeed = 0.8f,
            unlockLevel = 2,
            coinPrice = 250,
            glowBioluminescent = true,
            description = "One of the ocean's most breathtaking creatures with swirling turquoise and amber waves.",
            finStyle = FinStyle.ROUND
        ),
        FishSpecies(
            id = "flame_angelfish",
            name = "Flame Angelfish",
            scientificName = "Centropyge loricula",
            rarity = FishRarity.UNCOMMON,
            diet = FishDiet.OMNIVORE,
            primaryColorHex = 0xFFFF3D00,
            secondaryColorHex = 0xFF263238,
            finColorHex = 0xFF3D5AFE,
            bodyScale = 1.0f,
            baseSwimSpeed = 1.1f,
            unlockLevel = 3,
            coinPrice = 380,
            description = "Fiery scarlet body accented with deep midnight vertical bars and shimmering sapphire fin tips.",
            finStyle = FinStyle.ANGEL
        ),
        FishSpecies(
            id = "discus_royal",
            name = "Royal Blue Discus",
            scientificName = "Symphysodon aequifasciatus",
            rarity = FishRarity.RARE,
            diet = FishDiet.OMNIVORE,
            primaryColorHex = 0xFF00B0FF,
            secondaryColorHex = 0xFFE040FB,
            finColorHex = 0xFF00E5FF,
            bodyScale = 1.35f,
            baseSwimSpeed = 0.75f,
            unlockLevel = 4,
            coinPrice = 700,
            description = "The undisputed king of the freshwater aquascape, radiating majestic iridescent discs.",
            finStyle = FinStyle.ROUND
        ),
        FishSpecies(
            id = "lionfish",
            name = "Volitan Lionfish",
            scientificName = "Pterois volitans",
            rarity = FishRarity.RARE,
            diet = FishDiet.CARNIVORE,
            primaryColorHex = 0xFFD84315,
            secondaryColorHex = 0xFFFFF8E1,
            finColorHex = 0xFFBF360C,
            bodyScale = 1.25f,
            baseSwimSpeed = 0.65f,
            unlockLevel = 5,
            coinPrice = 950,
            description = "Graceful predator adorned with dramatic fan-like venomous spine plumes.",
            finStyle = FinStyle.FLOWING
        ),
        FishSpecies(
            id = "crystal_jelly",
            name = "Aequorea Bioluminescent Jelly",
            scientificName = "Aequorea victoria",
            rarity = FishRarity.MYTHIC,
            diet = FishDiet.CARNIVORE,
            primaryColorHex = 0xFF69F0AE,
            secondaryColorHex = 0xFF00E5FF,
            finColorHex = 0xFFB388FF,
            bodyScale = 1.2f,
            baseSwimSpeed = 0.45f,
            unlockLevel = 6,
            coinPrice = 1800,
            pearlPrice = 25,
            glowBioluminescent = true,
            description = "Translucent deep-water medusa pulsating with green fluorescent protein (GFP) luminescence.",
            finStyle = FinStyle.JELLY
        ),
        FishSpecies(
            id = "celestial_dragonet",
            name = "Celestial Golden Dragonet",
            scientificName = "Draco astraea",
            rarity = FishRarity.LEGENDARY,
            diet = FishDiet.OMNIVORE,
            primaryColorHex = 0xFFFFD700,
            secondaryColorHex = 0xFFFF4081,
            finColorHex = 0xFF00E5FF,
            bodyScale = 1.4f,
            baseSwimSpeed = 1.3f,
            unlockLevel = 8,
            coinPrice = 4500,
            pearlPrice = 60,
            glowBioluminescent = true,
            description = "Mythical deep-trench specimen that emits sparkling stardust ripples wherever it glides.",
            finStyle = FinStyle.SHARK
        )
    )

    val allDecor = listOf(
        DecorItem(
            id = "amazon_sword",
            name = "Amazon Sword Plant",
            category = DecorCategory.PLANTS,
            coinPrice = 50,
            pearlPrice = 0,
            waterClarityBoost = 0.05f,
            happinessBonus = 0.08f,
            description = "Broad lush green leaves that naturally oxygenate the water column.",
            iconEmoji = "🌿",
            colorHex = 0xFF4CAF50
        ),
        DecorItem(
            id = "fire_coral",
            name = "Glowing Fire Coral",
            category = DecorCategory.CORALS,
            coinPrice = 140,
            pearlPrice = 0,
            waterClarityBoost = 0.03f,
            happinessBonus = 0.12f,
            description = "Warm branching reef coral with vibrant crimson and magenta tips.",
            iconEmoji = "🪸",
            colorHex = 0xFFFF5252
        ),
        DecorItem(
            id = "ancient_ruin",
            name = "Sunken Atlantis Arch",
            category = DecorCategory.STRUCTURES,
            coinPrice = 320,
            pearlPrice = 0,
            waterClarityBoost = 0.02f,
            happinessBonus = 0.15f,
            description = "Carved stone monolith from forgotten sunken kingdoms. Fish love swimming through.",
            iconEmoji = "🏛️",
            colorHex = 0xFF78909C
        ),
        DecorItem(
            id = "neon_anemone",
            name = "Neon Pink Bubble Tip Anemone",
            category = DecorCategory.CORALS,
            coinPrice = 280,
            pearlPrice = 5,
            waterClarityBoost = 0.06f,
            happinessBonus = 0.20f,
            description = "Pulsates with gentle electric pink aura; clownfish gain instant bliss.",
            iconEmoji = "🌸",
            colorHex = 0xFFFF4081
        ),
        DecorItem(
            id = "pearl_treasure_chest",
            name = "Bubbling Gold Chest",
            category = DecorCategory.STRUCTURES,
            coinPrice = 500,
            pearlPrice = 10,
            waterClarityBoost = 0.08f,
            happinessBonus = 0.25f,
            isPremium = true,
            description = "Periodically pops open releasing streams of micro-bubbles and sparkling coins.",
            iconEmoji = "🪙",
            colorHex = 0xFFFFD700
        ),
        DecorItem(
            id = "bioluminescent_driftwood",
            name = "Ancient Bio-Moss Driftwood",
            category = DecorCategory.STRUCTURES,
            coinPrice = 450,
            pearlPrice = 8,
            waterClarityBoost = 0.10f,
            happinessBonus = 0.22f,
            isPremium = true,
            description = "Aged mangrove branch overgrown with tranquil luminescent sea moss.",
            iconEmoji = "🪵",
            colorHex = 0xFF00E676
        ),
        DecorItem(
            id = "volcanic_aerator",
            name = "Mini Volcanic Aerator",
            category = DecorCategory.STRUCTURES,
            coinPrice = 650,
            pearlPrice = 15,
            waterClarityBoost = 0.15f,
            happinessBonus = 0.30f,
            isPremium = true,
            description = "Creates hypnotic rising thermal bubble curtains that oxygenate 2x faster.",
            iconEmoji = "🌋",
            colorHex = 0xFFFF6E40
        )
    )

    val substrateOptions = listOf(
        "Caribbean White Sand",
        "Black Obsidian Volcanic Gravel",
        "Tahitian Azure Crystals",
        "Emerald River Pebbles"
    )

    val backgroundThemes = listOf(
        "Deep Blue Abyss",
        "Coral Paradise Twilight",
        "Luminescent Midnight Trench",
        "Golden Sunlit Lagoon"
    )

    fun getSpecies(id: String): FishSpecies {
        return allSpecies.find { it.id == id } ?: allSpecies.first()
    }

    fun getDecor(id: String): DecorItem? {
        return allDecor.find { it.id == id }
    }

    /**
     * Determines outcome of breeding two fish species.
     * Can produce rare or hybrid variants!
     */
    fun calculateBreedingResult(parent1SpeciesId: String, parent2SpeciesId: String): FishSpecies {
        val parents = setOf(parent1SpeciesId, parent2SpeciesId)
        return when {
            // Special breeding combos
            parents.contains("clownfish") && parents.contains("blue_tang") -> {
                // Cross reef combo produces Mandarin or Flame
                if (Math.random() > 0.5) getSpecies("mandarin") else getSpecies("flame_angelfish")
            }
            parents.contains("mandarin") && parents.contains("flame_angelfish") -> {
                // Mythic jelly or Royal Discus
                if (Math.random() > 0.4) getSpecies("discus_royal") else getSpecies("crystal_jelly")
            }
            parents.contains("discus_royal") && parents.contains("crystal_jelly") -> {
                // Chance for legendary dragonet!
                if (Math.random() > 0.6) getSpecies("celestial_dragonet") else getSpecies("crystal_jelly")
            }
            // Same species breeding
            parent1SpeciesId == parent2SpeciesId -> {
                getSpecies(parent1SpeciesId)
            }
            else -> {
                // Return either parent with slight random mutation
                if (Math.random() > 0.5) getSpecies(parent1SpeciesId) else getSpecies(parent2SpeciesId)
            }
        }
    }
}
