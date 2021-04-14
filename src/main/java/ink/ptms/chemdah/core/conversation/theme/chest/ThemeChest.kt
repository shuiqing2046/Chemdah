package ink.ptms.chemdah.core.conversation.theme.chest

import ink.ptms.chemdah.api.ChemdahAPI
import ink.ptms.chemdah.core.conversation.ConversationManager
import ink.ptms.chemdah.core.conversation.Session
import ink.ptms.chemdah.core.conversation.theme.Theme
import io.izzel.taboolib.util.item.ItemBuilder
import io.izzel.taboolib.util.item.inventory.MenuBuilder
import org.bukkit.Material
import java.util.concurrent.CompletableFuture
import kotlin.math.ceil

object ThemeChest : Theme {

    lateinit var setting: ThemeChestSetting


    override fun reloadConfig() {
        setting = ThemeChestSetting(
            ConversationManager.conf.getConfigurationSection("theme-test")!!
        )
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
        println("call")
        val future = CompletableFuture<Void>()
        session.npcTalking = true
        val checked = session.conversation.playerSide.checked(session)

        checked.thenAccept { replies ->
            MenuBuilder.builder()
                .items(*setting.layoutList.toTypedArray())
                .rows(ceil((replies.size / setting.layoutRowAmount).toDouble()).toInt())
                .put('1', ItemBuilder(Material.getMaterial(setting.title.getString("id")!!)).also {
                    it.name(
                        setting.title.getString("name")!!
                            .replace("{title}", session.conversation.option.title.replace("{name}", session.npcName))
                    )
                    val lore = arrayListOf<String>()
                    setting.title.getStringList("lore")
                        .forEach { loreLine ->
                            if (loreLine.contains("{npcSide}")) {
                                message.forEach { messageLine -> loreLine.replace("{npcSide}", messageLine) }
                            } else {
                                lore.add(loreLine)
                            }
                        }
                    it.lore(lore)
                }.build()).open(session.player)
            future.complete(null);
        }

        future.thenAccept {
            future.complete(null)
        }
        session.npcTalking = false
        return future
    }

}