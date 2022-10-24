package ink.ptms.chemdah.module.wizard

import ink.ptms.adyeshach.common.entity.EntityInstance
import ink.ptms.chemdah.module.Module
import ink.ptms.chemdah.module.Module.Companion.register
import taboolib.common.platform.Awake
import taboolib.common.platform.Schedule
import java.util.concurrent.ConcurrentHashMap

/**
 * Chemdah
 * ink.ptms.chemdah.module.wizard.WizardSystem
 *
 * @author sky
 * @since 2021/3/11 9:03 上午
 */
@Awake
object WizardSystem : Module {

    val infoMap = ConcurrentHashMap<String, WizardInfo>()
    val actions = ConcurrentHashMap<String, WizardAction>()

    init {
        register()
    }

    override fun reload() {
    }

    fun getWizardInfo(id: String): WizardInfo? {
        return infoMap[id]
    }

    fun cancel(entityInstance: EntityInstance) {
        actions.remove(entityInstance.uniqueId)
    }

    @Schedule(period = 20)
    private fun onTick() {
        actions.values.forEach { it.check() }
    }
}