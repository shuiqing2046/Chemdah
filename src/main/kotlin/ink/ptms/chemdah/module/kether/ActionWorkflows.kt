package ink.ptms.chemdah.module.kether

import ink.ptms.chemdah.api.event.collect.PlayerEvents
import ink.ptms.chemdah.util.getPlayer
import ink.ptms.chemdah.util.getProfile
import ink.ptms.chemdah.util.namespace
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.adaptPlayer
import taboolib.library.kether.ArgTypes
import taboolib.library.reflex.Reflex.Companion.getProperty
import taboolib.module.configuration.Type
import taboolib.module.configuration.createLocal
import taboolib.module.kether.*
import java.util.concurrent.ConcurrentHashMap

object ActionWorkflows {

    val data by lazy { createLocal("module/script/data/workflows.json", type = Type.FAST_JSON) }
    val fetchMap = ConcurrentHashMap<String, MutableList<ScriptContext>>()

    @SubscribeEvent
    private fun onSelected(e: PlayerEvents.Selected) {
        e.playerProfile.persistentDataContainer.forEach { (k, v) ->
            if (k.startsWith("workflows.fetch.")) {
                e.playerProfile.persistentDataContainer.remove(k)
                val id = k.substring("workflows.fetch.".length)
                val fetch = data.getString(id)!!
                KetherShell.eval(fetch, sender = adaptPlayer(e.player), namespace = namespace)
            }
        }
    }

    @SubscribeEvent
    private fun onReleased(e: PlayerEvents.Released) {
        fetchMap.remove(e.player.name)?.forEach { ScriptService.terminateQuest(it) }
    }

    @Suppress("ReplaceGetOrSet")
    @KetherParser(["fetch"])
    fun fetch() = scriptParser {
        it.switch {
            case("mark") {
                val id = it.nextToken()
                val begin = it.index
                it.next(ArgTypes.ACTION)
                var content = it.getProperty<CharArray>("content")!!.concatToString().substring(begin, it.index).trim()
                if (content.startsWith('{') && content.endsWith('}')) {
                    content = content.substring(1, content.length - 1)
                }
                content = content.lines().joinToString(" ") { i -> i.trim() }
                data.set(id, content)
                actionNow {
                    fetchMap.computeIfAbsent(getPlayer().name) { ArrayList() } += script()
                    getProfile().persistentDataContainer.set("workflows.fetch.$id", true)
                }
            }
            case("reset") {
                val id = it.nextToken()
                actionNow {
                    getProfile().persistentDataContainer.remove("workflows.fetch.$id")
                }
            }
        }
    }
}