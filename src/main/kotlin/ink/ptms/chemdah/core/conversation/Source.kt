package ink.ptms.chemdah.core.conversation

import org.bukkit.Location
import org.bukkit.entity.Player

/**
 * Chemdah
 * ink.ptms.chemdah.core.conversation.Source
 *
 * @author 坏黑
 * @since 2021/12/11 2:28 AM
 */
abstract class Source<T>(var name: String, var entity: T) {

    abstract fun transfer(player: Player, newId: String): Boolean

    abstract fun getOriginLocation(entity: T): Location
}