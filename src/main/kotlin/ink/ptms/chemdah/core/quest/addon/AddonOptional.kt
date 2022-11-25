package ink.ptms.chemdah.core.quest.addon

import ink.ptms.chemdah.core.quest.Id
import ink.ptms.chemdah.core.quest.Option
import ink.ptms.chemdah.core.quest.Task

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.addon.AddonOptional
 *
 * addon:
 *   depend: quest
 *   depend: group:quest
 *
 * @author sky
 * @since 2021/3/4 9:04 上午
 */
@Id("optional")
@Option(Option.Type.BOOLEAN)
class AddonOptional(val value: Boolean, task: Task) : Addon(value, task) {

    companion object {

        fun Task.isOptional() = addon<AddonOptional>("optional")?.value == true
    }
}