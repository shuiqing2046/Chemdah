package ink.ptms.chemdah.module.kether

import ink.ptms.chemdah.api.ChemdahAPI
import ink.ptms.chemdah.util.getProfile
import taboolib.library.kether.ArgTypes
import taboolib.module.kether.KetherParser
import taboolib.module.kether.actionFuture
import taboolib.module.kether.scriptParser
import taboolib.module.kether.switch

/**
 * Chemdah
 * ink.ptms.chemdah.module.kether.ActionQuestGroup
 *
 * @author mac
 * @since 2021/11/2 7:17 下午
 */
object ActionQuestGroup {

    /**
     * quest-group [accepted|completed] *group
     */
    @KetherParser(["quest-group"], shared = true)
    fun parser1() = scriptParser {
        it.switch {
            case("accepted") {
                val quest = it.next(ArgTypes.ACTION)
                actionFuture { future ->
                    newFrame(quest).run<Any>().thenApply { quest ->
                        val templateGroup = ChemdahAPI.getQuestTemplateGroup(quest.toString())
                        if (templateGroup == null) {
                            future.complete(false)
                        } else {
                            val profile = getProfile()
                            future.complete(templateGroup.quests.all { t -> profile.getQuestById(t.id) != null })
                        }
                    }
                }
            }
            case("completed") {
                val quest = it.next(ArgTypes.ACTION)
                actionFuture { future ->
                    newFrame(quest).run<Any>().thenApply { quest ->
                        val templateGroup = ChemdahAPI.getQuestTemplateGroup(quest.toString())
                        if (templateGroup == null) {
                            future.complete(false)
                        } else {
                            val profile = getProfile()
                            future.complete(templateGroup.quests.all { t -> profile.isQuestCompleted(t) })
                        }
                    }
                }
            }
        }
    }
}