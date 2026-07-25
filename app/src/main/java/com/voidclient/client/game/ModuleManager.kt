package com.voidclient.client.game

import android.content.Context
import android.net.Uri
import android.os.Environment
import com.voidclient.client.application.AppContext
import com.voidclient.client.game.module.combat.ACAModule
import com.voidclient.client.game.module.combat.AntiCrystalModule
import com.voidclient.client.game.module.combat.AntiKnockbackModule
import com.voidclient.client.game.module.combat.CrystalSmashModule
import com.voidclient.client.game.module.combat.EnemyHunterModule
import com.voidclient.client.game.module.combat.HitAndRunModule
import com.voidclient.client.game.module.combat.HitboxModule
import com.voidclient.client.game.module.combat.KillauraModule
import com.voidclient.client.game.module.combat.TriggerBotModule
import com.voidclient.client.game.module.combat.WAuraModule
import com.voidclient.client.game.module.combat.AutoFightModule
import com.voidclient.client.game.module.combat.AutoHvHModule
import com.voidclient.client.game.module.combat.AutoTotemModule
import com.voidclient.client.game.module.combat.HotbarSwitcherModule
import com.voidclient.client.game.module.combat.InfiniteAuraModule
import com.voidclient.client.game.module.misc.ArrayListModule
import com.voidclient.client.game.module.motion.NoClipModule
import com.voidclient.client.game.module.misc.AutoDisconnectModule
import com.voidclient.client.game.module.misc.CommandHandlerModule
import com.voidclient.client.game.module.visual.CoordinatesModule
import com.voidclient.client.game.module.misc.DesyncModule
import com.voidclient.client.game.module.misc.FakeDeathModule
import com.voidclient.client.game.module.misc.FakeXPModule
import com.voidclient.client.game.module.misc.MinerModule
import com.voidclient.client.game.module.misc.NoChatModule
import com.voidclient.client.game.module.misc.PieChartModule
import com.voidclient.client.game.module.misc.PositionLoggerModule
import com.voidclient.client.game.module.misc.ReplayModule
import com.voidclient.client.game.module.misc.ChestStealerModule
import com.voidclient.client.game.module.misc.SpammerModule
import com.voidclient.client.game.module.misc.ToggleSoundModule
import com.voidclient.client.game.module.misc.WaterMarkModule
import com.voidclient.client.game.module.world.AntiDebuffModule
import com.voidclient.client.game.module.world.EffectsModule
import com.voidclient.client.game.module.world.ParticlesModule
import com.voidclient.client.game.module.world.TimeShiftModule
import com.voidclient.client.game.module.world.WeatherControllerModule
import com.voidclient.client.game.module.motion.AirJumpModule
import com.voidclient.client.game.module.motion.AntiAFKModule
import com.voidclient.client.game.module.motion.AutoWalkModule
import com.voidclient.client.game.module.motion.BhopModule
import com.voidclient.client.game.module.motion.FlyModule
import com.voidclient.client.game.module.motion.HighJumpModule
import com.voidclient.client.game.module.motion.JetPackModule
import com.voidclient.client.game.module.motion.MotionFlyModule
import com.voidclient.client.game.module.motion.PlayerTPModule
import com.voidclient.client.game.module.motion.SpeedModule
import com.voidclient.client.game.module.motion.SpiderModule
import com.voidclient.client.game.module.motion.SprintModule
import com.voidclient.client.game.module.visual.CrosshairModule
import com.voidclient.client.game.module.visual.DamageTextModule
import com.voidclient.client.game.module.visual.ESPModule
import com.voidclient.client.game.module.visual.FullbrightModule
import com.voidclient.client.game.module.visual.MinimapModule
import com.voidclient.client.game.module.visual.NetworkInfoModule
import com.voidclient.client.game.module.visual.NoHurtCameraModule
import com.voidclient.client.game.module.visual.PlayerJoinModule
import com.voidclient.client.game.module.visual.SpeedDisplayModule
import com.voidclient.client.game.module.visual.WorldStateModule
import com.voidclient.client.game.module.visual.ZoomModule
import com.voidclient.client.game.module.visual.TargetHudModule
import com.voidclient.client.game.module.world.FreeCameraModule
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put
import java.io.File

object ModuleManager {

    private val _modules: MutableList<Module> = ArrayList()

