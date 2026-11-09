package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class GameState(
    val coins: Int = 350,
    val pearls: Int = 15,
    val playerLevel: Int = 1,
    val playerExp: Int = 40,
    val maxExp: Int = 100,
    val isDarkMode: Boolean = true,
    val soundVolume: Float = 0.7f,
    val isMusicMuted: Boolean = false,
    val speedGrowthActiveUntil: Long = 0L,
    val lastDailyClaimDate: String = "",
    val dailyStreak: Int = 1,
    val tank: TankEnvironment = TankEnvironment(),
    val fishList: List<FishInstance> = listOf(
        FishInstance(
            id = "f_1",
            speciesId = "clownfish",
            customName = "Nemo",
            growthStage = 0.8f,
            posX = 0.35f,
            posY = 0.45f,
            posZ = 0.5f,
            velX = 0.002f,
            velY = 0.001f
        ),
        FishInstance(
            id = "f_2",
            speciesId = "blue_tang",
            customName = "Dory",
            growthStage = 0.75f,
            posX = 0.65f,
            posY = 0.35f,
            posZ = 0.3f,
            velX = -0.0025f,
            velY = 0.0015f
        )
    ),
    val placedDecorList: List<PlacedDecor> = listOf(
        PlacedDecor(
            id = "pd_1",
            decorId = "amazon_sword",
            posX = 0.15f,
            posY = 0.75f,
            scale = 1.1f
        ),
        PlacedDecor(
            id = "pd_2",
            decorId = "fire_coral",
            posX = 0.82f,
            posY = 0.78f,
            scale = 1.0f
        )
    ),
    val unlockedSpeciesIds: Set<String> = setOf("clownfish", "blue_tang"),
    val dailyChallenges: List<DailyChallenge> = listOf(
        DailyChallenge("ch_1", "Feed Hungry Fish", "Feed your fishes 3 times today", 3, 1, 120, 2),
        DailyChallenge("ch_2", "Pristine Waters", "Clean tank filter & adjust temperature", 1, 0, 150, 3),
        DailyChallenge("ch_3", "Breed Sanctuary", "Perform 1 breeding pairing in the nursery", 1, 0, 300, 5, rewardSpeciesId = "mandarin"),
        DailyChallenge("ch_4", "Tank Stylist", "Place a decorative piece in your tank", 1, 1, 100, 1, isClaimed = true)
    ),
    val lastCloudSyncTimestamp: Long = System.currentTimeMillis() - 3600000L,
    val cloudSyncStatus: String = "In Sync",
    val cloudSyncCode: String = "AQZ-8942-ZEN"
)

class GameRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("aquazen_save_prefs", Context.MODE_PRIVATE)

    private val _gameState = MutableStateFlow(loadGameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private fun loadGameState(): GameState {
        val jsonStr = prefs.getString("game_state_json", null)
        if (jsonStr == null) {
            return GameState()
        }
        return try {
            val root = JSONObject(jsonStr)
            val coins = root.optInt("coins", 350)
            val pearls = root.optInt("pearls", 15)
            val level = root.optInt("playerLevel", 1)
            val exp = root.optInt("playerExp", 40)
            val isDark = root.optBoolean("isDarkMode", true)
            val soundVol = root.optDouble("soundVolume", 0.7).toFloat()
            val isMuted = root.optBoolean("isMusicMuted", false)
            val speedUntil = root.optLong("speedGrowthActiveUntil", 0L)
            val lastClaim = root.optString("lastDailyClaimDate", "")
            val streak = root.optInt("dailyStreak", 1)
            val lastSync = root.optLong("lastCloudSyncTimestamp", System.currentTimeMillis())
            val syncCode = root.optString("cloudSyncCode", "AQZ-8942-ZEN")

            val tankObj = root.optJSONObject("tank")
            val tank = if (tankObj != null) {
                TankEnvironment(
                    waterCleanliness = tankObj.optDouble("waterCleanliness", 0.92).toFloat(),
                    waterTemperatureC = tankObj.optDouble("waterTemperatureC", 24.5).toFloat(),
                    oxygenLevel = tankObj.optDouble("oxygenLevel", 0.95).toFloat(),
                    lightingLevel = tankObj.optDouble("lightingLevel", 0.85).toFloat(),
                    lightingColorHex = tankObj.optLong("lightingColorHex", 0xFF00E5FF),
                    substrateType = tankObj.optString("substrateType", "Caribbean White Sand"),
                    backgroundTheme = tankObj.optString("backgroundTheme", "Deep Blue Abyss"),
                    filterActive = tankObj.optBoolean("filterActive", true),
                    heaterActive = tankObj.optBoolean("heaterActive", true)
                )
            } else TankEnvironment()

            // Fish list
            val fishArr = root.optJSONArray("fishList")
            val fishList = mutableListOf<FishInstance>()
            if (fishArr != null) {
                for (i in 0 until fishArr.length()) {
                    val obj = fishArr.getJSONObject(i)
                    fishList.add(
                        FishInstance(
                            id = obj.optString("id", UUID.randomUUID().toString()),
                            speciesId = obj.optString("speciesId", "clownfish"),
                            customName = obj.optString("customName", "Fish"),
                            hungerPercent = obj.optDouble("hungerPercent", 0.85).toFloat(),
                            happinessPercent = obj.optDouble("happinessPercent", 0.95).toFloat(),
                            growthStage = obj.optDouble("growthStage", 0.5).toFloat(),
                            isPregnant = obj.optBoolean("isPregnant", false),
                            mateSpeciesId = obj.optString("mateSpeciesId").takeIf { it.isNotEmpty() },
                            pregnancyProgress = obj.optDouble("pregnancyProgress", 0.0).toFloat(),
                            posX = obj.optDouble("posX", 0.5).toFloat(),
                            posY = obj.optDouble("posY", 0.5).toFloat(),
                            posZ = obj.optDouble("posZ", 0.5).toFloat()
                        )
                    )
                }
            } else {
                fishList.addAll(GameState().fishList)
            }

            // Decor list
            val decorArr = root.optJSONArray("placedDecorList")
            val decorList = mutableListOf<PlacedDecor>()
            if (decorArr != null) {
                for (i in 0 until decorArr.length()) {
                    val obj = decorArr.getJSONObject(i)
                    decorList.add(
                        PlacedDecor(
                            id = obj.optString("id", UUID.randomUUID().toString()),
                            decorId = obj.optString("decorId", "amazon_sword"),
                            posX = obj.optDouble("posX", 0.5).toFloat(),
                            posY = obj.optDouble("posY", 0.7).toFloat(),
                            scale = obj.optDouble("scale", 1.0).toFloat()
                        )
                    )
                }
            } else {
                decorList.addAll(GameState().placedDecorList)
            }

            val unlockedArr = root.optJSONArray("unlockedSpeciesIds")
            val unlocked = mutableSetOf<String>()
            if (unlockedArr != null) {
                for (i in 0 until unlockedArr.length()) {
                    unlocked.add(unlockedArr.getString(i))
                }
            } else {
                unlocked.addAll(listOf("clownfish", "blue_tang"))
            }

            GameState(
                coins = coins,
                pearls = pearls,
                playerLevel = level,
                playerExp = exp,
                isDarkMode = isDark,
                soundVolume = soundVol,
                isMusicMuted = isMuted,
                speedGrowthActiveUntil = speedUntil,
                lastDailyClaimDate = lastClaim,
                dailyStreak = streak,
                tank = tank,
                fishList = fishList,
                placedDecorList = decorList,
                unlockedSpeciesIds = unlocked,
                lastCloudSyncTimestamp = lastSync,
                cloudSyncCode = syncCode
            )
        } catch (e: Exception) {
            GameState()
        }
    }

    fun saveGameState(state: GameState) {
        _gameState.value = state
        try {
            val root = JSONObject()
            root.put("coins", state.coins)
            root.put("pearls", state.pearls)
            root.put("playerLevel", state.playerLevel)
            root.put("playerExp", state.playerExp)
            root.put("isDarkMode", state.isDarkMode)
            root.put("soundVolume", state.soundVolume.toDouble())
            root.put("isMusicMuted", state.isMusicMuted)
            root.put("speedGrowthActiveUntil", state.speedGrowthActiveUntil)
            root.put("lastDailyClaimDate", state.lastDailyClaimDate)
            root.put("dailyStreak", state.dailyStreak)
            root.put("lastCloudSyncTimestamp", state.lastCloudSyncTimestamp)
            root.put("cloudSyncCode", state.cloudSyncCode)

            val tankObj = JSONObject()
            tankObj.put("waterCleanliness", state.tank.waterCleanliness.toDouble())
            tankObj.put("waterTemperatureC", state.tank.waterTemperatureC.toDouble())
            tankObj.put("oxygenLevel", state.tank.oxygenLevel.toDouble())
            tankObj.put("lightingLevel", state.tank.lightingLevel.toDouble())
            tankObj.put("lightingColorHex", state.tank.lightingColorHex)
            tankObj.put("substrateType", state.tank.substrateType)
            tankObj.put("backgroundTheme", state.tank.backgroundTheme)
            tankObj.put("filterActive", state.tank.filterActive)
            tankObj.put("heaterActive", state.tank.heaterActive)
            root.put("tank", tankObj)

            val fishArr = JSONArray()
            for (f in state.fishList) {
                val fObj = JSONObject()
                fObj.put("id", f.id)
                fObj.put("speciesId", f.speciesId)
                fObj.put("customName", f.customName)
                fObj.put("hungerPercent", f.hungerPercent.toDouble())
                fObj.put("happinessPercent", f.happinessPercent.toDouble())
                fObj.put("growthStage", f.growthStage.toDouble())
                fObj.put("isPregnant", f.isPregnant)
                fObj.put("mateSpeciesId", f.mateSpeciesId ?: "")
                fObj.put("pregnancyProgress", f.pregnancyProgress.toDouble())
                fObj.put("posX", f.posX.toDouble())
                fObj.put("posY", f.posY.toDouble())
                fObj.put("posZ", f.posZ.toDouble())
                fishArr.put(fObj)
            }
            root.put("fishList", fishArr)

            val decorArr = JSONArray()
            for (d in state.placedDecorList) {
                val dObj = JSONObject()
                dObj.put("id", d.id)
                dObj.put("decorId", d.decorId)
                dObj.put("posX", d.posX.toDouble())
                dObj.put("posY", d.posY.toDouble())
                dObj.put("scale", d.scale.toDouble())
                decorArr.put(dObj)
            }
            root.put("placedDecorList", decorArr)

            val unlockArr = JSONArray()
            state.unlockedSpeciesIds.forEach { unlockArr.put(it) }
            root.put("unlockedSpeciesIds", unlockArr)

            prefs.edit().putString("game_state_json", root.toString()).apply()
        } catch (e: Exception) {
            // Handled
        }
    }

    /** Persist player actions immediately (backward-compatible call shape). */
    fun updateState(transform: (GameState) -> GameState) {
        updateState(transform, persist = true)
    }

    /**
     * Publishes a new game state. High-frequency simulation ticks pass persist=false
     * and call persistCurrentState() on a bounded interval.
     */
    fun updateState(
        transform: (GameState) -> GameState,
        persist: Boolean
    ) {
        synchronized(this) {
            val newState = transform(_gameState.value)
            if (persist) {
                saveGameState(newState)
            } else {
                _gameState.value = newState
            }
        }
    }

    /** Persist the latest in-memory simulation snapshot to local storage. */
    fun persistCurrentState() {
        synchronized(this) {
            saveGameState(_gameState.value)
        }
    }
}
