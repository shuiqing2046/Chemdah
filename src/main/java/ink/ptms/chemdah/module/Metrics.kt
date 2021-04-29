package ink.ptms.chemdah.module

import ink.ptms.chemdah.Chemdah
import ink.ptms.chemdah.api.ChemdahAPI
import ink.ptms.chemdah.core.database.Type
import ink.ptms.chemdah.module.party.PartySystem
import io.izzel.taboolib.metrics.BMetrics
import io.izzel.taboolib.module.inject.TFunction

object Metrics {

    @TFunction.Init
    private fun init() {
        val metrics = BMetrics(Chemdah.plugin, 11183)
        metrics.addCustomChart(BMetrics.AdvancedPie("objectives") {
            HashMap<String, Int>().also { map ->
                ChemdahAPI.questTemplate.forEach { template ->
                    template.value.task.forEach {
                        map[it.value.objective.name] = (map[it.value.objective.name] ?: 0) + 1
                    }
                }
            }
        })
        metrics.addCustomChart(BMetrics.AdvancedPie("addon") {
            HashMap<String, Int>().also { map ->
                ChemdahAPI.questTemplate.forEach { template ->
                    template.value.task.forEach { task ->
                        task.value.addons.forEach {
                            map[it] = (map[it] ?: 0) + 1
                        }
                    }
                }
            }
        })
        metrics.addCustomChart(BMetrics.AdvancedPie("agent") {
            HashMap<String, Int>().also { map ->
                ChemdahAPI.questTemplate.forEach { template ->
                    template.value.task.forEach { task ->
                        task.value.agents.forEach {
                            map[it] = (map[it] ?: 0) + 1
                        }
                    }
                }
            }
        })
        metrics.addCustomChart(BMetrics.SimplePie("database") {
            return@SimplePie Type.INSTANCE.name
        })
        metrics.addCustomChart(BMetrics.SimplePie("party_hook") {
            return@SimplePie PartySystem.conf.getString("default.plugin", "")!!
        })
    }
}