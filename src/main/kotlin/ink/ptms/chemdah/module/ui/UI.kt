package ink.ptms.chemdah.module.ui

import ink.ptms.chemdah.api.ChemdahAPI
import ink.ptms.chemdah.core.PlayerProfile
import ink.ptms.chemdah.core.quest.AcceptResult
import ink.ptms.chemdah.core.quest.addon.AddonDepend.Companion.isQuestDependCompleted
import ink.ptms.chemdah.core.quest.addon.AddonUI.Companion.ui
import ink.ptms.chemdah.core.quest.meta.MetaType.Companion.type
import taboolib.library.configuration.ConfigurationSection
import taboolib.library.xseries.XItemStack
import taboolib.module.chat.colored
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap

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
    val exclude = config.getStringList("exclude").toList()
    val items = HashMap<ItemType, Item>()

    val playerFilters = ConcurrentHashMap<UUID, MutableList<String>>()

    init {
        config.getConfigurationSection("include")?.getKeys(false)?.forEach {
            val active = config.getConfigurationSection("include.$it.active") ?: return@forEach
            val normal = config.getConfigurationSection("include.$it.normal") ?: return@forEach
            include.add(Include(it, XItemStack.deserialize(active), XItemStack.deserialize(normal)))
        }
        items[ItemType.INFO] = Item(config.getConfigurationSection("item.info")!!)
        items[ItemType.FILTER] = ItemFilter(config.getConfigurationSection("item.filter")!!)
        items[ItemType.QUEST_STARTED] = ItemQuest(config.getConfigurationSection("item.quest.started")!!)
        items[ItemType.QUEST_STARTED_SHARED] = ItemQuest(config.getConfigurationSection("item.quest.started-shared")!!)
        items[ItemType.QUEST_CAN_START] = ItemQuest(config.getConfigurationSection("item.quest.can-start")!!)
        items[ItemType.QUEST_CANNOT_START] = ItemQuestNoIcon(config.getConfigurationSection("item.quest.cannot-start")!!)
        items[ItemType.QUEST_COMPLETE] = ItemQuestNoIcon(config.getConfigurationSection("item.quest.completed")!!)
        items[ItemType.QUEST_UNAVAILABLE] = ItemQuestNoIcon(config.getConfigurationSection("item.quest.unavailable")!!)
    }

    /**
     * 打开任务页面
     */
    fun open(playerProfile: PlayerProfile) {
        collectQuests(playerProfile).thenAccept { UIMenu(this, playerProfile, it).open() }
    }

    /**
     * 获取所有被收录的有效任务列表
     * 并根据任务状态排序
     */
    fun collectQuests(playerProfile: PlayerProfile): CompletableFuture<List<UITemplate>> {
        val completableFuture = CompletableFuture<List<UITemplate>>()
        // 临时容器
        val collect = ArrayList<UITemplate>()
        // 玩家筛选列表
        val includePlayer = playerFilters.computeIfAbsent(playerProfile.uniqueId) { ArrayList() }
        val include = include.map { it.id }.filter { it in includePlayer || includePlayer.isEmpty() }
        // 筛选任务列表
        val quests = ChemdahAPI.questTemplate.filter { (_, v) -> v.type().any { it in include } && v.type().none { it in exclude } }.values.toList()
        fun process(cur: Int) {
            if (cur < quests.size) {
                val quest = quests[cur]
                val ui = quest.ui()
                // 正在进行该任务
                val questById = playerProfile.getQuestById(quest.id)
                if (questById != null) {
                    if (questById.isOwner(playerProfile.player)) {
                        collect.add(UITemplate(quest, ItemType.QUEST_STARTED))
                        process(cur + 1)
                    } else {
                        collect.add(UITemplate(quest, ItemType.QUEST_STARTED_SHARED))
                        process(cur + 1)
                    }
                } else {
                    // 任务接受条件判断
                    quest.checkAccept(playerProfile).thenAccept { cond ->
                        // 任务可以接受
                        if (cond.type == AcceptResult.Type.SUCCESSFUL) {
                            // 任务允许显示可接受状态
                            // 且任务的前置任务已被完成 才显示在UI
                            if (ui?.visibleStart == true && quest.isQuestDependCompleted(playerProfile.player)) {
                                collect.add(UITemplate(quest, ItemType.QUEST_CAN_START))
                            }
                        } else {
                            // 任务已完成
                            if (playerProfile.isQuestCompleted(quest.id)) {
                                // 任务允许显示完成状态
                                if (ui?.visibleComplete == true) {
                                    collect.add(UITemplate(quest, ItemType.QUEST_COMPLETE))
                                }
                            } else {
                                // 任务允许显示无法接受状态
                                if (ui?.visibleStart == true) {
                                    collect.add(UITemplate(quest, ItemType.QUEST_CANNOT_START))
                                }
                            }
                        }
                        process(cur + 1)
                    }
                }
            } else {
                completableFuture.complete(collect.sortedByDescending { it.itemType.priority })
            }
        }
        process(0)
        return completableFuture
    }
}
