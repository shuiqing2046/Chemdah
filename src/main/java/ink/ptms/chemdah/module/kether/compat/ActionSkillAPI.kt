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
         */
        @KetherParser(["skillapi"])
        fun parser() = ScriptParser.parser {
            when (it.expects("class", "skills", "attribute", "level", "exp", "experience", "mana")) {
                "class" -> {
                    Base(
                        when (it.expects("main", "size")) {
                            "main" -> { clazz: PlayerData -> clazz.mainClass}
                            "size" -> { clazz: PlayerData -> clazz.classes.size}
                            else -> error("out of case")
                        })
                }
                "skills" -> {
                    Base(when (it.expects("point")) {
                        "point" -> { clazz: PlayerData -> clazz.mainClass.points}
                        else -> error("out of case")
                    })
                }
                "attribute" -> {
                    Base(when (it.expects("point")){
                        "point" -> { clazz: PlayerData -> clazz.attributePoints}
                        else -> error("out of case")
                    })
                }
                "level" -> {
                    Base(when (it.expects("level", "maxLevel")) {
                        "level" -> { clazz: PlayerData -> clazz.mainClass.level}
                        "maxLevel" -> { clazz: PlayerData -> clazz.mainClass.isLevelMaxed}
                        else -> error("ouf of case")
                    })
                }
                "experience", "exp" ->{
                    Base(when (it.expects("total", "require")) {
                        "total" -> { clazz: PlayerData -> clazz.mainClass.totalExp}
                        "require" -> { clazz: PlayerData -> clazz.mainClass.requiredExp}
                        else -> error("ouf of case")
                    })
                }
                "mana" -> {
                    Base { clazz -> clazz.mainClass.mana}
                }
                else -> error("out of case")
            }
        }
    }
}