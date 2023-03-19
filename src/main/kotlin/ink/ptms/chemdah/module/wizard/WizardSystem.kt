package ink.ptms.chemdah.module.wizard

import ink.ptms.adyeshach.common.entity.EntityInstance
import ink.ptms.chemdah.api.event.collect.ConversationEvents
import ink.ptms.chemdah.module.Module
import ink.ptms.chemdah.module.Module.Companion.register
import org.bukkit.event.player.PlayerQuitEvent
import taboolib.common.platform.Awake
import taboolib.common.platform.Schedule
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.getDataFolder
import taboolib.common.platform.function.info
import taboolib.common.platform.function.releaseResourceFile
import taboolib.common.platform.function.submit
import taboolib.module.configuration.Configuration
import java.io.File
import java.util.concurrent.ConcurrentHashMap

/**
 * Chemdah
 * ink.ptms.chemdah.module.wizard.WizardSystem
 *
 * @author sky
 * @since 2021/3/11 9:03 上午
 */
@Awake
object WizardSystem : Module {

    val infoMap = ConcurrentHashMap<String, WizardInfo>()
    val actions = ConcurrentHashMap<String, WizardAction>()

    init {
        register()
    }

    override fun reload() {
        infoMap.clear()
        val folder = File(getDataFolder(), "module/wizard")
        if (!folder.exists()) {
            releaseResourceFile("module/wizard/example.yml", false)
        }
        submit {
            loadFromFile(folder)
            info("${infoMap.size} wizards loaded.")
        }
    }

    fun loadFromFile(file: File) {
        if (file.isDirectory) {
            file.listFiles()?.forEach { loadFromFile(it) }
        } else {
            val conf = Configuration.loadFromFile(file)
            conf.getKeys(false).forEach {
                val info = WizardInfo(conf.getConfigurationSection(it)!!)
                infoMap[info.id] = info
            }
        }
    }

    fun getWizardInfo(id: String): WizardInfo? {
        return infoMap[id]
    }

    fun cancel(entityInstance: EntityInstance) {
        actions.remove(entityInstance.uniqueId)
    }

    @Schedule(period = 10)
    private fun onTick() {
        actions.values.forEach { it.check() }
    }

    @SubscribeEvent
    private fun onQuit(e: PlayerQuitEvent) {
        actions.values.filter { it.player.name == e.player.name }.forEach {
            it.cancel()
            actions.remove(it.entityInstance.uniqueId)
        }
    }

    /**
     * 打开对话
     */
    @SubscribeEvent
    private fun onSelect(e: ConversationEvents.Pre) {
        val entity = e.session.source.entity
        if (entity is EntityInstance) {
            val action = actions[entity.uniqueId] ?: return
            if (action.info.disableConversation) {
                e.isCancelled = true
            }
        }
    }

    /**
     * 从 NPC 选取对话
     */
    @SubscribeEvent
    private fun onSelect(e: ConversationEvents.Select) {
        if (e.conversation == null) {
            val entity = e.source
            if (entity is EntityInstance) {
                val action = actions[entity.uniqueId]
                if (action?.info?.disableConversation == true) {
                    e.isCancelled = true
                }
            }
        }
    }
}