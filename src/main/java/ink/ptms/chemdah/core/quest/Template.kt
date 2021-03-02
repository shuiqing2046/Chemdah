package ink.ptms.chemdah.core.quest

import ink.ptms.chemdah.core.quest.meta.Meta
import org.bukkit.configuration.ConfigurationSection

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.Template
 *
 * @author sky
 * @since 2021/3/1 11:43 下午
 */
class Template(val id: String, val config: ConfigurationSection) {

    val task = ArrayList<Task>()
    val meta = ArrayList<Meta>()

    init {

    }
}