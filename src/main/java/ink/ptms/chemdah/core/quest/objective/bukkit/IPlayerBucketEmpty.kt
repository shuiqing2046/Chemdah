package ink.ptms.chemdah.core.quest.objective.bukkit

import ink.ptms.chemdah.core.quest.objective.Dependency
import org.bukkit.event.player.PlayerBucketEmptyEvent

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.objective.bukkit.IPlayerBucketEmpty
 *
 * @author sky
 * @since 2021/3/2 5:09 下午
 */
@Dependency("minecraft")
object IPlayerBucketEmpty : APlayerBucket<PlayerBucketEmptyEvent>() {

    override val name = "bucket empty"
    override val event = PlayerBucketEmptyEvent::class
}