    val modules: List<Module> = _modules

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    init {
        with(_modules) {
            // Combat
            add(WAuraModule())
            add(HotbarSwitcherModule())
            add(KillauraModule())
            add(AutoFightModule())
            add(InfiniteAuraModule())
            add(ACAModule())
            add(AutoTotemModule())
            add(AutoHvHModule())
            add(EnemyHunterModule())
            add(AntiKnockbackModule())

            add(AntiCrystalModule())
            add(HitAndRunModule())
            add(HitboxModule())
            add(CrystalSmashModule())
            add(TriggerBotModule())

            // Motion
            add(MotionFlyModule())
            add(PlayerTPModule())
            add(FlyModule())
            add(SpeedModule())
            add(AirJumpModule())
            add(NoClipModule())
            add(JetPackModule())
            add(HighJumpModule())
            add(BhopModule())
            add(SprintModule())
            add(AutoWalkModule())
            add(AntiAFKModule())
            add(SpiderModule())

            // Visual
            add(DamageTextModule())
            add(ESPModule())
            add(PlayerJoinModule())
            add(ZoomModule())
            add(CoordinatesModule())
            add(NoHurtCameraModule())
            add(SpeedDisplayModule())
            add(NetworkInfoModule())
            add(WorldStateModule())
            add(MinimapModule())
            add(CrosshairModule())
            add(TargetHudModule())
            add(FullbrightModule())

            // World
            add(FreeCameraModule())
            add(TimeShiftModule())
            add(WeatherControllerModule())
            add(EffectsModule())
            add(ParticlesModule())
            add(AntiDebuffModule())

            // Misc

            add(AutoDisconnectModule())
            add(ArrayListModule())
            add(ToggleSoundModule())
            add(ChestStealerModule())
            add(DesyncModule())
            add(SpammerModule())
            add(WaterMarkModule())
            add(PositionLoggerModule())
            add(NoChatModule())
            add(CommandHandlerModule())
            add(ReplayModule())
            add(PieChartModule())
            add(FakeDeathModule())
            add(FakeXPModule())
            add(MinerModule())
        }
    }

    fun saveConfig() {

        if (!AppContext.isInitialized) {
            return
        }

        val configsDir = AppContext.instance.filesDir.resolve("configs")
        configsDir.mkdirs()

        val config = configsDir.resolve("UserConfig.json")
        val jsonObject = buildJsonObject {
            put("modules", buildJsonObject {
                _modules.forEach {
                    if (it.private) {
                        return@forEach
                    }
                    put(it.name, it.toJson())
                }
            })
        }

        config.writeText(json.encodeToString(JsonObject.serializer(), jsonObject))
    }

    fun loadConfig() {

        if (!AppContext.isInitialized) {
            return
        }

        val configsDir = AppContext.instance.filesDir.resolve("configs")
        configsDir.mkdirs()

        val config = configsDir.resolve("UserConfig.json")
        if (!config.exists()) {
            return
        }

        val jsonString = config.readText()
        if (jsonString.isEmpty()) {
            return
        }

        try {
            val jsonObject = json.parseToJsonElement(jsonString).jsonObject
            val modules = jsonObject["modules"]?.jsonObject ?: return

            _modules.forEach { module ->
                (modules[module.name] as? JsonObject)?.let {
                    module.fromJson(it)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun exportConfig(): String {
        val jsonObject = buildJsonObject {
            put("modules", buildJsonObject {
                _modules.forEach {
                    if (it.private) {
                        return@forEach
                    }
                    put(it.name, it.toJson())
                }
            })
        }
        return json.encodeToString(JsonObject.serializer(), jsonObject)
    }

    fun importConfig(configStr: String) {
        try {
            val jsonObject = json.parseToJsonElement(configStr).jsonObject
            val modules = jsonObject["modules"]?.jsonObject ?: return

            _modules.forEach { module ->
                modules[module.name]?.let {
                    if (it is JsonObject) {
                        module.fromJson(it)
                    }
                }
            }
        } catch (e: Exception) {
            throw IllegalArgumentException("Invalid config format: ${e.message}")
        }
    }

    fun exportConfigToFile(context: Context, filePath: String): Boolean {
        return try {
            val file = if (filePath.contains("/")) {
                File(filePath)
            } else {
                val configsDir = context.getExternalFilesDir("configs")
                configsDir?.mkdirs()
                File(configsDir, if (filePath.endsWith(".json")) filePath else "$filePath.json")
            }

            file.parentFile?.mkdirs()
            file.writeText(exportConfig())
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun getWClientConfigsDirectory(): File? {
        return try {
            val documentsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)

            val baseDir = if (documentsDir.exists() || documentsDir.mkdirs()) {
                documentsDir
            } else {
                downloadsDir
            }

            val wclientDir = File(baseDir, "WClient")
            val configsDir = File(wclientDir, "configs")

            if (configsDir.exists() || configsDir.mkdirs()) {
                configsDir
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun importConfigFromFile(context: Context, uri: Uri): Boolean {
        return try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                val configStr = input.bufferedReader().readText()
                importConfig(configStr)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}