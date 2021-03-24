package ink.ptms.chemdah.module.ui

import ink.ptms.chemdah.api.ChemdahAPI
import ink.ptms.chemdah.core.PlayerProfile
import ink.ptms.chemdah.core.quest.AcceptResult
import ink.ptms.chemdah.core.quest.Idx
import ink.ptms.chemdah.core.quest.Template
import ink.ptms.chemdah.core.quest.addon.AddonUI.Companion.ui
import ink.ptms.chemdah.core.quest.meta.MetaLabel.Companion.label
import ink.ptms.chemdah.util.colored
import io.izzel.taboolib.util.item.Items
import org.bukkit.configuration.ConfigurationSection
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

/**
 * Chemdah
 * ink.ptms.chemdah.module.ui.UI
 *
 * @author sky
 * @since 2021/3/11 9:03 上午
 */
class UI(val config: ConfigurationSection) {

    val name = config.getString("name")?.colored().toString()
    val menuQuestRows = config.getInt("menu.quest.rows")
    val menuQuestSlot: List<Int> = config.getIntegerList("menu.quest.slot")
    val menuQuestSlotInfo = config.getInt("menu.quest.methods.info")
    val menuQuestSlotFilter = config.getInt("menu.quest.methods.filter")
    val menuFilterRows = config.getInt("menu.filter.rows")
    val menuFilterSlot: List<Int> = config.getIntegerList("menu.filter.slot")
    val include = ArrayList<Include>()
    val exclude: List<String> = config.getStringList("exclude")
    val items = HashMap<ItemType, Item>()

    val playerFilters = ConcurrentHashMap<UUID, List<String>>()

    init {
        config.getConfigurationSection("include")?.getKeys(false)?.forEach {
            val active = config.getConfigurationSection("include.$it.active") ?: return@forEach
            val normal = config.getConfigurationSection("include.$it.normal") ?: return@forEach
            include.add(Include(it, Items.loadItem(active)!!, Items.loadItem(normal)!!))
        }
        items[ItemType.INFO] = Item(config.getConfigurationSection("item.info")!!)
        items[ItemType.FILTER] = ItemFilter(config.getConfigurationSection("item.filter")!!)
        items[ItemType.QUEST_STARTED] = ItemQuest(config.getConfigurationSection("item.quest.started")!!)
        items[ItemType.QUEST_CAN_START] = ItemQuest(config.getConfigurationSection("item.quest.can-start")!!)
        items[ItemType.QUEST_CANNOT_START] = ItemQuest(config.getConfigurationSection("item.quest.cannot-start")!!)
        items[ItemType.QUEST_COMPLETE] = ItemQuest(config.getConfigurationSection("item.quest.completed")!!)
        items[ItemType.QUEST_UNAVAILABLE] = ItemQuest(config.getConfigurationSection("item.quest.unavailable")!!)
    }

    fun collectQuests(playerProfile: PlayerProfile, callback: (List<Template>) -> Unit) {
        val collect = ArrayList<Pair<Template, ItemType>>()
        val quests = collectQuests()
        fun process(cur: Int) {
            if (cur < quests.size) {
                val quest = quests[cur]
                val ui = quest.ui()
                // 正在进行该任务
                if (playerProfile.getQuestById(quest.id) != null) {
                    collect.add(quest to ItemType.QUEST_STARTED)
                    process(cur + 1)
                } else {
                    // 任务接受条件判断
                    quest.checkAccept(playerProfile).thenAccept { cond ->
                        // 任务可以接受
                        if (cond == AcceptResult.SUCCESSFUL) {
                            collect.add(quest to ItemType.QUEST_CAN_START)
                        } else {
                            // 任务已完成
                            if (playerProfile.isQuestCompleted(quest.id)) {
                                // 任务允许显示完成状态
                                if (ui?.visibleComplete == true) {
                                    collect.add(quest to ItemType.QUEST_COMPLETE)
                                }
                            } else {
                                collect.add(quest to ItemType.QUEST_CANNOT_START)
                            }
                        }
                        process(cur + 1)
                    }
                }
            } else {

            }
        }
        process(0)
    }

    fun collectQuests(): List<Template> {
        val include = include.map { it.id }
        return ChemdahAPI.quest.filter { (_, v) -> v.label().any { it in include } && v.label().none { it in exclude } }.values.toList()
    }
}