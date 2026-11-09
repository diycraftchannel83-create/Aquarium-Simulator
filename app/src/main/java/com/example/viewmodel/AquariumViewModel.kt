package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.ZenAmbientSynthesizer
import com.example.data.AquariumCatalog
import com.example.data.GameRepository
import com.example.data.GameState
import com.example.model.*
import com.example.notification.AquariumNotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.hypot

enum class ActiveScreen {
    AQUARIUM_VIEW,
    TANK_CUSTOMIZATION,
    FISH_NURSERY_BREEDING,
    SPECIES_CATALOG,
    DAILY_CHALLENGES_LEADERBOARD,
    AQUA_STORE_IAP,
    TUTORIAL_GUIDE
}

class AquariumViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = GameRepository(application)
    val gameState: StateFlow<GameState> = repository.gameState

    private val ambientSynthesizer = ZenAmbientSynthesizer(application)

    val leaderboards = listOf(
        LeaderboardPlayer(1, "CoralMaster_Kai", "🐬", 14, 8, 9850),
        LeaderboardPlayer(2, "AquaZen_You", "🪸", 1, 2, 2400, isCurrentUser = true),
        LeaderboardPlayer(3, "DeepSeaDiver99", "🦈", 11, 7, 8920),
        LeaderboardPlayer(4, "ReefWhisperer", "🐠", 9, 6, 7340),
        LeaderboardPlayer(5, "OceaniaBelle", "🌊", 8, 5, 6800),
        LeaderboardPlayer(6, "BiolumGlow", "✨", 7, 5, 5950)
    )

    init {
        AquariumNotificationHelper.initNotificationChannel(application)
        startSimulationPhysicsLoop()
        startPeriodicPassiveEarnings()
        if (!gameState.value.isMusicMuted) {
            ambientSynthesizer.setVolume(gameState.value.soundVolume)
            ambientSynthesizer.startAmbientTrack(viewModelScope)
        }
    }

    /**
     * Continuous 3D fish movement, flocking, hunger digestion, and growth simulation loop.
     */
    private fun startSimulationPhysicsLoop() {
        viewModelScope.launch(Dispatchers.Default) {
            var lastPersistAt = System.currentTimeMillis()
            while (isActive) {
                delay(33) // ~30 fps physics update
                val currentState = repository.gameState.value
                val isGrowthBoosted = System.currentTimeMillis() < currentState.speedGrowthActiveUntil
                val growthRate = if (isGrowthBoosted) 0.0004f else 0.0001f

                val updatedFish = currentState.fishList.map { fish ->
                    var px = fish.posX
                    var py = fish.posY
                    var pz = fish.posZ
                    var vx = fish.velX
                    var vy = fish.velY
                    var vz = fish.velZ

                    // Flocking & wandering towards random targets
                    val distToTarget = hypot(fish.targetX - px, fish.targetY - py)
                    if (distToTarget < 0.05f || Math.random() < 0.015) {
                        fish.targetX = (0.15f + Math.random() * 0.7f).toFloat()
                        fish.targetY = (0.2f + Math.random() * 0.6f).toFloat()
                        fish.targetZ = (0.2f + Math.random() * 0.6f).toFloat()
                    }

                    // Steer gently towards target
                    val steerX = (fish.targetX - px) * 0.0005f
                    val steerY = (fish.targetY - py) * 0.0004f
                    val steerZ = (fish.targetZ - pz) * 0.0004f

                    vx = (vx + steerX).coerceIn(-0.004f, 0.004f)
                    vy = (vy + steerY).coerceIn(-0.003f, 0.003f)
                    vz = (vz + steerZ).coerceIn(-0.003f, 0.003f)

                    px = (px + vx).coerceIn(0.08f, 0.92f)
                    py = (py + vy).coerceIn(0.12f, 0.85f)
                    pz = (pz + vz).coerceIn(0.1f, 0.9f)

                    // Bounce if near boundaries
                    if (px <= 0.08f || px >= 0.92f) vx = -vx
                    if (py <= 0.12f || py >= 0.85f) vy = -vy

                    val newGrowth = (fish.growthStage + growthRate).coerceAtMost(1.0f)
                    val newHunger = (fish.hungerPercent - 0.00005f).coerceAtLeast(0.1f)

                    // Pregnancy incubation progress
                    var pregProgress = fish.pregnancyProgress
                    var isStillPregnant = fish.isPregnant
                    if (fish.isPregnant) {
                        pregProgress += if (isGrowthBoosted) 0.003f else 0.0008f
                        if (pregProgress >= 1.0f) {
                            // Birth ready!
                            isStillPregnant = false
                            pregProgress = 0.0f
                        }
                    }

                    fish.copy(
                        posX = px,
                        posY = py,
                        posZ = pz,
                        velX = vx,
                        velY = vy,
                        velZ = vz,
                        growthStage = newGrowth,
                        hungerPercent = newHunger,
                        pregnancyProgress = pregProgress,
                        isPregnant = isStillPregnant
                    )
                }

                // Check water cleanliness decay
                val newCleanliness = (currentState.tank.waterCleanliness - 0.00001f).coerceAtLeast(0.3f)

                // Keep the render state responsive without serializing the full save
                // file on every 33 ms simulation tick.
                repository.updateState(
                    transform = { state ->
                        state.copy(
                            fishList = updatedFish,
                            tank = state.tank.copy(waterCleanliness = newCleanliness)
                        )
                    },
                    persist = false
                )

                val now = System.currentTimeMillis()
                if (now - lastPersistAt >= 5_000L) {
                    repository.persistCurrentState()
                    lastPersistAt = now
                }
            }
        }
    }

    /**
     * Periodic passive coin generation from happy adult fish.
     */
    private fun startPeriodicPassiveEarnings() {
        viewModelScope.launch(Dispatchers.Default) {
            while (isActive) {
                delay(15000) // Every 15 seconds, collect passive rewards
                repository.updateState { state ->
                    val earnings = state.fishList.sumOf { (it.growthStage * 5).toInt().coerceAtLeast(1) }
                    state.copy(coins = state.coins + earnings)
                }
            }
        }
    }

    // --- User Actions ---

    fun feedFishAt(x: Float, y: Float) {
        repository.updateState { state ->
            val updatedFish = state.fishList.map { fish ->
                val fedHunger = (fish.hungerPercent + 0.25f).coerceAtMost(1.0f)
                val happy = (fish.happinessPercent + 0.15f).coerceAtMost(1.0f)
                fish.copy(hungerPercent = fedHunger, happinessPercent = happy)
            }
            // Advance feeding daily challenge
            val updatedChallenges = state.dailyChallenges.map { ch ->
                if (ch.id == "ch_1" && ch.currentCount < ch.targetCount) {
                    ch.copy(currentCount = ch.currentCount + 1)
                } else ch
            }
            state.copy(
                fishList = updatedFish,
                dailyChallenges = updatedChallenges,
                playerExp = (state.playerExp + 5).let { if (it >= state.maxExp) it - state.maxExp else it },
                playerLevel = if (state.playerExp + 5 >= state.maxExp) state.playerLevel + 1 else state.playerLevel
            )
        }
    }

    fun cleanWaterFilter() {
        repository.updateState { state ->
            val updatedChallenges = state.dailyChallenges.map { ch ->
                if (ch.id == "ch_2") ch.copy(currentCount = 1) else ch
            }
            state.copy(
                tank = state.tank.copy(waterCleanliness = 1.0f),
                dailyChallenges = updatedChallenges,
                playerExp = (state.playerExp + 15).coerceAtMost(state.maxExp)
            )
        }
    }

    fun updateWaterTemperature(temp: Float) {
        repository.updateState { state ->
            state.copy(tank = state.tank.copy(waterTemperatureC = temp))
        }
    }

    fun updateLightingColor(colorHex: Long) {
        repository.updateState { state ->
            state.copy(tank = state.tank.copy(lightingColorHex = colorHex))
        }
    }

    fun updateSubstrate(substrate: String) {
        repository.updateState { state ->
            state.copy(tank = state.tank.copy(substrateType = substrate))
        }
    }

    fun updateBackgroundTheme(theme: String) {
        repository.updateState { state ->
            state.copy(tank = state.tank.copy(backgroundTheme = theme))
        }
    }

    fun purchaseAndPlaceDecor(decor: DecorItem) {
        val state = gameState.value
        if (state.coins >= decor.coinPrice && state.pearls >= decor.pearlPrice) {
            repository.updateState { s ->
                val newPlaced = PlacedDecor(
                    id = UUID.randomUUID().toString(),
                    decorId = decor.id,
                    posX = (0.2f + Math.random() * 0.6f).toFloat(),
                    posY = 0.8f,
                    scale = 1.0f
                )
                val updatedChallenges = s.dailyChallenges.map { ch ->
                    if (ch.id == "ch_4") ch.copy(currentCount = 1) else ch
                }
                s.copy(
                    coins = s.coins - decor.coinPrice,
                    pearls = s.pearls - decor.pearlPrice,
                    placedDecorList = s.placedDecorList + newPlaced,
                    dailyChallenges = updatedChallenges,
                    tank = s.tank.copy(
                        waterCleanliness = (s.tank.waterCleanliness + decor.waterClarityBoost).coerceAtMost(1.0f)
                    )
                )
            }
        }
    }

    fun removeDecor(placedId: String) {
        repository.updateState { s ->
            s.copy(placedDecorList = s.placedDecorList.filterNot { it.id == placedId })
        }
    }

    fun buySpecies(species: FishSpecies) {
        val state = gameState.value
        if (state.coins >= species.coinPrice && state.pearls >= species.pearlPrice) {
            repository.updateState { s ->
                val newFish = FishInstance(
                    id = UUID.randomUUID().toString(),
                    speciesId = species.id,
                    customName = species.name.split(" ").last(),
                    growthStage = 0.35f,
                    posX = (0.3f + Math.random() * 0.4f).toFloat(),
                    posY = (0.3f + Math.random() * 0.4f).toFloat()
                )
                s.copy(
                    coins = s.coins - species.coinPrice,
                    pearls = s.pearls - species.pearlPrice,
                    fishList = s.fishList + newFish,
                    unlockedSpeciesIds = s.unlockedSpeciesIds + species.id
                )
            }
        }
    }

    fun breedFish(parent1: FishInstance, parent2: FishInstance) {
        val childSpecies = AquariumCatalog.calculateBreedingResult(parent1.speciesId, parent2.speciesId)
        repository.updateState { s ->
            val updatedFishList = s.fishList.map {
                if (it.id == parent1.id) it.copy(isPregnant = true, mateSpeciesId = parent2.speciesId, pregnancyProgress = 0.05f)
                else it
            }
            val updatedChallenges = s.dailyChallenges.map { ch ->
                if (ch.id == "ch_3") ch.copy(currentCount = 1) else ch
            }
            s.copy(
                fishList = updatedFishList,
                dailyChallenges = updatedChallenges
            )
        }
    }

    fun claimBreedingBaby(pregnantFish: FishInstance) {
        val mateId = pregnantFish.mateSpeciesId ?: pregnantFish.speciesId
        val babySpecies = AquariumCatalog.calculateBreedingResult(pregnantFish.speciesId, mateId)

        repository.updateState { s ->
            val newBaby = FishInstance(
                id = UUID.randomUUID().toString(),
                speciesId = babySpecies.id,
                customName = "Baby ${babySpecies.name.split(" ").last()}",
                growthStage = 0.15f,
                posX = pregnantFish.posX,
                posY = pregnantFish.posY
            )
            val updatedFish = s.fishList.map {
                if (it.id == pregnantFish.id) it.copy(isPregnant = false, mateSpeciesId = null, pregnancyProgress = 0f)
                else it
            }
            s.copy(
                fishList = updatedFish + newBaby,
                unlockedSpeciesIds = s.unlockedSpeciesIds + babySpecies.id,
                coins = s.coins + 150,
                playerExp = (s.playerExp + 30).coerceAtMost(s.maxExp)
            )
        }
    }

    fun claimDailyChallenge(challengeId: String) {
        repository.updateState { s ->
            val ch = s.dailyChallenges.find { it.id == challengeId } ?: return@updateState s
            if (ch.currentCount >= ch.targetCount && !ch.isClaimed) {
                val updatedChallenges = s.dailyChallenges.map {
                    if (it.id == challengeId) it.copy(isClaimed = true) else it
                }
                val newUnlocked = if (ch.rewardSpeciesId != null) s.unlockedSpeciesIds + ch.rewardSpeciesId else s.unlockedSpeciesIds
                s.copy(
                    coins = s.coins + ch.rewardCoins,
                    pearls = s.pearls + ch.rewardPearls,
                    dailyChallenges = updatedChallenges,
                    unlockedSpeciesIds = newUnlocked
                )
            } else s
        }
    }

    fun claimDailyLoginReward() {
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val state = gameState.value
        if (state.lastDailyClaimDate != todayStr) {
            repository.updateState { s ->
                val streakBonus = (s.dailyStreak * 50).coerceAtMost(500)
                s.copy(
                    lastDailyClaimDate = todayStr,
                    dailyStreak = s.dailyStreak + 1,
                    coins = s.coins + 200 + streakBonus,
                    pearls = s.pearls + 3
                )
            }
            AquariumNotificationHelper.sendCareAlert(
                getApplication(),
                "Daily Sanctuary Blessing Claimed!",
                "You claimed your streak bonus of 250+ Coins and 3 Pearls."
            )
        }
    }

    fun applySpeedGrowthBooster() {
        val durationMillis = 30 * 60 * 1000L // 30 minutes of 4x growth
        repository.updateState { s ->
            s.copy(speedGrowthActiveUntil = System.currentTimeMillis() + durationMillis)
        }
    }

    fun purchaseIAPPackage(title: String, coinsToAdd: Int, pearlsToAdd: Int) {
        repository.updateState { s ->
            s.copy(
                coins = s.coins + coinsToAdd,
                pearls = s.pearls + pearlsToAdd
            )
        }
    }

    fun performCloudSync() {
        repository.updateState { s ->
            s.copy(
                lastCloudSyncTimestamp = System.currentTimeMillis(),
                cloudSyncStatus = "Synced to Cloud Sanctuary"
            )
        }
    }

    fun toggleDarkMode() {
        repository.updateState { s ->
            s.copy(isDarkMode = !s.isDarkMode)
        }
    }

    fun toggleMusic() {
        repository.updateState { s ->
            val nextMuted = !s.isMusicMuted
            if (nextMuted) {
                ambientSynthesizer.stopAmbientTrack()
            } else {
                ambientSynthesizer.setVolume(s.soundVolume)
                ambientSynthesizer.startAmbientTrack(viewModelScope)
            }
            s.copy(isMusicMuted = nextMuted)
        }
    }

    fun setSoundVolume(volume: Float) {
        ambientSynthesizer.setVolume(volume)
        repository.updateState { s ->
            s.copy(soundVolume = volume)
        }
    }

    override fun onCleared() {
        repository.persistCurrentState()
        ambientSynthesizer.stopAmbientTrack()
        super.onCleared()
    }
}
