package ink.ptms.chemdah.module

import ink.ptms.chemdah.api.ChemdahAPI
import ink.ptms.chemdah.core.database.Type
import ink.ptms.chemdah.module.party.PartySystem
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.function.pluginVersion
import taboolib.module.metrics.Metrics
import taboolib.module.metrics.charts.AdvancedPie
import taboolib.module.metrics.charts.SimplePie

object Metrics {

    @Awake(LifeCycle.ENABLE)
    fun init() {
        val metrics = Metrics(11183, pluginVersion, Platform.BUKKIT)
        metrics.addCustomChart(AdvancedPie("objectives") {
            HashMap<String, Int>().also { map ->
                ChemdahAPI.questTemplate.forEach { template ->
                    template.value.taskMap.forEach {
                        map[it.value.objective.name] = (map[it.value.objective.name] ?: 0) + 1
                    }
                }
            }
        })
        metrics.addCustomChart(AdvancedPie("addon") {
            HashMap<String, Int>().also { map ->
                ChemdahAPI.questTemplate.forEach { template ->
                    template.value.taskMap.forEach { task ->
                        task.value.addonMap.keys.forEach {
                            map[it] = (map[it] ?: 0) + 1
                        }
                    }
                }
            }
        })
        metrics.addCustomChart(AdvancedPie("agent") {
            HashMap<String, Int>().also { map ->
                ChemdahAPI.questTemplate.forEach { template ->
                    template.value.taskMap.forEach { task ->
                        task.value.agents.forEach {
                            map[it] = (map[it] ?: 0) + 1
                        }
                    }
                }
            }
        })
        metrics.addCustomChart(SimplePie("database") {
            return@SimplePie Type.INSTANCE.name
        })
        metrics.addCustomChart(SimplePie("party_hook") {
            return@SimplePie PartySystem.conf.getString("default.plugin", "")!!
        })
    }
}