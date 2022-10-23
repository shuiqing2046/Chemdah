package ink.ptms.chemdah.module.wizard

import ink.ptms.chemdah.module.Module
import ink.ptms.chemdah.module.Module.Companion.register
import taboolib.common.platform.Awake

/**
 * Chemdah
 * ink.ptms.chemdah.module.wizard.WizardSystem
 *
 * @author sky
 * @since 2021/3/11 9:03 上午
 */
@Awake
object WizardSystem : Module {

    init {
        register()
    }

    override fun reload() {
    }
}