package ink.ptms.chemdah.module.kether.compat

import com.sucy.skill.SkillAPI
import com.sucy.skill.api.player.PlayerData
import ink.ptms.chemdah.util.getPlayer
import io.izzel.taboolib.kotlin.kether.Kether.expects
import io.izzel.taboolib.kotlin.kether.KetherParser
import io.izzel.taboolib.kotlin.kether.ScriptParser
import io.izzel.taboolib.kotlin.kether.common.api.QuestAction
import io.izzel.taboolib.kotlin.kether.common.api.QuestContext
import java.util.concurrent.CompletableFuture

/**
 * Chemdah
 * ink.ptms.chemdah.module.kether.compat.ActionSkillAPI
 *
 * @author Peng_Lx
 * @since 2021/5/4 16:03 下午
 */
class ActionSkillAPI {

    class Base(val action: (PlayerData) -> (Any)) : QuestAction<Any>() {

        override fun process(frame: QuestContext.Frame): CompletableFuture<Any> {
            return CompletableFuture.completedFuture(action(SkillAPI.getPlayerData(frame.getPlayer())))
        }
    }

    companion object {

        /**
         * skillapi class id
         * skillapi class name
         * skillapi level
         * skillapi cast skill1
         */
        @KetherParser(["skillapi"])
        fun parser() = ScriptParser.parser {
            when (it.expects("class", "skills", "attribute", "level", "exp", "experience", "mana", "cast")) {
                "class" -> {
                    Base(
                        when (it.expects("main", "size")) {
                            "main" -> { data: PlayerData -> data.mainClass }
                            "size" -> { data: PlayerData -> data.classes.size }
                            else -> error("out of case")
                        })
                }
                "skills" -> {
                    Base(when (it.expects("point")) {
                        "point" -> { data: PlayerData -> data.mainClass.points }
                        else -> error("out of case")
                    })
                }
                "attribute" -> {
                    Base(when (it.expects("point")) {
                        "point" -> { data: PlayerData -> data.attributePoints }
                        else -> error("out of case")
                    })
                }
                "level" -> {
                    Base(when (it.expects("level", "maxLevel")) {
                        "level" -> { data: PlayerData -> data.mainClass.level }
                        "maxLevel" -> { data: PlayerData -> data.mainClass.isLevelMaxed }
                        else -> error("ouf of case")
                    })
                }
                "experience", "exp" -> {
                    Base(when (it.expects("total", "require")) {
                        "total" -> { data: PlayerData -> data.mainClass.totalExp }
                        "require" -> { data: PlayerData -> data.mainClass.requiredExp }
                        else -> error("ouf of case")
                    })
                }
                "mana" -> {
                    Base { data -> data.mainClass.mana }
                }
                "cast" -> {
                    Base { data -> data.cast(it.nextToken()) }
                }
                else -> error("out of case")
            }
        }
    }
}