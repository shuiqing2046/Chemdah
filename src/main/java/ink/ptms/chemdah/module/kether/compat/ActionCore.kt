package ink.ptms.chemdah.module.kether.compat

import ink.ptms.chemdah.util.getPlayer
import io.izzel.taboolib.kotlin.kether.Kether.expects
import io.izzel.taboolib.kotlin.kether.KetherParser
import io.izzel.taboolib.kotlin.kether.ScriptParser
import io.izzel.taboolib.kotlin.kether.common.api.QuestAction
import io.izzel.taboolib.kotlin.kether.common.api.QuestContext
import net.Indyuce.mmocore.MMOCore
import net.Indyuce.mmocore.api.player.PlayerData
import java.util.concurrent.CompletableFuture

/**
 * Chemdah
 * ink.ptms.chemdah.module.kether.compat.ActionMMOCore
 *
 * @author sky
 * @since 2021/4/23 8:52 下午
 */
class ActionCore {

    class Base(val action: (PlayerData) -> (Any)) : QuestAction<Any>() {

        override fun process(frame: QuestContext.Frame): CompletableFuture<Any> {
            return CompletableFuture.completedFuture(action(MMOCore.plugin.dataProvider.dataManager.get(frame.getPlayer())))
        }
    }

    companion object {

        /**
         * mmocore class id
         * mmocore class name
         * mmocore level
         */
        @KetherParser(["mmocore"])
        fun parser() = ScriptParser.parser {
            when (it.expects("class", "skill", "attribute", "level", "exp", "experience", "mana", "stamina")) {
                "class" -> {
                    Base(when (it.expects("id", "name")) {
                        "id" -> { clazz: PlayerData -> clazz.profess.id }
                        "name" -> { clazz: PlayerData -> clazz.profess.name }
                        "point" -> { clazz: PlayerData -> clazz.classPoints }
                        else -> error("out of case")
                    })
                }
                "skill" -> {
                    Base(when (it.expects("point")) {
                        "point" -> { clazz: PlayerData -> clazz.skillPoints }
                        else -> error("out of case")
                    })
                }
                "attribute" -> {
                    Base(when (it.expects("point")) {
                        "point" -> { clazz: PlayerData -> clazz.attributePoints }
                        else -> error("out of case")
                    })
                }
                "level" -> {
                    Base { clazz -> clazz.level }
                }
                "experience", "exp" -> {
                    Base { clazz -> clazz.experience }
                }
                "mana" -> {
                    Base { clazz -> clazz.mana }
                }
                "stamina" -> {
                    Base { clazz -> clazz.stamina }
                }
                else -> error("out of case")
            }
        }
    }
}