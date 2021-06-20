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
import java.util.function.Function

/**
 * Chemdah
 * ink.ptms.chemdah.module.kether.compat.ActionSkillAPI
 *
 * @author Peng_Lx
 * @since 2021/5/4 16:03 下午
 */
class ActionSkillAPI {

    class Base(val action: Function<PlayerData, Any>) : QuestAction<Any>() {

        override fun process(frame: QuestContext.Frame): CompletableFuture<Any> {
            return CompletableFuture.completedFuture(action.apply(SkillAPI.getPlayerData(frame.getPlayer())))
        }

        override fun toString(): String {
            return "Base(action=$action)"
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
                    Base(when (it.expects("main", "size")) {
                        "main" -> { data -> data.mainClass }
                        "size" -> { data -> data.classes.size }
                        else -> error("out of case")
                    })
                }
                "skills" -> {
                    Base(when (it.expects("point")) {
                        "point" -> { data -> data.mainClass.points }
                        else -> error("out of case")
                    })
                }
                "attribute" -> {
                    val attribute = it.nextToken()
                    if (attribute == "point") {
                        Base { data -> data.attributePoints }
                    } else {
                        Base { data -> data.getAttribute(attribute) }
                    }
                }
                "level" -> {
                    try {
                        it.mark()
                        it.expect("maxed")
                        Base { data -> data.mainClass.isLevelMaxed }
                    } catch (ex: Exception) {
                        it.reset()
                        Base { data -> data.mainClass.level }
                    }
                }
                "experience", "exp" -> {
                    Base(when (it.expects("total", "required")) {
                        "total" -> { data -> data.mainClass.totalExp }
                        "require" -> { data -> data.mainClass.requiredExp }
                        else -> error("ouf of case")
                    })
                }
                "mana" -> {
                    Base { data -> data.mainClass.mana }
                }
                "cast" -> {
                    val skill = it.nextToken()
                    Base { data -> data.cast(skill) }
                }
                else -> error("out of case")
            }
        }
    }
}