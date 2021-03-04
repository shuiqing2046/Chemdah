package ink.ptms.chemdah.core.quest.addon

import ink.ptms.chemdah.core.quest.Task
import org.bukkit.configuration.ConfigurationSection

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.addon.Addon
 *
 * @author sky
 * @since 2021/3/2 1:03 上午
 */
abstract class Addon(val config: ConfigurationSection, val task: Task) {
}