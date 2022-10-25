package ink.ptms.chemdah.module.kether

import ink.ptms.adyeshach.common.entity.EntityInstance
import ink.ptms.chemdah.module.wizard.WizardSystem
import org.bukkit.entity.EntityType
import taboolib.common.platform.function.warning
import taboolib.module.kether.*

/**
 * Chemdah
 * ink.ptms.chemdah.module.kether.ActionWizard
 *
 * @author sky
 * @since 2021/2/10 6:39 下午
 */
object ActionWizard {

    /**
     * wizard to "example_0"
     * wizard cancel
     */
    @KetherParser(["wizard"], shared = true)
    fun parser() = scriptParser {
        it.switch {
            case("to") {
                val id = it.nextParsedAction()
                actionFuture { f ->
                    run(id).str { id ->
                        val npc = script().getEntities() ?: emptyList()
                        if (npc.isEmpty() || npc.size > 1) {
                            warning("Wizard action can only be used on a single NPC.")
                            f.complete(false)
                            return@str null
                        }
                        val wizardInfo = WizardSystem.getWizardInfo(id)
                        if (wizardInfo == null) {
                            warning("Wizard info $id not found.")
                            f.complete(false)
                            return@str null
                        }
                        wizardInfo.apply(player().cast(), npc.first()!!).thenAccept { success -> f.complete(success) }
                    }
                }
            }
            case("cancel") {
                actionNow {
                    val npc = script().getEntities() ?: emptyList()
                    if (npc.isEmpty() || npc.size > 1) {
                        warning("Wizard action can only be used on a single NPC.")
                    } else {
                        WizardSystem.cancel(npc.first()!!)
                    }
                }
            }
        }
    }

    fun ScriptContext.getEntities(): List<EntityInstance?>? {
        return rootFrame().variables().get<List<EntityInstance?>?>("@entities").orElse(null)
    }
}