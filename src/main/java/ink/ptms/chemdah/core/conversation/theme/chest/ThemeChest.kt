package ink.ptms.chemdah.core.conversation.theme.chest

import ink.ptms.chemdah.api.ChemdahAPI
import ink.ptms.chemdah.api.ChemdahAPI.conversationSession
import ink.ptms.chemdah.core.conversation.ConversationManager
import ink.ptms.chemdah.core.conversation.PlayerReply
import ink.ptms.chemdah.core.conversation.Session
import ink.ptms.chemdah.core.conversation.theme.Theme
import ink.ptms.chemdah.core.conversation.theme.ThemeTest
import ink.ptms.chemdah.core.conversation.theme.ThemeTest.npcTalk
import ink.ptms.chemdah.core.conversation.theme.chest.ThemeChest.buildReplyIconItem
import ink.ptms.chemdah.core.quest.selector.InferItem.Companion.toInferItem
import ink.ptms.chemdah.util.colored
import io.izzel.taboolib.module.inject.TListener
import io.izzel.taboolib.util.item.ItemBuilder
import io.izzel.taboolib.util.item.inventory.MenuBuilder
import org.bukkit.Material
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.event.Listener
import org.bukkit.inventory.ItemStack
import java.util.concurrent.CompletableFuture
import kotlin.math.ceil

@TListener
object ThemeChest : Theme, Listener {

    init {
        ChemdahAPI.conversationTheme["chest"] = this
    }


    lateinit var setting: ThemeChestSetting


    override fun reloadConfig() {
        try {
            setting = ThemeChestSetting(ConversationManager.conf.getConfigurationSection("theme-chest")!!)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun reload(session: Session): CompletableFuture<Void> {
        val future = CompletableFuture<Void>()
        session.conversation.playerSide.checked(session).thenApply {
            session.playerSide = it.getOrNull(session.player.inventory.heldItemSlot.coerceAtMost(it.size - 1))
            future.complete(null)
        }
        return future
    }


    override fun begin(session: Session): CompletableFuture<Void> {
        return npcTalk(session, session.npcSide);
    }

    override fun end(session: Session): CompletableFuture<Void> {
        return CompletableFuture.completedFuture(null);
    }

    override fun npcTalk(session: Session, message: List<String>, canReply: Boolean): CompletableFuture<Void> {
        val future = CompletableFuture<Void>()
        session.npcTalking = true
        val checked = session.conversation.playerSide.checked(session)
        var closed = true
        checked.thenAccept { replies ->
            try {

                val size = replies.size
                val row = ceil(setting.replySlots[size - 1].toDouble() / 9.0).toInt()

                MenuBuilder.builder()
                    .rows(row)
                    .title(setting.title.format0(session))
                    .build {
                        it.setItem(setting.npcSlot, setting.npcIcon.buildNPCIconItem(session, message))

                        replies.forEachIndexed { index, playerReply ->
                            val slot = setting.replySlots[index]
                            it.setItem(slot, setting.replyIcon.buildReplyIconItem(index + 1, playerReply.text))
                        }
                    }
                    .click {
                        it.isCancelled = true
                        val rawSlot = it.rawSlot
                        val indexOf = setting.replySlots.indexOf(rawSlot)
                        if (indexOf != -1) {
                            val conversationSession = it.clicker.conversationSession
                            if (conversationSession != null) {
                                val playerReply = replies[indexOf]
                                playerReply.check(session).thenAccept { aBoolean ->
                                    if (aBoolean) {
                                        closed = false
                                        playerReply.select(session)
                                    }
                                }
                            } else {
                                it.clicker.closeInventory()
                            }
                        }

                    }
                    .close {
                        if (closed) {
                            session.close()
                            closed = true
                        }
                    }
                    .open(session.player)
                future.complete(null);
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }

        future.thenAccept {
            session.npcTalking = false
        }
        return future
    }


    private fun ConfigurationSection.buildReplyIconItem(index: Int, text: String): ItemStack {
        val builder = ItemBuilder(Material.getMaterial(this.getString("id", "SKULL")!!))
            .name(this.getString("name")!!.replace("{index}", index.toString()).replace("{text}", text).colored())
        return builder.build()
    }

    private fun ConfigurationSection.buildNPCIconItem(session: Session, list: List<String>): ItemStack {

        val builder = ItemBuilder(Material.getMaterial(this.getString("id", "SKULL")!!))
            .name(this.getString("name")!!.format0(session))
        val lore = arrayListOf<String>()
        this.getStringList("lore").forEach {
            if (it.contains("{npcSide}")) {
                list.forEach { line -> lore.add(it.replace("{npcSide}", line).format0(session).colored()) }
            } else {
                lore.add(it.format0(session))
            }
        }
        builder.lore(lore)
        return builder.build()
    }

    private fun String.format0(session: Session): String {
        return this.replace("{title}", session.conversation.option.title.replace("{name}", session.npcName)).colored()
    }

}