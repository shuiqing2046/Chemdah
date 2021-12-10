package ink.ptms.chemdah.core.conversation.theme

import ink.ptms.chemdah.api.event.collect.ConversationEvents
import ink.ptms.chemdah.core.conversation.ConversationManager
import ink.ptms.chemdah.core.conversation.PlayerReply
import ink.ptms.chemdah.core.conversation.Session
import ink.ptms.chemdah.util.namespace
import ink.ptms.chemdah.util.setIcon
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import taboolib.common.platform.function.adaptPlayer
import taboolib.common.util.asList
import taboolib.common5.Coerce
import taboolib.module.chat.colored
import taboolib.module.kether.KetherFunction
import taboolib.module.kether.KetherShell
import taboolib.module.kether.printKetherErrorMessage
import taboolib.module.ui.openMenu
import taboolib.module.ui.type.Basic
import taboolib.platform.util.modifyMeta
import java.util.concurrent.CompletableFuture

/**
 * Chemdah
 * ink.ptms.chemdah.core.conversation.ThemeChest
 *
 * @author sky
 * @since 2021/2/12 2:08 上午
 */
object ThemeChest : Theme<ThemeChestSetting>() {

    override fun createConfig(): ThemeChestSetting {
        return ThemeChestSetting(ConversationManager.conf.getConfigurationSection("theme-chest")!!)
    }

    override fun allowFarewell(): Boolean {
        return false
    }

    override fun onDisplay(session: Session, message: List<String>, canReply: Boolean): CompletableFuture<Void> {
        var end = false
        return session.createDisplay { replies ->
            rows(session.player, replies.size).thenAccept { rows ->
                session.player.openMenu<Basic>(settings.title.toTitle(session)) {
                    rows(rows)
                    onBuild(async = true) { _, inventory ->
                        replies.forEachIndexed { index, playerReply ->
                            if (index < settings.playerSlot.size) {
                                inventory.setItem(settings.playerSlot[index], settings.playerItem.buildItem(session, playerReply, index + 1))
                            }
                        }
                        inventory.setItem(settings.npcSlot, settings.npcItem.buildItem(session, message))
                        // 唤起事件
                        ConversationEvents.ChestThemeBuild(session, message, canReply, inventory)
                    }
                    onClick(lock = true) { event ->
                        replies.getOrNull(settings.playerSlot.indexOf(event.rawSlot))?.run {
                            check(session).thenAccept { check ->
                                if (check) {
                                    end = true
                                    select(session).thenAccept {
                                        // 若未进行页面切换则关闭页面
                                        if (session.player.openInventory.topInventory == event.inventory) {
                                            session.player.closeInventory()
                                        }
                                    }
                                }
                            }
                        }
                    }
                    onClose {
                        if (!end) {
                            session.close(refuse = true)
                        }
                    }
                }
            }
        }
    }


    private fun ItemStack.buildItem(session: Session, reply: PlayerReply, index: Int): ItemStack {
        val icon = reply.root["icon"]?.toString()
        if (icon != null) {
            setIcon(icon)
        }
        val build = reply.build(session)
        return modifyMeta<ItemMeta> {
            setDisplayName(displayName.replace("[index]", index.toString()).replace("[playerSide]", build))
            lore = lore?.map { line ->
                val str = line.replace("[index]", index.toString()).replace("[playerSide]", build)
                KetherFunction.parse(str, sender = adaptPlayer(session.player), namespace = namespace)
            }
        }
    }

    private fun ItemStack.buildItem(session: Session, message: List<String>): ItemStack {
        val icon = session.conversation.root.getString("npc icon")
        if (icon != null) {
            setIcon(icon)
        }
        return modifyMeta<ItemMeta> {
            setDisplayName(displayName.toTitle(session))
            lore = lore?.flatMap { line ->
                val str = KetherFunction.parse(line, sender = adaptPlayer(session.player), namespace = namespace)
                if (str.contains("[npcSide]")) {
                    message.map { str.replace("[npcSide]", it) }
                } else {
                    str.toTitle(session).asList()
                }
            }
        }
    }

    private fun String.toTitle(session: Session): String {
        val str = replace("[title]", session.conversation.option.title.replace("[name]", session.source?.name.toString())).colored()
        return KetherFunction.parse(str, sender = adaptPlayer(session.player), namespace = namespace)
    }

    private fun rows(player: Player, size: Int): CompletableFuture<Int> {
        return try {
            KetherShell.eval(settings.rows, sender = adaptPlayer(player), namespace = namespace) {
                rootFrame().variables().set("size", size)
            }.thenApply {
                Coerce.toInteger(it)
            }
        } catch (ex: Exception) {
            ex.printKetherErrorMessage()
            CompletableFuture.completedFuture(1)
        }
    }
}